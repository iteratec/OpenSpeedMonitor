export class CalculationUtil {

  static round(input: number): number {
    const factor = Math.pow(10, 1);
    return Math.round(input * factor) / factor;
  }

  static convertBytesToMiB(bytes: number): number {
    return bytes / 1048576;
  }

  static convertMillisecsToSecs(milliSeconds: number): number {
    return milliSeconds / 1000;
  }

  static toRoundedStringWithFixedDecimals(value: number, decimals: number): string {
    return this.round(value).toFixed(decimals);
  }

  static toGermanDateFormat(date: string): string {
    let currentDate: Date = new Date(date);
    let monthString: string = currentDate.getMonth() < 9 ? '0' + (currentDate.getMonth() + 1) : (currentDate.getMonth() + 1).toString();
    let dayString: string = currentDate.getDate() < 10 ? '0' + currentDate.getDate() : currentDate.getDate().toString();

    return dayString + "." + monthString + "." + currentDate.getFullYear();
  }
}
