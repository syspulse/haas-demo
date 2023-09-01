export default function priceFormat(num: number, format?: string) {
  if (!num) {
    return 0;
  }

  !format && (format = "en-US");

  const currency = format === "en-US" ? "USD" : "";
  let maximumFractionDigits = 0;

  num < 99999 && (maximumFractionDigits = 2);
  num < 0.99 && (maximumFractionDigits = 4);
  num < 0.9999 && (maximumFractionDigits = 8);

  return num.toLocaleString(format, { minimumFractionDigits: 0, maximumFractionDigits }) + " " + currency;
}
