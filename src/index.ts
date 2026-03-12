import { registerPlugin } from '@capacitor/core';

import type { NetUtilsPlugin } from './definitions';

const NetUtilsCap = registerPlugin<NetUtilsPlugin>('NetUtils', {
  web: () => import('./web').then((m) => new m.NetUtilsWeb()),
});

export * from './definitions';

export const NetUtils = {
  checkUrl: (opts: Parameters<NetUtilsPlugin['checkUrl']>[0]) => NetUtilsCap.checkUrl(opts),
  checkPort: (opts: Parameters<NetUtilsPlugin['checkPort']>[0]) => NetUtilsCap.checkPort(opts),
  resolveHostname: (opts: Parameters<NetUtilsPlugin['resolveHostname']>[0]) => NetUtilsCap.resolveHostname(opts),
  getInterfaces: () => NetUtilsCap.getInterfaces(),
  addListener: NetUtilsCap.addListener.bind(NetUtilsCap),
  removeAllListeners: () => NetUtilsCap.removeAllListeners(),

  startForwarding: (opts: Parameters<NetUtilsPlugin['startForwarding']>[0]) => NetUtilsCap.startForwarding(opts),
  stopForwarding: (opts: Parameters<NetUtilsPlugin['stopForwarding']>[0]) => NetUtilsCap.stopForwarding(opts),

  ssh: {
    execSync: (opts: Parameters<NetUtilsPlugin['sshExecSync']>[0]) => NetUtilsCap.sshExecSync(opts),
    connect: (opts: Parameters<NetUtilsPlugin['sshConnect']>[0]) => NetUtilsCap.sshConnect(opts),
    startShell: () => NetUtilsCap.sshStartShell(),
    write: (opts: Parameters<NetUtilsPlugin['sshWrite']>[0]) => NetUtilsCap.sshWrite(opts),
    disconnect: () => NetUtilsCap.sshDisconnect(),
  },

  tcp: {
    connect: (opts: Parameters<NetUtilsPlugin['tcpConnect']>[0]) => NetUtilsCap.tcpConnect(opts),
    write: (opts: Parameters<NetUtilsPlugin['tcpWrite']>[0]) => NetUtilsCap.tcpWrite(opts),
    disconnect: () => NetUtilsCap.tcpDisconnect(),
  },
};
