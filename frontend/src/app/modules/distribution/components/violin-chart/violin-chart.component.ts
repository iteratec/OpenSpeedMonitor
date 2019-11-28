import {Component, ElementRef, Input, OnChanges, OnInit, ViewChild} from '@angular/core';
import * as d3 from 'd3';
import {DistributionDataDTO} from "../../models/distribution-data.model";
import ChartColorProvider from "../util/chart-color-provider";
import "../util/chart-label-util";
import ChartLabelUtil from "../util/chart-label-util";

@Component({
  selector: 'osm-violin-chart',
  templateUrl: './violin-chart.component.html',
  styleUrls: ['./violin-chart.component.scss']
})
export class ViolinChartComponent implements OnInit, OnChanges {

  @Input()
  dataInput: DistributionDataDTO;

  @ViewChild("svgContainer")
  svgContainerElem: ElementRef;

  private svgContainer: SvgContainer = null;

  ngOnInit(): void {
    this.svgContainer = new SvgContainer(this.svgContainerElem);

    window.addEventListener('resize', () => {
      if (this.dataInput) {
        this.distributionChart(this.dataInput, this.svgContainer)
      }
    });
  }

  ngOnChanges(): void {
    if(!this.dataInput || this.dataInput.series.length == 0) {
      return;
    }

    this.distributionChart(this.dataInput, this.svgContainer);
  }

