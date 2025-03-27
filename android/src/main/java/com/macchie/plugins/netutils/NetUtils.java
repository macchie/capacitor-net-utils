package com.macchie.plugins.netutils;

import android.util.Log;

public class NetUtils {

    @PluginMethod
    public void ping(final PluginCall call) {
        final String host = call.getString("host");
        final int count = call.getInt("count", 1);
        final int timeout = call.getInt("timeout", 5000);
        if (host == null) {
            call.reject("Host is required.");
            return;
        }
        
        new Thread(() -> {
            JSObject ret = new JSObject();
            try {
                // Construct a command string.
                // -c: number of packets; -W: per-packet timeout (in seconds)
                int timeoutSec = Math.max(1, timeout / 1000);
                String command = "ping -c " + count + " -W " + timeoutSec + " " + host;
                Process process = Runtime.getRuntime().exec(command);
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                process.waitFor();
                
                // Look for a line like: "rtt min/avg/max/mdev = 12.345/67.890/123.456/7.890 ms"
                String avgTimeStr = null;
                String[] lines = output.toString().split("\n");
                for (String l : lines) {
                    if (l.contains("rtt")) {
                        int equalsIndex = l.indexOf("=");
                        if (equalsIndex != -1) {
                            String stats = l.substring(equalsIndex + 1).trim();
                            String[] parts = stats.split("/");
                            if (parts.length >= 2) {
                                avgTimeStr = parts[1];
                            }
                        }
                        break;
                    }
                }
                if (avgTimeStr != null) {
                    double avgTime = Double.parseDouble(avgTimeStr);
                    ret.put("avgTime", avgTime);
                } else {
                    ret.put("avgTime", null);
                }
                call.resolve(ret);
            } catch (Exception e) {
                ret.put("avgTime", null);
                ret.put("error", e.getMessage());
                call.resolve(ret);
            }
        }).start();
    }

    @PluginMethod
    public void checkPort(final PluginCall call) {
        final String host = call.getString("host");
        final Integer port = call.getInt("port");
        final String protocol = call.getString("protocol", "tcp").toLowerCase();
        final int timeout = call.getInt("timeout", 5000);

        if (host == null || port == null || protocol == null) {
            call.reject("Host, port, and protocol are required.");
            return;
        }

        // Run the network operation on a background thread.
        new Thread(() -> {
            JSObject ret = new JSObject();
            if (protocol.equals("tcp")) {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(host, port), timeout);
                    socket.close();
                    ret.put("open", true);
                } catch (Exception e) {
                    ret.put("open", false);
                    ret.put("error", e.getMessage());
                }
            } else if (protocol.equals("udp")) {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    socket.setSoTimeout(timeout);
                    InetSocketAddress address = new InetSocketAddress(host, port);
                    byte[] buf = new byte[1]; // minimal payload
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address);
                    socket.send(packet);
                    
                    // Attempt to receive a response. Note: Many UDP services are connectionless and may not reply.
                    try {
                        socket.receive(packet);
                    } catch (SocketTimeoutException e) {
                        // Timeout is expected if no response is sent. We consider the send as a success.
                    }
                    socket.close();
                    ret.put("open", true);
                } catch (Exception e) {
                    ret.put("open", false);
                    ret.put("error", e.getMessage());
                }
            } else {
                ret.put("open", false);
                ret.put("error", "Unsupported protocol: " + protocol);
            }
            call.resolve(ret);
        }).start();
    }

    @PluginMethod
    public void resolveHostname(final PluginCall call) {
        final String ip = call.getString("ip");
        if (ip == null) {
            call.reject("IP is required.");
            return;
        }
        
        new Thread(() -> {
            JSObject ret = new JSObject();
            try {
                InetAddress addr = InetAddress.getByName(ip);
                String hostname = addr.getHostName();
                ret.put("hostname", hostname);
            } catch (Exception e) {
                ret.put("hostname", (String) null);
                ret.put("error", e.getMessage());
            }
            call.resolve(ret);
        }).start();
    }
}
