import {
  AfterContentInit,
  Component,
  ElementRef,
  Input, OnChanges, OnInit, SimpleChanges,
  ViewChild
} from '@angular/core';
import {select} from "d3-selection";
import {ScaleBand, scaleBand, ScaleLinear, scaleLinear, scaleOrdinal} from "d3-scale";
import {ChartCommons} from "../../../../enums/chart-commons.enum";
import {max} from "d3-array";

@Component({
  selector: 'osm-aggregation-chart',
  templateUrl: './aggregation-chart.component.html',
  styleUrls: ['./aggregation-chart.component.scss']
})
export class AggregationChartComponent implements AfterContentInit, OnChanges {

  @ViewChild('svg') svgElement: ElementRef;
  @Input() barchartAverageData;
  @Input() barchartMedianData;

  data = {
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
        page: "finn2",
        unit: "ms",
        value: 4314.2,
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
        page: "really_longPage_Name54",
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
        page: "finn3",
        unit: "ms",
        value: 2314.2,
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
        page: "finn5_biggest_val",
        unit: "ms",
        value: 23214.2,
        valueComparative: null
      }
    ]
  };


  margin = { top: 0, right: 0, bottom: 0, left: 0 };
  svgWidth: number;
  svgHeight: number;

  private xScale: ScaleLinear<number, number>;
  private yScale: ScaleBand<string>;

  private sideLabelWidth: number;
  private barsWidth: number;
  private barsHeight: number;

  private headerHeight: number;
  private barScorePosY: number;
  private barScoreHeight: number;
  private legendPosY: number;
  private legendHeight: number;

  constructor() {}


  redraw() {
    if (this.barchartAverageData.length < 1 || this.barchartMedianData.length < 1) {
      return;
    }

    this.data = this.barchartAverageData;


    this.data.series = this.data.series.sort((a, b) => (a.value > b.value) ? -1 : ((b.value > a.value) ? 1 : 0));

    this.svgWidth = this.svgElement.nativeElement.getBoundingClientRect().width;
    this.svgHeight = this.svgElement.nativeElement.parentElement.offsetHeight;

    this.sideLabelWidth = max(this.getTextWidths(this.svgElement.nativeElement, this.data.series.map(item => item.page)));
    this.barsWidth = this.svgWidth - 2 * ChartCommons.COMPONENT_MARGIN - this.sideLabelWidth;
    this.barsHeight = this.calculateChartBarsHeight();

    this.headerHeight = ChartCommons.CHART_HEADER_HEIGHT + ChartCommons.COMPONENT_MARGIN;
    this.barScorePosY = this.barsHeight + ChartCommons.COMPONENT_MARGIN;
    this.barScoreHeight = ChartCommons.BAR_BAND + ChartCommons.COMPONENT_MARGIN;
    this.legendPosY = this.barScorePosY + this.barScoreHeight;
    this.legendHeight = this.estimateHeight(this.svgElement.nativeElement) + ChartCommons.COMPONENT_MARGIN;

    this.svgHeight = this.legendPosY + this.legendHeight + this.headerHeight;
    this.svgElement.nativeElement.setAttribute('height', this.svgHeight);

    this.xScale = scaleLinear()
      .domain([0, max(this.data.series.map(it => it.value))])
      .range([0, this.barsWidth]);

    this.yScale = scaleBand()
      .domain(this.data.series.map(item => item.page))
      .range([0, this.barsHeight]);

    this.render();
  }

  render() {
    const selection = select(this.svgElement.nativeElement).selectAll('g.chart').data([this.data]);
    this.enter(selection.enter());
    this.update(selection.merge(selection.enter()));
    this.exit(selection.exit());
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
      .attr('class', 'bars-content-group')
      .attr('transform', 'translate('
        + (this.sideLabelWidth + ChartCommons.COMPONENT_MARGIN) + ', '
        + (ChartCommons.CHART_HEADER_HEIGHT + ChartCommons.COMPONENT_MARGIN)
        + ')');

    const chartBarGroup = barsContentGroup
      .append('g')
      .attr('class', 'chart-bar-group');

    const chartScoreGroup = barsContentGroup
      .append('g')
      .attr('class', 'chart-score-group')
      .attr('transform', 'translate(0, ' + this.barScorePosY + ')');

    const chartLegendGroup = barsContentGroup
      .append('g')
      .attr('class', 'chart-legend-group');

    const chartBars = chartBarGroup
      .append('g')
      .attr('class', 'chart-bars')
      .attr('transform', 'translate(0, 0)');

    this.data.series.forEach(() => {
      chartBars
        .append('g')
        .attr('class', 'bar');
      sideLabelsGroup
        .append('text')
        .attr('class', 'side-label')
        .attr('dominant-baseline', 'middle')
        .style('opacity', 0);
    })
  }

  private update(selection: any) {
    const headerText = selection.select('.header-text')
      .text((barData) => barData.series[0].jobGroup)
      .attr('x', this.svgWidth/2)
      .attr('y', ChartCommons.CHART_HEADER_HEIGHT)
      .transition()
      .duration(ChartCommons.TRANSITION_DURATION)
      .style('opacity', 1);

    const sideLabelsGroup = selection.select('.side-labels-group')
      .attr('transform', 'translate(0, ' + this.headerHeight + ')');

    const chartBars = selection.selectAll('.chart-bars')
      .transition()
      .duration(ChartCommons.TRANSITION_DURATION)
      .attr('transform', (data, index) => {
        return 'translate(0, ' + (index * ChartCommons.BAR_BAND) + ')';
      });

    const sideLabels = selection.selectAll('.side-label')
      .each((data, index, element) => {
        select(element[index])
          .transition()
          .duration(ChartCommons.TRANSITION_DURATION)
          .text(data.series[index].page)
          .style('opacity', 1)
          .attr('transform', () => {
            return 'translate(0 ' + (this.yScale(data.series[index].page) + (this.yScale.bandwidth() / 2)) + ')';
          })
      });

    const bars = selection.selectAll('.bar')
      .each((data, index, element) => {
        select(element[index])
          .append('rect')
          .attr('class', 'bar-rect')
          .attr('height', ChartCommons.BAR_BAND)
          .attr('width', () => {
            return this.barWidth(this.xScale, data.series[index].value);
          })
          .attr('fill', '#e0000f')
          .attr('x', () => {
            return this.barStart(this.xScale, data.series[index].value)
          })
          .transition()
          .duration(ChartCommons.TRANSITION_DURATION)
          .attr('y', () => {
            return this.yScale(data.series[index].page);
          });
        select(element[index])
          .append('text')
          .text(() => {
            return data.series[index].value
          })
          .attr('class', 'bar-value')
          .attr('dominant-baseline', 'middle')
          .style('fill', 'white')
          .style('font-weight', 'bold')
          .attr('x', () => {
            return (data.series[index].value < 0) ? (this.barStart(this.xScale, data.series[index].value) + 10) : (this.barEnd(this.xScale, data.series[index].value) - 10);
          })
          .attr('y', () => {
            return this.yScale(data.series[index].page) + ChartCommons.BAR_BAND / 2;
          })

      });
  }




  estimateHeight (svgForEstimation) {
    const maxEntryGroupSize = this.calculateMaxEntryGroupWidth(svgForEstimation);
    const maxEntriesInRow = Math.floor(this.svgWidth / maxEntryGroupSize);
    return Math.floor(this.data.series.length / maxEntriesInRow) * 20;
  };

  calculateMaxEntryGroupWidth (svgForEstimation) {
    const labels = this.data.series.map(item => item.page);
    const labelWidths = this.getTextWidths(svgForEstimation, labels);
    return max(labelWidths) + 10 + 20 + 5;
  };

  getTextWidths (svgForEstimation, texts) {
    const widths = [];
    select(svgForEstimation).selectAll('.invisible-text-to-measure')
      .data(texts)
      .enter()
      .append("text")
      .attr("opacity", 0)
      .text((d) => d.toString())
      .each(function() {
        widths.push(this.getComputedTextLength());
        this.remove();
      });
    return widths;
  };



  barWidth(xScale, value) {
    return value === null ? 0 : (this.barEnd(xScale, value) - this.barStart(xScale, value));
  };

  barEnd(xScale, value) {
    return (value < 0) ? xScale(0) : xScale(value);
  };

  barStart(xScale, value) {
    return (value < 0) ? xScale(value) : xScale(0);
  };

  calculateChartBarsHeight () {
    const barBand = ChartCommons.BAR_BAND;
    const barGap = ChartCommons.BAR_GAP;
    const numberOfMeasurands = new Set(this.data.series.map(item => item.measurand)).size;
    const numberOfBars = this.data.series.length * (numberOfMeasurands);
    const gapSize = barGap * ((numberOfMeasurands < 2) ? 1 : 2);
    return ((this.data.series.length - 1) * gapSize) + numberOfBars * barBand;
  };



  private exit(selection: any) {
    selection.remove();
  }



  ngAfterContentInit(): void {
    this.redraw();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.redraw();
  }

}
