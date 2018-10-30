import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {CsiUtils} from "../../../../../utils/csi-utils";

@Component({
  selector: 'osm-csi-value-small',
  templateUrl: './csi-value-small.component.html',
  styleUrls: ['./csi-value-small.component.scss']
})
export class CsiValueSmallComponent implements OnInit, OnChanges {
  @Input() csiValue: number;
  @Input() csiDate: string;
  @Input() lastResultDate: string;
  @Input() showLoading: boolean;
  formattedCsiValue: string;

  constructor() {
  }

  ngOnInit() {
    this.formattedCsiValue = CsiUtils.formatAsText(this.csiValue, this.showLoading, true);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.formattedCsiValue = CsiUtils.formatAsText(this.csiValue, this.showLoading, true);
  }
}
