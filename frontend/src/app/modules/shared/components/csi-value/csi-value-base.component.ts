import {Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewChild, ViewEncapsulation} from '@angular/core';

import {CsiUtils} from '../../../../utils/csi-utils';
import {TranslateService} from '@ngx-translate/core';
import {CsiValueSvgRenderer} from "./csi-value-svg.renderer";
import {CalculationUtil} from "../../../../utils/calculation.util";

@Component({
  selector: 'osm-base-csi-value',
  templateUrl: './csi-value-base.component.html',
  styleUrls: ['./csi-value-base.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class CsiValueBaseComponent implements OnInit, OnChanges {
  @Input() circleDiameter: number;
  @Input() withHint: boolean;
  @Input() csiValue: number;
  @Input() csiDate: string;
  @Input() lastResultDate: string;
  @Input() showLoading: boolean;

  csiValueClass: string;
  size: number;
  isNA: boolean;
  isOutdated: boolean;
  hint: string;
  padding = 5;

  @ViewChild("svg") svgElement: ElementRef;
  csiValueRenderer: CsiValueSvgRenderer;

  constructor(private translateService: TranslateService) {
  }


  ngOnInit(): void {
    this.initByInputs();
    this.updateComponent();
  }

  private updateComponent(previousCsiValue: number = 0) {
    this.isNA = CsiUtils.isCsiNA(this.csiValue);
    this.csiValue = this.isNA ? 0 : CalculationUtil.round(this.csiValue);
    this.isOutdated = CsiUtils.isCsiOutdated(this.csiDate, this.lastResultDate);
    this.csiValueClass = this.determineClass(this.csiValue);
    this.hint = this.getHint();
    this.drawCircle(previousCsiValue);
  }

  private drawCircle(previousCsiValue) {
    this.csiValueRenderer.drawCircle(this.csiValue, previousCsiValue);
  }

  private initByInputs() {
    this.size = this.circleDiameter + 2 * this.padding;
    this.csiValueRenderer = new CsiValueSvgRenderer(this.svgElement, this.circleDiameter, this.padding);
  }


  private determineClass(csiValue: number): string {
    if (this.isNA || this.isOutdated || this.showLoading) {
      return 'neutral';
    }

    return CsiUtils.getClassByThresholds(csiValue);
  }

  private getHint() {
    if (!this.withHint) {
      return '';
    }

    const dateParams = {date: CalculationUtil.toGermanDateFormat(this.csiDate)};
    if (this.isOutdated) {
      return this.translateService.instant('frontend.de.iteratec.osm.applicationDashboard.csi-older-than-results', dateParams);
    }
    if (CalculationUtil.isBeforeToday(this.csiDate)) {
      return this.translateService.instant('frontend.de.iteratec.osm.applicationDashboard.outdated-results', dateParams);
    }
    return '';
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.csiValueRenderer) {
      return;
    }
    if (changes.csiValue) {
      this.updateComponent(changes.csiValue.previousValue);
    }
  }
}

