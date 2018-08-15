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
    return new Date(date).toLocaleDateString("de-DE");
  }
}
