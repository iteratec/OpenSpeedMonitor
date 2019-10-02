import {ElementRef, Injectable} from "@angular/core";

import {TranslateService} from "@ngx-translate/core";
import {take} from "rxjs/operators";

import {
  mouse as d3Mouse,
  select as d3Select,
  selectAll as d3SelectAll,
  Selection as D3Selection,
  BaseType as D3BaseType,
  ContainerElement as D3ContainerElement
} from 'd3-selection';

import {bisector as d3Bisector, max as d3Max, min as d3Min} from 'd3-array';

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

/**
 * Generate line charts with ease and fun 😎
 */
@Injectable({
  providedIn: 'root'
})
export class LineChartService {

  private DOT_RADIUS = 3;
  private DOT_HIGHLIGHT_RADIUS = 5;

  // D3 margin conventions
  // > With this convention, all subsequent code can ignore margins.
  // see: https://bl.ocks.org/mbostock/3019563
  private _margin = { top: 40, right: 70, bottom: 40, left: 60 };
  private _width  = 600 - this._margin.left - this._margin.right;
  private _height = 500 - this._margin.top - this._margin.bottom;

  // Map that holds all points clustered by there x-axis values
  private _xAxisCluster: any = {};

  // Mouse events
  private _mouseEventCatcher: D3Selection<D3BaseType, {}, D3ContainerElement, {}>;
  private _markerTooltip: D3Selection<HTMLDivElement, {}, D3ContainerElement, {}>;


  constructor(private translationService: TranslateService) {}

  public initChart(svgElement: ElementRef): void {
    let data: TimeSeries[] = [new TimeSeries()];

    let chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}> = this.createChart(svgElement);

    let xScale: D3ScaleTime<number, number> = this.getXScale(data);
    let yScale: D3ScaleLinear<number, number> = this.getYScale(data);

    this.addXAxisToChart(chart, xScale);
    this.addYAxisToChart(chart, yScale);

