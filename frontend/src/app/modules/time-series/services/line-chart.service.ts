import {ElementRef, Injectable} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";
import {take} from "rxjs/operators";

import {
  select as d3Select,
  Selection as D3Selection,
  BaseType as D3BaseType,
  ContainerElement as D3ContainerElement
} from 'd3-selection';

import {max, max as d3Max, min as d3Min} from 'd3-array';

import {
  timeFormat as d3TimeFormat
} from 'd3-time-format';

import {
  scaleTime as d3ScaleTime,
  scaleLinear as d3ScaleLinear,
  ScaleLinear as D3ScaleLinear,
  ScaleTime as D3ScaleTime
} from 'd3-scale';

import {
  axisBottom as d3AxisBottom,
  axisLeft as d3AxisLeft,
  axisRight as d3AxisRight
} from 'd3-axis';

import {
  line as d3Line,
  curveMonotoneX as d3CurveMonotoneX,
  Line as D3Line
} from 'd3-shape';

import 'd3-transition';

import {EventResultDataDTO} from 'src/app/modules/time-series/models/event-result-data.model';
import {EventResultSeriesDTO} from 'src/app/modules/time-series/models/event-result-series.model';
import {EventResultPointDTO} from 'src/app/modules/time-series/models/event-result-point.model';
import {TimeSeries} from 'src/app/modules/time-series/models/time-series.model';
import {TimeSeriesPoint} from 'src/app/modules/time-series/models/time-series-point.model';
import {parseDate} from 'src/app/utils/date.util';
import {getColorScheme} from 'src/app/enums/color-scheme.enum';
import {stringify} from "querystring";
import {ChartCommons} from "../../../enums/chart-commons.enum";
import {TimeSeriesLegend} from "../models/time-series-legend.model";

/**
 * Generate line charts with ease and fun 😎
 */
@Injectable({
  providedIn: 'root'
})
export class LineChartService {

  // D3 margin conventions
  // > With this convention, all subsequent code can ignore margins.
  // see: https://bl.ocks.org/mbostock/3019563

  private _margin = { top: 40, right: 70, bottom: 40, left: 60 };
  private _width  = 600 - this._margin.left - this._margin.right;
  private _height = 500 - this._margin.top - this._margin.bottom;

  private _chartContainerMargin =  { top: 40, right: 70, bottom: 40, left: 60 };
  private _chartContainerWidth = 700 - this._chartContainerMargin.left - this._chartContainerMargin.right;
  private _chartContainerHeight = 600 - this._chartContainerMargin.top - this._chartContainerMargin.bottom;

  private _legendGroupTop = this._margin.top + this._height + 50;
  private _legendGroupLeft = this._margin.left;
  private anyHighlighted:boolean = false;
  private anySelected:boolean = false;
  private clickedLabel:string = '';

  private legendDataMap = {};

  constructor(private translationService: TranslateService) {}

  public initChart(svgElement: ElementRef): void {
    let data: TimeSeries[] = [new TimeSeries()];

    let chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}> = this.createChart(svgElement);

    let xScale: D3ScaleTime<number, number> = this.getXScale(data);
    let yScale: D3ScaleLinear<number, number> = this.getYScale(data);

    this.addXAxisToChart(chart, xScale);
    this.addYAxisToChart(chart, yScale);

