package com.macchie.plugins.netutils;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.net.NetworkInterface;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetUtils {

  private final ExecutorService executor = Executors.newCachedThreadPool();

  public void getInterfaces(PluginCall call) {
    JSONArray interfacesResult = new JSONArray();

    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

      for (NetworkInterface intf : Collections.list(interfaces)) {
        List<InterfaceAddress> addresses = intf.getInterfaceAddresses();
        for (InterfaceAddress addr : addresses) {
          InetAddress inetAddress = addr.getAddress();

          if (inetAddress == null || inetAddress.isLoopbackAddress()) continue;
          if (!(inetAddress instanceof java.net.Inet4Address || inetAddress instanceof java.net.Inet6Address))
          continue;

          String name = intf.getName();
          String address = inetAddress.getHostAddress();
          String type;

          if (name.startsWith("wlan")) {
            type = "wifi";
          } else if (name.startsWith("eth")) {
            type = "ethernet";
          } else if (name.startsWith("tun") || name.startsWith("tap")) {
            type = "vpn";
          } else if (name.startsWith("rmnet") || name.startsWith("ccmni") || name.startsWith("usb")) {
            type = "cellular";
          } else {
            type = "other";
          }

          JSObject ifaceObj = new JSObject();
          ifaceObj.put("name", name);
          ifaceObj.put("address", address);
          ifaceObj.put("type", type);
          interfacesResult.put(ifaceObj);
        }
      }

      JSObject result = new JSObject();
      result.put("output", interfacesResult);
      result.put("error", "");
      call.resolve(result);

    } catch (Exception e) {
      call.reject("Failed to get interfaces: " + e.getMessage(), e);
    }
  }

  public void resolveHostname(PluginCall call) {
    String host = call.getString("host");
    if (host == null) {
      call.reject("HOST is required.");
      return;
    }

    executor.execute(() -> {
      JSObject result = new JSObject();
      try {
        InetAddress addr = InetAddress.getByName(host);
        String hostname = addr.getCanonicalHostName();
        if (hostname != null && !hostname.equals(host)) {
          result.put("hostname", hostname);
        } else {
          result.put("hostname", JSONObject.NULL);
          result.put("error", "Hostname not found");
        }
        call.resolve(result);
      } catch (Exception e) {
        try {
          result.put("hostname", JSONObject.NULL);
          result.put("error", e.getMessage());
          call.resolve(result);
        } catch (Exception ignored) {}
      }
    });
  }

  public void checkUrl(PluginCall call) {
    String urlString = call.getString("url");
    if (urlString == null || urlString.isEmpty()) {
      call.reject("A valid URL is required.");
      return;
    }

    Integer timeoutVal = call.getInt("timeout");
    int timeout = timeoutVal != null ? timeoutVal : 5000;

    executor.execute(() -> {
      HttpURLConnection connection = null;
      try {
        URI uri = URI.create(urlString);
        connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("HEAD");
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setInstanceFollowRedirects(true);

        int statusCode = connection.getResponseCode();
        boolean exists = statusCode >= 200 && statusCode < 400;

        JSObject result = new JSObject();
        result.put("exists", exists);
        result.put("statusCode", statusCode);
        call.resolve(result);
      } catch (Exception e) {
        JSObject result = new JSObject();
        result.put("exists", false);
        result.put("error", e.getMessage());
        call.resolve(result);
      } finally {
        if (connection != null) {
          connection.disconnect();
        }
      }
    });
  }

  public void checkPort(PluginCall call) {
    String host = call.getString("host");
    Integer portInt = call.getInt("port");
    String protocol = call.getString("protocol");

    if (host == null || portInt == null || protocol == null) {
      call.reject("Host, port, and protocol are required.");
      return;
    }

    if (portInt < 1 || portInt > 65535) {
      call.reject("Invalid port number.");
      return;
    }

    Integer timeoutVal = call.getInt("timeout");
    int timeout = timeoutVal != null ? timeoutVal : 5000;
    int port = portInt;

    executor.execute(() -> {
      boolean isOpen = false;
      String error = "";

      try {
        if (protocol.equalsIgnoreCase("tcp")) {
          try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            isOpen = true;
          }
        } else if (protocol.equalsIgnoreCase("udp")) {
          // UDP is connectionless — send an empty probe and wait for ICMP unreachable.
          // If no error within timeout, assume open (best-effort).
          try (DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.setSoTimeout(timeout);
            InetAddress address = InetAddress.getByName(host);
            byte[] probe = new byte[0];
            DatagramPacket packet = new DatagramPacket(probe, 0, address, port);
            datagramSocket.send(packet);
            byte[] buf = new byte[1];
            DatagramPacket response = new DatagramPacket(buf, buf.length);
            try {
              datagramSocket.receive(response);
              isOpen = true;
            } catch (java.net.SocketTimeoutException e1) {
              // No response within timeout — assume open (UDP best-effort)
              isOpen = true;
            } catch (java.net.PortUnreachableException e1) {
              isOpen = false;
              error = "Port unreachable";
            }
          }
        } else {
          error = "Unsupported protocol: " + protocol;
        }
      } catch (Exception e) {
        error = e.getMessage();
      }

      JSObject result = new JSObject();
      try {
        result.put("open", isOpen);

        if (!isOpen) {
          result.put("error", error.isEmpty() ? "Timeout or unreachable" : error);
        }
        call.resolve(result);
      } catch (Exception e) {
        call.reject("Failed to build result JSON", e);
      }
    });
  }

  public void shutdown() {
    executor.shutdownNow();
  }
}
