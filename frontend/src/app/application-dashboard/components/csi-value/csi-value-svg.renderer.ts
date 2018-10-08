import {ElementRef} from "@angular/core";
import {select} from "d3-selection";
import {CalculationUtil} from "../../../shared/utils/calculation.util";
import {arc} from "d3-shape";
import {interpolate} from "d3-interpolate";
import {transition} from 'd3-transition';

export class CsiValueSvgRenderer {
  svgElement: ElementRef;
  arcGenerator: any;
  csiValue: number;
  size: number;
  outerRadius: number;
  padding: number;

  constructor(svgElement: ElementRef, diameter: number, padding: number) {
    this.svgElement = svgElement;
    this.padding = padding;
    this.size = diameter + 2 * this.padding;
    this.outerRadius = diameter / 2;
    let innerRadius = this.outerRadius - this.outerRadius * 0.15;
    this.arcGenerator = this.getArcGenerator(innerRadius, this.outerRadius);
    transition(); //needed for import statement;
  }

  private getArcGenerator(innerRadius: number, outerRadius: number): any {
    return arc()
      .innerRadius(innerRadius)
      .outerRadius(outerRadius)
      .startAngle(Math.PI)
      .endAngle((input: any) => {
        return input as number;
      })
  }

  drawCircle(csiValue, previousCsiValue) {
    this.csiValue = csiValue;
    const calculatedPreviousCsi = this.calculateCsiArcTarget(CalculationUtil.round(previousCsiValue));
    const selection = select(this.svgElement.nativeElement).selectAll("g.csi-circle").data([this.csiValue]);
    this.enter(selection.enter());
    this.update(selection.merge(selection.enter()), calculatedPreviousCsi);
    this.exit(selection.exit());
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
}
