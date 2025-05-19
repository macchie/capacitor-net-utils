import { PluginListenerHandle, WebPlugin } from '@capacitor/core';
import { NetUtilsPlugin } from './definitions';


export class NetUtilsWeb extends WebPlugin implements NetUtilsPlugin {

  constructor() {
    super();
  }

  // basic

  addListener(_eventName: 'ssh:stdout' | 'ssh:stderr' | 'tcp:message', _listenerFunc: (event: { data: string }) => void): Promise<PluginListenerHandle> & PluginListenerHandle {
    throw this.unimplemented('addListener is not available in the web environment.');
  }

  async removeAllListeners() {
    throw this.unimplemented('removeAllListeners is not available in the web environment.');
  }

  async getInterfaces(): Promise<{
    output: {
      name: string;
      address: string;
      type: 'wifi' | 'vpn' | 'cellular' | 'other';
    }[];
    error?: string;
  }> {
    throw this.unimplemented('getIpAddresses is not available in the web environment.');
  }

  async checkPort(_options: { host: string; port: number; protocol: 'tcp' | 'udp'; timeout?: number }): Promise<{ open: boolean; error?: string }> {
    throw this.unimplemented('checkPort is not available in the web environment.');
  }

  async resolveHostname(_options: { host: string; timeout?: number }): Promise<{ hostname: string | null; error?: string }> {
    throw this.unimplemented('resolveHostname is not available in the web environment.');
  }

  async startForwarding(_options: { id?: string; localPort: number; targetHost: string; targetPort: number; protocol: 'tcp' | 'udp'; }): Promise<{ success: boolean; id?: string; error?: string }> {
    throw this.unimplemented('startForwarding is not available in the web environment.');
  }

  async stopForwarding(_options: { id?: string; }): Promise<{ success: boolean; id?: string; error?: string }> {
    throw this.unimplemented('stopForwarding is not available in the web environment.');
  }

  // ssh

  async sshExecSync(_options: { host: string; port?: number; user: string; password: string; command: string }): Promise<{ output: string; error?: string }> {
    throw this.unimplemented('sshExecSync is not available in the web environment.');
  }

  async sshConnect(_options: {  host: string; port: number, username: string, password: string }): Promise<{ success: boolean; error?: string }> {
    throw this.unimplemented('sshConnect is not available in the web environment.');
  }
  
  async sshWrite(_options: { command: string }): Promise<{ success: boolean; error?: string }> {
    throw this.unimplemented('sshWrite is not available in the web environment.');
  }

  async sshStartShell(): Promise<{ success: boolean; error?: string }> {
    throw this.unimplemented('sshStartShell is not available in the web environment.');
  }

  async sshDisconnect(): Promise<{ success: boolean; error?: string }> {
    throw this.unimplemented('sshDisconnect is not available in the web environment.');
  }

  // tcp

  async tcpConnect(_options: {  host: string; port: number }): Promise<{ success: boolean; error?: string }> {
    throw this.unimplemented('tcpConnect is not available in the web environment.');
  }
  
  async tcpWrite(_options: { data: string }): Promise<{ success: boolean; error?: string }> {
    throw this.unimplemented('tcpWrite is not available in the web environment.');
  }

  async tcpDisconnect(): Promise<{ success: boolean; error?: string }> {
    throw this.unimplemented('tcpDisconnect is not available in the web environment.');
  }

}
