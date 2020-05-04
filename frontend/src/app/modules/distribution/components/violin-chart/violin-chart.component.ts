import {Component, HostListener, Input, OnChanges, ViewEncapsulation} from '@angular/core';
import * as d3 from 'd3';
import {DistributionDataDTO} from '../../models/distribution-data.model';
import '../util/chart-label-util';
import ChartLabelUtil from '../util/chart-label-util';
import {MeasurandColorService} from '../../../shared/services/measurand-color.service';
import {ViolinChartMathService} from '../../services/violin-chart-math.service';
import {SpinnerService} from '../../../shared/services/spinner.service';

@Component({
  selector: 'osm-violin-chart',
  encapsulation: ViewEncapsulation.None,
  templateUrl: './violin-chart.component.html',
  styleUrls: ['./violin-chart.component.scss']
})
export class ViolinChartComponent implements OnChanges {

  @Input()
  dataInput: DistributionDataDTO;

  private height = 600;
  private margin = {top: 50, right: 0, bottom: 70, left: 100};
  private maxViolinWidth = 150;
  private mainDataResolution = 30;
  private interpolation = d3.curveBasis;
  private width = 600;
  private chartData = null;
  private currentSeries: any[] = null;
  private violinWidth: number = null;
  private dataTrimValue: number = null;
  private commonLabelParts = null;

  drawChart = (() => {
    const initSvg = () => {
      const svgContainerSelection = d3.select('#svg-container');

      svgContainerSelection
        .append('svg')
        .attr('id', 'svg')
        .attr('class', 'd3chart')
        .attr('height', this.height)
        .attr('width', '100%');

      this.width = (<HTMLElement>svgContainerSelection.node()).getBoundingClientRect().width;
    };

    const drawChartElements = () => {
      this.violinWidth = this.calculateViolinWidth();

      const domain = this.violinChartMathService.getDomain(this.currentSeries, this.dataTrimValue);

      this.drawXAxis();

      // fixme - in backend data there is no 'dimensionalUnit'!
      // drawYAxis(domain, chartData.i18nMap['measurand'] + " [" + chartData.dimensionalUnit + "]");
      this.drawYAxis(domain, 'Ladezeiten [ms]');
      this.drawViolins(domain);
      this.drawHeader();

      // remove the xAxis lines
      d3.select('.d3chart-axis.d3chart-xAxis > path.domain').remove();
      d3.selectAll('.d3chart-axis.d3chart-xAxis g > line').remove();
    };

    return () => {
      // remove old chart
      d3.select('#svg-container')
        .selectAll('*')
        .remove();

      if (this.dataInput == null || this.dataInput.series.length === 0) {
        this.spinnerService.showSpinner('violin-chart-spinner');
        return;
      }

      this.spinnerService.hideSpinner('violin-chart-spinner');
      initSvg();

      this.chartData = this.prepareChartData(this.dataInput);

      this.assignShortLabels();

      // sort the violins after descending median as default
      this.sortByMedian();
      const sortedSeries = this.chartData.sortingRules.desc.map(trace => {
        return this.chartData.series[trace];
      });
      this.chartData.series = sortedSeries;
      this.currentSeries = sortedSeries;
      drawChartElements();
    };
  })();

