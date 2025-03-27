import Foundation
import Capacitor
import Network
import NMSSH

@objc(NetUtils)
public class NetUtils: NSObject {

    // @objc public func echo(_ value: String) -> String {
    //     print(value)
    //     return value
    // }

    @objc func checkPort(_ call: CAPPluginCall) {
    guard let host = call.getString("host"),
          let portInt = call.getInt("port"),
          let protocolStr = call.getString("protocol") else {
      call.reject("Host, port, and protocol are required.")
      return
    }
    
    guard let port = NWEndpoint.Port(rawValue: UInt16(portInt)) else {
      call.reject("Invalid port number.")
      return
    }
    
    let timeout = call.getInt("timeout") ?? 5000
    let parameters: NWParameters = (protocolStr.lowercased() == "udp") ? NWParameters.udp : NWParameters.tcp
    
    let connection = NWConnection(host: NWEndpoint.Host(host), port: port, using: parameters)
    
    connection.stateUpdateHandler = { newState in
      switch newState {
      case .ready:
        call.resolve(["open": true])
        connection.cancel()
      case .failed(let error):
        call.resolve(["open": false, "error": error.localizedDescription])
        connection.cancel()
      default:
        break
      }
    }
    
    connection.start(queue: .global())
    
    // If connection isn't ready within the timeout, resolve with a timeout error.
    DispatchQueue.global().asyncAfter(deadline: .now() + .milliseconds(timeout)) {
      if connection.state != .ready {
        call.resolve(["open": false, "error": "Timeout"])
        connection.cancel()
      }
    }
  }

  @objc func resolveHostname(_ call: CAPPluginCall) {
    guard let host = call.getString("host") else {
      call.reject("HOST is required.")
      return
    }
    
    // Run on a background queue
    DispatchQueue.global().async {
      var hostname: String?
      var infoPtr: UnsafeMutablePointer<addrinfo>?
      
      var hints = addrinfo(
        ai_flags: AI_NUMERICHOST,
        ai_family: AF_UNSPEC,
        ai_socktype: SOCK_STREAM,
        ai_protocol: 0,
        ai_addrlen: 0,
        ai_canonname: nil,
        ai_addr: nil,
        ai_next: nil)
      
      let error = getaddrinfo(host, nil, &hints, &infoPtr)
      if error == 0, let info = infoPtr, let addr = info.pointee.ai_addr {
        var hostBuffer = [CChar](repeating: 0, count: Int(NI_MAXHOST))
        let error2 = getnameinfo(addr, socklen_t(info.pointee.ai_addrlen), &hostBuffer, socklen_t(hostBuffer.count), nil, 0, NI_NAMEREQD)
        if error2 == 0 {
          hostname = String(cString: hostBuffer)
        }
      }
      if let infoPtr = infoPtr {
        freeaddrinfo(infoPtr)
      }
      
      if let hostname = hostname {
        call.resolve(["hostname": hostname])
      } else {
        call.resolve(["hostname": NSNull(), "error": "Hostname not found"])
      }
    }
  }

  @objc func runSSHCommand(_ call: CAPPluginCall) {
    guard let host = call.getString("host"),
          let user = call.getString("user"),
          let password = call.getString("password"),
          let command = call.getString("command") else {
      call.reject("host, user, password, and command are required.")
      return
    }
    
    let port = call.getInt("port", 22) ?? 22
    
    DispatchQueue.global().async {
      // Create and connect the session.
      if let session = NMSSHSession.connect(toHost: "\(host):\(port)", withUsername: user) {
        if session.isConnected {
          session.authenticate(byPassword: password)
          
          if session.isAuthorized {
            var error: NSError?
            let response = session.channel.execute(command, error: &error, timeout: 10)
            DispatchQueue.main.async {
              if let err = error {
                call.resolve(["output": "", "error": err.localizedDescription])
              } else {
                call.resolve(["output": response ?? ""])
              }
              session.disconnect()
            }
          } else {
            DispatchQueue.main.async {
              call.resolve(["output": "", "error": "Authentication failed"])
              session.disconnect()
            }
          }
        } else {
          DispatchQueue.main.async {
            call.resolve(["output": "", "error": "Connection failed"])
          }
        }
      } else {
        DispatchQueue.main.async {
          call.resolve(["output": "", "error": "Unable to create SSH session"])
        }
      }
    }
  }
}
