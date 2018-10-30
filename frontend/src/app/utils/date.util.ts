
export function parseDate(datelike: string | Date): Date {
  if (datelike instanceof Date) {
    return new Date(datelike.getTime());
  } else {
    return new Date(datelike);
  }

}
