import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(NetUtilsPlugin)
public class NetUtilsPlugin: CAPPlugin, CAPBridgedPlugin {

    public let identifier = "NetUtilsPlugin"
    public let jsName = "NetUtils"
    
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "checkPort", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "resolveHostname", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "runSSHCommand", returnType: CAPPluginReturnPromise),
    ]
    
    private let implementation = NetUtils()

    @objc func checkPort(_ call: CAPPluginCall) {
        return implementation.checkPort(call)
    }

    @objc func resolveHostname(_ call: CAPPluginCall) {
        return implementation.resolveHostname(call)
    }

    @objc func runSSHCommand(_ call: CAPPluginCall) {
        return implementation.runSSHCommand(call)
    }
}
