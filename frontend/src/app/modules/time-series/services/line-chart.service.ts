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
  extent as d3Extent,
  min as d3Min,
  max as d3Max
} from 'd3-array';

import { timeDay as d3TimeDay } from 'd3-time';
import { timeFormat as d3TimeFormat } from 'd3-time-format';

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
  private _margin = { top: 40, right: 30, bottom: 20, left: 30 };
  private _width  = 600 - this._margin.left - this._margin.right;
  private _height = 600 - this._margin.top - this._margin.bottom;

  constructor() {}



  /**
   * Draws a line chart for the given data into the given svg
   */
  drawLineChart(svgElement: ElementRef, incomingData: TimeSeriesResultsDTO): void {
    console.log("drawLineChart(...)"); console.log(incomingData);

    let data: LineChartDataDTO[] = this.prepareData(incomingData);
    console.log(data);

    if (data.length == 0) {
      console.log("No data > No chart !");
      return;
    }


    let chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}> = this.createChart(svgElement);
    let xScale: D3ScaleTime<number, number> = this.getXScale(data);
    let yScale: D3ScaleLinear<number, number> = this.getYScale(data);

    this.addXAxisToChart(chart, xScale);
    this.addYAxisToChart(chart, yScale);

  //  let dataSelection: any = chart.selectAll('g.line').data(data);
  //  dataSelection.join(
  //    enter => this.enter(enter, chart, xScale),
  //    update => this.update(update),
  //    exit => this.exit(exit)
  //  );

    this.addDataLinesToChart(chart, xScale, yScale, data);
  }




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

  private __prepareData(incomingData: TimeSeriesResultsDTO): LineChartDataDTO[] {
    if (!incomingData) return [];

    // Group the data by date (x-axis data points)
    return Object.values<LineChartDataDTO>( // Remove the helper keys
      incomingData.series
        .reduce((result: {}, dataElement: TimeSeriesDataDTO) => {

        // Transform given data for each data point to the given date grouping.
        dataElement.data.reduce((pointResults: {}, dataPoint: TimeSeriesDataPointDTO) => {
          let indexKey = dataPoint.date.toString();
          if (result[indexKey] == undefined) {   // No group for this date yet, so create one.
            result[indexKey] = new LineChartData();
            result[indexKey].key = parseDate(dataPoint.date);
            result[indexKey].values = [];
          }

          result[indexKey].values.push(new LineChartDataPoint(parseDate(dataPoint.date), dataPoint.value, this.generateTooltipText(dataPoint)));
          return result;
        }, result);

        return result;
      }, {}) // Start with an empty helper object
    ).slice(0, 10);
  }

  private generateTooltipText(dataPoint: TimeSeriesDataPointDTO): string {
    return dataPoint.agent;
  }

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
             ]);
  }




  private enter(dataSelection: any, chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,xScale: D3ScaleTime<number, number>) {
    console.log("enter(dataSelection)");
    console.log(dataSelection);
  }

  private update(dataSelection: any) {
    console.log("update(dataSelection)");
    console.log(dataSelection);
  }

  private exit(dataSelection: any) {
    console.log("exit(dataSelection)");
    console.log(dataSelection);
  }

  private addXAxisToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                          xScale: D3ScaleTime<number, number>): void {

    const xAxis = d3AxisBottom(xScale);
         //           .tickFormat(d3TimeFormat('%Y-%m-%d'))  // Format the tick labels
         //           .ticks(d3TimeDay.every(1));            // Set one tick for every day in the set

    // Add the X-Axis to the chart
    chart.append('g')                   // new group for the X-Axis (see https://developer.mozilla.org/en-US/docs/Web/SVG/Element/g)
         .attr('class', 'axis x-axis')  // a css class to style it later
         .attr('transform', 'translate(0, ' + this._height + ')') // even if the D3 method called `axisBottom` we have to move it to the bottom by ourselfs
         .call(xAxis);
  }

  private addYAxisToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                          yScale: D3ScaleLinear<number, number>): void {

    const yAxis = d3AxisLeft(yScale);

    // Add the Y-Axis to the chart
    chart.append('g')                   // new group for the y-axis
         .attr('class', 'axis y-axis')  // a css class to style it later
         .call(yAxis);
  }

  private getLineGenerator(xScale: D3ScaleTime<number, number>,
                           yScale: D3ScaleLinear<number, number>): D3Line<LineChartDataPointDTO> {

    return d3Line<LineChartDataPointDTO>()          // Setup a line generator
             .x((point: LineChartDataPointDTO) => { return xScale(point.date); })   // ... specify the data for the X-Coordinate
             .y((point: LineChartDataPointDTO) => { return yScale(point.value); })  // ... and for the Y-Coordinate
             .curve(d3CurveMonotoneX);  // smooth the line

  }

  private addDataLinesToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                              xScale: D3ScaleTime<number, number>,
                              yScale: D3ScaleLinear<number, number>,
                              data: LineChartDataDTO[]): void {

    // Create one group per line / data entry
    let chartLineGroups = chart.selectAll('.line')  // Get all lines already drawn
                               .data(data)          // ... for this data
                               .join('g')           // Group the path so we can add dots later to this group
                                 .attr('class', (data: TimeSeriesDataDTO) => { return 'line line-'+data.identifier; })

    // Draw each line into the group
    chartLineGroups.append('path')  // Draw one path for every item in the data set
                     .attr('fill', 'none')
                     .attr('stroke', (dataItem: LineChartDataDTO) => { return "#5E81AC"; /*TODO colors(item.key);*/ })
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

  private addDataPointsToChart(chartLineGroups: D3Selection<any, LineChartDataDTO, D3ContainerElement, {}>,
                               xScale: D3ScaleTime<number, number>,
                               yScale: D3ScaleLinear<number, number>,
                               data: LineChartDataDTO[]): void {

    let chartLineDotGroups = chartLineGroups.selectAll('.dots')
                   .data((data: LineChartDataDTO) => { return data.values; })  // Reduce the data to the data points per line
                   .join('g')
                     .attr('class', 'dot');

    chartLineDotGroups.append('circle')
                       .attr('fill', '#2E3440') // TODO Colors
                       .attr('stroke', '#5E81AC') // TODO Colors
                       .attr('stroke-width', 1.5)
                       .attr('cx', (point: LineChartDataPointDTO) => { return xScale(point.date); })
                       .attr('cy', (point: LineChartDataPointDTO) => { return yScale(point.value); })
                       .attr('r', 4)
                       .on('mouseover', (data, index) => {
                         // TODO: Add the class identifier to the element 'entry.id-index'
                         // console.log(d3.select(this))
                         //highlightLine(this)
                         // let element = d3.select(this.parentNode).select('.dot-desc');
                         // element.style('visibility', 'visible');
                       })
                       .on('mouseout', (data, index) => {
                         // let element = d3.select(this.parentNode).select('.dot-desc');
                         // element.style('visibility', 'hidden');
                       });
  }


  //private highlightLine (element: TimeSeriesResults) {
  //    d3SelectAll('.line > path').attr('stroke', '#D8DEE9');
  //    d3Select(element).attr('stroke', (item) => { return colors(item.key); });

  //    // Dots
  //    d3SelectAll('.dots > circle').attr('stroke', '#D8DEE9');
  //    d3Select(element.parentNode).selectAll('circle').attr('stroke', (item) => { return colors(item.id); });
  //  };
  //

//  private todo(): void {
//    // https://github.com/d3/d3/blob/master/API.md
//
//    // https://observablehq.com/@d3/multi-line-chart
//    // https://www.d3-graph-gallery.com/graph/line_several_group.html
//    // https://blog.risingstack.com/d3-js-tutorial-bar-charts-with-javascript/#
//
//
//    let normalizeColors = () => {
//      d3SelectAll('.line > path').attr('stroke', (item) => { return colors(item.key); });
//      d3SelectAll('.dots > circle').attr('stroke', (item) => { return colors(item.id); });
//    };
//
//    // Reformat the data to be grouped be the name
//    // Will give something like
//    //   [
//    //     { key : 'Foo 1', values: [ {date: '...', id: 'Foo 1', value: '...'}, {...} ] },
//    //     { key : 'Foo 2', values: [ {date: '...', id: 'Foo 2', value: '...'}, {...} ] },
//    //   ]
//    //let data = d3Nest()
//    //             .key((item) => { return item.id; })
//    //             .entries(dataFlat);
//
//    //             console.log(data);
//
//
//
//
//
//
//    const names = data.map((item) =>  { return item.name });
//    const colors = d3ScaleOrdinal()
//                     .range(['#BF616A', '#D08770', '#EBCB8B', '#A3BE8C', '#B48EAD'])  // The color palatte Aurora from Nord Theme (https://www.nordtheme.com/docs/colors-and-palettes)
//                     .domain(names);  // Map the colors to the keys from out dataset
//
//
//
//
//    // =================================================================
//    //                            DOTS
//    // =================================================================
//
//    // Add the dots to the line groups
//    let chartLineDotDescGroups = chartLineDotGroups.append('g')
//                                               .attr('class', (entry, index) => { return 'dot-desc dot-desc-' + entry.id + '-' + index; })
//
//    chartLineDotDescGroups.append('text')
//                      .attr('text-anchor', 'middle')
//                      .attr('x', (entry) => { return xScale(entry.date); })
//                      .attr('y', (entry) => { return yScale(entry.value) - 13; })
//                      .text((entry) => { return entry.id; });
//
//    chartLineDotDescGroups.insert('rect', ':first-child')
//                       .attr('fill', '#2E3440')
//                       .attr('stroke', (entry) => { return colors(entry.id); })
//                       .attr('stroke-width', 1.5)
//                      .attr('width', (entry, index) => { return this.parentElement.getBBox().width + 5;})
//                      .attr('height', (entry, index) => { return this.parentElement.getBBox().height + 5;})
//                      .attr('x', (entry) => { return xScale(entry.date) - this.getBBox().width / 2; })
//                      .attr('y', (entry) => { return yScale(entry.value) - 8 - this.getBBox().height; })
//
//
//  }

}
