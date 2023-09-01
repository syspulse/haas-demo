export default function getPercentage(a: number, b: number, trunc?: number, absolute?: boolean) {
  const value = absolute ? 100 - (a / b) * 100 : (a / b) * 100;

  Math.abs(value) < 0.0001 && trunc && (trunc = 6);
  Math.abs(value) < 0.01 && trunc && (trunc = 4);

  return Number(value.toFixed(trunc || 0));
}

export function formatPercentage(num: number) {
  return Number(num).toFixed(2) + " %";
}
