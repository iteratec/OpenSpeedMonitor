import {AfterContentInit, Component, ElementRef, Input, OnChanges, SimpleChanges, ViewChild} from '@angular/core';
import {ApplicationCsiListDTO} from "../../models/csi-list.model";
import {select} from "d3-selection";
import {axisBottom, axisLeft} from "d3-axis";
import {timeFormat} from "d3-time-format";
import {ScaleLinear, scaleLinear, ScaleTime, scaleTime} from "d3-scale";
import {area, Area, curveLinear, Line, line} from "d3-shape";
import {CsiDTO} from "../../models/csi.model";

@Component({
  selector: 'osm-csi-graph',
  templateUrl: './csi-graph.component.html',
  styleUrls: ['./csi-graph.component.scss']
})
export class CsiGraphComponent implements AfterContentInit, OnChanges {
  @Input() csiData: ApplicationCsiListDTO;
  @ViewChild("svg") svgElement: ElementRef;

  private lineGenerator: Line<CsiDTO>;
  private areaGenerator: Area<CsiDTO>;
  private yScale: ScaleLinear<number, number>;
  private xScale: ScaleTime<number, number>;

  private width: number = 500;
  private height: number = 100;
  private marginLeft: number = 30;
  private marginRight = 30;
  private marginTop: number = 10;
  private marginBottom = 30;

  constructor() {
  }

  private initGenerators() {
    if (!this.width || !this.height) {
      return;
    }

    this.xScale = this.getXScale(this.width);
    this.yScale = this.getYScale(this.height);
    this.lineGenerator = this.getLineGenerator(this.xScale, this.yScale);
    this.areaGenerator = this.getAreaGenerator(this.xScale, this.yScale);
  }

  private getXScale(width: number): ScaleTime<number, number> {
    const offset = (24 * 60 * 60 * 1000) * 7 * 4; //4 Weeks;
    let endDate: Date = this.dayStart(new Date(Date.now())); // + 24 * 60 * 60
    const startDate: Date = new Date(endDate.getTime() - offset);
    return scaleTime().domain([startDate, endDate]).range([0, width]);
  }

  private getYScale(height: number): ScaleLinear<number, number> {
    return scaleLinear().domain([0, 100]).range([height, 0]);
  }

  private dayStart(input: Date): Date {
    let date: Date = new Date(input.getTime());
    date.setUTCHours(0, 0, 0, 0)
    return date;
  }


  private getLineGenerator(xScale: ScaleTime<number, number>, yScale: ScaleLinear<number, number>): Line<CsiDTO> {
    return line<CsiDTO>()
      .curve(curveLinear)
      .x((csiDTO: CsiDTO) => xScale(this.dayStart(new Date(csiDTO.date))))
      .y((csiDTO: CsiDTO) => yScale(csiDTO.csiDocComplete))
  }

  private getAreaGenerator(xScale: ScaleTime<number, number>, yScale: ScaleLinear<number, number>): Area<CsiDTO> {
    return area<CsiDTO>()
      .x((csiDTO: CsiDTO) => xScale(this.dayStart(new Date(csiDTO.date))))
      .y1((csiDTO: CsiDTO) => yScale(csiDTO.csiDocComplete))
      .y0(yScale(0))
  }

  private drawGraph() {
    if (this.canDraw()) {
      const svgSelection = select(this.svgElement.nativeElement);
      let selection = svgSelection.selectAll("g.csi-graph").data<CsiDTO[]>([this.csiData.csiDtoList]);

      this.enter(selection.enter());
      this.update(selection.merge(selection.enter()));
      this.exit(selection.exit());
    }
  }

  private canDraw(): boolean {
    return !!this.xScale && !!this.yScale && !!this.areaGenerator && !!this.lineGenerator
  }


  private enter(selection: any) {
    const csiGraph = selection
      .append("g")
      .attr("transform", "translate(" + this.marginLeft + "," + this.marginTop + ")") //transform: translate(30px, 10px)
      .attr("class", "csi-graph");

    csiGraph
      .append("g")
      .attr("class", "x-axis")
      .attr("transform", `translate(0,${this.height})`)
      .call(axisBottom(this.xScale)
        .tickFormat(timeFormat("%Y-%m-%d")))
    // .selectAll("text")
    // .style("text-anchor", "end")
    // .attr("dx", "-.8em")
    // .attr("dy", ".15em")
    // .attr("transform", "rotate(-65)");

    csiGraph
      .append("g")
      .attr("class", "y-axis")
      .call(axisLeft(this.yScale));

    const csiGraphDrawingSpace = csiGraph
      .append("g");

    csiGraphDrawingSpace
      .append("path")
      .attr("class", "csi-graph-line")
      .attr("fill", "none")
      .attr("stroke", "currentColor")


    csiGraphDrawingSpace
      .append("path")
      .attr("class", "csi-graph-area")
      .attr("fill", "currentColor")
      .attr("opacity", 0.2);
  }

  private update(selection: any) {
    selection
      .select("g.x-axis")
      .call(axisBottom(this.xScale)
        .tickFormat(timeFormat("%Y-%m-%d")))

    selection
      .select("g.y-axis")
      .call(axisLeft(this.yScale));

    selection
      .select("path.csi-graph-line")
      .attr("d", this.lineGenerator);

    selection
      .select("path.csi-graph-area")
      .attr("d", this.areaGenerator);
  }

  private exit(selection: any) {
    selection.remove()
  }

  ngAfterContentInit(): void {
    this.redraw()
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.drawGraph();
  }

  onResize(event) {
    this.redraw();
  }

  private redraw() {
    this.width = this.svgElement.nativeElement.parentElement.offsetWidth - this.marginLeft - this.marginRight;
    this.height = this.svgElement.nativeElement.parentElement.offsetHeight - this.marginTop - this.marginBottom;
    this.initGenerators();
    this.drawGraph();
  }
}
