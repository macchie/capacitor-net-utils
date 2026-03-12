import Foundation
import Capacitor
import NMSSH

@objc(SSHSession)
public class SSHSession: NSObject, NMSSHChannelDelegate {

  var session: NMSSHSession?
  var channel: NMSSHChannel?

  weak var plugin: NetUtilsPlugin?

  private let bufferQueue = DispatchQueue(label: "com.macchie.SSHSession.buffer")
  private var stdoutBuffer: String = ""
  private var stderrBuffer: String = ""
  private var outputTimer: Timer?

  init(plugin: NetUtilsPlugin) {
    self.plugin = plugin
    super.init()
    startFlushTimer()
  }

  private func startFlushTimer() {
    DispatchQueue.main.async { [weak self] in
      guard let self = self else { return }
      self.outputTimer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { [weak self] _ in
        self?.flushOutputBuffers()
      }
    }
  }

  func cleanup() {
    outputTimer?.invalidate()
    outputTimer = nil
    if let channel = channel {
      channel.closeShell()
    }
    channel = nil
    session?.disconnect()
    session = nil
  }

  deinit {
    outputTimer?.invalidate()
  }

  public func channel(_ channel: NMSSHChannel!, didReadData message: String!) {
    guard let message = message else { return }
    bufferQueue.async {
      self.stdoutBuffer.append(message)
    }
  }

  public func channel(_ channel: NMSSHChannel!, didReadError error: String!) {
    guard let errorMsg = error else { return }
    bufferQueue.async {
      self.stderrBuffer.append(errorMsg)
    }
  }

  private func flushOutputBuffers() {
    bufferQueue.async { [weak self] in
      guard let self = self else { return }

      if !self.stdoutBuffer.isEmpty {
        let dataToSend = self.stdoutBuffer
        self.stdoutBuffer = ""
        self.plugin?.notifyListeners("ssh:stdout", data: ["data": dataToSend])
      }
      if !self.stderrBuffer.isEmpty {
        let dataToSend = self.stderrBuffer
        self.stderrBuffer = ""
        self.plugin?.notifyListeners("ssh:stderr", data: ["data": dataToSend])
      }
    }
  }

  @objc func connect(_ call: CAPPluginCall) {
    guard let host = call.getString("host"),
          let username = call.getString("username"),
          let password = call.getString("password") else {
      call.reject("Missing parameters. Provide host, username, and password.")
      return
    }

    let port = call.getInt("port") ?? 22

    DispatchQueue.global(qos: .userInitiated).async { [weak self] in
      let session = NMSSHSession.connect(toHost: "\(host):\(port)", withUsername: username)

      if let session = session, session.isConnected {
        session.authenticate(byPassword: password)
        if session.isAuthorized {
          self?.session = session
          call.resolve(["success": true])
        } else {
          session.disconnect()
          call.reject("Authentication failed")
        }
      } else {
        call.reject("Connection failed")
      }
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

    DispatchQueue.global(qos: .background).async { [weak self] in
      do {
        try self?.channel?.startShell()
        call.resolve(["success": true])
      } catch let error {
        call.reject("Failed to start shell: \(error.localizedDescription)")
      }
    }
  }

  @objc func write(_ call: CAPPluginCall) {
    guard let command = call.getString("command") else {
      call.reject("No command provided")
      return
    }

    guard let channel = channel else {
      call.reject("No active shell. Call startShell() first.")
      return
    }

    DispatchQueue.global(qos: .userInitiated).async {
      do {
        try channel.write(command + "\n")
        call.resolve(["success": true])
      } catch let error {
        call.reject("Failed to write command: \(error.localizedDescription)")
      }
    }
  }

  @objc func disconnect(_ call: CAPPluginCall) {
    cleanup()
    call.resolve(["success": true])
  }
}
