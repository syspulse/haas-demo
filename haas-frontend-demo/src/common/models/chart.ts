export interface IDoughnutChart<T> extends color {
  value: number;
  valueUnit: string;
  label: string;
  meta: T;
}

export interface color {
  colorRGB?: "string";
}
