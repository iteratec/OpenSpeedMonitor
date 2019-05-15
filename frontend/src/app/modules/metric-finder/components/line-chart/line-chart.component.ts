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
import {TestResult} from '../../models/test-result';
import {mouse, select, selectAll} from 'd3-selection';
import {line} from 'd3-shape';
import {ScaleLinear, scaleLinear, scaleTime, ScaleTime} from 'd3-scale';
import {bisector, extent, max} from 'd3-array';
import {axisBottom, axisLeft} from 'd3-axis';
import {format} from 'd3-format';
import {timeFormat} from 'd3-time-format';

@Component({
  selector: 'osm-line-chart',
  templateUrl: './line-chart.component.html',
  styleUrls: ['./line-chart.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class LineChartComponent implements AfterContentInit, OnChanges {

  @Input()
  results: TestResult[];

  @ViewChild('svg')
  svgElement: ElementRef;

  @ViewChild('tooltip')
  tooltipElement: ElementRef;

  @Input()
  metric: string;

  public width: number;
  public height: number;

  private tooltipHeight = 35;
  private margin = {
    left: 80,
    right: 40,
    top: 30,
    bottom: 30,
  };
  private xScale: ScaleTime<number, number>;
  private yScale: ScaleLinear<number, number>;
  private formatDate = timeFormat('%Y-%m-%d %H:%M:%S');

  constructor() {
  }

  redraw() {
    if (!this.results) {
      return;
    }
    this.width = this.svgElement.nativeElement.parentElement.offsetWidth - this.margin.left - this.margin.right;
    this.height = this.svgElement.nativeElement.parentElement.offsetHeight - this.margin.top - this.margin.bottom;
    this.xScale =  scaleTime()
      .domain(extent(this.results, result => result.date))
      .range([0, this.width]);
    this.yScale =  scaleLinear()
      .domain([0, max(this.results, result => result.timings[this.metric])])
      .range([this.height, 0])
      .nice();
    this.render();
  }

  ngAfterContentInit(): void {
    this.redraw();
  }

  public render() {
    const selection = select(this.svgElement.nativeElement).selectAll('g.graph').data<TestResult[]>([this.results]);
    this.enter(selection.enter());
    this.update(selection.merge(selection.enter()));
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.redraw();
  }


  private enter(selection: any) {
    const graph = selection
      .append('g')
      .attr('class', 'graph');
    graph
      .append('path')
      .attr('class', 'line');
    graph
      .append('g')
      .attr('class', 'y-axis');
    graph
      .append('g')
      .attr('class', 'x-axis');
    this.createMouseOver(graph);
  }

  private update(selection: any) {
    selection
      .select('g.graph')
      .attr('transform', `translate(${this.margin.left},${this.margin.top})`);
    selection.select('path.line')
      .attr('d', line<TestResult>()
        .x((result: TestResult) => this.xScale(result.date.getTime()))
        .y((result: TestResult) => this.yScale(result.timings[this.metric]))
      );

    selection
      .select('g.y-axis')
      .call(
        axisLeft(this.yScale)
          .tickFormat(d => this.formatValue(d))
          .ticks(5)
      );
    selection
      .select('g.x-axis')
      .attr('transform', `translate(0,${this.height})`)
      .call(axisBottom(this.xScale));
  }

  private createMouseOver(graph: any): any {
    const mouseOver = graph
      .append('g')
      .attr('class', 'mouse-over')
    mouseOver
      .append('line')
      .attr('class', 'mouse-over-line')
      .attr('y1', 0)
      .attr('y2', this.height)
      .attr('stroke', 'currentColor')
      .attr('stroke-width', '1');
    mouseOver
      .append('circle')
      .attr('class', 'mouse-over-point')
      .attr('r', '5')
      .style('filter', 'url(#glow)');
    const tooltip = select(this.tooltipElement.nativeElement)
      .attr('class', 'mouse-over tooltip');
    const mouseEvents = graph.append('rect')
      .attr('class', 'mouse-events')
      .attr('width', this.width)
      .attr('height', this.height)
      .attr('fill', 'none')
      .attr('pointer-events', 'all');
    mouseEvents
      .on('mouseover', () => selectAll('.mouse-over').style('opacity', 1))
      .on('mouseout', () => selectAll('.mouse-over').style('opacity', 0))
      .on('mousemove', () => this.mouseMove(mouseOver, tooltip, mouse(mouseEvents.node())));
  }

  private mouseMove(mouseOver, tooltip, [mouseX, mouseY]: [number, number]) {
    const xDate = this.xScale.invert(mouseX);
    const closestResult = this.closestResult(xDate);
    if (!closestResult) {
      return;
    }
    const resultPosX = this.xScale(closestResult.date);
    const yValue = closestResult.timings[this.metric];
    const resultPosY = this.yScale(yValue);
    mouseOver
      .select('line.mouse-over-line')
      .attr('x1', resultPosX)
      .attr('x2', resultPosX);
    mouseOver
      .select('circle.mouse-over-point')
      .attr('cx', resultPosX)
      .attr('cy', resultPosY);
    const formattedDate = this.formatDate(closestResult.date);
    const bottom = this.height + this.margin.top - Math.max(resultPosY, this.tooltipHeight);
    tooltip
      .html(`<span class="y-value">${this.formatValue(yValue)}</span><span class="x-value">${formattedDate}</span>`)
      .style('bottom', `${bottom}px`)
      .style('left', `${resultPosX + this.margin.left + 20}px`);
  }

  private closestResult(date: Date) {
    const timestamp = date.getTime();
    const bisectResults = bisector((testResult: TestResult) => testResult.date).left;
    const resultIndex = bisectResults(this.results, date);
    if (resultIndex > 0 &&
      timestamp - this.results[resultIndex - 1].date.getTime() < this.results[resultIndex].date.getTime() - timestamp) {
      return this.results[resultIndex - 1];
    } else {
      return this.results[resultIndex];
    }
  }

  private formatValue(value: number): string {
    return format(',')(value) + 'ms';
  }
}
