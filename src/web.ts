import { WebPlugin } from '@capacitor/core';

import type { NetUtilsPlugin } from './definitions';

export class NetUtilsWeb extends WebPlugin implements NetUtilsPlugin {

  // async ping(_options: { host: string; count?: number; timeout?: number }): Promise<{ avgTime: number | null; error?: string }> {
  //   throw this.unimplemented('ping is not available in the web environment.');
  // }

  async checkPort(_options: { host: string; port: number; protocol: 'tcp' | 'udp'; timeout?: number }): Promise<{ open: boolean; error?: string }> {
    throw this.unimplemented('checkPort is not available in the web environment.');
  }

  async resolveHostname(_options: { host: string; timeout?: number }): Promise<{ hostname: string | null; error?: string }> {
    throw this.unimplemented('resolveHostname is not available in the web environment.');
  }

  async runSSHCommand(_options: { host: string; port?: number; user: string; password: string; command: string }): Promise<{ output: string; error?: string }> {
    throw this.unimplemented('runSshCommand is not available in the web environment.');
  }

}
