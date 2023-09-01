import moment from "moment";

export function getDate(date: string | number, format?: string) {
  !format && (format = "DD MMM YYYY");
  
  return moment(date).utc(false).format(format);
}
