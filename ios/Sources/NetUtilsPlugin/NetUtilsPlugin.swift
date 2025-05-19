import Foundation
import Capacitor

@objc(NetUtilsPlugin)
public class NetUtilsPlugin: CAPPlugin, CAPBridgedPlugin {

  public let identifier = "NetUtilsPlugin"
  public let jsName = "NetUtils"
  
  public let pluginMethods: [CAPPluginMethod] = [
    CAPPluginMethod(name: "getInterfaces", returnType: CAPPluginReturnPromise),
    CAPPluginMethod(name: "checkPort", returnType: CAPPluginReturnPromise),
    CAPPluginMethod(name: "resolveHostname", returnType: CAPPluginReturnPromise),

    CAPPluginMethod(name: "startForwarding", returnType: CAPPluginReturnPromise),
    CAPPluginMethod(name: "stopForwarding", returnType: CAPPluginReturnPromise),

    CAPPluginMethod(name: "sshExecSync", returnType: CAPPluginReturnPromise),
    CAPPluginMethod(name: "sshConnect", returnType: CAPPluginReturnPromise),
    CAPPluginMethod(name: "sshStartShell", returnType: CAPPluginReturnPromise),
    CAPPluginMethod(name: "sshWrite", returnType: CAPPluginReturnPromise),
    CAPPluginMethod(name: "sshDisconnect", returnType: CAPPluginReturnPromise),

    CAPPluginMethod(name: "tcpConnect", returnType: CAPPluginReturnPromise),
    CAPPluginMethod(name: "tcpWrite", returnType: CAPPluginReturnPromise),
    CAPPluginMethod(name: "tcpDisconnect", returnType: CAPPluginReturnPromise),
  ]
  
  private let netUtils = NetUtils()
  private let sshUtils: SSHUtils = SSHUtils()
  private let portForwarder = PortForwarder()
  private var sshSession: SSHSession!
  private var tcpSocket: TCPSocket!

  // basic

  @objc func getInterfaces(_ call: CAPPluginCall) {
    return netUtils.getInterfaces(call)
  }

  @objc func checkPort(_ call: CAPPluginCall) {
    return netUtils.checkPort(call)
  }

  @objc func resolveHostname(_ call: CAPPluginCall) {
    return netUtils.resolveHostname(call)
  }

  @objc func startForwarding(_ call: CAPPluginCall) {
    return portForwarder.startForwarding(call)
  }

  @objc func stopForwarding(_ call: CAPPluginCall) {
    return portForwarder.stopForwarding(call)
  }

  // ssh

  @objc func sshExecSync(_ call: CAPPluginCall) {
    return sshUtils.execSync(call)
  }

  @objc func sshConnect(_ call: CAPPluginCall) {
    sshSession = SSHSession(plugin: self)
    return sshSession.connect(call)
  }

  @objc func sshStartShell(_ call: CAPPluginCall) {
    guard let sshSession = sshSession else {
      call.reject("No active connection to write to.")
      return
    }
    
    return sshSession.startShell(call)
  }

  @objc func sshWrite(_ call: CAPPluginCall) {
    guard let sshSession = sshSession else {
      call.reject("No active connection to write to.")
      return
    }
    
    return sshSession.write(call)
  }

  @objc func sshDisconnect(_ call: CAPPluginCall) {
    guard let sshSession = sshSession else {
      call.reject("No active connection to disconnect.")
      return
    }

    return sshSession.disconnect(call)
  }

  // tcp

  @objc func tcpConnect(_ call: CAPPluginCall) {
    tcpSocket = TCPSocket(plugin: self)
    return tcpSocket.connect(call)
  }

  @objc func tcpWrite(_ call: CAPPluginCall) {
    guard let tcpSocket = tcpSocket else {
      call.reject("No active connection to write to.")
      return
    }
    
    return tcpSocket.write(call)
  }

  @objc func tcpDisconnect(_ call: CAPPluginCall) {
    guard let tcpSocket = tcpSocket else {
      call.reject("No active connection to disconnect.")
      return
    }

    return tcpSocket.disconnect(call)
  }
}
