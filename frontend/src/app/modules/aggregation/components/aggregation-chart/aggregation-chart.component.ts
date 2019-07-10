import {
  AfterContentInit,
  Component,
  ElementRef,
  Input, OnChanges, SimpleChanges,
  ViewChild
} from '@angular/core';
import {select} from "d3-selection";
import {ScaleBand, scaleBand, ScaleLinear, scaleLinear} from "d3-scale";
import {ChartCommons} from "../../../../enums/chart-commons.enum";
import {max} from "d3-array";
import {AggregationChartDataService} from "../../services/aggregation-chart-data.service";
import {text} from "d3-fetch";

@Component({
  selector: 'osm-aggregation-chart',
  templateUrl: './aggregation-chart.component.html',
  styleUrls: ['./aggregation-chart.component.scss']
})
export class AggregationChartComponent implements OnChanges {

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
        jobGroup: "otto.de",
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
        jobGroup: "job group",
        measurand: "LOAD_TIME",
        measurandGroup: "LOAD_TIMES",
        measurandLabel: "load time",
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
        jobGroup: "Transfermarkt",
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


  margin = {top: 0, right: 0, bottom: 0, left: 0};
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
  private minValue: number;
  private maxValue: number;

  private dataForBarScore = [];
  private dataForLegend = [];

  constructor(private aggregationChartDataService: AggregationChartDataService) {
  }


  redraw() {
    if (this.barchartAverageData.length < 1 || this.barchartMedianData.length < 1) {
      return;
    }

    this.data = this.barchartAverageData;
    this.aggregationChartDataService.setData(this.data);
    this.dataForBarScore = this.aggregationChartDataService.getDataForScoreBar().barsToRender;
    this.maxValue = this.aggregationChartDataService.getDataForScoreBar().max;
    this.minValue = this.aggregationChartDataService.getDataForScoreBar().min;
    this.dataForLegend = this.aggregationChartDataService.getDataForLegend();

    this.data.series = this.data.series.sort((a, b) => (a.value > b.value) ? -1 : ((b.value > a.value) ? 1 : 0));

    this.svgWidth = this.svgElement.nativeElement.getBoundingClientRect().width;
    this.svgHeight = this.svgElement.nativeElement.parentElement.offsetHeight;

    this.sideLabelWidth = max(this.getTextWidths(this.svgElement.nativeElement, this.data.series.map(item => item.page)));
    this.barsWidth = this.svgWidth - 2 * ChartCommons.COMPONENT_MARGIN - this.sideLabelWidth;
    this.barsHeight = this.calculateChartBarsHeight();

    this.headerHeight = ChartCommons.CHART_HEADER_HEIGHT + ChartCommons.COMPONENT_MARGIN;
    this.barScorePosY = this.barsHeight + ChartCommons.COMPONENT_MARGIN;
    this.barScoreHeight = ChartCommons.BAR_BAND + ChartCommons.COMPONENT_MARGIN;
    this.legendPosY = this.barScorePosY + this.barScoreHeight + ChartCommons.COMPONENT_MARGIN;
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
    const svgElement = this.svgElement.nativeElement;

    this.renderHeaderGroup(svgElement);
    this.renderSideLabelGroup(svgElement);

    const contentGroup = select(svgElement).selectAll('.bars-content-group').data([1]);
    contentGroup.enter()
      .append('g')
      .attr('class', 'bars-content-group')
      .attr('transform', `translate(${this.sideLabelWidth + ChartCommons.COMPONENT_MARGIN}, ${ChartCommons.CHART_HEADER_HEIGHT + ChartCommons.COMPONENT_MARGIN})`);
    contentGroup.attr('transform', `translate(${this.sideLabelWidth + ChartCommons.COMPONENT_MARGIN}, ${ChartCommons.CHART_HEADER_HEIGHT + ChartCommons.COMPONENT_MARGIN})`);

    this.renderBarGroup();

  }

  private renderHeaderGroup(svgElement) {
    const header = select(svgElement).selectAll('.header-group').data([this.data.series]);
    header.exit().remove();
    header.enter()
      .append('g')
      .attr('class', 'header-group');
    this.renderChartHeader();
  }

  private renderChartHeader() {
    const headerText = select('.header-group').selectAll('.header-text').data([this.data.series]);
    headerText.exit()
      .transition()
      .duration(ChartCommons.TRANSITION_DURATION)
      .style('opacity', 0)
      .remove();

    headerText.enter()
      .append('text')
      .attr('class', 'header-text')
      .attr('text-anchor', 'middle')
      .attr('dominant-baseline', 'alphabetic')
      .text("Average")
      .style('opacity', 0);

    this.updateChartHeader(headerText.merge(headerText.enter()));
  }

