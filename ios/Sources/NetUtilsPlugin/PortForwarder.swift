import Foundation
import Network
import Capacitor

@objc(PortForwarder)
public class PortForwarder: NSObject {

  private let sessionsLock = NSLock()
  private var sessions: [String: ForwardingSession] = [:]

  @objc func startForwarding(_ call: CAPPluginCall) {
    guard let localPort = call.getInt("localPort"),
          let targetHost = call.getString("targetHost"),
          let targetPort = call.getInt("targetPort") else {
      call.reject("Missing required parameters: localPort, targetHost, targetPort")
      return
    }

    let protocolStr = call.getString("protocol")?.lowercased() ?? "tcp"
    let sessionId = call.getString("id") ?? UUID().uuidString

    do {
      let session = try ForwardingSession(
        id: sessionId,
        protocolType: protocolStr,
        localPort: UInt16(localPort),
        targetHost: targetHost,
        targetPort: UInt16(targetPort)
      )
        sessionsLock.lock()
        sessions[sessionId] = session
        sessionsLock.unlock()
        session.start()
        call.resolve(["success": true, "id": sessionId])
      } catch {
        call.reject("Failed to start forwarding: \(error.localizedDescription)")
      }
    }

  @objc func stopForwarding(_ call: CAPPluginCall) {
    guard let sessionId = call.getString("id") else {
      call.reject("Session id not found")
      return
    }

    sessionsLock.lock()
    let session = sessions.removeValue(forKey: sessionId)
    sessionsLock.unlock()

    guard let session = session else {
      call.reject("Session id not found")
      return
    }

    session.stop()
    call.resolve(["success": true, "id": sessionId])
  }
}

/// A production-ready session for port forwarding.
class ForwardingSession {
  
  let id: String
  let protocolType: String  // "tcp" or "udp"
  let localPort: UInt16
  let targetHost: String
  let targetPort: UInt16
  let listener: NWListener
  let listenerQueue: DispatchQueue

  /**
    Initializes a forwarding session.
    
    - Parameters:
      - id: Unique session identifier.
      - protocolType: "tcp" (default) or "udp".
      - localPort: The local port number to bind the listener.
      - targetHost: The destination host.
      - targetPort: The destination port.
    */
  init(id: String, protocolType: String, localPort: UInt16, targetHost: String, targetPort: UInt16) throws {
    self.id = id
    self.protocolType = protocolType
    self.localPort = localPort
    self.targetHost = targetHost
    self.targetPort = targetPort
    self.listenerQueue = DispatchQueue(label: "com.macchie.PortForwarder.\(id)")
        
    // Choose NWParameters based on protocol type.
    let params: NWParameters
    if protocolType == "udp" {
      let udpOptions = NWProtocolUDP.Options()
      params = NWParameters(dtls: nil, udp: udpOptions)
    } else {
      let tcpOptions = NWProtocolTCP.Options()
      params = NWParameters(tls: nil, tcp: tcpOptions)
    }
        
    // Create the listener.
    guard let nwPort = NWEndpoint.Port(rawValue: localPort) else {
        throw NSError(domain: "PortForwardingError", code: 0, userInfo: [NSLocalizedDescriptionKey: "Invalid local port"])
    }

    do {
      listener = try NWListener(using: params, on: nwPort)
    } catch {
      throw error
    }
  }
    
  /// Starts the listener and configures the connection handling.
  func start() {
    listener.stateUpdateHandler = { newState in
      switch newState {
      case .ready:
        print("Session \(self.id): Listener ready on port \(self.localPort)")
      case .failed(let error):
        print("Session \(self.id): Listener failed with error: \(error)")
        self.stop()
      default:
        break
      }
    }
    
    listener.newConnectionHandler = { newConnection in
      newConnection.start(queue: self.listenerQueue)
      self.handleNewConnection(newConnection)
    }
    
    listener.start(queue: listenerQueue)
  }
    
  /// Stops the listener and cleans up resources.
  func stop() {
    listener.cancel()
    print("Session \(self.id): Listener stopped")
  }
    
  /// Handles an incoming connection by establishing a corresponding connection to the target and starting bidirectional forwarding.
  private func handleNewConnection(_ connection: NWConnection) {
    let targetEndpoint = NWEndpoint.Host(self.targetHost)
    
    guard let targetPortNW = NWEndpoint.Port(rawValue: self.targetPort) else {
      print("Session \(self.id): Invalid target port \(self.targetPort)")
      connection.cancel()
      return
    }
      
    // Create connection parameters matching the protocol type.
    let params: NWParameters
    
    if self.protocolType == "udp" {
      let udpOptions = NWProtocolUDP.Options()
      params = NWParameters(dtls: nil, udp: udpOptions)
    } else {
      let tcpOptions = NWProtocolTCP.Options()
      params = NWParameters(tls: nil, tcp: tcpOptions)
    }
      
    let targetConnection = NWConnection(host: targetEndpoint, port: targetPortNW, using: params)

    targetConnection.stateUpdateHandler = { newState in
        switch newState {
        case .ready:
          print("Session \(self.id): Target connection ready")
        case .failed(let error):
          print("Session \(self.id): Target connection failed: \(error)")
          connection.cancel()
          targetConnection.cancel()
        default:
          break
        }
    }

    targetConnection.start(queue: self.listenerQueue)
      
    // Set up bidirectional forwarding.
    if self.protocolType == "udp" {
      self.forwardUDP(source: connection, destination: targetConnection)
      self.forwardUDP(source: targetConnection, destination: connection)
    } else {
      self.forwardTCP(source: connection, destination: targetConnection)
      self.forwardTCP(source: targetConnection, destination: connection)
    }
  }
    
  /// Forwards TCP data bidirectionally between two connections.
  private func forwardTCP(source: NWConnection, destination: NWConnection) {
    source.receive(minimumIncompleteLength: 1, maximumLength: 65536) { data, context, isComplete, error in
      if let data = data, !data.isEmpty {
        destination.send(content: data, completion: .contentProcessed({ sendError in
          if let sendError = sendError {
            print("Session \(self.id): TCP send error: \(sendError)")
          }
        }))
      }

      if isComplete {
        source.cancel()
        destination.cancel()
        return
      } else if let error = error {
        print("Session \(self.id): TCP receive error: \(error)")
        source.cancel()
        destination.cancel()
        return
      }

      self.forwardTCP(source: source, destination: destination)
    }
  }
    
  /// Forwards UDP data bidirectionally between two connections.
  private func forwardUDP(source: NWConnection, destination: NWConnection) {
    // Use receiveMessage for connectionless UDP traffic.
    source.receiveMessage { data, context, isComplete, error in
      if let data = data, !data.isEmpty {
        destination.send(content: data, completion: .contentProcessed({ sendError in
          if let sendError = sendError {
            print("Session \(self.id): UDP send error: \(sendError)")
          }
        }))
      }

      if isComplete {
        source.cancel()
        destination.cancel()
        return
      } else if let error = error {
        print("Session \(self.id): UDP receive error: \(error)")
        source.cancel()
        destination.cancel()
        return
      }

      self.forwardUDP(source: source, destination: destination)
    }
  }
}
