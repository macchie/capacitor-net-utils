import { PluginListenerHandle } from "@capacitor/core";

export interface NetUtilsPlugin {

  /**
   * Registers a listener for real-time events emitted by SSH or TCP sessions.
   *
   * Available events:
   * - `'ssh:stdout'` — Fired when the SSH shell produces standard output.
   * - `'ssh:stderr'` — Fired when the SSH shell produces error output.
   * - `'tcp:message'` — Fired when the TCP connection receives data.
   *
   * @since 1.0.0
   * @param eventName - The event to listen for.
   * @param listenerFunc - Callback invoked with the event payload.
   * @returns A promise resolving with a handle that can be used to remove the listener.
   *
   * @example
   * ```typescript
   * const handle = await NetUtils.addListener('ssh:stdout', (event) => {
   *   console.log('SSH output:', event.data);
   * });
   *
   * // Later, remove the listener
   * await handle.remove();
   * ```
   */
  addListener(
    eventName: 'ssh:stdout' | 'ssh:stderr' | 'tcp:message',
    listenerFunc: (event: { data: any }) => void
  ): Promise<PluginListenerHandle>;

  /**
   * Removes all registered listeners for this plugin.
   *
   * Call this when you no longer need any event updates, for example
   * when the component or page is destroyed.
   *
   * @since 1.0.0
   *
   * @example
   * ```typescript
   * await NetUtils.removeAllListeners();
   * ```
   */
  removeAllListeners(): Promise<void>;

  /**
   * Check if a URL exists by performing an HTTP HEAD request.
   *
   * @since 1.0.0
   * @param options - The options for the URL check.
   * @param options.url - The URL to check (must include the scheme, e.g. `"https://example.com"`).
   * @param options.timeout - Timeout in milliseconds (default: `5000`).
   * @returns A promise resolving with an object containing an `exists` boolean,
   *          the HTTP `statusCode`, and an optional `error` message.
   *
   * @example
   * ```typescript
   * const result = await NetUtils.checkUrl({
   *   url: 'https://example.com',
   *   timeout: 3000,
   * });
   * console.log(result.exists);     // true
   * console.log(result.statusCode); // 200
   * ```
   */
  checkUrl(options: { url: string; timeout?: number }): Promise<{ exists: boolean; statusCode?: number; error?: string }>;

  /**
   * Check if a network port is open on the given host using the specified protocol.
   *
   * @since 1.0.0
   * @param options - The options for the port check.
   * @param options.host - The target host (IP or domain).
   * @param options.port - The target port number.
   * @param options.protocol - The protocol to use (`"tcp"` or `"udp"`).
   * @param options.timeout - Timeout in milliseconds (default: `5000`).
   * @returns A promise resolving with an object containing an `open` boolean and an optional `error` message.
   *
   * @example
   * ```typescript
   * const result = await NetUtils.checkPort({
   *   host: '192.168.1.1',
   *   port: 22,
   *   protocol: 'tcp',
   *   timeout: 3000,
   * });
   * console.log(result.open); // true
   * ```
   */
  checkPort(options: { host: string; port: number; protocol: 'tcp' | 'udp'; timeout?: number }): Promise<{ open: boolean; error?: string }>;

  /**
   * Resolve the hostname for a given host address via reverse DNS lookup.
   *
   * @since 1.0.0
   * @param options - The options for the hostname resolution.
   * @param options.host - The IP address or domain to resolve.
   * @param options.timeout - Timeout in milliseconds.
   * @returns A promise resolving with an object containing the `hostname` (or `null` if not found) and an optional `error` message.
   *
   * @example
   * ```typescript
   * const result = await NetUtils.resolveHostname({
   *   host: '8.8.8.8',
   * });
   * console.log(result.hostname); // "dns.google"
   * ```
   */
  resolveHostname(options: { host: string; timeout?: number }): Promise<{ hostname: string | null; error?: string }>;

