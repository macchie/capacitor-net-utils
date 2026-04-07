import Foundation
import Capacitor
import Network

@objc(TCPSocket)
public class TCPSocket: NSObject {

  var connection: NWConnection?
  private let connectionQueue = DispatchQueue(label: "com.macchie.TCPSocket.connection")

  weak var plugin: NetUtilsPlugin?

  init(plugin: NetUtilsPlugin) {
    self.plugin = plugin
  }

  @objc func connect(_ call: CAPPluginCall) {
    guard let host = call.getString("host"), let port = call.getInt("port") else {
      call.reject("Missing host or port")
      return
    }

    guard port > 0, port <= 65535, let nwPort = NWEndpoint.Port(rawValue: UInt16(port)) else {
      call.reject("Invalid port number")
      return
    }

    let nwEndpoint = NWEndpoint.Host(host)
    connection = NWConnection(host: nwEndpoint, port: nwPort, using: .tcp)

    let timeoutSeconds: Double = 5
    var resolved = false
    let lock = NSLock()

    connection?.stateUpdateHandler = { [weak self] state in
      switch state {
      case .ready:
        lock.lock()
        let alreadyResolved = resolved
        resolved = true
        lock.unlock()
        if !alreadyResolved {
          self?.receive()
          call.resolve(["success": true])
        }
      case .failed(let error):
        lock.lock()
        let alreadyResolved = resolved
        resolved = true
        lock.unlock()
        if !alreadyResolved {
          call.reject("Connection failed: \(error.localizedDescription)")
        }
      case .cancelled:
        lock.lock()
        let alreadyResolved = resolved
        resolved = true
        lock.unlock()
        if !alreadyResolved {
          call.reject("Connection cancelled")
        }
      default:
        break
      }
    }

    connection?.start(queue: connectionQueue)

    DispatchQueue.global(qos: .userInitiated).asyncAfter(deadline: .now() + timeoutSeconds) { [weak self] in
      lock.lock()
      let alreadyResolved = resolved
      resolved = true
      lock.unlock()
      if !alreadyResolved {
        self?.connection?.cancel()
        call.reject("Connection timed out")
      }
    }
  }

  func receive() {
    connection?.receive(minimumIncompleteLength: 1, maximumLength: 1024) { [weak self] data, _, isComplete, error in
      if let data = data, !data.isEmpty, let string = String(data: data, encoding: .utf8) {
        self?.plugin?.notifyListeners("tcp:message", data: ["data": string])
      }

      if isComplete || error != nil {
        return
      }

      self?.receive()
    }
  }

  @objc func write(_ call: CAPPluginCall) {
    guard let dataStr = call.getString("data"), let data = dataStr.data(using: .utf8) else {
      call.reject("Missing or invalid data")
      return
    }

    guard let connection = connection else {
      call.reject("No active connection")
      return
    }

    connection.send(content: data, completion: .contentProcessed({ error in
      if let error = error {
        call.reject("Failed to send data: \(error.localizedDescription)")
      } else {
        call.resolve(["success": true])
      }
    }))
  }

  @objc func disconnect(_ call: CAPPluginCall?) {
    connection?.cancel()
    connection = nil
    call?.resolve(["success": true])
  }
}
