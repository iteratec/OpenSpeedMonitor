import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {CsiValueFormatter} from "../csi-value.formatter";

@Component({
  selector: 'osm-csi-value-medium',
  templateUrl: './csi-value-medium.component.html',
  styleUrls: ['./csi-value-medium.component.scss']
})
export class CsiValueMediumComponent implements OnInit, OnChanges {
  @Input() csiValue: number;
  @Input() csiDate: string;
  @Input() lastResultDate: string;
  @Input() showLoading: boolean;
  csiValueFormatter: CsiValueFormatter;
  formattedCsiValue: string;

  constructor() {
    this.csiValueFormatter = new CsiValueFormatter(1);
  }

  ngOnInit() {
    this.formattedCsiValue = this.csiValueFormatter.formatAsText(this.csiValue, this.showLoading);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.formattedCsiValue = this.csiValueFormatter.formatAsText(this.csiValue, this.showLoading);
  }
}
