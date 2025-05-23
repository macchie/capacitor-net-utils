package com.macchie.plugins.netutils;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PortForwarder {
  private final Map<String, ForwardingSession> sessions = new ConcurrentHashMap<>();

  public void startForwarding(PluginCall call) {
    Integer localPort = call.getInt("localPort");
    String targetHost = call.getString("targetHost");
    Integer targetPort = call.getInt("targetPort");

    if (localPort == null || targetHost == null || targetPort == null) {
      call.reject("Missing required parameters: localPort, targetHost, targetPort");
      return;
    }

    String protocol = call.getString("protocol", "tcp").toLowerCase();
    String sessionId = call.getString("id");
    if (sessionId == null || sessionId.isEmpty()) {
      sessionId = UUID.randomUUID().toString();
    }

    try {
      ForwardingSession session = new ForwardingSession(
          sessionId,
          protocol,
          localPort,
          targetHost,
          targetPort);
      sessions.put(sessionId, session);
      session.start();

      JSObject result = new JSObject();
      result.put("success", true);
      result.put("id", sessionId);
      call.resolve(result);

    } catch (Exception e) {
      call.reject("Failed to start forwarding: " + e.getMessage(), e);
    }
  }

  public void stopForwarding(PluginCall call) {
    String sessionId = call.getString("id");

    if (sessionId == null || !sessions.containsKey(sessionId)) {
      call.reject("Session id not found");
      return;
    }

    ForwardingSession session = sessions.get(sessionId);
    if (session != null) {
      session.stop();
      sessions.remove(sessionId);
    }

    try {
      JSObject result = new JSObject();
      result.put("success", true);
      result.put("id", sessionId);
      call.resolve(result);
    } catch (Exception e) {
      call.reject("Failed to stop forwarding: " + e.getMessage(), e);
    }
  }
}