  /**
   * Start a port forwarding session from the local device to a remote host.
   *
   * This creates a local listener that forwards all traffic to the specified
   * remote target. Useful for tunneling connections through the device.
   *
   * @since 1.0.0
   * @param options - The options for port forwarding.
   * @param options.id - An optional identifier for the forwarding session (auto-generated if omitted).
   * @param options.localPort - The local port to listen on.
   * @param options.targetHost - The target host (IP or domain) to forward traffic to.
   * @param options.targetPort - The target port on the remote host.
   * @param options.protocol - The protocol to use (`"tcp"` or `"udp"`).
   * @returns A promise resolving with a `success` boolean, the session `id`, and an optional `error` message.
   *
   * @example
   * ```typescript
   * const result = await NetUtils.startForwarding({
   *   localPort: 8080,
   *   targetHost: '192.168.1.100',
   *   targetPort: 80,
   *   protocol: 'tcp',
   * });
   * console.log(result.id); // "abc-123"
   * ```
   */
  startForwarding(options: { id?: string; localPort: number; targetHost: string; targetPort: number; protocol: 'tcp' | 'udp'; }): Promise<{ success: boolean; id?: string; error?: string }>;

  /**
   * Stop an active port forwarding session.
   *
   * @since 1.0.0
   * @param options - The options to identify the session.
   * @param options.id - The identifier of the forwarding session to stop.
   * @returns A promise resolving with a `success` boolean and an optional `error` message.
   *
   * @example
   * ```typescript
   * await NetUtils.stopForwarding({ id: 'abc-123' });
   * ```
   */
  stopForwarding(options: { id: string; }): Promise<{ success: boolean; id?: string; error?: string }>;

  /**
   * Run a command on a remote host via SSH and wait for the result.
   *
   * This is a one-shot execution — it connects, runs the command, and returns
   * the output. For interactive sessions, use `sshConnect` + `sshStartShell` instead.
   *
   * @since 1.0.0
   * @param options - The SSH execution options.
   * @param options.host - The remote host (IP or domain).
   * @param options.port - The SSH port (default: `22`).
   * @param options.user - The username for SSH authentication.
   * @param options.password - The password for SSH authentication.
   * @param options.command - The command to execute on the remote host.
   * @returns A promise resolving with the command `output` string, or an `error` message.
   *
   * @example
   * ```typescript
   * const result = await NetUtils.sshExecSync({
   *   host: '192.168.1.10',
   *   port: 22,
   *   user: 'admin',
   *   password: 's3cret',
   *   command: 'uname -a',
   * });
   * console.log(result.output); // "Linux myhost 5.15.0 ..."
   * ```
   */
  sshExecSync(options: {
    host: string;
    port?: number;
    user: string;
    password: string;
    command: string;
  }): Promise<{ output: string; error?: string }>;

  /**
   * Open a persistent SSH connection to a remote server.
   *
   * After connecting, you can start an interactive shell with `sshStartShell()`
   * and send commands with `sshWrite()`. Listen for output using `addListener('ssh:stdout', ...)`.
   *
   * @since 1.0.0
   * @param options - The connection options.
   * @param options.host - The IP address or hostname of the server.
   * @param options.port - The SSH port to connect to.
   * @param options.username - The username for SSH authentication.
   * @param options.password - The password for SSH authentication.
   * @returns A promise resolving with a `success` flag and an optional `error` message.
   *
   * @example
   * ```typescript
   * await NetUtils.sshConnect({
   *   host: '192.168.1.10',
   *   port: 22,
   *   username: 'admin',
   *   password: 's3cret',
   * });
   *
   * await NetUtils.sshStartShell();
   *
   * NetUtils.addListener('ssh:stdout', (event) => {
   *   console.log(event.data);
   * });
   *
   * await NetUtils.sshWrite({ command: 'ls -la\n' });
   * ```
   */
  sshConnect(options: { host: string; port: number, username: string, password: string }): Promise<{ success: boolean, error?: string }>;

