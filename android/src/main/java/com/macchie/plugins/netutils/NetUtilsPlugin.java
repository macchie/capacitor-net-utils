package com.macchie.plugins.netutils;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "NetUtils")
public class NetUtilsPlugin extends Plugin {

  private final NetUtils _netUtils = new NetUtils();
  private final SSHUtils _sshUtils = new SSHUtils();
  private final PortForwarder _portForwarder = new PortForwarder();
  private SSHSession _sshSession;
  private TCPSocket _tcpSocket;

  public void emit(String eventName, JSObject data) {
    notifyListeners(eventName, data);
  }

  // basic

  @PluginMethod
  public void getInterfaces(PluginCall call) {
    _netUtils.getInterfaces(call);
  }

  @PluginMethod
  public void checkUrl(PluginCall call) {
    _netUtils.checkUrl(call);
  }

  @PluginMethod
  public void checkPort(PluginCall call) {
    _netUtils.checkPort(call);
  }

  @PluginMethod
  public void resolveHostname(PluginCall call) {
    _netUtils.resolveHostname(call);
  }

  // forwarding

  @PluginMethod
  public void startForwarding(PluginCall call) {
    _portForwarder.startForwarding(call);
  }

  @PluginMethod
  public void stopForwarding(PluginCall call) {
    _portForwarder.stopForwarding(call);
  }

  // ssh

  @PluginMethod
  public void sshExecSync(PluginCall call) {
    _sshUtils.sshExecSync(call);
  }

  @PluginMethod
  public void sshConnect(PluginCall call) {
    _sshSession = new SSHSession(this);
    _sshSession.connect(call);
  }

  @PluginMethod
  public void sshStartShell(PluginCall call) {
    if (_sshSession == null) {
      call.reject("No active connection to start shell on.");
      return;
    }
    _sshSession.startShell(call);
  }

  @PluginMethod
  public void sshWrite(PluginCall call) {
    if (_sshSession == null) {
      call.reject("No active connection to write to.");
      return;
    }
    _sshSession.write(call);
  }

  @PluginMethod
  public void sshDisconnect(PluginCall call) {
    if (_sshSession == null) {
      call.reject("No active connection to disconnect.");
      return;
    }
    _sshSession.disconnect(call);
  }

  // tcp

  @PluginMethod
  public void tcpConnect(PluginCall call) {
    _tcpSocket = new TCPSocket(this);
    _tcpSocket.connect(call);
  }

  @PluginMethod
  public void tcpWrite(PluginCall call) {
    if (_tcpSocket == null) {
      call.reject("No active connection to write to.");
      return;
    }
    _tcpSocket.write(call);
  }

  @PluginMethod
  public void tcpDisconnect(PluginCall call) {
    if (_tcpSocket == null) {
      call.reject("No active connection to disconnect.");
      return;
    }
    _tcpSocket.disconnect(call);
  }
}
