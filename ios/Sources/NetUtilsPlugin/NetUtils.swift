import Foundation
import Capacitor
import Network

@objc(NetUtils)
public class NetUtils: NSObject {

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

  @objc func getInterfaces(_ call: CAPPluginCall) {
    var interfacesResult = [[String: String]]()
    var ifaddr: UnsafeMutablePointer<ifaddrs>? = nil

    if getifaddrs(&ifaddr) == 0 {
      var ptr = ifaddr
    
      while ptr != nil {
        defer { ptr = ptr?.pointee.ifa_next }

        guard let interface = ptr?.pointee else { continue }
        let name = String(cString: interface.ifa_name)
        let addrFamily = interface.ifa_addr.pointee.sa_family

        if addrFamily == UInt8(AF_INET) || addrFamily == UInt8(AF_INET6) {
          var hostname = [CChar](repeating: 0, count: Int(NI_MAXHOST))

          getnameinfo(
            interface.ifa_addr,
            socklen_t(interface.ifa_addr.pointee.sa_len),
            &hostname,
            socklen_t(hostname.count),
            nil,
            socklen_t(0),
            NI_NUMERICHOST
          )

          let address = String(cString: hostname)
          let type: String

          if name.hasPrefix("en") {
            type = "wifi"
          } else if name.hasPrefix("utun") || name.hasPrefix("tun") {
            type = "vpn"
          } else if name.hasPrefix("pdp_ip") {
            type = "cellular"
          } else {
            type = "other"
          }

          interfacesResult.append([
            "name": name,
            "address": address,
            "type": type
          ])
        }
      }

      freeifaddrs(ifaddr)
    }

    call.resolve([ "output": interfacesResult ?? [], "error": "" ])
  }
}
