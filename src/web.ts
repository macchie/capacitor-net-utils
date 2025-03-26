import { WebPlugin } from '@capacitor/core';

import type { NetUtilsPlugin } from './definitions';

export class NetUtilsWeb extends WebPlugin implements NetUtilsPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
