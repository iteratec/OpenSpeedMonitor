import {
  AfterContentInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  SimpleChanges,
  ViewChild,
  ViewEncapsulation
} from '@angular/core';
import {ApplicationCsiListDTO} from "../../models/csi-list.model";
import {select} from "d3-selection";
import {axisBottom, axisLeft} from "d3-axis";
import {CsiDTO} from "../../models/csi.model";
import {timeDay} from "d3-time";
import {timeFormat} from "d3-time-format";
import {CsiGraphCalculator} from "./csi-graph.calculator";


@Component({
  selector: 'osm-csi-graph',
  templateUrl: './csi-graph.component.html',
  styleUrls: ['./csi-graph.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class CsiGraphComponent implements AfterContentInit, OnChanges {
  @Input() csiData: ApplicationCsiListDTO;
  @ViewChild("svg") svgElement: ElementRef;

  private csiGraphCalculator: CsiGraphCalculator;

  private width: number;
  private height: number;
  private marginLeft: number = 50;
  private marginRight = 20;
  private marginTop: number = 10;
  private marginBottom = 30;

  private bad: number = 60;
  private good: number = 85;
  private perfect: number = 100;

  constructor() {
  }

  private drawGraph() {
    if (this.csiGraphCalculator.isValid()) {
      const selection = select(this.svgElement.nativeElement).selectAll("g.csi-graph").data<CsiDTO[]>([this.csiData.csiDtoList]);

      this.enter(selection.enter());
      this.update(selection.merge(selection.enter()));
      this.exit(selection.exit());
    }
  }

  private enter(selection: any) {
    const csiGraph = selection
      .append("g")
      .attr("transform", `translate(${this.marginLeft},${this.marginTop + 1})`) //transform: translate(30px, 10px)
      .attr("class", "csi-graph");

    csiGraph
      .append("g")
      .attr("class", "x axis")
      .attr("transform", `translate(0,${this.height})`);

    csiGraph
      .append("g")
      .attr("class", "y axis");
    csiGraph
      .append("g")
      .attr("class", "y axis grid-lines");

    const csiGraphDrawingSpace = csiGraph
      .append("g");

    csiGraphDrawingSpace
      .append("rect")
      .attr("class", "graph-border")
      .attr("id", "graph-border");

    selection
      .append("defs")
      .append("clipPath")
      .attr("id", "graph-border-clip-path")
      .append("use")
      .attr("xlink:href", "#graph-border");

    csiGraphDrawingSpace
      .append("path")
      .attr("class", "csi-graph-line");

    csiGraphDrawingSpace
      .append("path")
      .attr("class", "csi-graph-area")
      .attr("clip-path", "url(#graph-border-clip-path)");
  }

  private update(selection: any) {
    selection
      .select("g.x.axis")
      .call(axisBottom(this.csiGraphCalculator.xScale)
        .ticks(timeDay.every(7))
        .tickFormat(timeFormat("%Y-%m-%d")));

    const yAxisGenerator = axisLeft(this.csiGraphCalculator.yScale)
      .tickValues([this.bad, this.good, this.perfect]);

    selection
      .select("g.y.axis")
      .call(yAxisGenerator
        .tickFormat((tick => tick + "%")));

    selection
      .select("g.y.axis.grid-lines")
      .call(yAxisGenerator
        .tickSize(-this.width)
        .tickFormat((tick => "")));

    selection
      .select("path.csi-graph-line")
      .attr("d", this.csiGraphCalculator.lineGenerator);

    selection
      .select("path.csi-graph-area")
      .attr("d", this.csiGraphCalculator.areaGenerator);

    selection
      .select("rect.graph-border")
      .attr("transform", `translate(0,-${this.marginTop})`)
      .attr("width", this.width)
      .attr("height", this.height + this.marginTop)
      .attr("rx", 4).attr("ry", 4);
  }

  private exit(selection: any) {
    selection.remove()
  }

  ngAfterContentInit(): void {
    this.redraw()
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.redraw();
  }

  onResize(event) {
    this.redraw();
  }

  private redraw() {
    this.width = this.svgElement.nativeElement.parentElement.offsetWidth - this.marginLeft - this.marginRight;
    this.height = this.svgElement.nativeElement.parentElement.offsetHeight - this.marginTop - this.marginBottom;
    this.csiGraphCalculator = new CsiGraphCalculator(this.width, this.height);
    this.drawGraph();
  }
}