  /**
   * Send a string command over the active SSH shell session.
   *
   * Requires an active connection via `sshConnect()` and a running shell via `sshStartShell()`.
   * Include a trailing newline (`\n`) to execute the command.
   *
   * @since 1.0.0
   * @param options - The write options.
   * @param options.command - The string content to write to the SSH shell.
   * @returns A promise resolving with a `success` flag and an optional `error` message.
   *
   * @example
   * ```typescript
   * await NetUtils.sshWrite({ command: 'whoami\n' });
   * ```
   */
  sshWrite(options: { command: string }): Promise<{ success: boolean, error?: string }>;

  /**
   * Start an interactive shell over the current SSH connection.
   *
   * Must be called after `sshConnect()`. Once started, use `sshWrite()` to send
   * commands and `addListener('ssh:stdout', ...)` to receive output.
   *
   * @since 1.0.0
   * @returns A promise resolving with a `success` flag and an optional `error` message.
   *
   * @example
   * ```typescript
   * await NetUtils.sshConnect({ host: '192.168.1.10', port: 22, username: 'admin', password: 's3cret' });
   * await NetUtils.sshStartShell();
   * ```
   */
  sshStartShell(): Promise<{ success: boolean, error?: string }>;

  /**
   * Close the current SSH connection and release all resources.
   *
   * @since 1.0.0
   * @returns A promise resolving with a `success` flag and an optional `error` message.
   *
   * @example
   * ```typescript
   * await NetUtils.sshDisconnect();
   * ```
   */
  sshDisconnect(): Promise<{ success: boolean, error?: string }>;

  /**
   * Get the IP addresses of the device's network interfaces.
   *
   * Returns all active network interfaces with their name, IP address,
   * and connection type.
   *
   * @since 1.0.0
   * @returns A promise resolving with an array of network interfaces.
   *
   * @example
   * ```typescript
   * const result = await NetUtils.getInterfaces();
   * result.output.forEach((iface) => {
   *   console.log(`${iface.name}: ${iface.address} (${iface.type})`);
   * });
   * // "wlan0: 192.168.1.42 (wifi)"
   * // "tun0: 10.0.0.1 (vpn)"
   * ```
   */
  getInterfaces(): Promise<{
    output: {
      name: string;
      address: string;
      type: 'wifi' | 'ethernet' | 'vpn' | 'cellular' | 'other';
    }[];
    error?: string;
  }>;

  /**
   * Open a persistent TCP connection to a remote server.
   *
   * After connecting, use `tcpWrite()` to send data and
   * `addListener('tcp:message', ...)` to receive incoming data.
   *
   * @since 1.0.0
   * @param options - The connection options.
   * @param options.host - The IP address or hostname of the server.
   * @param options.port - The TCP port to connect to.
   * @returns A promise resolving with a `success` flag and an optional `error` message.
   *
   * @example
   * ```typescript
   * await NetUtils.tcpConnect({ host: '192.168.1.10', port: 9100 });
   *
   * NetUtils.addListener('tcp:message', (event) => {
   *   console.log('Received:', event.data);
   * });
   *
   * await NetUtils.tcpWrite({ data: 'Hello server\n' });
   * ```
   */
  tcpConnect(options: { host: string; port: number }): Promise<{ success: boolean, error?: string }>;

  /**
   * Send a string of data over the active TCP connection.
   *
   * Requires an active connection via `tcpConnect()`.
   *
   * @since 1.0.0
   * @param options - The write options.
   * @param options.data - The string content to write to the TCP socket.
   * @returns A promise resolving with a `success` flag and an optional `error` message.
   *
   * @example
   * ```typescript
   * await NetUtils.tcpWrite({ data: 'Hello server\n' });
   * ```
   */
  tcpWrite(options: { data: string }): Promise<{ success: boolean, error?: string }>;

  /**
   * Close the current TCP connection and release all resources.
   *
   * @since 1.0.0
   * @returns A promise resolving with a `success` flag and an optional `error` message.
   *
   * @example
   * ```typescript
   * await NetUtils.tcpDisconnect();
   * ```
   */
  tcpDisconnect(): Promise<{ success: boolean, error?: string }>;

}
