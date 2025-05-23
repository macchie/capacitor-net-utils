package com.macchie.plugins.netutils;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import com.jcraft.jsch.*;
import java.io.InputStream;
import java.util.concurrent.Executors;

public class SSHUtils {

  public void sshExecSync(PluginCall call) {
    String host = call.getString("host");
    String user = call.getString("user");
    String password = call.getString("password");
    String command = call.getString("command");
    Integer port = call.getInt("port", 22);

    if (host == null || user == null || password == null || command == null) {
      call.reject("host, user, password, and command are required.");
      return;
    }

    Executors.newSingleThreadExecutor().execute(() -> {
      JSObject result = new JSObject();
      Session session = null;

      try {
        JSch jsch = new JSch();
        session = jsch.getSession(user, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(10000); // 10 second timeout

        if (!session.isConnected()) {
          result.put("output", "");
          result.put("error", "Connection failed");
          call.resolve(result);
          return;
        }

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);

        InputStream in = channel.getInputStream();
        channel.connect(10000); // 10 second timeout

        StringBuilder outputBuffer = new StringBuilder();
        byte[] tmp = new byte[1024];
        int len;
        while ((len = in.read(tmp)) != -1) {
          outputBuffer.append(new String(tmp, 0, len));
        }

        result.put("output", outputBuffer.toString());
        result.put("error", "");

        channel.disconnect();
        session.disconnect();
        call.resolve(result);

      } catch (Exception e) {
        try {
          result.put("output", "");
          result.put("error", e.getMessage() != null ? e.getMessage() : "Unknown error");
        } catch (Exception ignored) {
        }
        if (session != null && session.isConnected()) {
          session.disconnect();
        }
        call.resolve(result);
      }
    });
  }

}