  distributionChart = (function (distributionChartData, svg: SvgContainer,) {
    const height = 600,
      margin = {top: 50, right: 0, bottom: 70, left: 100},
      maxViolinWidth = 150,
      mainDataResolution = 30,
      interpolation = d3.curveBasis;

    let width = 600,
      chartData = null,
      currentSeries: any[] = null,
      violinWidth: number = null,
      dataTrimValue: number = null,
      commonLabelParts = null;

    function drawChart(distributionChartData) {
      chartData = distributionChartData;
      sortSeriesDataAscending();

      assignShortLabels();

      // sort the violins after descending median as default
      sortByMedian();
      const sortedSeries = chartData.sortingRules.desc.map(trace => {
        return chartData.series[trace];
      });
      chartData.series = sortedSeries;
      currentSeries = sortedSeries;
      draw();
    }

    function draw() {
      initSvg();
      violinWidth = calculateViolinWidth();

      const domain = getDomain();

      drawXAxis();

      // fixme - in backend data there is no 'dimensionalUnit'!
      //drawYAxis(domain, chartData.i18nMap['measurand'] + " [" + chartData.dimensionalUnit + "]");
      drawYAxis(domain, "Ladezeiten [ms]");
      drawViolins(domain);
      drawHeader();

      postDraw();
    }

    function initSvg() {
      if (svg.svgElem) {
        svg.remove();
      }

      const svgSelection = d3.select(svg.svgContainerElem.nativeElement);

      svg.svgElem = svgSelection.append("svg")
        .attr("class", "d3chart")
        .attr("height", height)
        .attr("width", "100%");
      width = svg.svgElem.node().getBoundingClientRect().width;
    }

    function assignShortLabels() {
      const seriesLabelParts = chartData.series.map(elem => {
        return {grouping: elem.identifier, page: elem.page, jobGroup: elem.jobGroup, label: elem.identifier};
      });

      const labelUtil = ChartLabelUtil.processWith(seriesLabelParts, chartData.i18nMap);
      labelUtil.getSeriesWithShortestUniqueLabels();
      commonLabelParts = labelUtil.getCommonLabelParts();

      seriesLabelParts.forEach((labelPart, index) => {
        chartData.series[index].label = labelPart.label;
      });
    }

    function getHeaderTransform() {
      const widthOfAllViolins = currentSeries.length * violinWidth;
      return "translate(" + (margin.left + widthOfAllViolins / 2) + ",20)";
    }

    function drawHeader() {
      svg.svgElem
        .append("g")
        .selectAll("text")
        .data([commonLabelParts])
        .enter()
        .append("text")
        .text(commonLabelParts)
        .attr("id", "header-text")
        .attr("text-anchor", "middle")
        .attr("transform", getHeaderTransform());
    }

    function drawXAxis() {
      const x = d3.scaleOrdinal(xRange())
        .domain(currentSeries.map(elem => elem.label));

      svg.svgElem.append("g")
        .attr("id", "x-axis")
        .attr("class", "d3chart-axis d3chart-xAxis")
        .call(d3.axisBottom(x))
        .call(rotateLabels)
        .attr("transform", "translate(" + margin.left + ", " + (height - margin.bottom) + ")");
    }

    function drawYAxis(domain, text) {
      const y = d3.scaleLinear()
        .range([height - margin.bottom, margin.top])
        .domain(domain);

      const yAxisLeftLabel = document.getElementById("y-axis_left_label");
      yAxisLeftLabel.textContent = text;

      const g = svg.svgElem.append("g")
        .attr("class", "d3chart-axis d3chart-yAxis")
        .attr("transform", "translate(" + margin.left + ", 0)")
        .call(d3.axisLeft(y));

      g.selectAll(".tick line").classed("d3chart-yAxis-line", true);
      g.selectAll("path").classed("d3chart-yAxis-line", true);
    }

    function drawViolins(domain) {
      svg.svgElem.selectAll("clipPath").remove();
      svg.svgElem.select("[clip-path]").remove();

      const violinGroup = svg.svgElem.append("g");
      createClipPathAroundViolins(violinGroup);
      currentSeries.forEach((elem, i) => {
        const traceData = elem.data;

        const g = violinGroup.append("g")
          .attr("class", "d3chart-violin")
          .attr("style", "fill: none;")
          .attr("transform", "translate(" + (i * violinWidth + margin.left) + ",0)");

        addViolin(g, traceData, height - margin.bottom, violinWidth, domain);
      });
    }

    function createClipPathAroundViolins(violinGroup) {
      const clipPathId = "violin-clip";
      svg
        .svgElem
        .append("clipPath")
        .attr("id", clipPathId)
        .append("rect")
        .attr("x", margin.left)
        .attr("y", margin.top)
        .attr("width", width - margin.left - margin.right)
        .attr("height", height - margin.top - margin.bottom);
      violinGroup.attr("clip-path", "url(#" + clipPathId + ")");
    }

    function sortSeriesDataAscending() {
      chartData.series.forEach(elem => {
        elem.data.sort(d3.ascending);
      });
    }

    function postDraw() {
      chartStyling();
    }

    function calculateViolinWidth() {
      const svgWidth = width - margin.left;
      const numberOfViolins = currentSeries.length;

      if (numberOfViolins * maxViolinWidth > svgWidth) {
        return svgWidth / numberOfViolins;
      }

      return maxViolinWidth;
    }

    function getDomain() {
      const maxInSeries = currentSeries.map(elem => {
        return Math.max(...elem.data)
      });
      const maxValue = d3.max(maxInSeries);
      const trimValue = dataTrimValue || maxValue;

      return [0, Math.min(maxValue, trimValue)];
    }

    function getGreatestDomainTrace() {
      let maxDomainSize = -1;
      let greatestTrace = [];
      currentSeries.forEach(elem => {
        const curTrace = elem.data;
        const domainSize = d3.quantile(curTrace, 0.75) - d3.quantile(curTrace, 0.25);
        if (domainSize > maxDomainSize) {
          maxDomainSize = domainSize;
          greatestTrace = curTrace;
        }
      });
      return greatestTrace;
    }

    function xRange(): number[] {
      return currentSeries.map((_, index: number) => {
        return index * violinWidth + violinWidth / 2
      });
    }

    function addViolin(g, traceData, height, violinWidth, domain) {
      const resolution = histogramResolutionForTraceData(traceData);

      const data = d3.histogram()
        .thresholds(resolution)
        (traceData);

      // y is now the horizontal axis because of the violin being a 90 degree rotated histogram
      const y = d3.scaleLinear()
        .range([violinWidth / 2, 0])
        .domain([0, d3.max(data, d => d.length)]);

      // x is now the vertical axis because of the violin being a 90 degree rotated histogram
      const x = d3.scaleLinear()
        .range([height, margin.top])
        .domain(domain)
        .nice();

      const area = d3.area()
        .curve(interpolation)
        .x(d => x(d['x0']))
        .y0(violinWidth / 2)
        .y1(d => y(d.length));

      const line = d3.line()
        .curve(interpolation)
        .x(d => x(d['x0']))
        .y(d => y(d.length));

      const gPlus = g.append("g");
      const gMinus = g.append("g");

      //TODO There is no 'dimensionalUnit' in chartData
      const colorScale = ChartColorProvider.getColorscaleForMeasurandGroup("ms");
      const violinColor = colorScale("0");

      gPlus.append("path")
        .datum(data)
        .attr("class", "d3chart-violinArea")
        .attr("d", area)
        .style("fill", violinColor);

      gPlus.append("path")
        .datum(data)
        .attr("class", "d3chart-violinOutline")
        .attr("d", line)
        .style("stroke", violinColor);

      gMinus.append("path")
        .datum(data)
        .attr("class", "d3chart-violinArea")
        .attr("d", area)
        .style("fill", violinColor);

      gMinus.append("path")
        .datum(data)
        .attr("class", "d3chart-violinOutline")
        .attr("d", line)
        .style("stroke", violinColor);

      gPlus.attr("transform", "rotate(90, 0, 0)  translate(0, -" + violinWidth + ")");
      gMinus.attr("transform", "rotate(90, 0, 0) scale(1, -1)");
    }

    function histogramResolutionForTraceData(traceData) {
      const greatestDomainTrace = getGreatestDomainTrace();
      const quantile25 = d3.quantile(greatestDomainTrace, 0.25);
      const quantile75 = d3.quantile(greatestDomainTrace, 0.75);
      const binSize = (quantile75 - quantile25) / mainDataResolution;
      return Math.floor((traceData[traceData.length - 1] - traceData[0]) / binSize);
    }

    function sortByMedian() {
      chartData.series.forEach(elem => {
        elem.median = calculateMedian(elem.data);
      });

      chartData.sortingRules = {};

      chartData.sortingRules.desc = Object.keys(chartData.series).sort(function (a, b) {
        return chartData.series[b].median - chartData.series[a].median;
      });

      chartData.sortingRules.asc = Object.keys(chartData.series).sort(function (a, b) {
        return chartData.series[a].median - chartData.series[b].median;
      });
    }

    function calculateMedian(arr) {
      // for safety reasons sort the array
      arr.sort(function (a, b) {
        return a - b;
      });

      const i = arr.length / 2;
      return (arr.length % 2 === 0) ? arr[i - 1] : (arr[Math.floor(i) - 1] + arr[Math.floor(i)]) / 2;
    }

    function chartStyling() {
      // remove the xAxis lines
      d3.select(".d3chart-axis.d3chart-xAxis > path.domain").remove();
      d3.selectAll(".d3chart-axis.d3chart-xAxis g > line").remove();
    }

    function rotateLabels() {
      let maxLabelLength = -1;
      let rotate;
      d3.selectAll(".d3chart-xAxis g").each(function () {
        const childTextElem = d3.select(this).select("text");
        const childTextElemNode: SVGTSpanElement = <SVGTSpanElement>childTextElem.node();
        const labelLength = childTextElemNode.getComputedTextLength();

        if (labelLength > maxLabelLength)
          maxLabelLength = labelLength;

        if (labelLength > violinWidth) {
          rotate = true;
        }
      });

      margin.bottom = Math.cos(Math.PI / 4) * maxLabelLength + 20;

      if (rotate) {
        d3.selectAll(".d3chart-xAxis g").each(function () {
          const selectedLabel = d3.select(this);
          rotateLabel(selectedLabel)
        });
      }
    }

    function rotateLabel(labelElem) {
      labelElem.style("text-anchor", "start");

      const childTextElem = labelElem.select("text");
      const y = childTextElem.attr("y");

      const transformBaseVal: SVGTransformList = labelElem.node().transform.baseVal;
      const transformMatrix: SVGMatrix = transformBaseVal.getItem(0).matrix;

      const translateX = transformMatrix.e;
      const translateY = transformMatrix.f;

      const translateString = "translate(" + translateX + "," + translateY + ")";
      const rotateString = "rotate(45,0," + y + ")";

      labelElem.attr("transform", translateString + rotateString);
    }

    drawChart(distributionChartData)
  });
}

class SvgContainer {
  svgContainerElem: ElementRef = null;
  svgElem = null;

  constructor(svgContainerElem: ElementRef) {
    this.svgContainerElem = svgContainerElem;
  }

  remove() {
    if (this.svgElem) {
      this.svgElem.remove();
    }
    this.svgElem = null;
  }
}
