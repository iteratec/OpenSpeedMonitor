import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {CsiUtils} from "../../../../../utils/csi-utils";

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
  formattedCsiValue: string;

  constructor() {
  }

  ngOnInit() {
    this.formattedCsiValue = CsiUtils.formatAsText(this.csiValue, this.showLoading);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.formattedCsiValue = CsiUtils.formatAsText(this.csiValue, this.showLoading);
  }
}
