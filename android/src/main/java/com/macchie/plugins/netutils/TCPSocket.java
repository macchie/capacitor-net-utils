package com.macchie.plugins.netutils;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;

public class TCPSocket {

  private Socket socket;
  private OutputStream outputStream;
  private final NetUtilsPlugin plugin;

  public TCPSocket(NetUtilsPlugin plugin) {
    this.plugin = plugin;
  }

  public void connect(PluginCall call) {
    String host = call.getString("host");
    Integer port = call.getInt("port");

    if (host == null || port == null) {
      call.reject("Missing host or port");
      return;
    }

    Executors.newSingleThreadExecutor().execute(() -> {
      try {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 5000);
        outputStream = socket.getOutputStream();

        InputStream in = socket.getInputStream();

        // Start background read loop
        Executors.newSingleThreadExecutor().execute(() -> {
          byte[] buffer = new byte[1024];
          int len;
          try {
            while ((len = in.read(buffer)) != -1) {
              String data = new String(buffer, 0, len);
              JSObject event = new JSObject();
              event.put("data", data);
              plugin.emit("tcp:message", event);
            }
          } catch (Exception ignored) {
            // Connection closed
          }
        });

        JSObject result = new JSObject();
        result.put("success", true);
        call.resolve(result);
      } catch (Exception e) {
        call.reject("Connection failed: " + e.getMessage());
      }
    });
  }

  public void write(PluginCall call) {
    String data = call.getString("data");
    if (data == null) {
      call.reject("Missing or invalid data");
      return;
    }

    if (outputStream == null) {
      call.reject("No active connection");
      return;
    }

    Executors.newSingleThreadExecutor().execute(() -> {
      try {
        outputStream.write(data.getBytes());
        outputStream.flush();
        JSObject result = new JSObject();
        result.put("success", true);
        call.resolve(result);
      } catch (Exception e) {
        call.reject("Failed to write data: " + e.getMessage());
      }
    });
  }

  public void disconnect(PluginCall call) {
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
        socket = null;
        outputStream = null;
      }
      JSObject result = new JSObject();
      result.put("success", true);
      call.resolve(result);
    } catch (Exception e) {
      call.reject("Failed to disconnect: " + e.getMessage());
    }
  }
}