  private updateChartHeader(selection: any) {
    selection.selectAll('.header-text')
      .attr('x', this.svgWidth / 2)
      .attr('y', ChartCommons.CHART_HEADER_HEIGHT)
      .transition()
      .duration(ChartCommons.TRANSITION_DURATION)
      .style('opacity', 1);
  }

  private renderSideLabelGroup(svgElement) {
    const sideLabels = select(svgElement).selectAll('.side-labels-group').data([this.data]);
    sideLabels.exit().remove();
    sideLabels.enter()
      .append('g')
      .attr('class', 'side-labels-group');

    sideLabels.merge(sideLabels.enter()).selectAll('.side-labels-group')
      .attr('transform', `translate(0, ${this.headerHeight})`);

    this.renderChartSideLabels();
  }

  private renderChartSideLabels() {
    const sideLabels = select('.side-labels-group').selectAll('.side-label').data(this.data.series, (data: any) => data.jobGroup + data.page);

    sideLabels.exit()
      .transition()
      .duration(ChartCommons.TRANSITION_DURATION)
      .style('opacity', 0)
      .remove();

    sideLabels.enter()
      .append('text')
      .attr('class', 'side-label')
      .attr('dominant-baseline', 'middle')
      .text(data => data.page)
      .style('opacity', 0)
      .transition()
      .duration(ChartCommons.TRANSITION_DURATION)
      .style('opacity', 1)
      .attr('transform', (data) => {
        return `translate(0, ${this.yScale(data.page) + this.yScale.bandwidth() / 2})`;
      });

    sideLabels
      .transition()
      .style('opacity', 1)
      .duration(ChartCommons.TRANSITION_DURATION)
      .attr('transform', (data) => {
        return `translate(0, ${this.yScale(data.page) + this.yScale.bandwidth() / 2})`;
      });
  }

  private renderBarGroup() {
    const barGroup = select('.bars-content-group').selectAll('.chart-bar-group').data([1]);
    barGroup.exit().remove();
    barGroup.enter()
      .append('g')
      .attr('class', 'chart-bar-group');

    //TODO stacked bars

    this.renderChartBars();
  }

  private renderChartBars() {
    const uniqueMeasurands = Array.from(new Set(this.data.series.map(item => item.measurand)));

    const chartBars = select('.chart-bar-group').selectAll('.chart-bars').data(uniqueMeasurands);
    chartBars.exit()
      .transition()
      .duration(ChartCommons.TRANSITION_DURATION)
      .attr('transform', 'translate(0, 0)')
      .style('opacity', 0)
      .remove();

    chartBars.enter()
      .append('g')
      .attr('class', 'chart-bars')
      .attr('transform', (data, index) => {
        return `translate(0, ${index * ChartCommons.BAR_BAND})`;
      })
      .each((datum, index, groups) => {
        this.renderBar(select(groups[index]));
      });

    chartBars
      .transition()
      .duration(ChartCommons.TRANSITION_DURATION)
      .attr('transform', (data, index) => {
        return `translate(0, ${index * ChartCommons.BAR_BAND})`;
      })
      .each((datum, index, groups) => {
        this.renderBar(select(groups[index]));
      });
  }

  private renderBar(chartBarSelection) {
    const bars = chartBarSelection.selectAll('.bar').data(this.data.series, (data: any) => data.jobGroup + data.page);
    bars.exit()
      .transition()
      .duration(ChartCommons.TRANSITION_DURATION)
      .style('opacity', 0)
      .remove();

    const bar = bars.enter()
      .append('g')
      .attr('class', 'bar');

    bar
      .append('rect')
      .attr('class', 'bar-rect')
      .attr('x', 0)
      .attr('y', 0)
      .attr('height', ChartCommons.BAR_BAND)
      .each((datum, index, groups) => {
        const color = false ? datum.color : '#1660a7';
        select(groups[index]).attr('fill', color);
      })
      .transition()
      .duration(ChartCommons.TRANSITION_DURATION)
      .attr('x', (datum) => {
        return this.barStart(this.xScale, datum.value)
      })
      .attr('y', (datum) => {
        return this.yScale(datum.page);
      })
      .attr('width', (datum) => {
        return this.barWidth(this.xScale, datum.value)
      });

    bar
      .append('text')
      .attr('class', 'bar-value')
      .attr('dominant-baseline', 'middle')
      .style('fill', 'white')
      .style('font-weight', 'bold')
      .transition()
      .duration(ChartCommons.TRANSITION_DURATION)
      .text((datum) => {
        return datum.value.toString();
      })
      .attr('x', (datum) => {
        return (datum.value < 0) ? (this.barStart(this.xScale, datum.value) + 10) : (this.barEnd(this.xScale, datum.value) - 10);
      })
      .attr('y', (datum) => {
        return (this.yScale(datum.page) + ChartCommons.BAR_BAND / 2);
      })
      .attr('text-anchor', (datum) => {
        return (datum.value < 0) ? 'start' : 'end';
      });

    this.renderBarUpdate(bars);
  }

