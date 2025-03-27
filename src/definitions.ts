export interface NetUtilsPlugin {

  //  /**
  //  * Ping a host to measure latency.
  //  *
  //  * @param options.host - The target host to ping.
  //  * @param options.count - The number of echo requests to send (default: 1).
  //  * @param options.timeout - Timeout for the entire operation in milliseconds (default: 5000).
  //  * @returns A promise resolving with an object containing the average round-trip time (in ms) under the key "avgTime", or null if no replies were received.
  //  */
  // ping(options: { host: string; count?: number; timeout?: number }): Promise<{ avgTime: number | null; error?: string }>;

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
   * Run an SSH command on a remote host.
   *
   * @param options.host - The remote host (IP or domain).
   * @param options.port - The SSH port (default: 22).
   * @param options.user - The username for SSH authentication.
   * @param options.password - The password for SSH authentication.
   * @param options.command - The command to execute.
   * @returns A promise resolving with an object containing the "output" string from the command,
   *          or an "error" message if something goes wrong.
   */
  runSSHCommand(options: { 
    host: string; 
    port?: number; 
    user: string; 
    password: string; 
    command: string; 
  }): Promise<{ output: string; error?: string }>;
  
}
