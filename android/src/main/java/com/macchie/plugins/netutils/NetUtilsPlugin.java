package com.macchie.plugins.netutils;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "NetUtils")
public class NetUtilsPlugin extends Plugin {
  
  private NetUtils _netUtils = new NetUtils();
  private SSHUtils _sshUtils = new SSHUtils();
  private PortForwarder _portForwarder = new PortForwarder();
  
  // basic
  
  @PluginMethod
  public void getInterfaces(PluginCall call) {
    _netUtils.getInterfaces(call);
  }
  
  @PluginMethod
  public void checkPort(PluginCall call) {
    _netUtils.checkPort(call);
  }

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
}