    this.addMouseMarkerToChart(chart, xScale, data);
  }

  /**
   * Draws a line chart for the given data into the given svg
   */
  public drawLineChart(incomingData: EventResultDataDTO): void {

    let data: TimeSeries[] = this.prepareData(incomingData);

    if (data.length == 0) {
      return;
    }

    let chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}> = d3Select('g#time-series-chart-drawing-area');
    d3Select('osm-time-series-line-chart').transition().duration(500).style('visibility', 'visible');
    d3Select('svg#time-series-chart').transition().duration(500).attr('height', this._height + this._margin.top  + this._margin.bottom);

    let xScale: D3ScaleTime<number, number> = this.getXScale(data);
    let yScale: D3ScaleLinear<number, number> = this.getYScale(data);

    d3Select('.x-axis').transition().call(this.updateXAxis, xScale);
    d3Select('.y-axis').transition().call(this.updateYAxis, yScale, this._width, this._margin);

    this.addDataLinesToChart(chart, xScale, yScale, data);

    this.bringMouseMarkerToTheFront(xScale, yScale);
  }

  private bringMouseMarkerToTheFront(xScale: D3ScaleTime<number, number>, yScale: D3ScaleLinear<number, number>) {
    const markerGroup = d3Select('#marker-group').remove();
    d3Select('#time-series-chart-drawing-area').append(() => { return markerGroup.node(); });
    this._mouseEventCatcher.on('mousemove', (_, index, nodes: D3ContainerElement[]) => this.moveMarker(nodes[index], xScale, yScale, this._height));
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
        lineChartDataPoint.tooltipText = data.jobGroup + ' | ' + data.measuredEvent + ' : '; // TODO Set exact label text when IT-2793 is implemented
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
                  .attr('id', 'time-series-chart')
                  .attr('width',  this._width  + this._margin.left + this._margin.right)
                  .attr('height', 0);

    return svg.append('g') // g =  grouping element; group all other stuff into the chart
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
             //.curve(d3CurveMonotoneX);  // smooth the line

  }

  /**
   * Adds one line per data group to the chart
   */
  private addDataLinesToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                              xScale: D3ScaleTime<number, number>,
                              yScale: D3ScaleLinear<number, number>,
                              data: TimeSeries[]): void {
    // Create one group per line / data entry
    chart.selectAll('.line')                             // Get all lines already drawn
         .data(data, (timeSeries: TimeSeries) => timeSeries.key)   // ... for this data
         .join(
           enter => {
             this.addDataPointsToXAxisCluster(enter);
             const lineSelection: any = this.drawLine(enter, xScale, yScale);

             const lineGroup = lineSelection.select(function() { return (<SVGPathElement>this).parentNode; });
             this.addDataPointMarkersToChart(lineGroup, xScale, yScale);

             return lineSelection;
           },
           update => update,
           exit => {
             this.removeDataPointsFromXAxisCluster(exit);
             exit.transition().duration(200).style('opacity', '0').remove();
           }
         );

  }

  private addDataPointsToXAxisCluster(enter: D3Selection<D3BaseType, TimeSeries, D3BaseType, {}>) {
    enter.each((timeSeries: TimeSeries) => {
      timeSeries.values.forEach((timeSeriesPoint: TimeSeriesPoint) => {
        if(!this._xAxisCluster[timeSeriesPoint.date.getTime()]) this._xAxisCluster[timeSeriesPoint.date.getTime()] = [];
        this._xAxisCluster[timeSeriesPoint.date.getTime()].push(timeSeriesPoint);
      });
    });
  }

  private removeDataPointsFromXAxisCluster(exit: D3Selection<D3BaseType, TimeSeries, D3BaseType, {}>): void {
    exit.each((timeSeries: TimeSeries) => {
      timeSeries.values.forEach((timeSeriesPoint: TimeSeriesPoint) => {
        this._xAxisCluster[timeSeriesPoint.date.getTime()].splice(timeSeriesPoint, 1);
        if (this._xAxisCluster[timeSeriesPoint.date.getTime()].length == 0) {
          delete this._xAxisCluster[timeSeriesPoint.date.getTime()];
        }
      });
    });
  }

  private drawLine(selection: any,
                   xScale: D3ScaleTime<number, number>,
                   yScale: D3ScaleLinear<number, number>): D3Selection<D3BaseType, TimeSeries, D3BaseType, {}> {

    const resultingSelection = selection
      .append('g')       // Group each line so we can add dots to this group latter
      .attr('class', (timeSeries: TimeSeries) => 'line line-'+timeSeries.key)
      .style('opacity', '0')
        .append('path')  // Draw one path for every item in the data set
          .attr('fill', 'none')
          .attr('stroke-width', 1.5)
          .attr('d', (dataItem: TimeSeries) => {
            return this.getLineGenerator(xScale, yScale)(dataItem.values);
          });

    d3SelectAll('.line')
      // colorize (in reverse order as d3 adds new line before the existing ones ...
      .attr('stroke', (_, index: number, nodes: []) => { return getColorScheme()[nodes.length - index - 1]; })
      // fade in
      .transition().duration(500).style('opacity', '1');

    return resultingSelection;
  }

  private addDataPointMarkersToChart(lineGroups: D3Selection<any, TimeSeries, D3BaseType, {}>, xScale: D3ScaleTime<number, number>, yScale: D3ScaleLinear<number, number>) : void {
    lineGroups.each((timeSeries: TimeSeries, index: number, nodes: D3BaseType[]) => {
      const lineGroup = d3Select(nodes[index]);

      // TODO: Some dots are group into the wrong parent line group (and in turn do have the wrong color!) and I fucking don't know why!
      lineGroup
        .append('g')
        .selectAll('.dot-'+timeSeries.key)
        .data((timeSeries: TimeSeries) => timeSeries.values)
        .enter()
          .append('circle')
          .attr('class', (dot: TimeSeriesPoint) => 'dot dot-'+timeSeries.key+' dot-x-'+xScale(dot.date).toString().replace('.', '_'))
          .style('opacity', '0')
          .attr('r', this.DOT_RADIUS)
          .attr('cx', (dot: TimeSeriesPoint) => { return xScale(dot.date); })
          .attr('cy', (dot: TimeSeriesPoint) => { return yScale(dot.value); })
    });
  }

  private addMouseMarkerToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>, xScale: D3ScaleTime<number, number>, data: TimeSeries[]) : void {
    let markerGroup = chart.append('g').attr('id', 'marker-group');

    // Append the marker line, initialy hidden
    markerGroup.append('path')
      .attr('class', 'marker-line')
      .style('opacity', '0');

    // Watcher for mouse events
    this._mouseEventCatcher = markerGroup.append('svg:rect')
      .attr('width', this._width)
      .attr('height', this._height)
      .attr('fill', 'none')
      .attr('pointer-events', 'all');

    this._mouseEventCatcher.on('mouseover', this.showMarker);
    this._mouseEventCatcher.on('mouseout', this.hideMarker);

    this.addTooltipBoxToChart();
  }

  private addTooltipBoxToChart() {
    this._markerTooltip = d3Select('#time-series-chart').select(function() { return (<SVGElement>this).parentNode; }).append('div')
      .attr('id', 'marker-tooltip')
      .style('opacity', '0.9');
  }

  private showMarker() {
    d3Select('.marker-line').style('opacity', '1');
  }

  private hideMarker() {
    d3Select('.marker-line').style('opacity', '0');
    d3SelectAll('.dot').style('opacity', '0');
  }

  private moveMarker(node: D3ContainerElement, xScale: D3ScaleTime<number, number>, yScale: D3ScaleLinear<number, number>, containerHeight: number) {
    const mouseCoordinates = d3Mouse(node);
    const mouseX = mouseCoordinates[0];
    const mouseXDatum = xScale.invert(mouseX);

    const bisect = d3Bisector((timestamp: string) => timestamp).left;
    const clusterKeys = Object.keys(this._xAxisCluster);
    if (clusterKeys.length < 2) return;
    clusterKeys.sort();

    var clusterIndex = bisect(clusterKeys, mouseXDatum.getTime().toString());

    if (clusterIndex == 0) clusterIndex = 1;
    if (clusterIndex >= clusterKeys.length) clusterIndex = clusterKeys.length - 1;

    const firstPointFromClusterBefore: TimeSeriesPoint = this._xAxisCluster[clusterKeys[clusterIndex - 1]][0];
    const firstPointFromClusterAfter: TimeSeriesPoint = this._xAxisCluster[clusterKeys[clusterIndex]][0];
    const pointXBefore = xScale(firstPointFromClusterBefore.date);
    const pointXAfter = xScale(firstPointFromClusterAfter.date);

    var pointX: number;
    var point: TimeSeriesPoint;
    if (pointXAfter - mouseX < mouseX - pointXBefore) {
      pointX = pointXAfter;
      point = firstPointFromClusterAfter;
    } else {
      pointX = pointXBefore;
      point = firstPointFromClusterBefore;
    }
    d3Select('.marker-line')
      .attr('d', function() {
        let d = "M" + pointX + "," + containerHeight;
        d += " " + pointX + "," + 0;
        return d;
      });

    const visibleDots = this.findDotsOnMarkerAndShow(pointX, xScale);

    const mouseY = mouseCoordinates[1];
    const nearestDot = this.highlightNearestDot(visibleDots, mouseY, yScale);
    this.showTooltip(nearestDot, visibleDots, point.date);
  }

  private findDotsOnMarkerAndShow(pointX: number, xScale: D3ScaleTime<number, number>) {
    // Hide all dots before showing the current ones
    d3SelectAll('.dot').attr('r', this.DOT_RADIUS).style('opacity', '0');

    const cx = pointX.toString().replace('.', '_');
    return d3SelectAll('.dot-x-'+cx).style('opacity', '1');
  }

  private highlightNearestDot(visibleDots: D3Selection<D3BaseType, {}, HTMLElement, any>, mouseY: number, yScale: D3ScaleLinear<number, number>) {
    var nearestDot;
    var minDistance;
    visibleDots.each((_, index: number, nodes: []) => {
      const cy = parseFloat(d3Select(nodes[index]).attr('cy'));
      const distance = Math.abs(mouseY - cy);
      if (minDistance === undefined || distance < minDistance) {
        nearestDot = nodes[index];
        minDistance = distance;
      }
    });

    return d3Select(nearestDot).attr('r', this.DOT_HIGHLIGHT_RADIUS);
  }

  private showTooltip(nearestDot: D3Selection<any, unknown, null, undefined>, visibleDots: D3Selection<D3BaseType, {}, HTMLElement, any>, highlightedDate: Date) {
    // TODO: Styling of the tooltip needs some love (see frontend/src/app/modules/time-series/components/time-series-line-chart/time-series-line-chart.component.scss)
    const tooltip = d3Select('#marker-tooltip');
    const svg = d3Select('#time-series-chart');

    const tooltipText = this.generateTooltipText(nearestDot, visibleDots, highlightedDate);
    tooltip.html(tooltipText.outerHTML);

    // TODO: Positioning of the tooltip must be left or right of the box, depending on the space left
    const tooltipWidth: number = (<HTMLDivElement>tooltip.node()).getBoundingClientRect().width;
    const nearestDotXPosition: number = parseFloat(nearestDot.attr('cx'));

    const top = parseFloat(nearestDot.attr('cy')) + this._margin.top;
    const left = ( tooltipWidth + nearestDotXPosition > this._width ) ?
      (nearestDotXPosition - tooltipWidth + this._margin.right + 10) : nearestDotXPosition + this._margin.left + 50;
    tooltip.style('top', top+'px');
    tooltip.style('left', left+ 'px');

  }

  private generateTooltipText(nearestDot: D3Selection<any, unknown, null, undefined>, visibleDots: D3Selection<D3BaseType, {}, HTMLElement, any>, highlightedDate: Date): HTMLTableElement {
    const table: HTMLTableElement = document.createElement('table');
    const tableBody: HTMLTableSectionElement = document.createElement('tbody');
    table.append(tableBody);

    tableBody.append(this.generateTooltipTimestampRow(highlightedDate));

    visibleDots
      .sort((a: TimeSeriesPoint, b: TimeSeriesPoint) => {
        // console.log(a,b);
        if (a.value > b.value) return -1
        else if (a.value < b.value) return 1
        else return 0;
      })
      .each((timeSeriesPoint: TimeSeriesPoint, index: number, nodes: D3BaseType[]) => {
        // console.log(timeSeriesPoint, index, nodes[index]);
        tableBody.append(this.generateTooltipDatapointRow(timeSeriesPoint, nodes[index], nearestDot));
      });

    return table;
  }

  private generateTooltipTimestampRow(highlightedDate: Date): string | Node {
    const label: HTMLTableCellElement = document.createElement('td');
    label.append('Timestamp');

    const date: HTMLTableCellElement = document.createElement('td');
    date.append(highlightedDate.toLocaleString());

    const row: HTMLTableRowElement = document.createElement('tr');
    row.append(label);
    row.append(date);
    return row;
  }

  private generateTooltipDatapointRow(timeSeriesPoint: TimeSeriesPoint, node: D3BaseType, nearestDot: D3Selection<any, unknown, null, undefined>): string | Node {
    const nodeSelection = d3Select(node);

    const label: HTMLTableCellElement = document.createElement('td');
    label.append(timeSeriesPoint.tooltipText);

    const value: HTMLTableCellElement = document.createElement('td');
    const lineColorDot: HTMLElement = document.createElement('i');
    lineColorDot.className = 'fas fa-circle';
    lineColorDot.style.color = nodeSelection.style('stroke');
    value.append(lineColorDot);
    if (timeSeriesPoint.value !== undefined) {
      value.append(timeSeriesPoint.value.toString());
    }

    const row: HTMLTableRowElement = document.createElement('tr');
    if (nodeSelection.node() === nearestDot.node()) {
      row.className = 'active';
    }
    row.append(label);
    row.append(value);
    return row;
  }
}
