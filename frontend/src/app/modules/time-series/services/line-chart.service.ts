import {ElementRef, Injectable} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";
import {take} from "rxjs/operators";

import {
  BaseType as D3BaseType,
  ContainerElement as D3ContainerElement,
  event as d3Event,
  mouse as d3Mouse,
  select as d3Select,
  selectAll as d3SelectAll,
  Selection as D3Selection
} from 'd3-selection';

import {BrushBehavior, brushX as d3BrushX} from 'd3-brush';

import {max as d3Max, min as d3Min} from 'd3-array';

import {timeFormat as d3TimeFormat} from 'd3-time-format';

import {
  scaleLinear as d3ScaleLinear,
  ScaleLinear as D3ScaleLinear,
  scaleTime as d3ScaleTime,
  ScaleTime as D3ScaleTime
} from 'd3-scale';

import {axisBottom as d3AxisBottom, axisLeft as d3AxisLeft, axisRight as d3AxisRight} from 'd3-axis';

import {line as d3Line, Line as D3Line} from 'd3-shape';

import 'd3-transition';

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
import {SummaryLabel} from "../models/summary-label.model";
import ContextMenuPosition from "../models/context-menu-position.model";

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
  private _legendGroupTop: number = this._margin.top + this._height + 50;
  private _legendGroupHeight: number;
  private _legendGroupColumnWidth: number;
  private _legendGroupColumns: number;
  private legendDataMap: Object = {};
  private brush: BrushBehavior<{}>;
  private focusedLegendEntry: string;

  // Map that holds all points clustered by their x-axis values
  private _xAxisCluster: any = {};

  // Mouse events
  private _pointSelectionErrorHandler: Function;
  private _mouseEventCatcher: D3Selection<D3BaseType, {}, D3ContainerElement, {}>;
  private _contextMenuBackground: D3Selection<D3BaseType, number, D3BaseType, unknown>;
  private _contextMenu: D3Selection<D3BaseType, number, D3BaseType, unknown>;

  private _dotsOnMarker: D3Selection<D3BaseType, {}, HTMLElement, any>;
  private _pointsSelection: PointsSelection;
  private _contextMenuPoint: D3Selection<D3BaseType, TimeSeriesPoint, D3BaseType, any>;

  private contextMenu: ContextMenuPosition[] = [
    {
      title: 'summary',
      icon: "fas fa-file-alt",
      action: (d: TimeSeriesPoint) => {
        window.open(this.urlBuilderService
          .buildSummaryUrl(d.wptInfo));
      }
    },
    {
      title: 'waterfall',
      icon: "fas fa-bars",
      action: (d: TimeSeriesPoint) => {
        window.open(this.urlBuilderService
          .buildUrlByOption(d.wptInfo, this.urlBuilderService.options.waterfall));
      }
    },
    {
      title: 'performanceReview',
      icon: "fas fa-check",
      action: (d: TimeSeriesPoint) => {
        window.open(this.urlBuilderService
          .buildUrlByOption(d.wptInfo, this.urlBuilderService.options.performanceReview));
      }
    },
    {
      title: 'contentBreakdown',
      icon: "fas fa-chart-pie",
      action: (d: TimeSeriesPoint) => {
        window.open(this.urlBuilderService
          .buildUrlByOption(d.wptInfo, this.urlBuilderService.options.contentBreakdown));
      }
    },
    {
      title: 'domains',
      icon: "fas fa-list",
      action: (d: TimeSeriesPoint) => {
        window.open(this.urlBuilderService
          .buildUrlByOption(d.wptInfo, this.urlBuilderService.options.domains));
      }
    },
    {
      title: 'screenshot',
      icon: "fas fa-image",
      action: (d: TimeSeriesPoint) => {
        window.open(this.urlBuilderService
          .buildUrlByOption(d.wptInfo, this.urlBuilderService.options.screenshot));
      }
    },
    {
      title: 'filmstrip',
      icon: "fas fa-film",
      action: (d: TimeSeriesPoint) => {
        window.open(this.urlBuilderService
          .buildFilmstripUrl(d.wptInfo));
      }
    },
    {
      title: 'filmstripTool',
      icon: "fas fa-money-check",
      action: (d: TimeSeriesPoint) => {
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
      action: () => {
        const selectedDots = this._pointsSelection.getAll();
        const wptInfos = selectedDots.map(it => it.wptInfo);
        window.open(this.urlBuilderService
          .buildFilmstripsComparisionUrl(wptInfos));
      }
    },
    {
      divider: true
    },
    {
      title: 'selectPoint',
      icon: "fas fa-dot-circle",
      visible: (d: TimeSeriesPoint) => {
        return !this._pointsSelection.isPointSelected(d);
      },
      action: (d: TimeSeriesPoint) => {
        this.changePointSelection(d);
      }
    },
    {
      title: 'deselectPoint',
      icon: "fas fa-trash-alt",
      visible: (d: TimeSeriesPoint) => {
        return this._pointsSelection.isPointSelected(d);
      },
      action: (d: TimeSeriesPoint) => {
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
      action: () => {
        this.unselectAllPoints();
      }
    },
  ];

  constructor(private translationService: TranslateService,
              private urlBuilderService: UrlBuilderService) {
  }

  public initChart(svgElement: ElementRef, pointSelectionErrorHandler: Function): void {
    this._pointSelectionErrorHandler = pointSelectionErrorHandler;

    const data: TimeSeries[] = [];
    const chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}> = this.createChart(svgElement);
    const xScale: D3ScaleTime<number, number> = this.getXScale(data);
    const yScale: D3ScaleLinear<number, number> = this.getYScale(data);

    this.addXAxisToChart(chart, xScale);
    this.addYAxisToChart(chart, yScale);

    this.addMouseMarkerToChart(chart);
  }

  /**
   * Draws a line chart for the given data into the given svg
   */
  public drawLineChart(incomingData: EventResultDataDTO): void {
    const updateYAxis = (transition: any, yScale: any, width: number) => {
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
    };

    if (incomingData.series.length == 0) {
      return;
    }

    this._pointsSelection = new PointsSelection();
    this._contextMenuPoint = null;
    this._xAxisCluster = {};

    const data: TimeSeries[] = this.prepareData(incomingData);
    const chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}> = d3Select('g#time-series-chart-drawing-area');
    const xScale: D3ScaleTime<number, number> = this.getXScale(data);

    const yScale: D3ScaleLinear<number, number> = this.getYScale(data);
    this.calculateLegendDimensions();
    d3Select('svg#time-series-chart').transition().duration(500).attr('height', this._height + this._legendGroupHeight + this._margin.top + this._margin.bottom);
    d3Select('.x-axis').transition().call(this.updateXAxis, xScale);
    d3Select('.y-axis').transition().call(updateYAxis, yScale, this._width);

    this.brush = d3BrushX().extent([[0, 0], [this._width, this._height]]);
    this.addBrush(chart, xScale, yScale, data);

    this.addLegendsToChart(chart, incomingData);
    this.setSummaryLabel(chart, incomingData.summaryLabels);
    this.addDataLinesToChart(chart, xScale, yScale, data);
    this.drawAllSelectedPoints();

    this.resizeChartBackground();
    this.bringMouseMarkerToTheFront();
  }

  private resizeChartBackground() {
    d3Select('.chart-background')
      .attr('width', this._width)
      .attr('height', this._height);
    this._mouseEventCatcher
      .attr('width', this._width)
      .attr('height', this._height);
  }

  private bringMouseMarkerToTheFront() {
    const markerLine = d3Select('.marker-line').remove();
    d3Select('#time-series-chart-drawing-area')
      .append(() => markerLine.node());

    this._mouseEventCatcher
      .on('mousemove', (_, index, nodes: D3ContainerElement[]) => {
        this.moveMarker(nodes[index], this._height)
      });
  }

  /**
   * Set the data for the legend after the incoming data is received
   */
  public setLegendData(incomingData: EventResultDataDTO) {
    if (incomingData.series.length == 0) {
      return;
    }

    const labelDataMap = {};
    incomingData.series.forEach((data: EventResultSeriesDTO) => {
      if (incomingData.summaryLabels.length > 0 && incomingData.summaryLabels[0].key != "measurand") {
        data.identifier = this.translateMeasurand(data);
      }
      const key = this.generateKey(data);
      labelDataMap[key] = {
        text: data.identifier,
        key: key,
        show: true
      }
    });
    this.legendDataMap = labelDataMap;
  }

  /**
   * Prepares the incoming data for drawing with D3.js
   */
  private prepareData(incomingData: EventResultDataDTO): TimeSeries[] {
    return incomingData.series.map((data: EventResultSeriesDTO) => {
      const lineChartData: TimeSeries = new TimeSeries();
      if (incomingData.summaryLabels.length > 0 && incomingData.summaryLabels[0].key != "measurand") {
        data.identifier = this.translateMeasurand(data);
      }
      lineChartData.key = this.generateKey(data);

      lineChartData.values = data.data.map((point: EventResultPointDTO) => {
        const lineChartDataPoint: TimeSeriesPoint = new TimeSeriesPoint();
        lineChartDataPoint.date = parseDate(point.date);
        lineChartDataPoint.value = point.value;
        lineChartDataPoint.tooltipText = data.identifier + ' : ';
        lineChartDataPoint.wptInfo = point.wptInfo;
        return lineChartDataPoint;
      });

      return lineChartData;
    });
  }

  private translateMeasurand(data: EventResultSeriesDTO): string {
    const splitLabelList: string[] = data.identifier.split(' | ');
    const splitLabel: string = this.translationService.instant('frontend.de.iteratec.isr.measurand.' + splitLabelList[0]);
    if (!splitLabel.startsWith('frontend.de.iteratec.isr.measurand.')) {
      splitLabelList[0] = splitLabel;
    }
    return splitLabelList.join(' | ');
  }

  private addBrush = (() => {
    //TODO duplicated code
    const getDate = (data: TimeSeries[], f: Function): Date => {
      return f(data, (dataItem: TimeSeries) => {
        return f(dataItem.values, (point: TimeSeriesPoint) => {
          return point.date;
        });
      });
    };

    const getMinDate = (data: TimeSeries[]): Date => {
      return getDate(data, d3Min);
    };

    const getMaxDate = (data: TimeSeries[]): Date => {
      return getDate(data, d3Max);
    };

    const resetChart = (chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>, data: TimeSeries[], xScale: D3ScaleTime<number, number>, yScale: D3ScaleLinear<number, number>) => {
      xScale.domain([getMinDate(data), getMaxDate(data)]);

      d3Select('.x-axis').transition().call((transition) => this.updateXAxis(transition, xScale));
      chart.selectAll('.line')
        .each((data: TimeSeries, index: number, nodes) => {
          d3Select(nodes[index])
            .select("path")
            .attr('d', (dataItem: TimeSeries) => this.getLineGenerator(xScale, yScale)(dataItem.values));
        });
    };

    const updateChart = (chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>, xScale: D3ScaleTime<number, number>, yScale: D3ScaleLinear<number, number>) => {
      // selected boundaries
      let extent = d3Event.selection;
      // If no selection, back to initial coordinate. Otherwise, update X axis domain
      if (!extent) {
        return;
      }

      let minDate = xScale.invert(extent[0]);
      let maxDate = xScale.invert(extent[1]);

      xScale.domain([minDate, maxDate]);
      this._mouseEventCatcher.call(this.brush.move, null); // This remove the grey brush area
      d3Select('.x-axis').transition().call((transition) => this.updateXAxis(transition, xScale));

      chart.selectAll('.line').each((data: TimeSeries, index, nodes) => {
        d3Select(nodes[index])
          .select("path")
          .transition()
          .attr('d', (dataItem: TimeSeries) => {
            const newDataValues: TimeSeriesPoint[] = dataItem.values.filter((point) => {
              return point.date <= maxDate && point.date >= minDate;
            });
            return this.getLineGenerator(xScale, yScale)(newDataValues);
          });
      });
    };

    return (chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
            xScale: D3ScaleTime<number, number>,
            yScale: D3ScaleLinear<number, number>,
            data: TimeSeries[]) => {
      this.brush.on('end', () => updateChart(chart, xScale, yScale));
      this._mouseEventCatcher
        .call(this.brush)
        .on('dblclick', () => resetChart(chart, data, xScale, yScale));
      d3Select('.overlay')
        .on('contextmenu', (d,i,e) => this.showContextMenu(this.backgroundContextMenu)(d,i,e));
    };
  })();

  private generateKey(data: EventResultSeriesDTO): string {
    //remove every non alpha numeric character
    const key: string = data.identifier.replace(/[^_a-zA-Z0-9-]/g, "");

    //if first character is digit, replace it with '_'
    const digitRegex = /[0-9]/;
    if (digitRegex.test(key.charAt(0))) {
      return key.replace(digitRegex, '_');
    }
    return key;
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
      .attr('id', 'header-group')
      .attr('transform', `translate(${this._margin.left}, ${this._margin.top - 16})`);

    const chart = svg.append('g') // g =  grouping element; group all other stuff into the chart
      .attr('id', 'time-series-chart-drawing-area')
      .attr('transform', `translate(${this._margin.left}, ${this._margin.top})`); // translates the origin to the top left corner (default behavior of D3)

    svg.append('g')
      .attr('id', 'time-series-chart-legend')
      .attr('class', 'legend-group')
      .attr('transform', `translate(${this._margin.left}, ${this._legendGroupTop})`);

    return chart;
  }

  private setSummaryLabel(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>, summaryLabels: SummaryLabel[]): void {
    const addSummaryLabel = (key: string, label: string, index: number): void => {
      d3Select(`#summary-label-part${index}`)
        .append('tspan')
        .attr('id', `summary-label-part${index + 1}`)
        .attr('class', 'summary-label-key')
        .text(`${key}: `)
        .append('tspan')
        .attr('class', 'summary-label')
        .text(label);
    };

    d3Select('g#header-group').selectAll('.summary-label-text').remove();
    if (summaryLabels.length > 0) {
      d3Select('#header-group')
        .append('g')
        .attr('class', 'summary-label-text')
        .append('text')
        .attr('id', 'summary-label-part0')
        .attr('x', this._width / 2)
        .attr('text-anchor', 'middle')
        .attr('fill', '#555555');

      summaryLabels.forEach((summaryLabel: SummaryLabel, index: number) => {
        this.translationService
          .get('frontend.de.iteratec.osm.timeSeries.chart.label.' + summaryLabel.key)
          .pipe(take(1))
          .subscribe((key: string) => {
            if (summaryLabel.key == 'measurand') {
              this.translationService
                .get('frontend.de.iteratec.isr.measurand.' + summaryLabel.label)
                .pipe(take(1))
                .subscribe((label: string) => {
                  if (label.startsWith('frontend.de.iteratec.isr.measurand.')) {
                    label = summaryLabel.label
                  }
                  label = index < summaryLabels.length - 1 ? `${label} | ` : label;
                  addSummaryLabel(key, label, index);
                });
            } else {
              const label: string = index < summaryLabels.length - 1 ? `${summaryLabel.label} | ` : summaryLabel.label;
              addSummaryLabel(key, label, index);
            }
          });
      });
      chart.selectAll('.summary-label-text').remove();
    }
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
    const getDate = (data: TimeSeries[], f: Function): Date => {
      return f(data, (dataItem: TimeSeries) => {
        return f(dataItem.values, (point: TimeSeriesPoint) => {
          return point.date;
        });
      });
    };

    const getMinDate = (data: TimeSeries[]): Date => {
      return getDate(data, d3Min);
    };

    const getMaxDate = (data: TimeSeries[]): Date => {
      return getDate(data, d3Max);
    };

    return d3ScaleTime()               // Define a scale for the X-Axis
      .range([0, this._width])  // Display the X-Axis over the complete width
      .domain([getMinDate(data), getMaxDate(data)]);
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
    const labels = Object.keys(this.legendDataMap);

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
        const element = d3Select(nodes[index]);
        const lines = element.text().split(' _nl_ ');

        // Reset the text as we will replace it
        element.text('');

        lines.forEach((line, index) => {
          const tspan = element.append('tspan').text(line);
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

  /**
   * Configuration of the line generator which does print the lines
   */
  private getLineGenerator(xScale: D3ScaleTime<number, number>,
                           yScale: D3ScaleLinear<number, number>): D3Line<TimeSeriesPoint> {
    return d3Line<TimeSeriesPoint>()               // Setup a line generator
      .x((p: TimeSeriesPoint) => xScale(p.date))   // ... specify the data for the X-Coordinate
      .y((p: TimeSeriesPoint) => yScale(p.value))  // ... and for the Y-Coordinate
    // .curve(d3CurveMonotoneX);  // smooth the line
  }

  /**
   * Adds one line per data group to the chart
   */
  private addDataLinesToChart = (() => {
    const addDataPointsToXAxisCluster = (enter: D3Selection<D3BaseType, TimeSeries, D3BaseType, {}>): void => {
      enter.each((timeSeries: TimeSeries) => {
        timeSeries.values.forEach((timeSeriesPoint: TimeSeriesPoint) => {
          if (!this._xAxisCluster[timeSeriesPoint.date.getTime()]) this._xAxisCluster[timeSeriesPoint.date.getTime()] = [];
          this._xAxisCluster[timeSeriesPoint.date.getTime()].push(timeSeriesPoint);
        });
      });
    };

    const removeDataPointsFromXAxisCluster = (exit: D3Selection<D3BaseType, TimeSeries, D3BaseType, {}>): void => {
      exit.each((timeSeries: TimeSeries, index: number) => {
        timeSeries.values.forEach((timeSeriesPoint: TimeSeriesPoint) => {
          this._xAxisCluster[timeSeriesPoint.date.getTime()].splice(index, 1);
          if (this._xAxisCluster[timeSeriesPoint.date.getTime()].length == 0) {
            delete this._xAxisCluster[timeSeriesPoint.date.getTime()];
          }
        });
      });
    };

    return (chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
            xScale: D3ScaleTime<number, number>,
            yScale: D3ScaleLinear<number, number>,
            data: TimeSeries[]): void => {
      // Remove after resize
      chart.selectAll('.line').remove();
      // Create one group per line / data entry
      this._mouseEventCatcher
        .selectAll('.line')                             // Get all lines already drawn
        .data(data, (timeSeries: TimeSeries) => timeSeries.key)   // ... for this data
        .join(enter => {
            addDataPointsToXAxisCluster(enter);
            const lineSelection: any = this.drawLine(enter, xScale, yScale);

            const lineGroup = lineSelection.select(function () {
              return (<SVGPathElement>this).parentNode;
            });
            this.addDataPointsToChart(lineGroup, xScale, yScale);

            return lineSelection;
          },
          update => update,
          exit => {
            removeDataPointsFromXAxisCluster(exit);
            exit.transition().duration(200).style('opacity', '0').remove();
          }
        )
    }
  })();

  private drawAllSelectedPoints() {
    this.drawSelectedPoints(d3SelectAll(".dot"));
  }

  private drawSelectedPoints(dotsToCheck: D3Selection<D3BaseType, {}, HTMLElement, any>) {
    dotsToCheck.each((currentDotData: TimeSeriesPoint, index: number, dots: D3BaseType[]) => {
      const isDotSelected: boolean = this._pointsSelection.isPointSelected(currentDotData);
      if (isDotSelected) {
        d3Select(dots[index]).style("opacity", 1)
      }
    })
  }

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
      return (this.legendDataMap[timeSeries.key].show) ? '1' : '0.1';
    });

    return resultingSelection;
  }

  private addDataPointsToChart(lineGroups: D3Selection<any, TimeSeries, D3BaseType, {}>, xScale: D3ScaleTime<number, number>, yScale: D3ScaleLinear<number, number>): void {
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
        .attr('pointer-events', "visible")
    });
  }

  private addMouseMarkerToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>): void {
    const markerGroup = chart;

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

    const hideMarker = () => {
      d3Select('.marker-line').style('opacity', 0);
      d3Select('#marker-tooltip').style('opacity', 0);
      this.hideOldDotsOnMarker();
    };

    // Watcher for mouse events
    this._mouseEventCatcher = markerGroup.append('svg:g')
      .attr('class', 'mouse-event-catcher')
      .attr('width', this._width)
      .attr('height', this._height)
      .attr('fill', 'none')
      .on('mouseenter', () => showMarker())
      .on('mouseleave', () => hideMarker())
      .on("contextmenu", () => d3Event.preventDefault());

    // Append the marker line, initially hidden
    markerGroup.append('path')
      .attr('class', 'marker-line')
      .style('opacity', '0')
      .style('pointer-events', 'none');

    // Add tooltip box to chart
    d3Select('#time-series-chart')
      .select((_, index: number, elem) => (<SVGElement>elem[index]).parentNode)
      .append('div')
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

  private moveMarker(node: D3ContainerElement, containerHeight: number) {
    const mouseCoordinates = d3Mouse(node);

    if (this._xAxisCluster.length < 2) {
      return;
    }

    const pointsCompareDistance = (p1, p2): number => {
      // Taxicab/Manhattan approximation of euclidean distance
      return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    };

    const findNearestDot = (dots) => {
      const mousePosition = {x: mouseCoordinates[0], y: mouseCoordinates[1]};

      let nearestDot = null;
      let minCompareDistance;
      dots.each((_, index: number, nodes: []) => {
        const pointSelection = d3Select(nodes[index]);
        const cx = parseFloat(pointSelection.attr('cx'));
        const cy = parseFloat(pointSelection.attr('cy'));
        const compareDistance = pointsCompareDistance(mousePosition, {x: cx, y: cy});
        if (minCompareDistance === undefined || compareDistance < minCompareDistance) {
          nearestDot = pointSelection;
          minCompareDistance = compareDistance;
        }
      });
      return nearestDot;
    };

    const findDotsOnMarker = (pointX: number): D3Selection<D3BaseType, {}, HTMLElement, any> => {
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
          d3Event.preventDefault();
          if (d3Event.metaKey || d3Event.ctrlKey) {
            this.changePointSelection(dotData);
          } else {
            window.open(this.urlBuilderService
              .buildUrlByOption(dotData.wptInfo, this.urlBuilderService.options.waterfall));
          }
        });
    };

    const nearestDot = findNearestDot(d3SelectAll('.dot'));
    const markerPositionX = nearestDot.attr('cx');
    //draw marker line
    const markerPath = `M${markerPositionX},${containerHeight} ${markerPositionX},0`;
    d3Select('.marker-line').attr('d', markerPath);

    this.hideOldDotsOnMarker();
    const dotsOnMarker = findDotsOnMarker(markerPositionX);
    showDotsOnMarker(dotsOnMarker);
    nearestDot.attr('r', this.DOT_HIGHLIGHT_RADIUS);

    this._dotsOnMarker = dotsOnMarker;

    this.showTooltip(nearestDot, dotsOnMarker, nearestDot.datum().date);
  }

  private showContextMenu(menu: ContextMenuPosition[]) {
    // this gets executed when a contextmenu event occurs
    return (data, currentIndex, viewElements): void => {
      const selectedNode = viewElements[currentIndex];
      this._contextMenuPoint = d3Select(selectedNode);

      const visibleMenuElements = menu.filter(elem => {
        //visible is optional value, so even without this property the element is visible
        return (elem.visible == undefined) || (elem.visible(data, currentIndex, selectedNode));
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
        e.action(data, currentIndex, selectedNode);
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
      const left = ((d3Event.pageX + contextMenuWidth + 40) < window.innerWidth) ? (d3Event.pageX) : (d3Event.pageX - contextMenuWidth);

      //move context menu
      contextMenu
        .style('left', left + 'px')
        .style('top', (d3Event.pageY - 2) + 'px');

      d3Event.preventDefault();
    };
  };

  private closeContextMenu() {
    d3Event.preventDefault();
    this._contextMenuBackground.style('display', 'none');
    this._contextMenu.style('display', 'none');

    //hide context menu point
    this._contextMenuPoint = null;
    this.hideOldDotsOnMarker();
  };

  private changePointSelection(point: TimeSeriesPoint) {
    let canPointBeSelected = true;
    if (this._pointsSelection.count() > 0) {
      const testServerUrl = this._pointsSelection.getFirst().wptInfo.baseUrl;
      if (point.wptInfo.baseUrl != testServerUrl) {
        canPointBeSelected = false;
      }
    }

    if (!canPointBeSelected) {
      this._pointSelectionErrorHandler();
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
    d3SelectAll(".dot").each((currentDotData: TimeSeriesPoint, index: number, dots: D3BaseType[]) => {
      const wasDotSelected: boolean = this._pointsSelection.isPointSelected(currentDotData);
      const isDotOnMarkerLine: boolean = this._dotsOnMarker.data().some((elem: TimeSeriesPoint) => {
        return currentDotData.equals(elem);
      });
      if (wasDotSelected && !isDotOnMarkerLine) {
        d3Select(dots[index]).style("opacity", "0");
      }
    });
    this._pointsSelection.unselectAll();
  }

  private showTooltip(nearestDot: D3Selection<any, TimeSeriesPoint, null, undefined>, visibleDots: D3Selection<D3BaseType, {}, HTMLElement, any>, highlightedDate: Date) {
    const tooltip = d3Select('#marker-tooltip');

    const tooltipText = this.generateTooltipText(nearestDot, visibleDots, highlightedDate);
    tooltip.html(tooltipText.outerHTML);

    const tooltipWidth: number = (<HTMLDivElement>tooltip.node()).getBoundingClientRect().width;
    const nearestDotXPosition: number = parseFloat(nearestDot.attr('cx'));

    const top = parseFloat(nearestDot.attr('cy')) + this._margin.top;
    const left = (nearestDotXPosition + tooltipWidth > this._width) ?
      (nearestDotXPosition - tooltipWidth + this._margin.left) : nearestDotXPosition + this._margin.left + 50;
    tooltip.style('top', top + 'px');
    tooltip.style('left', left + 'px');
  }

  private generateTooltipText = (() => {
    const generateTooltipTimestampRow = (highlightedDate: Date): string | Node => {
      const label: HTMLTableCellElement = document.createElement('td');
      label.append('Timestamp');

      const date: HTMLTableCellElement = document.createElement('td');
      date.append(highlightedDate.toLocaleString());

      const row: HTMLTableRowElement = document.createElement('tr');
      row.append(label);
      row.append(date);
      return row;
    };

    const generateTooltipDataPointRow = (currentPoint: TimeSeriesPoint, node: D3BaseType, nearestDotData: TimeSeriesPoint): string | Node => {
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
    };

    return (nearestDot: D3Selection<any, TimeSeriesPoint, null, undefined>, visibleDots: D3Selection<D3BaseType, {}, HTMLElement, any>, highlightedDate: Date): HTMLTableElement => {
      const nearestDotData = nearestDot.datum() as TimeSeriesPoint;

      const table: HTMLTableElement = document.createElement('table');
      const tableBody: HTMLTableSectionElement = document.createElement('tbody');
      table.append(tableBody);
      tableBody.append(generateTooltipTimestampRow(highlightedDate));

      const tempArray = [];
      visibleDots
        .each((timeSeriesPoint: TimeSeriesPoint, index: number, nodes: D3BaseType[]) => {
          tempArray.push({
            "value": timeSeriesPoint.value,
            "htmlNode": generateTooltipDataPointRow(timeSeriesPoint, nodes[index], nearestDotData)
          });
        });
      tempArray
        .sort((a, b) => b.value - a.value)
        .forEach(elem => tableBody.append(elem.htmlNode));

      return table;
    };
  })();

  private addLegendsToChart = (() => {
    const onLegendClick = (labelKey: string, incomingData: EventResultDataDTO): void => {
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

      //redraw chart
      this.drawLineChart(incomingData);
    };

    const getPosition = (index: number): string => {
      const x = index % this._legendGroupColumns * this._legendGroupColumnWidth;
      const y = Math.floor(index / this._legendGroupColumns) * ChartCommons.LABEL_HEIGHT + 12;

      return "translate(" + x + "," + y + ")";
    };

    return (chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>, incomingData: EventResultDataDTO): void => {
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
        .attr("transform", (datum, index) => getPosition(index))
        .on('click', (datum) => onLegendClick(datum, incomingData));
    };
  })();
}
