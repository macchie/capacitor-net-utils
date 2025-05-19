import { registerPlugin } from '@capacitor/core';

import type { NetUtilsPlugin } from './definitions';

const NetUtilsCap = registerPlugin<NetUtilsPlugin>('NetUtils', {
  web: () => import('./web').then((m) => new m.NetUtilsWeb()),
});

// export * from './definitions';
//export { NetUtilsCap };

export const NetUtils = {
  checkPort: NetUtilsCap.checkPort,
  resolveHostname: NetUtilsCap.resolveHostname,
  getInterfaces: NetUtilsCap.getInterfaces,
  addListener: NetUtilsCap.addListener,
  removeAllListeners: NetUtilsCap.removeAllListeners,

  startForwarding: NetUtilsCap.startForwarding,
  stopForwarding: NetUtilsCap.stopForwarding,

  ssh: {
    execSync: NetUtilsCap.sshExecSync,
    connect: NetUtilsCap.sshConnect,
    startShell: NetUtilsCap.sshStartShell,
    write: NetUtilsCap.sshWrite,
    disconnect: NetUtilsCap.sshDisconnect,
  },
  
  tcp: {
    connect: NetUtilsCap.tcpConnect,
    write: NetUtilsCap.tcpWrite,
    disconnect: NetUtilsCap.tcpDisconnect,
  }
};
