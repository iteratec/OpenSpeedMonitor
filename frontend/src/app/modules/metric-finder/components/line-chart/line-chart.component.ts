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
import {select} from 'd3-selection';
import {line} from 'd3-shape';
import {ScaleLinear, scaleLinear, scaleTime, ScaleTime} from 'd3-scale';
import {extent, max} from 'd3-array';
import {axisBottom, axisLeft} from 'd3-axis';
import {format} from 'd3-format';

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

  @Input()
  metric: string;

  public width: number;
  public height: number;

  private margin = {
    left: 80,
    right: 40,
    top: 30,
    bottom: 30,
  };
  private xScale: ScaleTime<number, number>;
  private yScale: ScaleLinear<number, number>;

  constructor() {
  }

  redraw() {
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
    const svg = selection
      .append('g')
      .attr('class', 'graph');
    svg
      .append('path')
      .attr('class', 'line');
    svg
      .append('g')
      .attr('class', 'y-axis');
    svg
      .append('g')
      .attr('class', 'x-axis');
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
          .tickFormat(d => format(',')(d) + 'ms')
          .ticks(5)
      );
    selection
      .select('g.x-axis')
      .attr('transform', `translate(0,${this.height})`)
      .call(axisBottom(this.xScale));
  }
}
