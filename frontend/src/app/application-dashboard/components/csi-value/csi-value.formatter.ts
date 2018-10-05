import {CsiUtils} from "../../utils/csi-utils";

export class CsiValueFormatter {

  constructor(private digits: number) {
  };

  formatAsText(csiValue: number, showLoading: boolean): string {
    if (showLoading) {
      return "loading...";
    }
    if (CsiUtils.isCsiNA(csiValue)) {
      return "n/a";
    }
    if (csiValue >= 100) {
      return "100%";
    }
    return csiValue.toFixed(this.digits) + "%";
  }
}
