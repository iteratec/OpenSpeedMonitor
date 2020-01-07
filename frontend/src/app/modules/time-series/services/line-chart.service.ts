import {ElementRef, Injectable} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";
import {take} from "rxjs/operators";

import {
  mouse as d3Mouse,
  select as d3Select,
  selectAll as d3SelectAll,
  event as d3Event,
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

import {BrushBehavior, brushX as d3BrushX} from 'd3-brush';
import {EventResultDataDTO} from 'src/app/modules/time-series/models/event-result-data.model';
import {EventResultSeriesDTO} from 'src/app/modules/time-series/models/event-result-series.model';
import {EventResultPointDTO} from 'src/app/modules/time-series/models/event-result-point.model';
import {TimeSeries} from 'src/app/modules/time-series/models/time-series.model';
import {TimeSeriesPoint} from 'src/app/modules/time-series/models/time-series-point.model';
import {parseDate} from 'src/app/utils/date.util';
import {getColorScheme} from 'src/app/enums/color-scheme.enum';
import {ChartCommons} from "../../../enums/chart-commons.enum";
import {UrlBuilderService} from "./url-builder.service";
import {PointsSelection} from "../models/points-selection.model";

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
  private _margin: any = {top: 40, right: 70, bottom: 40, left: 60};
  private _width: number = 600 - this._margin.left - this._margin.right;
  private _height: number = 550 - this._margin.top - this._margin.bottom;
  private _labelGroupHeight: number;
  private _legendGroupTop: number = this._margin.top + this._height + 50;
  private _legendGroupLeft: number = this._margin.left;
  private _legendGroupHeight;
  private _legendGroupColumnWidth;
  private _legendGroupColumns;
  private legendDataMap: Object = {};
  private brush: BrushBehavior<{}>;
  private focusedLegendEntry: string;

  // Map that holds all points clustered by their x-axis values
  private _xAxisCluster: any = {};

  // Mouse events
  private _mouseEventCatcher: D3Selection<D3BaseType, {}, D3ContainerElement, {}>;
  private _markerTooltip: D3Selection<HTMLDivElement, {}, D3ContainerElement, {}>;
  private _contextMenuBackground: D3Selection<D3BaseType, number, D3BaseType, unknown>;
  private _contextMenu: D3Selection<D3BaseType, number, D3BaseType, unknown>;

  private _dotsOnMarker: D3Selection<D3BaseType, {}, HTMLElement, any>;
  private _pointsSelection: PointsSelection;
  private _contextMenuPoint: D3Selection<D3BaseType, TimeSeriesPoint, D3BaseType, any>;

  private contextMenu: ContextMenuPosition[] = [
    {
      title: 'summary',
      icon: "fas fa-file-alt",
      action: (elm, d: TimeSeriesPoint, i) => {
        window.open(this.urlBuilderService
          .buildSummaryUrl(d.wptInfo));
      }
    },
    {
      title: 'waterfall',
      icon: "fas fa-bars",
      action: (elm, d, i) => {
        window.open(this.urlBuilderService
          .buildUrlByOption(d.wptInfo, this.urlBuilderService.options.waterfall));
      }
    },
    {
      title: 'performanceReview',
      icon: "fas fa-check",
      action: (elm, d, i) => {
        window.open(this.urlBuilderService
          .buildUrlByOption(d.wptInfo, this.urlBuilderService.options.performanceReview));
      }
    },
    {
      title: 'contentBreakdown',
      icon: "fas fa-chart-pie",
      action: (elm, d, i) => {
        window.open(this.urlBuilderService
          .buildUrlByOption(d.wptInfo, this.urlBuilderService.options.contentBreakdown));
      }
    },
    {
      title: 'domains',
      icon: "fas fa-list",
      action: (elm, d, i) => {
        window.open(this.urlBuilderService
          .buildUrlByOption(d.wptInfo, this.urlBuilderService.options.domains));
      }
    },
    {
      title: 'screenshot',
      icon: "fas fa-image",
      action: (elm, d, i) => {
        window.open(this.urlBuilderService
          .buildUrlByOption(d.wptInfo, this.urlBuilderService.options.screenshot));
      }
    },
    {
      title: 'filmstrip',
      icon: "fas fa-film",
      action: (elm, d, i) => {
        window.open(this.urlBuilderService
          .buildFilmstripUrl(d.wptInfo));
      }
    },
    {
      title: 'filmstripTool',
      icon: "fas fa-money-check",
      action: (elm, d, i) => {
        window.open(this.urlBuilderService
          .buildFilmstripToolUrl(d.wptInfo));
      }
    },
    {
      title: 'compareFilmstrips',
      icon: "fas fa-columns",
      visible: () => {
        return this._pointsSelection.count() > 0;
      },
      action: (elm, d, i) => {
        console.log("Item 'Compare Filmstrips' clicked!");
      }
    },
    {
      divider: true
    },
    {
      title: 'selectPoint',
      icon: "fas fa-dot-circle",
      visible: (elm, d: TimeSeriesPoint, i) => {
        return !this._pointsSelection.isPointSelected(d);
      },
      action: (elm, d: TimeSeriesPoint, i) => {
        this.changePointSelection(d);
      }
    },
    {
      title: 'deselectPoint',
      icon: "fas fa-trash-alt",
      visible: (elm, d: TimeSeriesPoint, i) => {
        return this._pointsSelection.isPointSelected(d);
      },
      action: (elm, d: TimeSeriesPoint, i) => {
        this.changePointSelection(d);
      }
    },
  ];

  private backgroundContextMenu: ContextMenuPosition[] = [
    {
      title: 'compareFilmstrips',
      icon: "fas fa-columns",
      visible: () => {
        return this._pointsSelection.count() >= 2;
      },
      action: () => {
        const selectedDots = this._pointsSelection.getAll();
        const wptInfos = selectedDots.map(it => it.wptInfo);
        window.open(this.urlBuilderService
          .buildFilmstripsComparisionUrl(wptInfos));
      }
    },
    {
      title: 'deselectAllPoints',
      icon: "fas fa-trash-alt",
      visible: () => {
        return this._pointsSelection.count() > 0;
      },
      action: (elm, d, i) => {
        this.unselectAllPoints();
      }
    },
  ];

  constructor(private translationService: TranslateService,
              private urlBuilderService: UrlBuilderService) {
  }

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
    if (incomingData.series.length == 0) {
      return;
    }

    this._pointsSelection = new PointsSelection();
    this._contextMenuPoint = null;
    this._xAxisCluster = {};

    let data: TimeSeries[] = this.prepareData(incomingData);
    let chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}> = d3Select('g#time-series-chart-drawing-area');
    let xScale: D3ScaleTime<number, number> = this.getXScale(data);
    let yScale: D3ScaleLinear<number, number> = this.getYScale(data);
    this.calculateLegendDimensions();
    d3Select('osm-time-series-line-chart').transition().duration(500).style('visibility', 'visible');
    d3Select('svg#time-series-chart').transition().duration(500).attr('height', this._height + this._legendGroupHeight + this._margin.top + this._margin.bottom);
    d3Select('.x-axis').transition().call(this.updateXAxis, xScale);
    d3Select('.y-axis').transition().call(this.updateYAxis, yScale, this._width, this._margin);

    this.addLegendsToChart(chart, incomingData);
    this.addDataLinesToChart(chart, xScale, yScale, data);
    this.drawAllSelectedPoints();

    this.resizeChartBackground();
    this.bringMouseMarkerToTheFront(xScale, yScale);
  }

  private resizeChartBackground() {
    d3Select('.char-background')
      .attr('width', this._width)
      .attr('height', this._height);
    this._mouseEventCatcher
      .attr('width', this._width)
      .attr('height', this._height);
  }

  private bringMouseMarkerToTheFront(xScale: D3ScaleTime<number, number>, yScale: D3ScaleLinear<number, number>) {
    const markerLine = d3Select('.marker-line').remove();
    d3Select('#time-series-chart-drawing-area')
      .append(() => markerLine.node());

    this._mouseEventCatcher
      .on('mousemove', (_, index, nodes: D3ContainerElement[]) => {
        this.moveMarker(nodes[index], xScale, yScale, this._height)
      });
  }

  /**
   * Set the data for the legend after the incoming data is received
   */
  public setLegendData(incomingData: EventResultDataDTO) {
    if (incomingData.series.length == 0) {
      return;
    }

    let labelDataMap = {};
    incomingData.series.forEach((data: EventResultSeriesDTO) => {
      let label = data.identifier;
      let key = this.generateKey(data);
      labelDataMap[key] = {
        text: label,
        key: key,
        show: true,
      }
    });
    this.legendDataMap = labelDataMap;
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
        lineChartDataPoint.wptInfo = point.wptInfo;
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
    const svg = d3Select(svgElement.nativeElement)
      .attr('id', 'time-series-chart')
      .attr('width', this._width + this._margin.left + this._margin.right)
      .attr('height', 0);

    svg.append('g')
      .attr('id', 'time-series-chart-legend')
      .attr('class', 'legend-group')
      .attr('transform', `translate(${this._legendGroupLeft}, ${this._legendGroupTop})`);

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
      .attr('width', this._width + this._margin.left + this._margin.right)
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

  private calculateLegendDimensions(): void {
    let maximumLabelWidth: number = 1;
    let labels = Object.keys(this.legendDataMap);

    d3Select('g#time-series-chart-legend')
      .append('g')
      .attr('id', 'renderToCalculateMaxWidth')
      .selectAll('.renderToCalculateMaxWidth')
      .data(labels)
      .enter()
      .append('text')
      .attr('class', 'legend-text')
      .text(datum => this.legendDataMap[datum].text)
      .each((datum, index, groups) => {
        Array.from(groups).forEach((text) => {
          if (text) {
            maximumLabelWidth = Math.max(maximumLabelWidth, text.getBoundingClientRect().width)
          }
        });
      });

    d3Select('g#renderToCalculateMaxWidth').remove();

    this._legendGroupColumnWidth = maximumLabelWidth + ChartCommons.COLOR_PREVIEW_SIZE + 30;
    this._legendGroupColumns = Math.floor(this._width / this._legendGroupColumnWidth);
    this._legendGroupHeight = Math.ceil(labels.length / this._legendGroupColumns) * ChartCommons.LABEL_HEIGHT + 30;
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
      .attr('transform', 'translate(0, ' + this._height + ')') // even if the D3 method called `axisBottom` we have to move it to the bottom by ourselves
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
        .attr('transform', 'translate(-' + (this._margin.left - 20) + ', ' + (this._height / 2 - this._margin.bottom) + ') rotate(-90)')
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
    };

    const timeFormat = function (ticks: Date[]) {
      let onlyDays = true;  // Should weekday names instead of hours and minutes should be shown
      let lastTick: Date = null;
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
    };


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

    // Show the ticks again as now all manipulation should have been happened
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
    // .curve(d3CurveMonotoneX);  // smooth the line

  }

  /**
   * Adds one line per data group to the chart
   */
  private addDataLinesToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                              xScale: D3ScaleTime<number, number>,
                              yScale: D3ScaleLinear<number, number>,
                              data: TimeSeries[]): void {
    // Remove after resize
    chart.selectAll('.line').remove();
    // Create one group per line / data entry
    chart.select('.mouse-event-catcher')
      .selectAll('.line')                             // Get all lines already drawn
      .data(data, (timeSeries: TimeSeries) => timeSeries.key)   // ... for this data
      .join(enter => {
          this.addDataPointsToXAxisCluster(enter);
          const lineSelection: any = this.drawLine(enter, xScale, yScale);

          const lineGroup = lineSelection.select(function () {
            return (<SVGPathElement>this).parentNode;
          });
          this.addDataPointMarkersToChart(lineGroup, xScale, yScale);

          return lineSelection;
        },
        update => update,
        exit => {
          this.removeDataPointsFromXAxisCluster(exit);
          exit.transition().duration(200).style('opacity', '0').remove();
        }
      )
  }

  private addDataPointsToXAxisCluster(enter: D3Selection<D3BaseType, TimeSeries, D3BaseType, {}>) {
    enter.each((timeSeries: TimeSeries) => {
      timeSeries.values.forEach((timeSeriesPoint: TimeSeriesPoint) => {
        if (!this._xAxisCluster[timeSeriesPoint.date.getTime()]) this._xAxisCluster[timeSeriesPoint.date.getTime()] = [];
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

  private drawAllSelectedPoints() {
    this.drawSelectedPoints(d3SelectAll(".dot"));
  }

  private drawSelectedPoints(dotsToCheck: D3Selection<D3BaseType, {}, HTMLElement, any>) {
    dotsToCheck.each((currentDotData: TimeSeriesPoint, index: number, dots: D3BaseType[]) => {
      const isDotSelected = this._pointsSelection.isPointSelected(currentDotData);
      if (isDotSelected) {
        d3Select(dots[index]).style("opacity", 1)
      }
    })
  }

  // private addBrush(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
  //                  xScale: D3ScaleTime<number, number>,
  //                  yScale: D3ScaleLinear<number, number>,
  //                  data: TimeSeries[]): void {
  //   chart.selectAll('.brush')
  //     .remove();
  //   this.brush.on('end', () => this.updateChart(chart, xScale, yScale));
  //   chart.append('g')
  //     .attr('class', 'brush')
  //     .data([1])
  //     .call(this.brush)
  //     .on('dblclick', () => {
  //       xScale.domain([this.getMinDate(data), this.getMaxDate(data)]);
  //       this.resetChart(xScale, yScale);
  //     });
  // }
  //
  // private resetChart(xScale: D3ScaleTime<number, number>, yScale: D3ScaleLinear<number, number>) {
  //   d3Select('.x-axis').transition().call(this.updateXAxis, xScale);
  //   d3Select('g#time-series-chart-drawing-area').selectAll('.line')
  //     .each((data, index, nodes) => {
  //       d3Select(nodes[index])
  //         .attr('d', (dataItem: TimeSeries) => this.getLineGenerator(xScale, yScale)(dataItem.values));
  //     })
  // }
  //
  // private updateChart(selection: any, xScale: D3ScaleTime<number, number>, yScale: D3ScaleLinear<number, number>) {
  //   // selected boundaries
  //   let extent = d3Event.selection;
  //   // If no selection, back to initial coordinate. Otherwise, update X axis domain
  //   if (!extent) {
  //     return
  //   } else {
  //     let minDate = xScale.invert(extent[0]);
  //     let maxDate = xScale.invert(extent[1]);
  //     xScale.domain([minDate, maxDate]);
  //     selection.select(".brush").call(this.brush.move, null); // This remove the grey brush area
  //     d3Select('.x-axis').transition().call(this.updateXAxis, xScale);
  //     selection.selectAll('.line').each((_, index, nodes) => {
  //         d3Select(nodes[index])
  //           .transition()
  //           .attr('d', (dataItem: TimeSeries) => {
  //             let newDataValues = dataItem.values.filter((point) => {
  //               return point.date <= maxDate && point.date >= minDate;
  //             });
  //             return this.getLineGenerator(xScale, yScale)(newDataValues);
  //           })
  //       }
  //     )
  //   }
  // }

  private drawLine(selection: any,
                   xScale: D3ScaleTime<number, number>,
                   yScale: D3ScaleLinear<number, number>): D3Selection<D3BaseType, TimeSeries, D3BaseType, {}> {

    const resultingSelection = selection
      .append('g')       // Group each line so we can add dots to this group latter
      .attr('class', (timeSeries: TimeSeries) => 'line line-' + timeSeries.key)
      .style('opacity', '0')
      .append('path')  // Draw one path for every item in the data set
      .style('pointer-events', 'none')
      .attr('fill', 'none')
      .attr('stroke-width', 1.5)
      .attr('d', (dataItem: TimeSeries) => {
        return this.getLineGenerator(xScale, yScale)(dataItem.values);
      });

    d3SelectAll('.line')
    // colorize (in reverse order as d3 adds new line before the existing ones ...
      .attr('stroke', (_, index: number, nodes: []) => {
        return getColorScheme()[nodes.length - index - 1];
      })
      // fade in
      .transition().duration(500).style('opacity', (timeSeries: TimeSeries) => {
      return (this.legendDataMap[timeSeries.key].show) ? '1' : '0.2';
    });

    return resultingSelection;
  }

  private addDataPointMarkersToChart(lineGroups: D3Selection<any, TimeSeries, D3BaseType, {}>, xScale: D3ScaleTime<number, number>, yScale: D3ScaleLinear<number, number>): void {
    lineGroups.each((timeSeries: TimeSeries, index: number, nodes: D3BaseType[]) => {
      const lineGroup = d3Select(nodes[index]);

      lineGroup
        .append('g')
        .selectAll('.dot-' + timeSeries.key)
        .data((timeSeries: TimeSeries) => timeSeries.values)
        .enter()
        .append('circle')
        .attr('class', (dot: TimeSeriesPoint) => 'dot dot-' + timeSeries.key + ' dot-x-' + xScale(dot.date).toString().replace('.', '_'))
        .style('opacity', 0)
        .attr('r', this.DOT_RADIUS)
        .attr('cx', (dot: TimeSeriesPoint) => xScale(dot.date))
        .attr('cy', (dot: TimeSeriesPoint) => yScale(dot.value))
    });
  }

  private hideMarker() {
    d3Select('.marker-line').style('opacity', 0);
    d3Select('#marker-tooltip').style('opacity', 0);
    this.hideOldDotsOnMarker();
  }

  private addMouseMarkerToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>, xScale: D3ScaleTime<number, number>, data: TimeSeries[]): void {
    let markerGroup = chart;

    this._contextMenuBackground = d3Select('body')
      .selectAll('.d3-context-menu-background')
      .data([1])
      .enter()
      .append('div')
      .attr('class', 'd3-context-menu-background')
      .on('click', () => {
        this.closeContextMenu();
      }).on('contextmenu', () => {
        this.closeContextMenu();
      }, false);

    this._contextMenu = d3Select('body')
      .selectAll('.d3-context-menu')
      .data([1])
      .enter()
      .append('rect')
      .attr('class', 'd3-context-menu')
      .on('contextmenu', () => d3Event.preventDefault());

    const showMarker = () => {
      d3Select('.marker-line').style('opacity', 1);
      d3Select('#marker-tooltip').style('opacity', 1);
    };

    // Watcher for mouse events
    this._mouseEventCatcher = markerGroup.append('svg:g')
      .attr('class', 'mouse-event-catcher')
      .attr('width', this._width)
      .attr('height', this._height)
      .attr('fill', 'none')
      .on('mouseenter', () => showMarker())
      .on('mouseleave', () => this.hideMarker())
      .on("contextmenu", () => d3Event.preventDefault());

    this._mouseEventCatcher.append('svg:rect')
      .attr('class', 'char-background')
      .attr('width', this._width)
      .attr('height', this._height)
      .attr('fill', 'none')
      .style('pointer-events', 'visible')
      .on('contextmenu', this.showContextMenu(this.backgroundContextMenu));

    // Append the marker line, initially hidden
    markerGroup.append('path')
      .attr('class', 'marker-line')
      .style('opacity', '0')
      .style('pointer-events', 'none');

    this.addTooltipBoxToChart();
  }

  private addTooltipBoxToChart() {
    this._markerTooltip = d3Select('#time-series-chart').select(function () {
      return (<SVGElement>this).parentNode;
    }).append('div')
      .attr('id', 'marker-tooltip')
      .style('opacity', '0.9');
  }

  private hideOldDotsOnMarker() {
    if (this._dotsOnMarker) {
      const contextMenuPointData = this._contextMenuPoint && this._contextMenuPoint.data()[0];

      this._dotsOnMarker
        .filter((s: TimeSeriesPoint) => !s.equals(contextMenuPointData))
        .attr('r', this.DOT_RADIUS)
        .style('opacity', '0')
        .style('cursor', 'auto')
        .on('click', null)
        .on('contextmenu', null);
      this.drawSelectedPoints(this._dotsOnMarker);
    }
  };

  private moveMarker(node: D3ContainerElement, xScale: D3ScaleTime<number, number>, yScale: D3ScaleLinear<number, number>, containerHeight: number) {
    const mouseCoordinates = d3Mouse(node);
    const mouseX = mouseCoordinates[0];
    const mouseXDatum = xScale.invert(mouseX);

    const bisect = d3Bisector((timestamp: string) => timestamp).left;
    const clusterKeys = Object.keys(this._xAxisCluster);
    if (clusterKeys.length < 2) return;
    clusterKeys.sort();

    let clusterIndex = bisect(clusterKeys, mouseXDatum.getTime().toString());

    if (clusterIndex == 0) clusterIndex = 1;
    if (clusterIndex >= clusterKeys.length) clusterIndex = clusterKeys.length - 1;

    const firstPointFromClusterBefore: TimeSeriesPoint = this._xAxisCluster[clusterKeys[clusterIndex - 1]][0];
    const firstPointFromClusterAfter: TimeSeriesPoint = this._xAxisCluster[clusterKeys[clusterIndex]][0];
    const pointXBefore = xScale(firstPointFromClusterBefore.date);
    const pointXAfter = xScale(firstPointFromClusterAfter.date);

    let pointX: number;
    let point: TimeSeriesPoint;
    if (pointXAfter - mouseX < mouseX - pointXBefore) {
      pointX = pointXAfter;
      point = firstPointFromClusterAfter;
    } else {
      pointX = pointXBefore;
      point = firstPointFromClusterBefore;
    }
    //draw marker line
    d3Select('.marker-line')
      .attr('d', function () {
        let d = "M" + pointX + "," + containerHeight;
        d += " " + pointX + "," + 0;
        return d;
      });

    const findDotsOnMarker = (pointX: number) => {
      const cx = pointX.toString().replace('.', '_');
      return d3SelectAll('.dot-x-' + cx);
    };

    const showDotsOnMarker = (dotsOnMarker: D3Selection<D3BaseType, {}, HTMLElement, any>) => {
      //show dots on marker
      dotsOnMarker
        .style('opacity', '1')
        .style('fill', 'white')
        .style('cursor', 'pointer')
        .on("contextmenu", this.showContextMenu(this.contextMenu))
        .on("click", (dotData: TimeSeriesPoint) => {
          if (d3Event.ctrlKey) {
            this.changePointSelection(dotData);
          } else {
            window.open(this.urlBuilderService
              .buildUrlByOption(dotData.wptInfo, this.urlBuilderService.options.waterfall));
          }
        });
    };

    this.hideOldDotsOnMarker();
    const dotsOnMarker = findDotsOnMarker(pointX);
    showDotsOnMarker(dotsOnMarker);

    this._dotsOnMarker = dotsOnMarker;

    const mouseY = mouseCoordinates[1];
    const nearestDot = this.highlightNearestDot(dotsOnMarker, mouseY, yScale);
    this.showTooltip(nearestDot, dotsOnMarker, point.date);
  }

  //TODO for background context menu there is no 'cx' value
  private showContextMenu(menu: ContextMenuPosition[]) {
    // this gets executed when a contextmenu event occurs
    return (data, currentIndex, viewElements) => {
      const selectedNode = viewElements[currentIndex];
      this._contextMenuPoint = d3Select(selectedNode);

      const visibleMenuElements = menu.filter(elem => {
        //visible is optional value, so even without this property the element is visible
        return (elem.visible == undefined) || (elem.visible(selectedNode, data, currentIndex));
      });

      if (visibleMenuElements.length == 0) {
        //do not show empty context menu
        return;
      }

      const background = this._contextMenuBackground.html('');
      const contextMenu = this._contextMenu.html('');
      const contextMenuPositions = contextMenu
        .selectAll('li')
        .data(visibleMenuElements)
        .enter()
        .append('li');

      const clickListener = (e: ContextMenuPosition) => {
        e.action(selectedNode, data, currentIndex);
        this.closeContextMenu();
      };

      contextMenuPositions.each((ctxMenuPositionData: ContextMenuPosition, ctxMenuPositionIndex, ctxMenuPositions) => {
        const currentMenuPosition = d3Select(ctxMenuPositions[ctxMenuPositionIndex]);
        if (ctxMenuPositionData.divider) {
          currentMenuPosition
            .attr('class', 'd3-context-menu-divider')
            .on('contextmenu', () => d3Event.preventDefault());
        } else {
          currentMenuPosition.append('i').attr('class', (d: ContextMenuPosition) => d.icon);
          currentMenuPosition.append('span').html((d: ContextMenuPosition) => {
            return this.translationService.instant("frontend.de.iteratec.chart.contextMenu." + d.title);
          });
          currentMenuPosition
            .on('click', clickListener)
            .on('contextmenu', clickListener);
        }
      });

      // display context menu
      background.style('display', 'block');
      contextMenu.style('display', 'block');

      //context menu must be displayed to take its width
      const contextMenuWidth = (<HTMLDivElement>this._contextMenu.node()).offsetWidth;
      const selectedPointX = Number(d3Select(selectedNode).attr("cx"));
      const left = (selectedPointX + contextMenuWidth < this._width) ? (d3Event.pageX) : (d3Event.pageX - contextMenuWidth);

      //move context menu
      contextMenu
        .style('left', left + 'px')
        .style('top', (d3Event.pageY - 2) + 'px');

      d3Event.preventDefault();
    };
  };

  private closeContextMenu() {
    d3Event.preventDefault();
    this._contextMenuPoint = null;
    this._contextMenuBackground.style('display', 'none');
    this._contextMenu.style('display', 'none');
  };

  private changePointSelection(point: TimeSeriesPoint) {
    let canPointBeSelected = true;
    if(this._pointsSelection.count() > 0) {
      const testServerUrl = this._pointsSelection.getFirst().wptInfo.baseUrl;
      if(point.wptInfo.baseUrl != testServerUrl) {
        canPointBeSelected = false;
      }
    }

    if(!canPointBeSelected) {
      //TODO temp
      alert('Comparison of the filmstrips is only possible for measurements on the same server.');
      return;
    }

    if (this._pointsSelection.isPointSelected(point)) {
      this._pointsSelection.unselectPoint(point);
    } else {
      this._pointsSelection.selectPoint(point);
    }
    this.drawAllSelectedPoints();
  }

  private unselectAllPoints() {
    const arePointEqual = (t1: TimeSeriesPoint, t2: TimeSeriesPoint) => {
      return t1.tooltipText == t2.tooltipText && t1.date.getTime() == t2.date.getTime();
    };

    d3SelectAll(".dot").each((currentDotData: TimeSeriesPoint, index: number, dots: D3BaseType[]) => {
      const wasDotSelected = this._pointsSelection.isPointSelected(currentDotData);
      const isDotOnMarkerLine = this._dotsOnMarker.data().some((elem: TimeSeriesPoint) => {
        return arePointEqual(currentDotData, elem);
      });
      if (wasDotSelected && !isDotOnMarkerLine) {
        d3Select(dots[index]).style("opacity", "0");
      }
    });
    this._pointsSelection.unselectAll();
  }

  private highlightNearestDot(visibleDots: D3Selection<D3BaseType, {}, HTMLElement, any>, mouseY: number, yScale: D3ScaleLinear<number, number>) {
    let nearestDot;
    let minDistance;
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
    const tooltip = d3Select('#marker-tooltip');

    const tooltipText = this.generateTooltipText(nearestDot, visibleDots, highlightedDate);
    tooltip.html(tooltipText.outerHTML);

    const tooltipWidth: number = (<HTMLDivElement>tooltip.node()).getBoundingClientRect().width;
    const nearestDotXPosition: number = parseFloat(nearestDot.attr('cx'));

    const top = parseFloat(nearestDot.attr('cy')) + this._margin.top;
    const left = (tooltipWidth + nearestDotXPosition > this._width) ?
      (nearestDotXPosition - tooltipWidth + this._margin.right + 10) : nearestDotXPosition + this._margin.left + 50;
    tooltip.style('top', top + 'px');
    tooltip.style('left', left + 'px');
  }

  private generateTooltipText(nearestDot: D3Selection<any, unknown, null, undefined>, visibleDots: D3Selection<D3BaseType, {}, HTMLElement, any>, highlightedDate: Date): HTMLTableElement {
    const nearestDotData = nearestDot.datum() as TimeSeriesPoint;

    const table: HTMLTableElement = document.createElement('table');
    const tableBody: HTMLTableSectionElement = document.createElement('tbody');
    table.append(tableBody);
    tableBody.append(this.generateTooltipTimestampRow(highlightedDate));

    const tempArray = [];
    visibleDots
      .each((timeSeriesPoint: TimeSeriesPoint, index: number, nodes: D3BaseType[]) => {
        tempArray.push({"value": timeSeriesPoint.value, "htmlNode": this.generateTooltipDataPointRow(timeSeriesPoint, nodes[index], nearestDotData)});
      });
    tempArray
      .sort((a, b) => b.value - a.value)
      .forEach(elem => table.append(elem.htmlNode));

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

  private generateTooltipDataPointRow(currentPoint: TimeSeriesPoint, node: D3BaseType, nearestDotData: TimeSeriesPoint): string | Node {
    const label: HTMLTableCellElement = document.createElement('td');
    label.append(currentPoint.tooltipText);

    const value: HTMLTableCellElement = document.createElement('td');
    const lineColorDot: HTMLElement = document.createElement('i');
    lineColorDot.className = 'fas fa-circle';
    lineColorDot.style.color = d3Select(node).style('stroke');
    value.append(lineColorDot);
    if (currentPoint.value !== undefined && currentPoint.value !== null) {
      value.append(currentPoint.value.toString());
    }

    const row: HTMLTableRowElement = document.createElement('tr');
    if (currentPoint.equals(nearestDotData)) {
      row.className = 'active';
    }
    row.append(label);
    row.append(value);
    return row;
  }

  private addLegendsToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                            incomingData: EventResultDataDTO) {
    chart.selectAll('.legend-entry').remove();
    const legendEntry = d3Select('.legend-group').selectAll('.legend-entry').data(Object.keys(this.legendDataMap));
    legendEntry.join(
      enter => {
        const legendElement = enter
          .append('g')
          .attr('class', 'legend-entry');
        legendElement
          .append('rect')
          .attr('class', 'legend-rect')
          .attr('height', ChartCommons.COLOR_PREVIEW_SIZE)
          .attr('width', ChartCommons.COLOR_PREVIEW_SIZE)
          .attr("rx", 2)
          .attr("ry", 2)
          .attr('fill', (key: string, index: number) => {
            return getColorScheme()[incomingData.series.length - index - 1]
          });
        legendElement
          .append('text')
          .attr('class', 'legend-text')
          .attr('x', 10 + 5)
          .attr('y', ChartCommons.COLOR_PREVIEW_SIZE)
          .text(datum => this.legendDataMap[datum].text);
        return legendElement;
      },
      update => {
        update
          .transition()
          .duration(ChartCommons.TRANSITION_DURATION)
          .style('opacity', (datum) => {
            return (this.legendDataMap[datum].show) ? 1 : 0.2;
          });
        return update;
      },
      exit => exit
        .transition()
        .duration(ChartCommons.TRANSITION_DURATION)
        .style('opacity', 0)
        .remove()
    )
      .attr("transform", (datum, index) => this.getPosition(index))
      .on('click', (datum) => this.onMouseClick(datum, incomingData));
  }

  private getPosition(index: number): string {
    const x = index % this._legendGroupColumns * this._legendGroupColumnWidth;
    const y = Math.floor(index / this._legendGroupColumns) * ChartCommons.LABEL_HEIGHT + 12;

    return "translate(" + x + "," + y + ")";
  }

  private onMouseClick(labelKey: string, incomingData: EventResultDataDTO): void {
    if (d3Event.metaKey || d3Event.ctrlKey) {
      this.legendDataMap[labelKey].show = !this.legendDataMap[labelKey].show;
    } else {
      if (labelKey == this.focusedLegendEntry) {
        Object.keys(this.legendDataMap).forEach((legend) => {
          this.legendDataMap[legend].show = true;
        });
        this.focusedLegendEntry = "";
      } else {
        Object.keys(this.legendDataMap).forEach((legend) => {
          if (legend === labelKey) {
            this.legendDataMap[legend].show = true;
            this.focusedLegendEntry = legend;
          } else {
            this.legendDataMap[legend].show = false;
          }
        });
      }
    }
    this.drawLineChart(incomingData);
  }
}

class ContextMenuPosition {
  title?: string;
  icon?: string;
  visible?: (elm, d: TimeSeriesPoint, i) => boolean;
  action?: (elm, d: TimeSeriesPoint, i) => void;
  divider?: boolean = false;
}