  drawXAxis = (() => {
    const xRange = (): number[] => {
      return this.currentSeries.map((_, index: number) => {
        return index * this.violinWidth + this.violinWidth / 2;
      });
    };

    const rotateLabels = (): void => {
      let maxLabelLength = -1;
      let rotate;
      d3.selectAll('.d3chart-xAxis g').each((d, i, e) => {
        const childTextElem = d3.select(e[i]).select('text');
        const childTextElemNode: SVGTSpanElement = <SVGTSpanElement>childTextElem.node();
        const labelLength = childTextElemNode.getComputedTextLength();

        if (labelLength > maxLabelLength) {
          maxLabelLength = labelLength;
        }

        if (labelLength > this.violinWidth) {
          rotate = true;
        }
      });

      this.margin.bottom = Math.cos(Math.PI / 4) * maxLabelLength + 20;

      if (rotate) {
        d3.selectAll('.d3chart-xAxis g').each((d, i, e) => {
          const selectedLabel = d3.select(e[i]);
          rotateLabel(selectedLabel);
        });
      }
    };

    const rotateLabel = (labelElem): void => {
      labelElem.style('text-anchor', 'start');

      const childTextElem = labelElem.select('text');
      const y = childTextElem.attr('y');

      const transformBaseVal: SVGTransformList = labelElem.node().transform.baseVal;
      const transformMatrix: SVGMatrix = transformBaseVal.getItem(0).matrix;

      const translateX = transformMatrix.e;
      const translateY = transformMatrix.f;

      const translateString = 'translate(' + translateX + ',' + translateY + ')';
      const rotateString = 'rotate(45,0,' + y + ')';

      labelElem.attr('transform', translateString + rotateString);
    };

    return () => {
      const x = d3.scaleOrdinal(xRange())
        .domain(this.currentSeries.map(elem => elem.label));

      d3.select('#svg')
        .append('g')
        .attr('id', 'x-axis')
        .attr('class', 'd3chart-axis d3chart-xAxis')
        .call(d3.axisBottom(x))
        .call(() => rotateLabels())
        .attr('transform', 'translate(' + this.margin.left + ', ' + (this.height - this.margin.bottom) + ')');
    };
  })();

  constructor(private measurandColorProviderService: MeasurandColorService,
              private violinChartMathService: ViolinChartMathService,
              private spinnerService: SpinnerService) {
  }

  @HostListener('window:resize')
  windowResize() {
    this.drawChart();
  }

  ngOnChanges(): void {
    this.drawChart();
  }

  prepareChartData(inputData: DistributionDataDTO): DistributionDataDTO {
    const dataInputCopy = {...inputData};
    dataInputCopy.series.forEach(elem => {
      return elem.data.sort(d3.ascending);
    });
    return dataInputCopy;
  }

  assignShortLabels(): void {
    const seriesLabelParts = this.chartData.series.map(elem => {
      return {grouping: elem.identifier, page: elem.page, jobGroup: elem.jobGroup, label: elem.identifier};
    });

    const labelUtil = ChartLabelUtil.processWith(seriesLabelParts, this.chartData.i18nMap);
    labelUtil.getSeriesWithShortestUniqueLabels();
    this.commonLabelParts = labelUtil.getCommonLabelParts();

    seriesLabelParts.forEach((labelPart, index) => {
      this.chartData.series[index].label = labelPart.label;
    });
  }

  drawHeader(): void {
    const getHeaderTransform = () => {
      const widthOfAllViolins = this.currentSeries.length * this.violinWidth;
      return 'translate(' + (this.margin.left + widthOfAllViolins / 2) + ',20)';
    };

    d3.select('#svg')
      .append('g')
      .selectAll('text')
      .data([this.commonLabelParts])
      .enter()
      .append('text')
      .text(this.commonLabelParts)
      .attr('id', 'header-text')
      .attr('text-anchor', 'middle')
      .attr('transform', getHeaderTransform());
  }

  drawYAxis(domain, text): void {
    const y = d3.scaleLinear()
      .range([this.height - this.margin.bottom, this.margin.top])
      .domain(domain);

    d3.select('#svg-container')
      .append('div')
      .attr('id', 'y-axis_left_label')
      .text(text);

    const g = d3.select('#svg')
      .append('g')
      .attr('class', 'd3chart-axis d3chart-yAxis')
      .attr('transform', 'translate(' + this.margin.left + ', 0)')
      .call(d3.axisLeft(y));

    g.selectAll('.tick line').classed('d3chart-yAxis-line', true);
    g.selectAll('path').classed('d3chart-yAxis-line', true);
  }

