import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {CsiValueFormatter} from "../csi-value.formatter";
import {CalculationUtil} from "../../../../shared/utils/calculation.util";

@Component({
  selector: 'osm-csi-value-big',
  templateUrl: './csi-value-big.component.html',
  styleUrls: ['./csi-value-big.component.scss']
})
export class CsiValueBigComponent implements OnInit, OnChanges {
  @Input() csiValue: number;
  @Input() csiDate: string;
  @Input() lastResultDate: string;
  @Input() showLoading: boolean;
  csiValueFormatter: CsiValueFormatter;
  description: string;
  formattedCsiValue: string;

  constructor() {
    this.csiValueFormatter = new CsiValueFormatter(1);
  }

  private getDescription(): string {
    if (CalculationUtil.isBeforeToday(this.csiDate)) {
      return CalculationUtil.toGermanDateFormat(this.csiDate);
    }
    return 'today';
  }

  setValues() {
    this.formattedCsiValue = this.csiValueFormatter.formatAsText(this.csiValue, this.showLoading);
    this.description = this.getDescription();
  }


  ngOnInit() {
    this.setValues();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.setValues();
  }

}
