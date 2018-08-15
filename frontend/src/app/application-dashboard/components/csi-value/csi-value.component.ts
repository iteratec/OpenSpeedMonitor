import {Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {arc} from 'd3-shape';
import {select} from 'd3-selection';
import {transition} from 'd3-transition';
import {interpolate} from 'd3-interpolate';
import {CalculationUtil} from '../../../shared/utils/calculation.util';
import {CsiUtils} from '../../utils/csi-utils';

@Component({
  selector: 'osm-csi-value',
  templateUrl: './csi-value.component.html',
  styleUrls: ['./csi-value.component.scss']
})
export class CsiValueComponent implements OnInit, OnChanges {
  @Input() isBig: boolean;
  @Input() csiValue: number;
  @Input() csiDate: string;
  @Input() lastResultDate: string;

  description: string;
  formattedCsiValue: string;
  csiValueClass: string;
  size: number;
  outerRadius: number;
  valueFontSize: string;
  descriptionFontSize: string;
  isNA: boolean;
  isOutdated: boolean;

  arcGenerator: any;
  @ViewChild("svg") svgElement: ElementRef;

  constructor() {
    transition() // needed for the import statement
  }

  ngOnInit(): void {
    this.initByInputs();
    this.drawCircle();
  }

  private drawCircle(previousCsiValue: number = 0) {
    const calculatedPreviousCsi = this.calculateCsiArcTarget(CalculationUtil.round(previousCsiValue));
    this.isNA = !this.csiValue && this.csiValue !== 0;
    this.csiValue = this.isNA ? 0 : CalculationUtil.round(this.csiValue);
    this.isOutdated = CsiUtils.isCsiOutdated(this.csiDate, this.lastResultDate);
    this.formattedCsiValue = this.formatCsiValue(this.csiValue);
    this.csiValueClass = this.determineClass(this.csiValue);
    this.updateDescription();

    const selection = select(this.svgElement.nativeElement).selectAll("g.csi-circle").data([this.csiValue]);
    this.enter(selection.enter());
    this.update(selection.merge(selection.enter()), calculatedPreviousCsi);
    this.exit(selection.exit());
  }

  private formatCsiValue(csiValue: number): string {
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

    if (this.isBig) {
      this.size = 150;
      this.valueFontSize = '34';
      this.descriptionFontSize = '14';
    } else {
      this.size = 75;
      this.valueFontSize = '18';
      this.descriptionFontSize = '12';
    }

    this.outerRadius = this.size / 2;
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
      .attr("transform", `translate(${this.outerRadius},${this.outerRadius})`);

    circleGroup
      .append("path")
      .attr("class", "csi-circle-background")
      .attr("d", baseCircle)
      .attr("fill", "currentColor")
      .style("opacity", 0.1);
    circleGroup
      .append("path")
      .attr("class", "csi-circle-value")
      .attr("fill", "currentColor")
  }

  private update(selection: any, start: number) {
    selection
      .select("path.csi-circle-value")
      .transition()
      .duration(1000)
      .attrTween("d", this.tweenArc(this.calculateCsiArcTarget(this.csiValue), start))
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
    if (this.isNA) {
      return 'not-available';
    } else if (this.isOutdated) {
      return 'outdated';
    }

    return CsiUtils.getClassByThresholds(csiValue);
  }

  private updateDescription() {
    if (this.isBig && new Date().toISOString().substring(0, 10) > this.csiDate) {
      this.description = CalculationUtil.toGermanDateFormat(this.csiDate);
    } else if (!this.isBig) {
      this.description = 'CSI';
    } else {
      this.description = 'today';
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.arcGenerator) {
      return;
    }
    if (changes.csiValue) {
      this.drawCircle(changes.csiValue.previousValue);
    }
  }
}

