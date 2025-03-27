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

* [`checkPort(...)`](#checkport)
* [`resolveHostname(...)`](#resolvehostname)
* [`runSSHCommand(...)`](#runsshcommand)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

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


### runSSHCommand(...)

```typescript
runSSHCommand(options: { host: string; port?: number; user: string; password: string; command: string; }) => Promise<{ output: string; error?: string; }>
```

Run an SSH command on a remote host.

| Param         | Type                                                                                           |
| ------------- | ---------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ host: string; port?: number; user: string; password: string; command: string; }</code> |

**Returns:** <code>Promise&lt;{ output: string; error?: string; }&gt;</code>

--------------------

</docgen-api>