    console.log("this._chartContainerMargin.top: " + this._chartContainerMargin.top);
    console.log("this._chartContainerMargin.left: " + this._chartContainerMargin.left);
    console.log("this._chartContainerHeight: " + this._chartContainerHeight);
    console.log("drawing-area _margin.top: " + this._margin.top);
    console.log("drawing-area _margin.left: " + this._margin.left);
    console.log("drawing-area this.__height: " + this._height);
    console.log("_legendGroupTop: " + this._legendGroupTop);
    console.log("_legendGroupLeft: " + this._legendGroupLeft);

  }

  /**
   * Draws a line chart for the given data into the given svg
   */
  public drawLineChart(incomingData: EventResultDataDTO): void {

    let data: TimeSeries[] = this.prepareData(incomingData);
    /*const labels = incomingData.series.map(el =>  el.identifier);*/
    //console.log(incomingData); console.log(data);

    if (data.length == 0) {
      console.log("No data > No chart !");
      return;
    }

    let chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}> = d3Select('g#time-series-chart-drawing-area');
    d3Select('osm-time-series-line-chart').transition().duration(500).style('visibility', 'visible');
    d3Select('svg#time-series-svg').transition().duration(500).attr('height', this._chartContainerHeight);  //TODO fix containers height

    let xScale: D3ScaleTime<number, number> = this.getXScale(data);
    let yScale: D3ScaleLinear<number, number> = this.getYScale(data);

    d3Select('.x-axis').transition().call(this.updateXAxis, xScale);
    d3Select('.y-axis').transition().call(this.updateYAxis, yScale, this._width, this._margin);

    this.addDataLinesToChart(chart, xScale, yScale, data);
  }

  public drawLegends(incomingData: EventResultDataDTO): void {
    let chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}> = d3Select('g#time-series-chart-drawing-area');
    this.addIdentifierLegendsToChart(chart, incomingData)
  }

  public initLegendData(incomingData:EventResultDataDTO){
    let labelDataMap= {};
    let labels = incomingData.series.map(el =>  el.identifier);
    labels.map(label =>{
      labelDataMap[label] = {
        text: label,
        selected: false
      }
    });
    this.legendDataMap= labelDataMap;
  }



  /**
   * Prepares the incoming data for drawing with D3.js
   */
  private prepareData(incomingData: EventResultDataDTO): TimeSeries[] {

    return incomingData.series.map((data: EventResultSeriesDTO) => {
      let lineChartData: TimeSeries = new TimeSeries();
      lineChartData.key = this.generateKey(data);

      lineChartData.values = data.data.map((point: EventResultPointDTO) => {
        let lineChartDataPoint: TimeSeriesPoint = new TimeSeriesPoint();
        lineChartDataPoint.date = parseDate(point.date);
        lineChartDataPoint.value = point.value;
        lineChartDataPoint.tooltipText = '';
        return lineChartDataPoint;
      });

      return lineChartData;
    });
  }

  private generateKey(data: EventResultSeriesDTO): string {
    return data.jobGroup
         + data.measuredEvent
         + data.data.length;
  }

  /**
   * Setup the basic chart with an x- and y-axis
   */
  private createChart(svgElement: ElementRef): D3Selection<D3BaseType, {}, D3ContainerElement, {}> {

    this._width = svgElement.nativeElement.parentElement.offsetWidth - this._margin.left - this._margin.right;
    //this._height = svgElement.nativeElement.parentElement.offsetHeight - this._margin.top - this._margin.bottom;
    const svg = d3Select(svgElement.nativeElement)
                  .attr('id', 'time-series-svg')
                  .attr('width',  this._chartContainerWidth  + this._chartContainerMargin.left + this._chartContainerMargin.right)
                  .attr('height', 0);


    const contentGroup = svg.append('g')
      .attr('class', 'chart-content-group');

    return contentGroup.append('g') // g =  grouping element; group all other stuff into the chart
              .attr('id', 'time-series-chart-drawing-area')
              .attr('transform', 'translate(' + this._margin.left + ', ' + this._margin.top + ')'); // translates the origin to the top left corner (default behavior of D3)
  }

  public startResize(svgElement: ElementRef): void {
    d3Select(svgElement.nativeElement).transition().duration(100).style('opacity', 0.0);
  }

  public resizeChart(svgElement: ElementRef): void {
    this._width = svgElement.nativeElement.parentElement.offsetWidth - this._margin.left - this._margin.right;

    d3Select(svgElement.nativeElement)
      .attr('width',  this._width  + this._margin.left + this._margin.right)
  }

  public endResize(svgElement: ElementRef): void {
    d3Select(svgElement.nativeElement).transition().duration(50).style('opacity', 1.0);
  }

  /**
   * Determine the xScale for the given data
   */
  private getXScale(data: TimeSeries[]): D3ScaleTime<number, number> {
    return d3ScaleTime()               // Define a scale for the X-Axis
             .range([0, this._width])  // Display the X-Axis over the complete width
             .domain([this.getMinDate(data), this.getMaxDate(data)]);
  }

  private getMinDate(data: TimeSeries[]): Date {
    return d3Min(data, (dataItem: TimeSeries) => {
      return d3Min(dataItem.values, (point: TimeSeriesPoint) => {
        return point.date;
      });
    });
  }

  private getMaxDate(data: TimeSeries[]): Date {
    return d3Max(data, (dataItem: TimeSeries) => {
      return d3Max(dataItem.values, (point: TimeSeriesPoint) => {
        return point.date;
      });
    });
  }

  /**
   * Determine the yScale for the given data
   */
  private getYScale(data: TimeSeries[]): D3ScaleLinear<number, number> {
    return d3ScaleLinear()              // Linear scale for the numbers on the Y-Axis
             .range([this._height, 0])  // Display the Y-Axis over the complete height - origin is top left corner, so height comes first
             .domain([0, this.getMaxValue(data)])
             .nice();
  }

  private getMaxValue(data: TimeSeries[]): number {
    return d3Max(data, (dataItem: TimeSeries) => {
      return d3Max(dataItem.values, (point: TimeSeriesPoint) => {
        return point.value;
      });
    });
  }

  /**
   * Print the x-axis on the graph
   */
  private addXAxisToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                          xScale: D3ScaleTime<number, number>): void {

    const xAxis = d3AxisBottom(xScale);

    // Add the X-Axis to the chart
    chart.append('g')                   // new group for the X-Axis (see https://developer.mozilla.org/en-US/docs/Web/SVG/Element/g)
         .attr('class', 'axis x-axis')  // a css class to style it later
         .attr('transform', 'translate(0, ' + this._height + ')') // even if the D3 method called `axisBottom` we have to move it to the bottom by ourselfs
         .call(xAxis);
  }

  /**
   * Print the y-axis on the graph
   */
  private addYAxisToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                          yScale: D3ScaleLinear<number, number>): void {

    const yAxis = d3AxisLeft(yScale);

    // Add the Y-Axis to the chart
    chart.append('g')                   // new group for the y-axis
         .attr('class', 'axis y-axis')  // a css class to style it later
         .call(yAxis);

    // Add the axis description
    this.translationService.get("frontend.de.iteratec.osm.timeSeries.loadTimes").pipe(take(1)).subscribe(title => {
      d3Select('.y-axis').append('text')
                         .attr('class', 'description')
                         .attr('transform', 'translate(-' + (this._margin.left - 20) + ', ' + (this._height/2 - this._margin.bottom) +') rotate(-90)')
                         .text(title + ' [ms]');
    });
  }

  private updateXAxis(transition: any, xScale: D3ScaleTime<number, number>) {
    /**********  SOME HELPER FUNCTIONS  **********/
    const insertLinebreakToLabels = function () {
      d3Select(this).selectAll('g.tick text').each((_, index, nodes) => {
        let element = d3Select(nodes[index]);
        let lines = element.text().split(' _nl_ ');

        // Reset the text as we will replace it
        element.text('');

        lines.forEach((line, index) => {
          let tspan = element.append('tspan').text(line);
          if (index > 0)
              tspan.attr('x', 0).attr('dy', '15');
        });
      });
    }

    const timeFormat = function (ticks: Date[]) {
      let onlyDays = true;  // Should weekday names instead of hours and minutes should be shown
      let lastTick : Date = null;
      // Check if every tick step is at least one day.
      // If not set onlyDays to false
      ticks.forEach((tick: Date) => {
        if (lastTick) {
          if (tick.getUTCDate() == lastTick.getUTCDate()) {
            onlyDays = false;
          }
        }
        lastTick = tick;
      });

      return (onlyDays ? "%A" : "%H:%M") + " _nl_ %Y-%m-%d"
    }


    /**********  MAIN FUNCTION CONTEXT  **********/
    // Hide the ticks to avoid ugly transition
    transition.on('start.hideTicks', function hide() {
      d3Select(this).selectAll('g.tick text').attr('opacity', '0.0');
    });

    // Redraw the x-axis
    transition.call(
      d3AxisBottom(xScale)
        .tickFormat(d3TimeFormat(timeFormat(xScale.ticks())))
    );

    // Include line breaks
    transition.on('end.linebreak', insertLinebreakToLabels);

    // Show the ticks again as now all manipulation should have been happend
    transition.on('end.showTicks', function show() {
      d3Select(this).selectAll('g.tick text')
        .transition()
          .delay(100)
          .duration(500)
          .attr('opacity', '1.0')
    });
  }

  private updateYAxis(transition: any, yScale: any, width: number, margin: any) {
    transition.call(
      d3AxisRight(yScale)  // axis right, because we draw the background line with this
        .tickSize(width)   // background line over complete chart width
    )
    .attr('transform', 'translate(0, 0)') // move the axis to the left
    .call(g => g.selectAll(".tick:not(:first-of-type) line")  // make all line dotted, except the one on the bottom as this will indicate the x-axis
                .attr("stroke-opacity", 0.5)
                .attr("stroke-dasharray", "1,1"))
    .call(g => g.selectAll(".tick text")  // move the text a little so it does not overlap with the lines
                .attr("x", -5));
  }

  /**
   * Configuration of the line generator which does print the lines
   */
  private getLineGenerator(xScale: D3ScaleTime<number, number>,
                           yScale: D3ScaleLinear<number, number>): D3Line<TimeSeriesPoint> {

    return d3Line<TimeSeriesPoint>()          // Setup a line generator
      .x((point: TimeSeriesPoint) => {
        return xScale(point.date);
      })   // ... specify the data for the X-Coordinate
      .y((point: TimeSeriesPoint) => {
        return yScale(point.value);
      })  // ... and for the Y-Coordinate
             .curve(d3CurveMonotoneX);  // smooth the line

  }

  /**
   * Adds one line per data group to the chart
   */
  private addDataLinesToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                              xScale: D3ScaleTime<number, number>,
                              yScale: D3ScaleLinear<number, number>,
                              data: TimeSeries[]): void {
    chart.selectAll('.line').remove();
    // Create one group per line / data entry
    chart.selectAll('.line')                             // Get all lines already drawn
         .data(data, (datum: TimeSeries) => datum.key)   // ... for this data
         .join(
           enter => this.drawLine(enter, xScale, yScale)
         )
         .attr('class', (dataItem: TimeSeries) => {
           return 'line line-' + dataItem.key;
         })

     //this.addDataPointsToChart(chartLineGroups, xScale, yScale, data);
  }

  private drawLine(selection: any,
                   xScale: D3ScaleTime<number, number>,
                   yScale: D3ScaleLinear<number, number>
  ) {
    return selection
             .append('g')       // Group each line so we can add dots to this group latter
               .append('path')  // Draw one path for every item in the data set
                 .attr('fill', 'none')
                 .attr('stroke', (d, index: number) => { return getColorScheme()[index]; })
                 .attr('stroke-width', 1.5)
                 .attr('d', (dataItem: TimeSeries) => {
                   return this.getLineGenerator(xScale, yScale)(dataItem.values);
                 })
                 .on('mouseover', () => {
                   console.log("Mouse over line");
                   //this.highlightLine(this);
                 })
                 .on('mouseout', () => {
                   //normalizeColors();
                 });
  }

  private  addIdentifierLegendsToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                                       incomingData: EventResultDataDTO) {

    const contentGroup = d3Select(".time-series-svg");

    contentGroup.append('g') // g =  grouping element; group all other stuff into the chart
      .attr('class', 'chart-legend-group')
      .attr('transform', `translate(${this._legendGroupLeft}, ${this._legendGroupTop })`);


    const legendGroup = d3Select(".chart-content-group").selectAll('.chart-legend-group').data([1]);
    legendGroup.join(
      enter => enter
        .append('g')
        .attr('class', 'chart-legend-group')
        .style('cursor', 'pointer'),
      update => update,
      exit => exit.remove()
    )
      .attr('transform', 'translate(' + this._margin.left + ', ' + this._margin.top + ')');


    const legendEntry = d3Select('.chart-legend-group').selectAll('.legend-entry').data(Object.keys(this.legendDataMap));
    /*const maxEntryGroupSize = this.calculateMaxEntryGroupWidth(this.svgElement.nativeElement, keys);
    const maxEntriesInRow = Math.floor(this.svgWidth / maxEntryGroupSize);*/
    const maxEntriesInRow = this._width/10;
    legendEntry.join(
      enter => {
        const legendElement = enter
          .append('g')
          .attr('class', 'legend-entry')
          .style('opacity', (datum)=>{return ((this.anyHighlighted && !this.legendDataMap[datum].highlighted) || (this.anySelected && !this.legendDataMap[datum].selected)) ? 0.2 : 1});
        legendElement
          .append('rect')
          .attr('class', 'legend-rect')
          .attr('height', ChartCommons.COLOR_PREVIEW_SIZE)
          .attr('width', ChartCommons.COLOR_PREVIEW_SIZE)
          .attr("rx", 2)
          .attr("ry", 2)
          .attr("ry", 2)
          .attr('fill', (d, index: number) => { return getColorScheme()[index]; });
        legendElement
          .append('text')
          .attr('class', 'legend-text')
          .attr('x', ChartCommons.COLOR_PREVIEW_SIZE + ChartCommons.COLOR_PREVIEW_MARGIN)
          .attr('y', ChartCommons.COLOR_PREVIEW_SIZE)
          .text(datum => datum);
        return legendElement;
      },
      update => {
        update
          .transition()
          .duration(ChartCommons.TRANSITION_DURATION)
          .style('opacity', 0.9);
        return update;
        },
      exit => exit
        .transition()
        .duration(ChartCommons.TRANSITION_DURATION)
        .style('opacity', 0)
        .remove()
    )
      .attr('transform', (datum, index) => `translate(${300*(index % maxEntriesInRow)}, 0)`)
      .on('mouseover', (datum) => this.onMouseOver(datum, incomingData))
      .on('mouseout', (datum) => this.onMouseOut(datum, incomingData))
      /*.on('click', (datum) => this.onMouseClick(datum));*/

  }

  calculateMaxEntryGroupWidth(svgForEstimation: SVGElement, keys: string[]): number {
    /*let dataMap = keys;
    const labels = Object.keys(dataMap).map(measurand => dataMap[measurand].label);*/
    const labelWidths = this.getTextWidths(svgForEstimation, keys);
    return max(labelWidths) + 10 + 20 + 5;
  }

  getTextWidths(svgForEstimation: SVGElement, texts: string[]): number[] {
    const widths = [];
    d3Select(svgForEstimation).selectAll('.invisible-text-to-measure')
      .data(texts)
      .enter()
      .append("text")
      .attr("opacity", 0)
      .text((d) =>  d.toString())
      .each(function () {
        widths.push(this.getComputedTextLength());
        this.remove();
      });
    return widths;
  }

  private onMouseOver(label: string, incomingData:EventResultDataDTO): void {
    if(this.anySelected===false) {
      this.anyHighlighted = true;
      this.legendDataMap[label].highlighted = true;
      this.drawLineChart(incomingData);
      /*this.renderLegendGroup('.legend-content-group');*/
    }
  }

  private onMouseOut(label: string, incomingData:EventResultDataDTO): void {
    if(this.anyHighlighted === true) {
      this.anyHighlighted = false;
      this.legendDataMap[label].highlighted = false;
      this.drawLineChart(incomingData);
      /*this.renderLegendGroup('.legend-content-group');*/
    }
  }

  //private addDataPointsToChart(chartLineGroups: D3Selection<any, LineChartDataDTO, D3ContainerElement, {}>,
  //                             xScale: D3ScaleTime<number, number>,
  //                             yScale: D3ScaleLinear<number, number>,
  //                             data: LineChartDataDTO[]): void {

  //  let chartLineDotGroups = chartLineGroups.selectAll('.dots')
  //                 .data((data: LineChartDataDTO) => { return data.values; })  // Reduce the data to the data points per line
  //                 .join('g')
  //                   .attr('class', 'dot');

  //  chartLineDotGroups.append('circle')
  //                     .attr('fill', '#2E3440') // TODO Colors
  //                     .attr('stroke', '#5E81AC') // TODO Colors
  //                     .attr('stroke-width', 1.5)
  //                     .attr('cx', (point: LineChartDataPointDTO) => { return xScale(point.date); })
  //                     .attr('cy', (point: LineChartDataPointDTO) => { return yScale(point.value); })
  //                     .attr('r', 4)
  //                     .on('mouseover', (data, index) => {
  //                       // TODO: Add the class identifier to the element 'entry.id-index'
  //                       // console.log(d3.select(this))
  //                       //highlightLine(this)
  //                       // let element = d3.select(this.parentNode).select('.dot-desc');
  //                       // element.style('visibility', 'visible');
  //                     })
  //                     .on('mouseout', (data, index) => {
  //                       // let element = d3.select(this.parentNode).select('.dot-desc');
  //                       // element.style('visibility', 'hidden');
  //                     });
  //}


  //private highlightLine (element: TimeSeriesResults) {
  //    d3SelectAll('.line > path').attr('stroke', '#D8DEE9');
  //    d3Select(element).attr('stroke', (item) => { return colors(item.key); });

  //    // Dots
  //    d3SelectAll('.dots > circle').attr('stroke', '#D8DEE9');
  //    d3Select(element.parentNode).selectAll('circle').attr('stroke', (item) => { return colors(item.id); });
  //  };
  //
}
