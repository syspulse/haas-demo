export default function parseBigInt<T>(i: number | undefined, dcml: number, format?: string): T {
  if (!i) {
    return 0 as T;
  }
  !dcml && (dcml = 18);

  if (format) {
    const value = i / Number(1 + new Array(dcml).fill(0).join(""));
    let maximumFractionDigits = 0;

    value < 100 && (maximumFractionDigits = 2);
    value < 0.01 && (maximumFractionDigits = 4);
    value < 0.001 && (maximumFractionDigits = 6);

    return value.toLocaleString(format, { minimumFractionDigits: 0, maximumFractionDigits }) as T;
  }

  return (i / 1e18) as T;
}
