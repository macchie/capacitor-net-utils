import Foundation
import Capacitor
import NMSSH

@objc(SSHSession)
public class SSHSession: NSObject, NMSSHChannelDelegate {

  var session: NMSSHSession?
  var channel: NMSSHChannel?

  weak var plugin: NetUtilsPlugin?

  // Buffers for stdout and stderr data
  private var stdoutBuffer: String = ""
  private var stderrBuffer: String = ""
  private var outputTimer: Timer?

  init(plugin: NetUtilsPlugin) {
    self.plugin = plugin
    super.init()
    // Schedule a timer to flush buffers every 0.1 seconds on the main thread
    DispatchQueue.main.async {
      self.outputTimer = Timer.scheduledTimer(timeInterval: 0.1,
                                              target: self,
                                              selector: #selector(self.flushOutputBuffers),
                                              userInfo: nil,
                                              repeats: true)
    }
  }

  deinit {
    outputTimer?.invalidate()
  }

  // NMSSHChannelDelegate: Called when data is received from the shell's stdout
  public func channel(_ channel: NMSSHChannel!, didReadData message: String!) {
    DispatchQueue.global(qos: .userInitiated).async {
      if let message = message {
        self.stdoutBuffer.append(message)
      }
    }
  }
  
  // NMSSHChannelDelegate: Called when error output is received from the shell's stderr
  public func channel(_ channel: NMSSHChannel!, didReadError error: String!) {
    DispatchQueue.global(qos: .userInitiated).async {
      if let errorMsg = error {
        self.stderrBuffer.append(errorMsg)
      }
    }
  }
  
  // Timer callback to flush the buffered output
  @objc private func flushOutputBuffers() {
    if !stdoutBuffer.isEmpty {
      let dataToSend = stdoutBuffer
      stdoutBuffer = ""
      self.plugin?.notifyListeners("ssh:stdout", data: ["data": dataToSend])
    }
    if !stderrBuffer.isEmpty {
      let dataToSend = stderrBuffer
      stderrBuffer = ""
      self.plugin?.notifyListeners("ssh:stderr", data: ["data": dataToSend])
    }
  }

  @objc func connect(_ call: CAPPluginCall) {
    guard let host = call.getString("host"),
          let username = call.getString("username"),
          let password = call.getString("password") else {
      call.reject("Missing parameters. Provide host, username, and password.")
      return
    }
    
    session = NMSSHSession.connect(toHost: host, withUsername: username)
      
    if let session = session, session.isConnected {
      session.authenticate(byPassword: password)
      if session.isAuthorized {
        call.resolve(["success": true])
      } else {
        call.reject("Authentication failed")
      }
    } else {
      call.reject("Connection failed")
    }
  }

  @objc func startShell(_ call: CAPPluginCall) {
    guard let session = session, session.isConnected else {
      call.reject("No active session. Call connect() first.")
      return
    }
    
    channel = session.channel
    channel?.delegate = self
    channel?.requestPty = true
    
    // Run the shell on a background thread to prevent blocking the main thread
    DispatchQueue.global(qos: .background).async {
      do {
        try self.channel?.startShell()
        DispatchQueue.main.async {
          call.resolve(["success": true])
        }
      } catch let error {
        DispatchQueue.main.async {
          call.reject("Failed to start shell: \(error.localizedDescription)")
        }
      }
    }
  }

  @objc func write(_ call: CAPPluginCall) {
    guard let command = call.getString("command") else {
      call.reject("No command provided")
      return
    }
    
    do {
      try channel?.write(command + "\n")
      call.resolve(["success": true])
    } catch let error {
      call.reject("Failed to write command: \(error.localizedDescription)")
    }
  }

  @objc func disconnect(_ call: CAPPluginCall) {
    if let channel = channel {
      channel.closeShell()
    }
    session?.disconnect()
    session = nil
    call.resolve(["success": true])
  }
}
