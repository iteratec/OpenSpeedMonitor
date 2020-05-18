import {Injectable} from '@angular/core';
import {
  BaseType as D3BaseType,
  ContainerElement as D3ContainerElement,
  select as d3Select,
  selectAll as d3SelectAll,
  Selection as D3Selection
} from 'd3-selection';
import {Transition as D3Transition} from 'd3-transition';
import {TimeSeries} from '../../models/time-series.model';
import {TimeSeriesPoint} from '../../models/time-series-point.model';
import {ScaleLinear as D3ScaleLinear, ScaleTime as D3ScaleTime} from 'd3-scale';
import {getColorScheme} from '../../../../enums/color-scheme.enum';
import {line as d3Line, Line as D3Line} from 'd3-shape';
import {axisBottom as d3AxisBottom, axisLeft as d3AxisLeft, axisRight as d3AxisRight} from 'd3-axis';
import {timeFormat as d3TimeFormat} from 'd3-time-format';
import {PointsSelection} from '../../models/points-selection.model';

@Injectable({
  providedIn: 'root'
})
export class LineChartDrawService {

  constructor() {
  }

  private _DOT_RADIUS = 3;

  get DOT_RADIUS(): number {
    return this._DOT_RADIUS;
  }

  // Map that holds all points clustered by their x-axis values
  private _xAxisCluster: any = {};

  get xAxisCluster(): any {
    return this._xAxisCluster;
  }

  set xAxisCluster(value: any) {
    this._xAxisCluster = value;
  }

  /**
   * Adds one line per data group to the chart
   */
  addDataLinesToChart(chartContentContainer: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                      pointsSelection: PointsSelection,
                      xScale: D3ScaleTime<number, number>,
                      yScale: D3ScaleLinear<number, number>,
                      data: TimeSeries[],
                      legendDataMap: { [key: string]: { [key: string]: (boolean | string) } },
                      index: number): void {
    if (index === 0) {
      // Remove after resize
      chartContentContainer.selectAll('.lines').remove();
      // Remove old dots
      chartContentContainer.select('.single-dots').remove();
      // Remove old dots
      chartContentContainer.select('.dots').remove();
    }

    // Create one group per line / data entry
    chartContentContainer
      .append('g')
      .attr('class', `lines y-axis-${index}`)
      .selectAll('.line')                             // Get all lines already drawn
      .data(data, (timeSeries: TimeSeries) => timeSeries.key)   // ... for this data
      .join(enter => {
          this.addDataPointsToXAxisCluster(enter);
          const lineSelection: any = this.drawLine(enter, xScale, yScale, legendDataMap);
          this.drawSinglePointsDots(chartContentContainer, data, xScale, yScale, legendDataMap);
          this.addDataPointsToChart(chartContentContainer, pointsSelection, data, xScale, yScale, legendDataMap);
          return lineSelection;
        },
        update => update,
        exit => {
          this.removeDataPointsFromXAxisCluster(exit);
          exit.transition().duration(200).style('opacity', '0').remove();
        }
      );
  }

  drawAllSelectedPoints(pointsSelection: PointsSelection) {
    this.drawSelectedPoints(d3SelectAll('.dot'), pointsSelection);
  }

  drawSelectedPoints(dotsToCheck: D3Selection<D3BaseType, {}, HTMLElement, any>, pointsSelection: PointsSelection) {
    dotsToCheck.each((currentDotData: TimeSeriesPoint, index: number, dots: D3BaseType[]) => {
      const isDotSelected: boolean = pointsSelection.isPointSelected(currentDotData);
      if (isDotSelected) {
        d3Select(dots[index]).attr('visibility', 'visible');
      }
    });
  }

