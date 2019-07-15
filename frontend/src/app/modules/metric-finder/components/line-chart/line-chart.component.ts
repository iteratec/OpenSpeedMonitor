import {
  AfterContentInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  ViewChild,
  ViewEncapsulation
} from '@angular/core';
import {TestResult} from '../../models/test-result.model';
import {mouse, select, selectAll} from 'd3-selection';
import {line} from 'd3-shape';
import {ScaleLinear, scaleLinear, scaleTime, ScaleTime} from 'd3-scale';
import {bisector, extent, max} from 'd3-array';
import {axisBottom, axisLeft} from 'd3-axis';
import {format} from 'd3-format';
import {timeFormat} from 'd3-time-format';
import {transition} from 'd3-transition';

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

  @Output()
  resultSelectionChange = new EventEmitter<TestResult[]>();

  public width: number;
  public height: number;

  private tooltipHeight = 35;
  private tooltipWidth = 130;
  private margin = {
    left: 80,
    right: 40,
    top: 30,
    bottom: 30,
  };
  private xScale: ScaleTime<number, number>;
  private yScale: ScaleLinear<number, number>;
  private formatDate = timeFormat('%Y-%m-%d %H:%M:%S');
  private defaultDuration = 200;

  private highlightedResult: TestResult;
  private selectedResults: TestResult[] = [];

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
    const selection = select(this.svgElement.nativeElement).selectAll('g.graph').data<TestResult[]>([this.results.filter((result: TestResult) => {
      return !!result.timings[this.metric] && result.timings[this.metric] > 0
    })]);
    this.enter(selection.enter());
    this.update(selection.merge(selection.enter()));
    this.renderSelectedPoints();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.redraw();
  }

  private renderSelectedPoints() {
    const selectedPoints = select('g.selected-points')
      .selectAll('circle.selected-point')
      .data<TestResult>(
        this.selectedResults.filter((result: TestResult) => {
          return !!result.timings[this.metric] && result.timings[this.metric] > 0
        }),
        (result: TestResult) => result.id
      );
    selectedPoints.enter()
      .append('circle')
      .attr('class', 'selected-point')
      .attr('r', '4')
      .attr('cx', d => this.xScale(d.date))
      .attr('cy', d => this.yScale(d.timings[this.metric]));
    selectedPoints
      .transition().duration(this.defaultDuration)
      .attr('cx', d => this.xScale(d.date))
      .attr('cy', d => this.yScale(d.timings[this.metric]));
    selectedPoints.exit()
      .remove();
  }

  private enter(selection: any) {
    const graph = selection
      .append('g')
      .attr('class', 'graph');
    graph
      .append('g')
      .attr('class', 'y-axis-lines');
    graph
      .append('g')
      .attr('class', 'y-axis');
    graph
      .append('g')
      .attr('class', 'x-axis');
    graph
      .append('path')
      .attr('class', 'line');
    graph
      .append('g')
      .attr('class', 'selected-points');
    this.createMouseOver(graph);
  }

  private update(selection: any) {
    selection
      .select('g.graph')
      .attr('transform', `translate(${this.margin.left},${this.margin.top})`);
    selection.select('path.line')
      .transition(transition().duration(this.defaultDuration))
      .attr('d', line<TestResult>()
        .x((result: TestResult) => this.xScale(result.date.getTime()))
        .y((result: TestResult) => this.yScale(result.timings[this.metric]))
      );

    const yAxis = axisLeft(this.yScale).ticks(5);
    selection
      .select('g.y-axis')
      .transition(transition().duration(this.defaultDuration))
      .call(yAxis.tickFormat((d: number) => this.formatValue(d)));
    selection
      .select('g.y-axis-lines')
      .transition(transition().duration(this.defaultDuration))
      .call(yAxis.tickFormat(() => '').tickSize(-this.width));
    selection
      .select('g.x-axis')
      .attr('transform', `translate(0,${this.height})`)
      .call(axisBottom(this.xScale));
    this.updateHighlightedPoint();
  }

  private createMouseOver(graph: any): any {
    const mouseEvents = graph.append('rect')
      .attr('class', 'mouse-events')
      .attr('width', this.width)
      .attr('height', this.height)
      .attr('fill', 'none')
      .attr('pointer-events', 'all');
    const mouseOver = graph
      .append('g')
      .attr('class', 'mouse-over');
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
      .style('filter', 'url(#glow)')
      .on('click', () => this.clickPoint());
    const tooltip = select(this.tooltipElement.nativeElement)
      .attr('class', 'mouse-over tooltip');
    mouseEvents
      .on('mouseover', () => selectAll('.mouse-over').style('opacity', 1))
      .on('mouseout', () => this.mouseOut(mouse(mouseEvents.node())))
      .on('mousemove', () => this.mouseMove(mouseOver, tooltip, mouse(mouseEvents.node())));
  }

  private mouseOut([mouseX, mouseY]: [number, number]) {
    if (mouseX < 0 || mouseX > this.width || mouseY < 0 || mouseY > this.height) {
      selectAll('.mouse-over').style('opacity', 0);
    }
  }

  private mouseMove(mouseOver, tooltip, [mouseX, mouseY]: [number, number]) {
    const xDate = this.xScale.invert(mouseX);
    this.highlightedResult = this.closestResult(xDate);
    if (!this.highlightedResult) {
      return;
    }
    const resultPosX = this.xScale(this.highlightedResult.date);
    const yValue = this.highlightedResult.timings[this.metric];
    const resultPosY = this.yScale(yValue);
    mouseOver
      .select('line.mouse-over-line')
      .attr('x1', resultPosX)
      .attr('x2', resultPosX);
    mouseOver
      .select('circle.mouse-over-point')
      .attr('cx', resultPosX)
      .attr('cy', resultPosY);
    this.updateHighlightedPoint();
    const formattedDate = this.formatDate(this.highlightedResult.date);
    const bottom = this.height + this.margin.top - Math.max(resultPosY - 5, this.tooltipHeight);
    const left = this.leftMarginOfTooltip(resultPosX);
    tooltip
      .html(`<span class="y-value">${this.formatValue(yValue)}</span><span class="x-value">${formattedDate}</span>`)
      .style('bottom', `${bottom}px`)
      .style('left', `${left}px`);
  }

  private leftMarginOfTooltip(resultPosX) {
    const rightEdgeOfChart = resultPosX + this.tooltipWidth > this.width;
    if (rightEdgeOfChart) {
      return resultPosX + this.margin.left - this.tooltipWidth - 15;
    } else {
      return resultPosX + this.margin.left + 20;
    }
  }

  private updateHighlightedPoint() {
    select('circle.mouse-over-point')
      .classed('selected', this.selectedResults.indexOf(this.highlightedResult) >= 0);
  }

  private clickPoint() {
    if (!this.highlightedResult) {
      return;
    }
    const index = this.selectedResults.indexOf(this.highlightedResult);
    if (index < 0) {
      this.selectedResults.push(this.highlightedResult);
    } else {
      this.selectedResults.splice(index, 1);
    }
    this.resultSelectionChange.emit(this.selectedResults);
    this.render();
  }

  private closestResult(date: Date) {
    const timestamp = date.getTime();
    const bisectResults = bisector((testResult: TestResult) => testResult.date).left;
    const resultIndex = bisectResults(this.results, date);
    if (!this.results[resultIndex]) {
      return null;
    }
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

  public clearSelection() {
    this.selectedResults = [];
  }

  public clearResults() {
    this.results = [];
  }
}
