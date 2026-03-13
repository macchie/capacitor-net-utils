# @macchie/capacitor-net-utils

A Capacitor Plugin for Network Utilities

## Install

_Not published yet_

```bash
npm install @macchie/capacitor-net-utils
npx cap sync
```

## API

<docgen-index>

* [`addListener('ssh:stdout' | 'ssh:stderr' | 'tcp:message', ...)`](#addlistenersshstdout--sshstderr--tcpmessage-)
* [`removeAllListeners()`](#removealllisteners)
* [`checkUrl(...)`](#checkurl)
* [`checkPort(...)`](#checkport)
* [`resolveHostname(...)`](#resolvehostname)
* [`startForwarding(...)`](#startforwarding)
* [`stopForwarding(...)`](#stopforwarding)
* [`sshExecSync(...)`](#sshexecsync)
* [`sshConnect(...)`](#sshconnect)
* [`sshWrite(...)`](#sshwrite)
* [`sshStartShell()`](#sshstartshell)
* [`sshDisconnect()`](#sshdisconnect)
* [`getInterfaces()`](#getinterfaces)
* [`tcpConnect(...)`](#tcpconnect)
* [`tcpWrite(...)`](#tcpwrite)
* [`tcpDisconnect()`](#tcpdisconnect)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### addListener('ssh:stdout' | 'ssh:stderr' | 'tcp:message', ...)

```typescript
addListener(eventName: 'ssh:stdout' | 'ssh:stderr' | 'tcp:message', listenerFunc: (event: { data: any; }) => void) => Promise<PluginListenerHandle>
```

Registers a listener for real-time events emitted by SSH or TCP sessions.

Available events:
- `'ssh:stdout'` — Fired when the SSH shell produces standard output.
- `'ssh:stderr'` — Fired when the SSH shell produces error output.
- `'tcp:message'` — Fired when the TCP connection receives data.

| Param              | Type                                                       | Description                                |
| ------------------ | ---------------------------------------------------------- | ------------------------------------------ |
| **`eventName`**    | <code>'ssh:stdout' \| 'ssh:stderr' \| 'tcp:message'</code> | - The event to listen for.                 |
| **`listenerFunc`** | <code>(event: { data: any; }) =&gt; void</code>            | - Callback invoked with the event payload. |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 1.0.0

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

Removes all registered listeners for this plugin.

Call this when you no longer need any event updates, for example
when the component or page is destroyed.

**Since:** 1.0.0

--------------------


### checkUrl(...)

```typescript
checkUrl(options: { url: string; timeout?: number; }) => Promise<{ exists: boolean; statusCode?: number; error?: string; }>
```

Check if a URL exists by performing an HTTP HEAD request.

| Param         | Type                                            | Description                      |
| ------------- | ----------------------------------------------- | -------------------------------- |
| **`options`** | <code>{ url: string; timeout?: number; }</code> | - The options for the URL check. |

**Returns:** <code>Promise&lt;{ exists: boolean; statusCode?: number; error?: string; }&gt;</code>

**Since:** 1.0.0

--------------------


### checkPort(...)

```typescript
checkPort(options: { host: string; port: number; protocol: 'tcp' | 'udp'; timeout?: number; }) => Promise<{ open: boolean; error?: string; }>
```

Check if a network port is open on the given host using the specified protocol.

| Param         | Type                                                                                     | Description                       |
| ------------- | ---------------------------------------------------------------------------------------- | --------------------------------- |
| **`options`** | <code>{ host: string; port: number; protocol: 'tcp' \| 'udp'; timeout?: number; }</code> | - The options for the port check. |

**Returns:** <code>Promise&lt;{ open: boolean; error?: string; }&gt;</code>

**Since:** 1.0.0

--------------------


### resolveHostname(...)

```typescript
resolveHostname(options: { host: string; timeout?: number; }) => Promise<{ hostname: string | null; error?: string; }>
```

Resolve the hostname for a given host address via reverse DNS lookup.

| Param         | Type                                             | Description                                |
| ------------- | ------------------------------------------------ | ------------------------------------------ |
| **`options`** | <code>{ host: string; timeout?: number; }</code> | - The options for the hostname resolution. |

**Returns:** <code>Promise&lt;{ hostname: string | null; error?: string; }&gt;</code>

**Since:** 1.0.0

--------------------


### startForwarding(...)

```typescript
startForwarding(options: { id?: string; localPort: number; targetHost: string; targetPort: number; protocol: 'tcp' | 'udp'; }) => Promise<{ success: boolean; id?: string; error?: string; }>
```

Start a port forwarding session from the local device to a remote host.

This creates a local listener that forwards all traffic to the specified
remote target. Useful for tunneling connections through the device.

| Param         | Type                                                                                                               | Description                        |
| ------------- | ------------------------------------------------------------------------------------------------------------------ | ---------------------------------- |
| **`options`** | <code>{ id?: string; localPort: number; targetHost: string; targetPort: number; protocol: 'tcp' \| 'udp'; }</code> | - The options for port forwarding. |

**Returns:** <code>Promise&lt;{ success: boolean; id?: string; error?: string; }&gt;</code>

**Since:** 1.0.0

--------------------


### stopForwarding(...)

```typescript
stopForwarding(options: { id: string; }) => Promise<{ success: boolean; id?: string; error?: string; }>
```

Stop an active port forwarding session.

| Param         | Type                         | Description                            |
| ------------- | ---------------------------- | -------------------------------------- |
| **`options`** | <code>{ id: string; }</code> | - The options to identify the session. |

**Returns:** <code>Promise&lt;{ success: boolean; id?: string; error?: string; }&gt;</code>

**Since:** 1.0.0

--------------------


### sshExecSync(...)

```typescript
sshExecSync(options: { host: string; port?: number; user: string; password: string; command: string; }) => Promise<{ output: string; error?: string; }>
```

Run a command on a remote host via SSH and wait for the result.

This is a one-shot execution — it connects, runs the command, and returns
the output. For interactive sessions, use `sshConnect` + `sshStartShell` instead.

| Param         | Type                                                                                           | Description                  |
| ------------- | ---------------------------------------------------------------------------------------------- | ---------------------------- |
| **`options`** | <code>{ host: string; port?: number; user: string; password: string; command: string; }</code> | - The SSH execution options. |

**Returns:** <code>Promise&lt;{ output: string; error?: string; }&gt;</code>

**Since:** 1.0.0

--------------------


### sshConnect(...)

```typescript
sshConnect(options: { host: string; port: number; username: string; password: string; }) => Promise<{ success: boolean; error?: string; }>
```

Open a persistent SSH connection to a remote server.

After connecting, you can start an interactive shell with `sshStartShell()`
and send commands with `sshWrite()`. Listen for output using `addListener('ssh:stdout', ...)`.

| Param         | Type                                                                             | Description               |
| ------------- | -------------------------------------------------------------------------------- | ------------------------- |
| **`options`** | <code>{ host: string; port: number; username: string; password: string; }</code> | - The connection options. |

**Returns:** <code>Promise&lt;{ success: boolean; error?: string; }&gt;</code>

**Since:** 1.0.0

--------------------


### sshWrite(...)

```typescript
sshWrite(options: { command: string; }) => Promise<{ success: boolean; error?: string; }>
```

Send a string command over the active SSH shell session.

Requires an active connection via `sshConnect()` and a running shell via `sshStartShell()`.
Include a trailing newline (`\n`) to execute the command.

| Param         | Type                              | Description          |
| ------------- | --------------------------------- | -------------------- |
| **`options`** | <code>{ command: string; }</code> | - The write options. |

**Returns:** <code>Promise&lt;{ success: boolean; error?: string; }&gt;</code>

**Since:** 1.0.0

--------------------


### sshStartShell()

```typescript
sshStartShell() => Promise<{ success: boolean; error?: string; }>
```

Start an interactive shell over the current SSH connection.

Must be called after `sshConnect()`. Once started, use `sshWrite()` to send
commands and `addListener('ssh:stdout', ...)` to receive output.

**Returns:** <code>Promise&lt;{ success: boolean; error?: string; }&gt;</code>

**Since:** 1.0.0

--------------------


### sshDisconnect()

```typescript
sshDisconnect() => Promise<{ success: boolean; error?: string; }>
```

Close the current SSH connection and release all resources.

**Returns:** <code>Promise&lt;{ success: boolean; error?: string; }&gt;</code>

**Since:** 1.0.0

--------------------


### getInterfaces()

```typescript
getInterfaces() => Promise<{ output: { name: string; address: string; type: 'wifi' | 'ethernet' | 'vpn' | 'cellular' | 'other'; }[]; error?: string; }>
```

Get the IP addresses of the device's network interfaces.

Returns all active network interfaces with their name, IP address,
and connection type.

**Returns:** <code>Promise&lt;{ output: { name: string; address: string; type: 'wifi' | 'ethernet' | 'vpn' | 'cellular' | 'other'; }[]; error?: string; }&gt;</code>

**Since:** 1.0.0

--------------------


### tcpConnect(...)

```typescript
tcpConnect(options: { host: string; port: number; }) => Promise<{ success: boolean; error?: string; }>
```

Open a persistent TCP connection to a remote server.

After connecting, use `tcpWrite()` to send data and
`addListener('tcp:message', ...)` to receive incoming data.

| Param         | Type                                         | Description               |
| ------------- | -------------------------------------------- | ------------------------- |
| **`options`** | <code>{ host: string; port: number; }</code> | - The connection options. |

**Returns:** <code>Promise&lt;{ success: boolean; error?: string; }&gt;</code>

**Since:** 1.0.0

--------------------


### tcpWrite(...)

```typescript
tcpWrite(options: { data: string; }) => Promise<{ success: boolean; error?: string; }>
```

Send a string of data over the active TCP connection.

Requires an active connection via `tcpConnect()`.

| Param         | Type                           | Description          |
| ------------- | ------------------------------ | -------------------- |
| **`options`** | <code>{ data: string; }</code> | - The write options. |

**Returns:** <code>Promise&lt;{ success: boolean; error?: string; }&gt;</code>

**Since:** 1.0.0

--------------------


### tcpDisconnect()

```typescript
tcpDisconnect() => Promise<{ success: boolean; error?: string; }>
```

Close the current TCP connection and release all resources.

**Returns:** <code>Promise&lt;{ success: boolean; error?: string; }&gt;</code>

**Since:** 1.0.0

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

</docgen-api>
