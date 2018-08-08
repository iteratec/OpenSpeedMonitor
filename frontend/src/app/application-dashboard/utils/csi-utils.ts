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
}
