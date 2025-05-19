import Foundation
import Capacitor
import Network
import NMSSH

@objc(SSHUtils)
public class SSHUtils: NSObject {
  
  @objc func execSync(_ call: CAPPluginCall) {
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
