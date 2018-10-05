export const CSI_THRESHOLD_GOOD = 90;
export const CSI_THRESHOLD_OKAY = 70;
export const CSI_MAX = 100;
export const CSI_MIN = 0;

export class CsiUtils {

  static getClassByThresholds(csiValue: number): string {
    if (csiValue >= CSI_THRESHOLD_GOOD) {
      return 'good';
    }
    if (csiValue >= CSI_THRESHOLD_OKAY) {
      return 'okay';
    }
    return 'bad';
  }

  static isCsiOutdated(csiDate: string, jobDate: string): boolean {
    return csiDate < jobDate;
  }

  static isCsiNA(csiValue: Number) {
    return !csiValue && csiValue !== 0;
  }

  static formatAsText(csiValue: number, showLoading: boolean, digits: number = 1): string {
    if (showLoading) {
      return "loading...";
    }
    if (CsiUtils.isCsiNA(csiValue)) {
      return "n/a";
    }
    if (csiValue >= 100) {
      return "100%";
    }
    return csiValue.toFixed(digits) + "%";
  }
}
