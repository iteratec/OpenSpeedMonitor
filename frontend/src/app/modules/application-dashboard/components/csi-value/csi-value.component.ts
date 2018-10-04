import {Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewChild, ViewEncapsulation} from '@angular/core';
import {arc} from 'd3-shape';
import {select} from 'd3-selection';
import {transition} from 'd3-transition';
import {interpolate} from 'd3-interpolate';
import {CalculationUtil} from '../../../../utils/calculation.util';
import {CsiUtils} from '../../utils/csi-utils';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'osm-csi-value',
  templateUrl: './csi-value.component.html',
  styleUrls: ['./csi-value.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class CsiValueComponent implements OnInit, OnChanges {
  @Input() isBig: boolean;
  @Input() csiValue: number;
  @Input() csiDate: string;
  @Input() lastResultDate: string;
  @Input() showLoading: boolean;

  description: string;
  formattedCsiValue: string;
  csiValueClass: string;
  size: number;
  outerRadius: number;
  isNA: boolean;
  isOutdated: boolean;
  hint: string;
  padding = 5;

  arcGenerator: any;
  @ViewChild("svg") svgElement: ElementRef;

  constructor(private translateService: TranslateService) {
    transition() // needed for the import statement
  }

  ngOnInit(): void {
    this.initByInputs();
    this.updateComponent();
  }

  private updateComponent(previousCsiValue: number = 0) {
    this.isNA = !this.csiValue && this.csiValue !== 0;
    this.isOutdated = CsiUtils.isCsiOutdated(this.csiDate, this.lastResultDate);
    this.csiValue = this.isNA ? 0 : CalculationUtil.round(this.csiValue);
    this.formattedCsiValue = this.formatCsiValue(this.csiValue);
    this.csiValueClass = this.determineClass(this.csiValue);
    this.description = this.getDescription();
    this.hint = this.getHint();
    this.drawCircle(previousCsiValue);
  }

  private drawCircle(previousCsiValue) {
    const calculatedPreviousCsi = this.calculateCsiArcTarget(CalculationUtil.round(previousCsiValue));
    const selection = select(this.svgElement.nativeElement).selectAll("g.csi-circle").data([this.csiValue]);
    this.enter(selection.enter());
    this.update(selection.merge(selection.enter()), calculatedPreviousCsi);
    this.exit(selection.exit());
  }

  private formatCsiValue(csiValue: number): string {
    if (this.showLoading) {
      return "loading...";
    }
    if (this.isNA) {
      return "n/a";
    }
    if (csiValue >= 100) {
      return "100%";
    }
    return csiValue.toFixed(1) + "%";
  }

  private initByInputs() {
    if (!this.description) {
      this.description = "CSI";
    }
    const diameter = this.isBig ? 150 : 76;
    this.size = diameter + 2 * this.padding;
    this.outerRadius = diameter / 2;
    let innerRadius = this.outerRadius - this.outerRadius * 0.15;
    this.arcGenerator = this.getArcGenerator(innerRadius, this.outerRadius);
  }

  private getArcGenerator(innerRadius: number, outerRadius: number) {
    return arc()
      .innerRadius(innerRadius)
      .outerRadius(outerRadius)
      .startAngle(Math.PI)
      .endAngle((input: any) => {
        return input as number;
      })
  }

  private enter(selection: any) {
    const baseCircle = this.arcGenerator(3 * Math.PI);

    const circleGroup = selection
      .append("g")
      .attr("class", "csi-circle")
      .attr('transform', `translate(${(this.outerRadius + this.padding)},${(this.outerRadius + this.padding)})`);

    circleGroup
      .append("path")
      .attr("class", "csi-circle-background")
      .attr("d", baseCircle)
      .attr("fill", "currentColor")
      .style("opacity", 0.1);

    circleGroup
      .append("path")
      .attr("class", "csi-circle-foreground")
      .attr("fill", "currentColor");
  }

  private update(selection: any, start: number) {
    selection
      .select("path.csi-circle-foreground")
      .transition()
      .duration(1000)
      .attrTween("d", this.tweenArc(this.calculateCsiArcTarget(this.csiValue), start));
  }

  private exit(selection: any) {
    selection.remove()
  }

  private tweenArc(target: any, start: number) {
    return (d: any) => {
      const interpolator = interpolate(start, target);
      return (t) => {
        return this.arcGenerator(interpolator(t));
      }
    };
  }

  private calculateCsiArcTarget(csiValue: number) {
    return 2 * csiValue / 100 * Math.PI + Math.PI;
  }

  private determineClass(csiValue: number): string {
    if (this.isNA || this.isOutdated || this.showLoading) {
      return 'neutral';
    }

    return CsiUtils.getClassByThresholds(csiValue);
  }

  private isBeforeToday(isoDate: string) {
    return new Date().toISOString().substring(0, 10) > isoDate;
  }

  private getDescription() {
    if (this.isBig && this.isBeforeToday(this.csiDate)) {
      return CalculationUtil.toGermanDateFormat(this.csiDate);
    }
    if (!this.isBig) {
      return 'CSI';
    }
    return 'today';
  }

  private getHint() {
    const dateParams = {date: CalculationUtil.toGermanDateFormat(this.csiDate)};
    if (this.isOutdated) {
      return this.translateService.instant('frontend.de.iteratec.osm.applicationDashboard.csi-older-than-results', dateParams);
    }
    if (this.isBeforeToday(this.csiDate)) {
      return this.translateService.instant('frontend.de.iteratec.osm.applicationDashboard.outdated-results', dateParams);
    }
    return '';
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.arcGenerator) {
      return;
    }
    if (changes.csiValue) {
      this.updateComponent(changes.csiValue.previousValue);
    }
  }
}

