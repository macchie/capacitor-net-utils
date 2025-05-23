package com.macchie.plugins.netutils;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.net.NetworkInterface;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import org.json.JSONArray;
import java.util.concurrent.Executors;

public class NetUtils {

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
          
          if (name.startsWith("wlan") || name.startsWith("eth")) {
            type = "wifi";
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

    int timeout = call.getInt("timeout") != null ? call.getInt("timeout") : 5000;
    int port = portInt;

    Executors.newSingleThreadExecutor().execute(() -> {
      boolean isOpen = false;
      String error = "";

      try {
        if (protocol.equalsIgnoreCase("tcp")) {
          try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            isOpen = true;
          }
        } else if (protocol.equalsIgnoreCase("udp")) {
          try (DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.connect(new InetSocketAddress(host, port));
            isOpen = true;
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
  
}
