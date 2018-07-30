import {Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
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
export class CsiGraphComponent implements OnInit, OnChanges {
  @Input() csiData: ApplicationCsiListDTO;
  @ViewChild("svg") svgElement: ElementRef;

  private lineGenerator: Line<CsiDTO>;
  private areaGenerator: Area<CsiDTO>;
  private yScale: ScaleLinear<number, number>;
  private xScale: ScaleTime<number, number>;

  constructor() {
    this.initGenerators();
  }

  private initGenerators() {
    // console.log(this.svgElement.nativeElement.width);
    this.xScale = this.getXScale(800);
    this.yScale = this.getYScale(100);
    this.lineGenerator = this.getLineGenerator(this.xScale, this.yScale);
    this.areaGenerator = this.getAreaGenerator(this.xScale, this.yScale);
  }

  private getXScale(width: number): ScaleTime<number, number> {
    const offset = (24 * 60 * 60 * 1000) * 7 * 4; //4 Weeks;
    let endDate: Date = new Date();
    let startDate: Date = new Date();
    startDate.setTime(startDate.getTime() - offset);

    return scaleTime().range([0, width]).domain([startDate, endDate]);
  }

  private getYScale(height: number): ScaleLinear<number, number> {
    return scaleLinear().domain([0, 100]).range([height, 0]);
  }


  private getLineGenerator(xScale: ScaleTime<number, number>, yScale: ScaleLinear<number, number>): Line<CsiDTO> {
    return line<CsiDTO>()
      .curve(curveLinear)
      .x((csiDTO: CsiDTO) => xScale(new Date(csiDTO.date)))
      .y((csiDTO: CsiDTO) => yScale(csiDTO.csiDocComplete))
  }

  private getAreaGenerator(xScale: ScaleTime<number, number>, yScale: ScaleLinear<number, number>): Area<CsiDTO> {
    return area<CsiDTO>()
      .x((csiDTO: CsiDTO) => xScale(new Date(csiDTO.date)))
      .y1((csiDTO: CsiDTO) => yScale(csiDTO.csiDocComplete))
      .y0(yScale(0))
  }

  ngOnInit(): void {

    this.drawGraph();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.drawGraph()
  }

  private drawGraph() {
    let selection = select(this.svgElement.nativeElement).selectAll("g.csi-graph").data<CsiDTO[]>([this.csiData.csiDtoList]);

    this.enter(selection.enter());
    this.update(selection.merge(selection.enter()));
    this.exit(selection.exit());
  }

  private enter(selection: any) {
    let height = 100;

    const csiGraph = selection
      .append("g")
      .attr("class", "csi-graph")


    // Add the X Axis
    csiGraph
      .append("g")
      .attr("class", "axis")
      .attr("transform", `translate(0,${height})`)
      .call(axisBottom(this.xScale)
        .tickFormat(timeFormat("%Y-%m-%d")))
    // .selectAll("text")
    // .style("text-anchor", "end")
    // .attr("dx", "-.8em")
    // .attr("dy", ".15em")
    // .attr("transform", "rotate(-65)");

    // Add the Y Axis
    csiGraph
      .append("g")
      .attr("class", "axis")
      .call(axisLeft(this.yScale));

    csiGraph
      .append("path")
      .attr("class", "csi-graph-line")
      .attr("fill", "none")
      .attr("stroke", "currentColor");

    csiGraph
      .append("path")
      .attr("class", "csi-graph-area")
      .attr("fill", "currentColor")
      .attr("opacity", 0.2);
  }

  private update(selection: any) {
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


}
