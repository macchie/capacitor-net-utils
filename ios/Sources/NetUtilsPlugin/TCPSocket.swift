import Foundation
import Capacitor
import Network

@objc(TCPSocket)
public class TCPSocket: NSObject {
  
  var connection: NWConnection?
  var listenerId: String?

  weak var plugin: NetUtilsPlugin?

  init(plugin: NetUtilsPlugin) {
    self.plugin = plugin
  }

  @objc func connect(_ call: CAPPluginCall) {
    guard let host = call.getString("host"), let port = call.getInt("port") else {
      call.reject("Missing host or port")
      return
    }

    let nwEndpoint = NWEndpoint.Host(host)
    let nwPort = NWEndpoint.Port("\(port)")!

    connection = NWConnection(host: nwEndpoint, port: nwPort, using: .tcp)
    
    let timeoutSeconds: Double = 5 // Timeout duration
    let timeoutItem = DispatchWorkItem { [weak self] in
      if let conn = self?.connection, conn.state != .ready {
        conn.cancel()
        call.reject("Connection timed out")
      }
    }

    connection?.stateUpdateHandler = { [weak self] state in
      switch state {
      case .ready:
        timeoutItem.cancel()
        self?.receive()
        call.resolve(["success": true])
      case .failed(let error):
        call.reject("Connection failed: \(error)")
      default:
        break
      }
    }

    connection?.start(queue: .global())

    // If connection isn't ready within the timeout, cancel the connection and reject the call.
    DispatchQueue.main.asyncAfter(deadline: .now() + timeoutSeconds, execute: timeoutItem)

  }

  func receive() {
    connection?.receive(minimumIncompleteLength: 1, maximumLength: 1024) { [weak self] data, _, _, _ in
      if let data = data, let string = String(data: data, encoding: .utf8) {
          self!.plugin!.notifyListeners("tcp:message", data: ["data": string])
      }

      self?.receive()
    }
  }

  @objc func write(_ call: CAPPluginCall) {
    guard let dataStr = call.getString("data"), let data = dataStr.data(using: .utf8) else {
      call.reject("Missing or invalid data")

      return
    }

    connection?.send(content: data, completion: .contentProcessed({ _ in
      call.resolve(["success": true])
    }))
  }

  @objc func disconnect(_ call: CAPPluginCall) {
    if (connection?.state == .ready) {
      connection?.cancel()
    }

    call.resolve(["success": true])
  }
}
