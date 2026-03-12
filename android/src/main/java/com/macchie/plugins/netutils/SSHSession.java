package com.macchie.plugins.netutils;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.jcraft.jsch.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SSHSession {

  private Session jschSession;
  private Channel shellChannel;
  private OutputStream shellInput;
  private final NetUtilsPlugin plugin;
  private final ExecutorService executor = Executors.newCachedThreadPool();

  public SSHSession(NetUtilsPlugin plugin) {
    this.plugin = plugin;
  }

  public void connect(PluginCall call) {
    String host = call.getString("host");
    String username = call.getString("username");
    String password = call.getString("password");
    int port = call.getInt("port", 22);

    if (host == null || username == null || password == null) {
      call.reject("Missing parameters. Provide host, username, and password.");
      return;
    }

    executor.execute(() -> {
      try {
        JSch jsch = new JSch();
        jschSession = jsch.getSession(username, host, port);
        jschSession.setPassword(password);
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.connect(10000);

        if (jschSession.isConnected()) {
          JSObject result = new JSObject();
          result.put("success", true);
          call.resolve(result);
        } else {
          call.reject("Connection failed");
        }
      } catch (Exception e) {
        call.reject("Connection failed: " + e.getMessage());
      }
    });
  }

  public void startShell(PluginCall call) {
    if (jschSession == null || !jschSession.isConnected()) {
      call.reject("No active session. Call connect() first.");
      return;
    }

    executor.execute(() -> {
      try {
        shellChannel = jschSession.openChannel("shell");
        InputStream in = shellChannel.getInputStream();
        shellInput = shellChannel.getOutputStream();
        shellChannel.connect();

        JSObject result = new JSObject();
        result.put("success", true);
        call.resolve(result);

        // Read loop in background
        executor.execute(() -> {
          byte[] buffer = new byte[1024];
          int len;
          try {
            while ((len = in.read(buffer)) != -1) {
              String data = new String(buffer, 0, len, StandardCharsets.UTF_8);
              JSObject event = new JSObject();
              event.put("data", data);
              plugin.emit("ssh:stdout", event);
            }
          } catch (Exception ignored) {
            // Connection closed
          }
        });

      } catch (Exception e) {
        call.reject("Failed to start shell: " + e.getMessage());
      }
    });
  }

  public void write(PluginCall call) {
    String command = call.getString("command");
    if (command == null) {
      call.reject("No command provided");
      return;
    }

    if (shellInput == null) {
      call.reject("No active shell. Call startShell() first.");
      return;
    }

    executor.execute(() -> {
      try {
        shellInput.write((command + "\n").getBytes(StandardCharsets.UTF_8));
        shellInput.flush();
        JSObject result = new JSObject();
        result.put("success", true);
        call.resolve(result);
      } catch (Exception e) {
        call.reject("Failed to write command: " + e.getMessage());
      }
    });
  }

  public void disconnect(PluginCall call) {
    cleanup();
    if (call != null) {
      JSObject result = new JSObject();
      result.put("success", true);
      call.resolve(result);
    }
  }

  public void cleanup() {
    try {
      if (shellChannel != null) {
        shellChannel.disconnect();
        shellChannel = null;
        shellInput = null;
      }
      if (jschSession != null && jschSession.isConnected()) {
        jschSession.disconnect();
        jschSession = null;
      }
    } catch (Exception ignored) {}
    executor.shutdownNow();
  }
}
