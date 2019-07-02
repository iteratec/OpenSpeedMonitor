import {
  AfterContentInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {select} from "d3-selection";
import {scaleBand, scaleLinear, scaleOrdinal} from "d3-scale";

@Component({
  selector: 'osm-aggregation-chart',
  templateUrl: './aggregation-chart.component.html',
  styleUrls: ['./aggregation-chart.component.scss']
})
export class AggregationChartComponent implements AfterContentInit {

  @ViewChild('svg') svgElement: ElementRef;
  @Input() barchartAverageData;
  @Input() barchartMedianData;


  dat = {
    filterRules: {},
    hasComparativeData: false,
    i18nMap: {
      measurand: "Measurand",
      jobGroup: "Job Group",
      page: "Page",
      comparativeImprovement: "Improvement",
      comparativeDeterioration: "Deterioration"
    },
    series: [
      {
        aggregationValue: "avg",
        browser: null,
        deviceType: null,
        jobGroup: "LokalTest_pal",
        measurand: "DOC_COMPLETE_TIME",
        measurandGroup: "LOAD_TIMES",
        measurandLabel: "Document Complete",
        operatingSystem: null,
        page: "finn1",
        unit: "ms",
        value: 1314.2,
        valueComparative: null
      },
      {
        aggregationValue: "avg",
        browser: null,
        deviceType: null,
        jobGroup: "LokalTest_pal",
        measurand: "DOC_COMPLETE_TIME",
        measurandGroup: "LOAD_TIMES",
        measurandLabel: "Document Complete",
        operatingSystem: null,
        page: "finn2",
        unit: "ms",
        value: 21314.2,
        valueComparative: null
      },
      {
        aggregationValue: "avg",
        browser: null,
        deviceType: null,
        jobGroup: "LokalTest_pal",
        measurand: "DOC_COMPLETE_TIME",
        measurandGroup: "LOAD_TIMES",
        measurandLabel: "Document Complete",
        operatingSystem: null,
        page: "finn3",
        unit: "ms",
        value: 11314.2,
        valueComparative: null
      }
    ]
  };



  margin = { top: 0, right: 0, bottom: 0, left: 0 };
  width: number = 1800;
  height: number = 270;

  xScale = scaleLinear().range([0, this.width]);
  yScale = scaleBand().range([0, this.height]);

  constructor() { }


  redraw() {
    if (!this.barchartAverageData || !this.barchartMedianData) {
      return;
    }

    this.xScale.domain([1314.2, 21314.2]);
    this.yScale.domain(this.dat.series.map(item => item.page));

    this.width = this.svgElement.nativeElement.parentElement.offsetWidth - this.margin.left - this.margin.right;
    this.height = this.svgElement.nativeElement.parentElement.offsetHeight - this.margin.top - this.margin.bottom;

    this.render();
  }

  render() {
    // const selection = select(this.svgElement.nativeElement).selectAll('g.chart').data<any>([this.dat]);
    // this.renderHeader(this.svgElement.nativeElement);

    const selection = select(this.svgElement.nativeElement).selectAll('g.chart').data([this.dat]);
    this.enter(selection.enter());
    this.update(selection.merge(selection.enter()));
    this.exit(selection.exit());
  }

  private renderHeader(svg: SVGElement) {
    const selection = select(svg).selectAll('.header-group').data([this.dat.series]);
  }



  private enter(selection: any) {
    const chart = selection
      .append('g')
      .attr('class', 'chart');

    const headerGroup = chart
      .append('g')
      .attr('class', 'header-group');
    headerGroup
      .append('text')
      .attr('class', 'header-text')
      .attr('text-anchor', 'middle')
      .attr('dominant-baseline', 'alphabetic')
      .style('opacity', 0);

    const sideLabelsGroup = chart
      .append('g')
      .attr('class', 'side-labels-group');

    const barsContentGroup = chart
      .append('g')
      .attr('class', 'bars-content-group');

    const chartBarGroup = barsContentGroup
      .append('g')
      .attr('class', 'chart-bar-group');

    const chartScoreGroup = barsContentGroup
      .append('g')
      .attr('class', 'chart-score-group');

    const chartLegendGroup = barsContentGroup
      .append('g')
      .attr('class', 'chart-legend-group');

    const chartBars = chartBarGroup
      .append('g')
      .attr('transform', '');

    this.dat.series.forEach(() => {
      chartBars
        .append('g')
        .attr('class', 'bar')
    })
  }

  private update(selection: any) {
    const headerText = selection.select('.header-text')
      .text((barData) => barData.series[0].jobGroup)
      .attr('x', this.width/2)
      .attr('y', 40)
      .transition()
      .duration(5)
      .style('opacity', 1);

    const bars = selection.selectAll('.bar')
      .each((data, index, element) => {
        select(element[index])
          .append('rect')
          .attr('class', 'bar-rect')
          .attr('x', 0)
          .attr('y', () => {
            return this.yScale(data.series[index].page);
          })
          .attr('height', 50)
          .attr('width', () => {
            return this.barWidth(this.xScale, data.series[index].value);
          })
          .attr('fill', '#e0000f')

      });
  }

  barWidth(xScale, value) {
    return value === null ? 0 : (this.barEnd(xScale, value) - this.barStart(xScale, value));
  };

  barEnd(xScale, value) {
    return (value < 0) ? xScale(0) : xScale(value);
  };

  barStart(xScale, value) {
    return (value < 0) ? xScale(value) : xScale(0);
  };


  private exit(selection: any) {
    selection.remove();
  }




  ngAfterContentInit(): void {
    this.redraw();
  }



}
