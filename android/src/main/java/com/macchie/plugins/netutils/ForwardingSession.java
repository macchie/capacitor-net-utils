package com.macchie.plugins.netutils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForwardingSession {
  private final String id;
  private final String protocol;
  private final int localPort;
  private final String targetHost;
  private final int targetPort;
  private ServerSocket serverSocket;
  private ExecutorService executor = Executors.newCachedThreadPool();

  public ForwardingSession(String id, String protocol, int localPort, String targetHost, int targetPort) {
    this.id = id;
    this.protocol = protocol;
    this.localPort = localPort;
    this.targetHost = targetHost;
    this.targetPort = targetPort;
  }

  public void start() {
    if (protocol.equalsIgnoreCase("tcp")) {
      startTCP();
    } else {
      startUDP();
    }
  }

  private void startTCP() {
    executor.submit(() -> {
      try {
        serverSocket = new ServerSocket(localPort);
        System.out.println("Session " + id + ": Listening on TCP port " + localPort);
        while (!serverSocket.isClosed()) {
          Socket clientSocket = serverSocket.accept();
          Socket targetSocket = new Socket(targetHost, targetPort);
          forwardTCP(clientSocket, targetSocket);
          forwardTCP(targetSocket, clientSocket);
        }
      } catch (Exception e) {
        System.err.println("Session " + id + ": TCP listener failed - " + e.getMessage());
      }
    });
  }

  private void forwardTCP(Socket inSocket, Socket outSocket) {
    executor.submit(() -> {
      try (InputStream in = inSocket.getInputStream(); OutputStream out = outSocket.getOutputStream()) {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
          out.write(buffer, 0, bytesRead);
          out.flush();
        }
      } catch (Exception e) {
        System.err.println("Session " + id + ": TCP forward error - " + e.getMessage());
      } finally {
        try {
          inSocket.close();
        } catch (Exception ignored) {
        }
        try {
          outSocket.close();
        } catch (Exception ignored) {
        }
      }
    });
  }

  private void startUDP() {
    // Implement similar logic using DatagramSocket + DatagramPacket
  }

  public void stop() {
    try {
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
      }
      executor.shutdownNow();
    } catch (Exception e) {
      System.err.println("Session " + id + ": Failed to stop - " + e.getMessage());
    }
  }
}
