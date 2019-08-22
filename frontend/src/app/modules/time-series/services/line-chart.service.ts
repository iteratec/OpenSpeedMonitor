import {
  ElementRef,
  Injectable
} from "@angular/core/";

import {
  select as d3Select,
  Selection as D3Selection,
  BaseType as D3BaseType,
  ContainerElement as D3ContainerElement
} from 'd3-selection';

import {
  min as d3Min,
  max as d3Max
} from 'd3-array';

import {
  scaleTime as d3ScaleTime,
  scaleLinear as d3ScaleLinear,
  ScaleLinear as D3ScaleLinear,
  ScaleTime as D3ScaleTime,
} from 'd3-scale';

import {
  axisBottom as d3AxisBottom,
  axisLeft as d3AxisLeft
} from 'd3-axis';

import { 
  line as d3Line,
  curveMonotoneX as d3CurveMonotoneX,
  Line as D3Line
} from 'd3-shape';

import {TimeSeriesResultsDTO} from 'src/app/modules/time-series/models/time-series-results.model';
import {TimeSeriesDataDTO} from 'src/app/modules/time-series/models/time-series-data.model';
import {TimeSeriesDataPointDTO} from 'src/app/modules/time-series/models/time-series-data-point.model';
import {LineChartData, LineChartDataDTO} from 'src/app/modules/time-series/models/line-chart-data.model';
import {LineChartDataPoint, LineChartDataPointDTO} from 'src/app/modules/time-series/models/line-chart-data-value.model';
import {parseDate} from 'src/app/utils/date.util';
import {getColorScheme} from 'src/app/enums/color-scheme.enum';

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
  // TODO Move into frontend/src/app/enums/chart-commons.enum.ts when available (see https://github.com/iteratec/OpenSpeedMonitor/pull/241/files)
  private _margin = { top: 40, right: 30, bottom: 20, left: 40 };
  private _width  = 600 - this._margin.left - this._margin.right;
  private _height = 600 - this._margin.top - this._margin.bottom;

  constructor() {}

  /**
   * Draws a line chart for the given data into the given svg
   */
  drawLineChart(svgElement: ElementRef, incomingData: TimeSeriesResultsDTO): void {

    let data: LineChartDataDTO[] = this.prepareData(incomingData);

    if (data.length == 0) {
      console.log("No data > No chart !");
      return;
    }

    let chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}> = this.createChart(svgElement);
    let xScale: D3ScaleTime<number, number> = this.getXScale(data);
    let yScale: D3ScaleLinear<number, number> = this.getYScale(data);

    this.addXAxisToChart(chart, xScale);
    this.addYAxisToChart(chart, yScale);

    this.addDataLinesToChart(chart, xScale, yScale, data);
  }


  /**
   * Prepares the incoming data for drawing with D3.js
   */
  private prepareData(incomingData: TimeSeriesResultsDTO): LineChartDataDTO[] {

    return incomingData.series.map((data: TimeSeriesDataDTO) => {
      let lineChartData: LineChartDataDTO = new LineChartData();
      lineChartData.key = data.identifier;

      lineChartData.values = data.data.map((point: TimeSeriesDataPointDTO) => {
        let lineChartDataPoint: LineChartDataPointDTO = new LineChartDataPoint();
        lineChartDataPoint.date = parseDate(point.date);
        lineChartDataPoint.value = point.value;
        lineChartDataPoint.tooltipText = '';
        return lineChartDataPoint;
      });

      return lineChartData;
    });
  }

  /**
   * Setup the basic chart with an x- and y-axis
   */
  private createChart(svgElement: ElementRef): D3Selection<D3BaseType, {}, D3ContainerElement, {}> {

    this._width = svgElement.nativeElement.parentElement.offsetWidth - this._margin.left - this._margin.right;
    //this._height = svgElement.nativeElement.parentElement.offsetHeight - this._margin.top - this._margin.bottom;
    const svg = d3Select(svgElement.nativeElement)
                  .attr('width',  this._width  + this._margin.left + this._margin.right)
                  .attr('height', this._height + this._margin.top  + this._margin.bottom);

    svg.select('g.time-series-chart').remove(); // TODO Avoid this in some way

    return svg.append('g') // g =  grouping element; group all other stuff into the chart
              .attr('class', 'time-series-chart')
              .attr('transform', 'translate(' + this._margin.left + ', ' + this._margin.top + ')'); // translates the origin to the top left corner (default behavior of D3)
  }

  /**
   * Determine the xScale for the given data
   */
  private getXScale(data: LineChartDataDTO[]): D3ScaleTime<number, number> {
    return d3ScaleTime()               // Define a scale for the X-Axis
             .range([0, this._width])  // Display the X-Axis over the complete width
             .domain([                  // Use the min and max dates as the first and last points
               d3Min(data, (dataItem: LineChartDataDTO) => { 
                 return d3Min(dataItem.values, (point: LineChartDataPointDTO) => { return point.date; });
               }),
               d3Max(data, (dataItem: LineChartDataDTO) => { 
                 return d3Max(dataItem.values, (point: LineChartDataPointDTO) => { return point.date; });
               })
             ]);
  }

  /**
   * Determine the yScale for the given data
   */
  private getYScale(data: LineChartDataDTO[]): D3ScaleLinear<number, number> {
    return d3ScaleLinear()              // Linear scale for the numbers on the Y-Axis
             .range([this._height, 0])  // Display the Y-Axis over the complete height - origin is top left corner, so height comes first
             .domain([                  // min and max values from the data given
               d3Min(data, (dataItem: LineChartDataDTO) => { 
                 return d3Min(dataItem.values, (point: LineChartDataPointDTO) => { return point.value; });
               }),
               d3Max(data, (dataItem: LineChartDataDTO) => { 
                 return d3Max(dataItem.values, (point: LineChartDataPointDTO) => { return point.value; });
               })
             ])
             .nice();
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
  }

  /**
   * Configuration of the line generator which does print the lines
   */
  private getLineGenerator(xScale: D3ScaleTime<number, number>,
                           yScale: D3ScaleLinear<number, number>): D3Line<LineChartDataPointDTO> {

    return d3Line<LineChartDataPointDTO>()          // Setup a line generator
             .x((point: LineChartDataPointDTO) => { return xScale(point.date); })   // ... specify the data for the X-Coordinate
             .y((point: LineChartDataPointDTO) => { return yScale(point.value); })  // ... and for the Y-Coordinate
             .curve(d3CurveMonotoneX);  // smooth the line

  }

  /**
   * Adds one line per data group to the chart
   */
  private addDataLinesToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                              xScale: D3ScaleTime<number, number>,
                              yScale: D3ScaleLinear<number, number>,
                              data: LineChartDataDTO[]): void {

    // Create one group per line / data entry
    let chartLineGroups = chart.selectAll('.line')  // Get all lines already drawn
                               .data(data)          // ... for this data
                               .join('g')           // Group the path so we can add dots later to this group
                                 .attr('class', (dataItem: LineChartDataDTO) => { return 'line line-'+dataItem.key; })

    // Draw each line into the group
    chartLineGroups.append('path')  // Draw one path for every item in the data set
                     .attr('fill', 'none')
                     .attr('stroke', (d, index: number) => { return getColorScheme()[index]; })
                     .attr('stroke-width', 1.5)
                     .attr('d', (dataItem: LineChartDataDTO) => {     // Apply a function to every item (data grouped by the key 'name') from the data
                       return this.getLineGenerator(xScale, yScale)(dataItem.values);  // Draw the line for every value (point) of this item
                     })
                     .on('mouseover', () => {
                       console.log("Mouse over line");
                       //this.highlightLine(this);
                     })
                     .on('mouseout', () => {
                       //normalizeColors();
                     });

     //this.addDataPointsToChart(chartLineGroups, xScale, yScale, data);
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