  /**
   * Print the y-axes on the graph
   */
  setYAxesInChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                         yScales: { [key: string]: D3ScaleLinear<number, number> }): void {
    const yAxes = d3SelectAll('.y-axis');
    if (Object.keys(yScales).length !== yAxes.size()) {
      yAxes.remove();
      Object.keys(yScales).forEach((key: string, index: number) => {
        let yAxis;
        if (index === 0) {
          yAxis = d3AxisLeft(yScales[key]);
        } else {
          yAxis = d3AxisRight(yScales[key]);
        }

        // Add the Y-Axis to the chart
        chart.append('g')                   // new group for the y-axis
          .attr('class', `axis y-axis y-axis${index}`)  // a css class to style it later
          .call(yAxis)
          .append('g')
          .attr('class', `axis-unit axis-unit${index}`);
      });
    }
  }

  updateXAxis(transition: D3Transition<SVGGElement, any, HTMLElement, any>, xScale: D3ScaleTime<number, number>): void {
    // Hide the ticks to avoid ugly transition
    transition.on('start.hideTicks', (_, index: number, groups: SVGGElement[]) => {
      d3Select(groups[index]).selectAll('g.tick text').attr('opacity', '0.0');
    });

    // Redraw the x-axis
    transition.call(
      d3AxisBottom(xScale)
        .tickFormat(d3TimeFormat(this.setTimeFormat(xScale.ticks())))
    );

    // Include line breaks
    transition.on('end.linebreak', ((_, index: number, groups: SVGGElement[]) => this.insertLinebreakToLabels(index, groups)));

    // Show the ticks again as now all manipulation should have been happened
    transition.on('end.showTicks', (_, index: number, groups: SVGGElement[]) => {
      d3Select(groups[index]).selectAll('g.tick text')
        .transition()
        .delay(100)
        .duration(500)
        .attr('opacity', '1.0');
    });
  }

  updateYAxes(yScales: { [key: string]: D3ScaleLinear<number, number> }, width: number, yAxisWidth: number): void {
    Object.keys(yScales).forEach((key: string, index: number) => {
      d3Select(`.y-axis${index}`)
        .transition()
        .call((transition) =>
          this.updateYAxis(
            transition,
            yScales[key],
            this.getDrawingAreaWidth(width, yAxisWidth, Object.keys(yScales).length),
            yAxisWidth,
            index)
        );
    });
  }

  getDrawingAreaWidth(width: number, yAxisWidth: number, numberOfYAxes: number): number {
    if (numberOfYAxes > 1) {
      width = width - ((numberOfYAxes - 1) * yAxisWidth);
    }

    return width;
  }

  private addDataPointsToXAxisCluster(enter: D3Selection<D3BaseType, TimeSeries, D3BaseType, {}>): void {
    enter.each((timeSeries: TimeSeries) => {
      timeSeries.values.forEach((timeSeriesPoint: TimeSeriesPoint) => {
        if (!this._xAxisCluster[timeSeriesPoint.date.getTime()]) {
          this._xAxisCluster[timeSeriesPoint.date.getTime()] = [];
        }
        this._xAxisCluster[timeSeriesPoint.date.getTime()].push(timeSeriesPoint);
      });
    });
  }

  private drawLine(selection: D3Selection<D3BaseType, TimeSeries, D3BaseType, {}>,
                   xScale: D3ScaleTime<number, number>,
                   yScale: D3ScaleLinear<number, number>,
                   legendDataMap: { [key: string]: { [key: string]: (boolean | string) } }
  ): D3Selection<D3BaseType, TimeSeries, D3BaseType, {}> {
    const resultingSelection = selection
      .append('g')       // Group each line so we can add dots to this group latter
      .attr('class', (timeSeries: TimeSeries) => `line line-${timeSeries.key}`)
      .style('opacity', '0')
      .append('path')  // Draw one path for every item in the data set
      .style('pointer-events', 'none')
      .attr('fill', 'none')
      .attr('stroke-width', 1.5)
      .attr('d', (dataItem: TimeSeries) => {
        const minDate = xScale.domain()[0];
        const maxDate = xScale.domain()[1];
        const values = dataItem.values.filter((point) => point.date <= maxDate && point.date >= minDate);
        return this.getLineGenerator(xScale, yScale)(values);
      });

    d3SelectAll('.line')
      // colorize (in reverse order as d3 adds new line before the existing ones ...
      .attr('stroke', (_, strokeIndex: number, nodes: []) => {
        return getColorScheme()[(nodes.length - strokeIndex - 1) % getColorScheme().length];
      })
      // fade in
      .transition().duration(500).style('opacity', (timeSeries: TimeSeries) => {
      return (legendDataMap[timeSeries.key].show) ? '1' : '0.1';
    });

    return resultingSelection;
  }

  private drawSinglePointsDots(chartContentContainer: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                               data: TimeSeries[],
                               xScale: D3ScaleTime<number, number>,
                               yScale: D3ScaleLinear<number, number>,
                               legendDataMap: { [key: string]: { [key: string]: (boolean | string) } }): void {
    const minDate = xScale.domain()[0];
    const maxDate = xScale.domain()[1];

    // Find series with only one dot in range
    const seriesWithOneDot = data
      .map((d: TimeSeries, index: number) => {
        return {
          key: d.key,
          values: d.values.filter((point) => point.date <= maxDate && point.date >= minDate),
          index: index
        };
      })
      .filter((d: TimeSeries) => {
        return d.values.length === 1;
      });

    // If there is no series with one dot, end the function
    if (seriesWithOneDot.length === 0) {
      return;
    }

    const singleDotsContainerSelection = chartContentContainer
      .append('g')
      .attr('class', 'single-dots')
      .selectAll()
      .data(seriesWithOneDot)
      .enter()
      .append('g')
      .attr('class', s => `single-dot-${s.key}`)
      .style('fill', d => getColorScheme()[(data.length - d.index - 1) % getColorScheme().length])
      .style('opacity', '0');
    singleDotsContainerSelection
      .selectAll()
      .filter((d: TimeSeries) => legendDataMap[d.key].show as boolean)
      .data((s: TimeSeries) => s.values)
      .enter()
      .append('circle')
      .attr('r', this._DOT_RADIUS)
      .attr('cx', (dot: TimeSeriesPoint) => xScale(dot.date))
      .attr('cy', (dot: TimeSeriesPoint) => yScale(dot.value))
      .style('pointer-events', 'visible');
    singleDotsContainerSelection
      .transition()
      .duration(500)
      .style('opacity', (timeSeries: TimeSeries) => {
        return (legendDataMap[timeSeries.key].show) ? '1' : '0.1';
      });
  }

  private addDataPointsToChart(chartContentContainer: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                               pointsSelection: PointsSelection,
                               timeSeries: TimeSeries[],
                               xScale: D3ScaleTime<number, number>,
                               yScale: D3ScaleLinear<number, number>,
                               legendDataMap: { [key: string]: { [key: string]: (boolean | string) } }): void {
    chartContentContainer
      .append('g')
      .attr('class', 'dots')
      .selectAll()
      .data(timeSeries)
      .enter()
      .append('g')
      .attr('class', (series: TimeSeries) => series.key)
      .attr('stroke', (_, index: number, nodes: []) => {
        return getColorScheme()[(nodes.length - index - 1) % getColorScheme().length];
      })
      .each((d: TimeSeries, i: number, e) => {
          // Do not render dots from not active lines
          if (!legendDataMap[d.key].show) {
            return;
          }

          const dotsGroupSelection = d3Select(e[i]);
          const minDate = xScale.domain()[0];
          const maxDate = xScale.domain()[1];
          dotsGroupSelection
            .selectAll()
            .data((series: TimeSeries) => {
              return series.values.filter((point) => point.date <= maxDate && point.date >= minDate);
            })
            .enter()
            .append('circle')
            .attr('class', (dot: TimeSeriesPoint) => `dot dot-${d.key} dot-x-${xScale(dot.date).toString().replace('.', '_')}`)
            .attr('visibility', 'hidden')
            .attr('r', this._DOT_RADIUS)
            .attr('cx', (dot: TimeSeriesPoint) => xScale(dot.date))
            .attr('cy', (dot: TimeSeriesPoint) => yScale(dot.value))
            .style('pointer-events', 'visible');
        }
      );

    // Redraw selected dots
    this.drawSelectedPoints(d3SelectAll('.dot'), pointsSelection);
  }

  private removeDataPointsFromXAxisCluster(exit: D3Selection<D3BaseType, TimeSeries, D3BaseType, {}>): void {
    exit.each((timeSeries: TimeSeries, index: number) => {
      timeSeries.values.forEach((timeSeriesPoint: TimeSeriesPoint) => {
        this._xAxisCluster[timeSeriesPoint.date.getTime()].splice(index, 1);
        if (this._xAxisCluster[timeSeriesPoint.date.getTime()].length === 0) {
          delete this._xAxisCluster[timeSeriesPoint.date.getTime()];
        }
      });
    });
  }

  /**
   * Configuration of the line generator which does print the lines
   */
  private getLineGenerator(xScale: D3ScaleTime<number, number>,
                           yScale: D3ScaleLinear<number, number>): D3Line<TimeSeriesPoint> {
    return d3Line<TimeSeriesPoint>()               // Setup a line generator
      .x((p: TimeSeriesPoint) => xScale(p.date))   // ... specify the data for the X-Coordinate
      .y((p: TimeSeriesPoint) => yScale(p.value));  // ... and for the Y-Coordinate
    // .curve(d3CurveMonotoneX);  // smooth the line
  }

  private setTimeFormat(ticks: Date[]): string {
    let onlyDays = true;  // Should weekday names instead of hours and minutes should be shown
    let lastTick: Date = null;
    // Check if every tick step is at least one day.
    // If not set onlyDays to false
    ticks.forEach((tick: Date) => {
      if (lastTick) {
        if (tick.getUTCDate() === lastTick.getUTCDate()) {
          onlyDays = false;
        }
      }
      lastTick = tick;
    });

    return (onlyDays ? '%A' : '%H:%M') + ' _nl_ %Y-%m-%d';
  }

  private insertLinebreakToLabels(index: number, groups: SVGGElement[]): void {
    d3Select(groups[index]).selectAll('g.tick text').each((_, nodeIndex: number, nodes: SVGTextElement[]) => {
      const element = d3Select(nodes[nodeIndex]);
      const lines = element.text().split(' _nl_ ');

      // Reset the text as we will replace it
      element.text('');

      lines.forEach((line, lineIndex) => {
        const tspan = element.append('tspan').text(line);
        if (lineIndex > 0) {
          tspan.attr('x', 0).attr('dy', '15');
        }
      });
    });
  }

  private updateYAxis(transition: any, yScale: any, drawingAreaWidth: number, yAxisWidth: number, index: number): void {
    let strokeOpacity: number, textPosition: number;
    if (index === 0) {
      strokeOpacity = 0.5;
      textPosition = -5;
    } else {
      strokeOpacity = 0;
      textPosition = (index - 1) * yAxisWidth + drawingAreaWidth + 5;
    }
    transition.call(
      d3AxisRight(yScale)  // axis right, because we draw the background line with this
        .tickSize(drawingAreaWidth)   // background line over complete chart width
    )
      .attr('transform', 'translate(0, 0)') // move the axis to the left
      // make all line dotted, except the one on the bottom as this will indicate the x-axis
      .call(g => g.selectAll('.tick:not(:first-of-type) line')
        .attr('stroke-opacity', strokeOpacity)
        .attr('stroke-dasharray', '1,1'))
      .call(g => g.selectAll('.tick text')  // move the text a little so it does not overlap with the lines
        .attr('x', textPosition));
  }
}
