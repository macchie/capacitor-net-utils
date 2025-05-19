import { PluginListenerHandle } from "@capacitor/core";

export interface NetUtilsPlugin {

  addListener(
    eventName: 'ssh:stdout' | 'ssh:stderr' | 'tcp:message',
    listenerFunc: (event: { data: any }) => void
  ): PluginListenerHandle;

  removeAllListeners( ): Promise<void>;

  /**
   * Check if a network port is open on the given host using the specified protocol.
   *
   * @param options.host - The target host (IP or domain)
   * @param options.port - The target port number
   * @param options.protocol - The protocol to use ("tcp" or "udp")
   * @param options.timeout - Timeout in milliseconds (default: 5000)
   * @returns A promise resolving with an object containing an "open" boolean and an optional "error" message.
   */
  checkPort(options: { host: string; port: number; protocol: 'tcp' | 'udp'; timeout?: number }): Promise<{ open: boolean; error?: string }>;

  /**
   * Resolve the hostname for a given IP address.
   *
   * @param options.ip - The IP address to resolve.
   * @param options.timeout - (Optional) Timeout in milliseconds.
   * @returns A promise resolving with an object containing the "hostname" (or null if not found) and an optional "error" message.
   */
  resolveHostname(options: { host: string; timeout?: number }): Promise<{ hostname: string | null; error?: string }>;

  /**
   * Start a port forwarding session from the local device to a remote host.
   *
   * @param options.id - An optional identifier for the forwarding session.
   * @param options.localPort - The local port to forward.
   * @param options.targetHost - The target host (IP or domain) to forward to.
   * @param options.targetPort - The target port on the remote host.
   * @param options.protocol - The protocol to use ("tcp" or "udp").
   * @returns A promise resolving with an object containing a "success" boolean and an optional "error" message.
   */
  startForwarding(options: { id?: string; localPort: number; targetHost: string; targetPort: number; protocol: 'tcp' | 'udp'; }): Promise<{ success: boolean; id?: string; error?: string }>;
  
  /**
   * Stop a port forwarding session.
   *
   * @param options.id - An identifier for the forwarding session.
   * @returns A promise resolving with an object containing a "success" boolean and an optional "error" message.
   */
  stopForwarding(options: { id: string; }): Promise<{ success: boolean; id?: string; error?: string }>;

  /**
   * Run an SSH command on a remote host and get result.
   *
   * @param options.host - The remote host (IP or domain).
   * @param options.port - The SSH port (default: 22).
   * @param options.user - The username for SSH authentication.
   * @param options.password - The password for SSH authentication.
   * @param options.command - The command to execute.
   * @returns A promise resolving with an object containing the "output" string from the command,
   *          or an "error" message if something goes wrong.
   */
  sshExecSync(options: { 
    host: string; 
    port?: number; 
    user: string; 
    password: string; 
    command: string; 
  }): Promise<{ output: string; error?: string }>;

  /**
   * Connects to a remote SSH server using the given parameters.
   * 
   * @param options - The connection options
   * @param options.host - The IP address or hostname of the server
   * @param options.port - The SSH port to connect to
   * @param options.username - The username for SSH authentication
   * @param options.password - The password for SSH authentication
   * @returns A Promise resolving with a success flag
   */
  sshConnect(options: { host: string; port: number, username: string, password: string }): Promise<{ success: boolean, error?: string }>;

  /**
   * Sends a string of data over the SSH connection.
   * 
   * @param options - The data to send
   * @param options.command - The string content to be written to the socket
   * @returns A Promise resolving with a success flag
   */
  sshWrite(options: { command: string }): Promise<{ success: boolean, error?: string }>;

  /**
   * Starts a Shell over SSH connection.
   * 
   * @returns A Promise resolving with a success flag
   */
  sshStartShell(): Promise<{ success: boolean, error?: string }>;

  /**
   * Closes the current SSH connection.
   * 
   * @returns A Promise resolving with a success flag
   */
  sshDisconnect(): Promise<{ success: boolean, error?: string }>;

  /**
   * Get the IP addresses of the device's network interfaces.
   *
   * @returns A promise resolving with an object containing an array of interfaces,
   *          each with a name, address, and type (wifi, vpn, cellular, or other).
   */
  getInterfaces(): Promise<{
    output: {
      name: string;
      address: string;
      type: 'wifi' | 'vpn' | 'cellular' | 'other';
    }[];
    error?: string;
  }>;

  /**
   * Connects to a remote TCP server using the given host and port.
   * 
   * @param options - The connection options
   * @param options.host - The IP address or hostname of the server
   * @param options.port - The TCP port to connect to
   * @returns A Promise resolving with a success flag
   */
  tcpConnect(options: { host: string; port: number }): Promise<{ success: boolean, error?: string }>;

  /**
   * Sends a string of data over the TCP connection.
   * 
   * @param options - The data to send
   * @param options.data - The string content to be written to the socket
   * @returns A Promise resolving with a success flag
   */
  tcpWrite(options: { data: string }): Promise<{ success: boolean, error?: string }>;

  /**
   * Closes the current TCP connection.
   * 
   * @returns A Promise resolving with a success flag
   */
  tcpDisconnect(): Promise<{ success: boolean, error?: string }>;

}
