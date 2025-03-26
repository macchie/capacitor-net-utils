import { registerPlugin } from '@capacitor/core';

import type { NetUtilsPlugin } from './definitions';

const NetUtils = registerPlugin<NetUtilsPlugin>('NetUtils', {
  web: () => import('./web').then((m) => new m.NetUtilsWeb()),
});

export * from './definitions';
export { NetUtils };