  drawViolins(domain): void {
    const svgSelection = d3.select('#svg');
    svgSelection.selectAll('clipPath').remove();
    svgSelection.select('[clip-path]').remove();

    const violinGroup = svgSelection.append('g');
    this.createClipPathAroundViolins(violinGroup);
    this.currentSeries.forEach((d, i) => {
      const traceData = d.data;

      const g = violinGroup.append('g')
        .attr('class', 'd3chart-violin')
        .attr('style', 'fill: none;')
        .attr('transform', 'translate(' + (i * this.violinWidth + this.margin.left) + ',0)');

      this.addViolin(g, traceData, this.height - this.margin.bottom, this.violinWidth, domain);
    });
  }

  createClipPathAroundViolins(violinGroup): void {
    const clipPathId = 'violin-clip';
    d3.select('#svg')
      .append('clipPath')
      .attr('id', clipPathId)
      .append('rect')
      .attr('x', this.margin.left)
      .attr('y', this.margin.top)
      .attr('width', this.width - this.margin.left - this.margin.right)
      .attr('height', this.height - this.margin.top - this.margin.bottom);
    violinGroup.attr('clip-path', 'url(#' + clipPathId + ')');
  }

  calculateViolinWidth(): number {
    const svgWidth = this.width - this.margin.left;
    const numberOfViolins = this.currentSeries.length;

    if (numberOfViolins * this.maxViolinWidth > svgWidth) {
      return svgWidth / numberOfViolins;
    }

    return this.maxViolinWidth;
  }

  sortByMedian(): void {
    this.chartData.series.forEach(seriesData => {
      seriesData.median = this.violinChartMathService.calculateMedian(seriesData.data);
    });

    this.chartData.sortingRules = {};

    this.chartData.sortingRules.desc = Object.keys(this.chartData.series).sort((a, b) => {
      return this.chartData.series[b].median - this.chartData.series[a].median;
    });

    this.chartData.sortingRules.asc = Object.keys(this.chartData.series).sort((a, b) => {
      return this.chartData.series[a].median - this.chartData.series[b].median;
    });
  }

  private addViolin(g, traceData, height, violinWidth, domain): void {
    const resolution = this.violinChartMathService
      .histogramResolutionForTraceData(this.currentSeries, traceData, this.mainDataResolution);

    const data = d3.histogram()
      .thresholds(resolution)
      (traceData);

    // y is now the horizontal axis because of the violin being a 90 degree rotated histogram
    const y = d3.scaleLinear()
      .range([violinWidth / 2, 0])
      .domain([0, d3.max(data, d => d.length)]);

    // x is now the vertical axis because of the violin being a 90 degree rotated histogram
    const x = d3.scaleLinear()
      .range([height, this.margin.top])
      .domain(domain)
      .nice();

    const area = d3.area()
      .curve(this.interpolation)
      .x(d => x(d['x0']))
      .y0(violinWidth / 2)
      .y1(d => y(d.length));

    const line = d3.line()
      .curve(this.interpolation)
      .x(d => x(d['x0']))
      .y(d => y(d.length));

    const gPlus = g.append('g');
    const gMinus = g.append('g');

    // TODO There is no 'dimensionalUnit' in chartData
    const colorScale = this.measurandColorProviderService.getColorScaleForMeasurandGroup('ms');
    const violinColor = colorScale('0');

    gPlus.append('path')
      .datum(data)
      .attr('class', 'd3chart-violinArea')
      .attr('d', area)
      .style('fill', violinColor);

    gPlus.append('path')
      .datum(data)
      .attr('class', 'd3chart-violinOutline')
      .attr('d', line)
      .style('stroke', violinColor);

    gMinus.append('path')
      .datum(data)
      .attr('class', 'd3chart-violinArea')
      .attr('d', area)
      .style('fill', violinColor);

    gMinus.append('path')
      .datum(data)
      .attr('class', 'd3chart-violinOutline')
      .attr('d', line)
      .style('stroke', violinColor);

    gPlus.attr('transform', 'rotate(90, 0, 0)  translate(0, -' + violinWidth + ')');
    gMinus.attr('transform', 'rotate(90, 0, 0) scale(1, -1)');
  }
}
