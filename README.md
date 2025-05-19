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
addListener(eventName: 'ssh:stdout' | 'ssh:stderr' | 'tcp:message', listenerFunc: (event: { data: any; }) => void) => PluginListenerHandle
```

| Param              | Type                                                       |
| ------------------ | ---------------------------------------------------------- |
| **`eventName`**    | <code>'ssh:stdout' \| 'ssh:stderr' \| 'tcp:message'</code> |
| **`listenerFunc`** | <code>(event: { data: any; }) =&gt; void</code>            |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

--------------------


### checkPort(...)

```typescript
checkPort(options: { host: string; port: number; protocol: 'tcp' | 'udp'; timeout?: number; }) => Promise<{ open: boolean; error?: string; }>
```

Check if a network port is open on the given host using the specified protocol.

| Param         | Type                                                                                     |
| ------------- | ---------------------------------------------------------------------------------------- |
| **`options`** | <code>{ host: string; port: number; protocol: 'tcp' \| 'udp'; timeout?: number; }</code> |

**Returns:** <code>Promise&lt;{ open: boolean; error?: string; }&gt;</code>

--------------------


### resolveHostname(...)

```typescript
resolveHostname(options: { host: string; timeout?: number; }) => Promise<{ hostname: string | null; error?: string; }>
```

Resolve the hostname for a given IP address.

| Param         | Type                                             |
| ------------- | ------------------------------------------------ |
| **`options`** | <code>{ host: string; timeout?: number; }</code> |

**Returns:** <code>Promise&lt;{ hostname: string | null; error?: string; }&gt;</code>

--------------------


### startForwarding(...)

```typescript
startForwarding(options: { id?: string; localPort: number; targetHost: string; targetPort: number; protocol: 'tcp' | 'udp'; }) => Promise<{ success: boolean; id?: string; error?: string; }>
```

Start a port forwarding session from the local device to a remote host.

| Param         | Type                                                                                                               |
| ------------- | ------------------------------------------------------------------------------------------------------------------ |
| **`options`** | <code>{ id?: string; localPort: number; targetHost: string; targetPort: number; protocol: 'tcp' \| 'udp'; }</code> |

**Returns:** <code>Promise&lt;{ success: boolean; id?: string; error?: string; }&gt;</code>

--------------------


### stopForwarding(...)

```typescript
stopForwarding(options: { id: string; }) => Promise<{ success: boolean; id?: string; error?: string; }>
```

Stop a port forwarding session.

| Param         | Type                         |
| ------------- | ---------------------------- |
| **`options`** | <code>{ id: string; }</code> |

**Returns:** <code>Promise&lt;{ success: boolean; id?: string; error?: string; }&gt;</code>

--------------------


### sshExecSync(...)

```typescript
sshExecSync(options: { host: string; port?: number; user: string; password: string; command: string; }) => Promise<{ output: string; error?: string; }>
```

Run an SSH command on a remote host and get result.

| Param         | Type                                                                                           |
| ------------- | ---------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ host: string; port?: number; user: string; password: string; command: string; }</code> |

**Returns:** <code>Promise&lt;{ output: string; error?: string; }&gt;</code>

--------------------


### sshConnect(...)

```typescript
sshConnect(options: { host: string; port: number; username: string; password: string; }) => Promise<{ success: boolean; error?: string; }>
```

Connects to a remote SSH server using the given parameters.

| Param         | Type                                                                             | Description              |
| ------------- | -------------------------------------------------------------------------------- | ------------------------ |
| **`options`** | <code>{ host: string; port: number; username: string; password: string; }</code> | - The connection options |

**Returns:** <code>Promise&lt;{ success: boolean; error?: string; }&gt;</code>

--------------------


### sshWrite(...)

```typescript
sshWrite(options: { command: string; }) => Promise<{ success: boolean; error?: string; }>
```

Sends a string of data over the SSH connection.

| Param         | Type                              | Description        |
| ------------- | --------------------------------- | ------------------ |
| **`options`** | <code>{ command: string; }</code> | - The data to send |

**Returns:** <code>Promise&lt;{ success: boolean; error?: string; }&gt;</code>

--------------------


### sshStartShell()

```typescript
sshStartShell() => Promise<{ success: boolean; error?: string; }>
```

Starts a Shell over SSH connection.

**Returns:** <code>Promise&lt;{ success: boolean; error?: string; }&gt;</code>

--------------------


### sshDisconnect()

```typescript
sshDisconnect() => Promise<{ success: boolean; error?: string; }>
```

Closes the current SSH connection.

**Returns:** <code>Promise&lt;{ success: boolean; error?: string; }&gt;</code>

--------------------


### getInterfaces()

```typescript
getInterfaces() => Promise<{ output: { name: string; address: string; type: 'wifi' | 'vpn' | 'cellular' | 'other'; }[]; error?: string; }>
```

Get the IP addresses of the device's network interfaces.

**Returns:** <code>Promise&lt;{ output: { name: string; address: string; type: 'wifi' | 'vpn' | 'cellular' | 'other'; }[]; error?: string; }&gt;</code>

--------------------


### tcpConnect(...)

```typescript
tcpConnect(options: { host: string; port: number; }) => Promise<{ success: boolean; error?: string; }>
```

Connects to a remote TCP server using the given host and port.

| Param         | Type                                         | Description              |
| ------------- | -------------------------------------------- | ------------------------ |
| **`options`** | <code>{ host: string; port: number; }</code> | - The connection options |

**Returns:** <code>Promise&lt;{ success: boolean; error?: string; }&gt;</code>

--------------------


### tcpWrite(...)

```typescript
tcpWrite(options: { data: string; }) => Promise<{ success: boolean; error?: string; }>
```

Sends a string of data over the TCP connection.

| Param         | Type                           | Description        |
| ------------- | ------------------------------ | ------------------ |
| **`options`** | <code>{ data: string; }</code> | - The data to send |

**Returns:** <code>Promise&lt;{ success: boolean; error?: string; }&gt;</code>

--------------------


### tcpDisconnect()

```typescript
tcpDisconnect() => Promise<{ success: boolean; error?: string; }>
```

Closes the current TCP connection.

**Returns:** <code>Promise&lt;{ success: boolean; error?: string; }&gt;</code>

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

</docgen-api>
