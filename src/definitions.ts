export interface NetUtilsPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
