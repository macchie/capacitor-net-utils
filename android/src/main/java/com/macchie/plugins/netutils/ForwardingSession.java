package com.macchie.plugins.netutils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ForwardingSession {
  private final String id;
  private final String protocol;
  private final int localPort;
  private final String targetHost;
  private final int targetPort;
  private ServerSocket serverSocket;
  private DatagramSocket datagramSocket;
  private volatile boolean running = false;
  private final ExecutorService executor = new ThreadPoolExecutor(
    1, 4, 30L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(32)
  );

  public ForwardingSession(String id, String protocol, int localPort, String targetHost, int targetPort) {
    this.id = id;
    this.protocol = protocol;
    this.localPort = localPort;
    this.targetHost = targetHost;
    this.targetPort = targetPort;
  }

  public void start() {
    running = true;
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
        while (running && !serverSocket.isClosed()) {
          Socket clientSocket = serverSocket.accept();
          if (!running) {
            clientSocket.close();
            break;
          }
          executor.submit(() -> handleTCPConnection(clientSocket));
        }
      } catch (Exception e) {
        if (running) {
          System.err.println("Session " + id + ": TCP listener failed - " + e.getMessage());
        }
      }
    });
  }

  private void handleTCPConnection(Socket clientSocket) {
    Socket targetSocket = null;
    try {
      targetSocket = new Socket();
      targetSocket.connect(new InetSocketAddress(targetHost, targetPort), 5000);
      Socket finalTarget = targetSocket;
      executor.submit(() -> forwardTCP(clientSocket, finalTarget));
      forwardTCP(targetSocket, clientSocket);
    } catch (Exception e) {
      System.err.println("Session " + id + ": TCP connection to target failed - " + e.getMessage());
      try { clientSocket.close(); } catch (Exception ignored) {}
      if (targetSocket != null) {
        try { targetSocket.close(); } catch (Exception ignored) {}
      }
    }
  }

  private void forwardTCP(Socket inSocket, Socket outSocket) {
    try (InputStream in = inSocket.getInputStream(); OutputStream out = outSocket.getOutputStream()) {
      byte[] buffer = new byte[8192];
      int bytesRead;
      while (running && (bytesRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
        out.flush();
      }
    } catch (Exception e) {
      if (running) {
        System.err.println("Session " + id + ": TCP forward error - " + e.getMessage());
      }
    } finally {
      try { inSocket.close(); } catch (Exception ignored) {}
      try { outSocket.close(); } catch (Exception ignored) {}
    }
  }

  private void startUDP() {
    executor.submit(() -> {
      try {
        datagramSocket = new DatagramSocket(localPort);
        InetAddress targetAddress = InetAddress.getByName(targetHost);
        byte[] buffer = new byte[65535];

        while (running && !datagramSocket.isClosed()) {
          DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
          datagramSocket.receive(packet);

          if (!running) break;

          // Forward to target
          byte[] data = new byte[packet.getLength()];
          System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
          DatagramPacket forwardPacket = new DatagramPacket(data, data.length, targetAddress, targetPort);
          datagramSocket.send(forwardPacket);
        }
      } catch (Exception e) {
        if (running) {
          System.err.println("Session " + id + ": UDP forwarding failed - " + e.getMessage());
        }
      }
    });
  }

  public void stop() {
    running = false;
    try {
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
      }
    } catch (Exception ignored) {}
    try {
      if (datagramSocket != null && !datagramSocket.isClosed()) {
        datagramSocket.close();
      }
    } catch (Exception ignored) {}
    executor.shutdownNow();
  }
}