  private renderBarUpdate(selection: any) {
    selection.select('.bar-rect')
      .attr('x', (datum) => {
        return this.barStart(this.xScale, datum.value)
      })
      .attr('y', (datum) => {
        return this.yScale(datum.page);
      })
      .attr('width', (datum) => {
        return this.barWidth(this.xScale, datum.value)
      });

    selection.select('.bar-value')
      .text((datum) => {
        return datum.value.toString();
      })
      .attr('x', (datum) => {
        return (datum.value < 0) ? (this.barStart(this.xScale, datum.value) + 10) : (this.barEnd(this.xScale, datum.value) - 10);
      })
      .attr('y', (datum) => {
        return (this.yScale(datum.page) + ChartCommons.BAR_BAND / 2);
      })
      .attr('text-anchor', (datum) => {
        return (datum.value < 0) ? 'start' : 'end';
      });

    this.updateScoreBar(selection);
    this.updateLegend(selection);
  }

  private updateScoreBar(selection: any) {
    let scaleForScoreBar = scaleLinear().range([0, this.barsWidth]).domain([this.minValue, this.maxValue]);
    const scoreBars = selection.selectAll('.score-bar')
      .data(this.dataForBarScore)
      .each((data, index, element) => {
        select(element[index])
          .append('rect')
          .attr('height', ChartCommons.BAR_BAND)
          .attr('y', ChartCommons.BAR_BAND / 2)
          .attr('fill', data.fill)
          .attr('width', () => {
            return scaleForScoreBar(data.end) - scaleForScoreBar(data.start);
          })
          .attr("x", () => {
            return scaleForScoreBar(data.start);
          });
        select(element[index])
          .append('text')
          .text(() => {
            return data.label;
          })
          .attr('class', 'chart-' + data.id)
          .attr('dominant-baseline', 'middle')
          .style('opacity', 1)
          .attr("text-anchor", "middle")
          .attr('x', () => {
            return (scaleForScoreBar(data.end) + scaleForScoreBar(data.start)) / 2;
          })
          .attr('y', ChartCommons.BAR_BAND);
      });
  }

  private updateLegend(selection: any) {
    let maxEntryGroupSize = this.calculateMaxEntryGroupWidth(this.svgElement.nativeElement);
    let maxEntriesInRow = Math.floor(this.svgWidth / maxEntryGroupSize);
    const legend = selection.selectAll('.legend-entry')
      .data(this.dataForLegend)
      .each((data, index, element) => {
        select(element[index])
          .attr("transform", () => {
            let x = maxEntryGroupSize * (index % maxEntriesInRow);
            return "translate(" + x + "," + this.legendPosY + ")";
          });
        select(element[index])
          .append('rect')
          .attr('width', ChartCommons.COLOR_PREVIEW_SIZE)
          .attr('height', ChartCommons.COLOR_PREVIEW_SIZE)
          .attr("rx", 2)
          .attr("ry", 2)
          .attr('fill', data.color);

        select(element[index])
          .append('text')
          .attr('x', ChartCommons.COLOR_PREVIEW_SIZE + ChartCommons.COLOR_PREVIEW_MARGIN)
          .attr('y', ChartCommons.COLOR_PREVIEW_SIZE)
          .text(data.label);
      })
  }

  estimateHeight(svgForEstimation) {
    const maxEntryGroupSize = this.calculateMaxEntryGroupWidth(svgForEstimation);
    const maxEntriesInRow = Math.floor(this.svgWidth / maxEntryGroupSize);
    return Math.floor(this.data.series.length / maxEntriesInRow) * 20;
  };

  calculateMaxEntryGroupWidth(svgForEstimation) {
    let dataMap = this.aggregationChartDataService.allMeasurandDataMap;
    const labels = Object.keys(dataMap).map(measurand => dataMap[measurand].label);
    console.log(labels);
    const labelWidths = this.getTextWidths(svgForEstimation, labels);
    return max(labelWidths) + 10 + 20 + 5;
  };

  getTextWidths(svgForEstimation, texts) {
    const widths = [];
    select(svgForEstimation).selectAll('.invisible-text-to-measure')
      .data(texts)
      .enter()
      .append("text")
      .attr("opacity", 0)
      .text((d) => d.toString())
      .each(function () {
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

  calculateChartBarsHeight() {
    const barBand = ChartCommons.BAR_BAND;
    const barGap = ChartCommons.BAR_GAP;
    const numberOfMeasurands = new Set(this.data.series.map(item => item.measurand)).size;
    const numberOfBars = this.data.series.length * (numberOfMeasurands);
    const gapSize = barGap * ((numberOfMeasurands < 2) ? 1 : 2);
    return ((this.data.series.length - 1) * gapSize) + numberOfBars * barBand;
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.redraw();
  }

}
