import {Component, ElementRef, Input, OnChanges, ViewChild} from '@angular/core';
import * as d3 from 'd3';
import {BehaviorSubject} from "rxjs";
import {DistributionDataDTO} from "../../models/distribution-data.model";
import ChartColorProvider from "../util/chart-color-provider";
import "../util/chart-label-util";

@Component({
  selector: 'osm-violin-chart',
  templateUrl: './violin-chart.component.html',
  styleUrls: ['./violin-chart.component.scss']
})
export class ViolinChartComponent implements OnChanges {

  @ViewChild('yAxisLeftLabel')
  private yAxisLeftLabel: ElementRef;

  @Input()
  dataInput: BehaviorSubject<DistributionDataDTO>;

  constructor() {
  }

  ngOnChanges() {
    if (!this.dataInput) {
      return;
    }

    this.dataInput.subscribe(d => {
      if (d.series.length > 0) {
        this.distributionChart(d);
      }
    })
  }

  distributionChart = (function (distributionChartData) {
    let svg = null,
      chartData = null,
      currentSeries = null,
      width = 600,
      height = 600,
      margin = {top: 50, right: 0, bottom: 70, left: 100},
      maxViolinWidth = 150,
      violinWidth: number = null,
      mainDataResolution = 30,
      interpolation = d3.curveBasis,
      dataTrimValue: number = null,
      commonLabelParts: string[] = null;

    // var drawUpdatedSize = function () {
    //   var domain = getDomain();
    //
    //   width = svg.node().getBoundingClientRect().width;
    //   violinWidth = calculateViolinWidth();
    //
    //   drawXAxis();
    //   drawViolins(domain);
    //   svg.select("#header-text").attr("transform", getHeaderTransform());
    //   chartStyling();
    // };

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
      currentSeries = Object.keys(sortedSeries).map(key => {
        return sortedSeries[key];
      });
      draw();
    }

    function draw() {
      initSvg();
      violinWidth = calculateViolinWidth();

      const domain = getDomain();

      drawXAxis();

      //fixme in backend data there is no i18nMap and no dimensionalUnit!
      // drawYAxis(domain, chartData.i18nMap['measurand'] + " [" + chartData.dimensionalUnit + "]");
      drawYAxis(domain, "Ladezeiten [ms]");
      drawViolins(domain);
      drawHeader();

      postDraw();
    }

    function initSvg() {
      //TODO it does not work because 'svg' variable is set every time as null
      if (svg) {
        svg.remove();
      }

      const svgSelection = d3.select("#svg-container");

      svg = svgSelection.append("svg")
        .attr("class", "d3chart")
        .attr("height", height)
        .attr("width", "100%");
      width = svg.node().getBoundingClientRect().width;
    }

    function assignShortLabels() {
      const seriesLabelParts = Object.keys(chartData.series).map(function (traceLabel) {
        return {grouping: traceLabel};
      });

      //TODO why that way?

      // const labelUtil = ChartLabelUtil.processWith(seriesLabelParts, chartData.i18nMap);
      // labelUtil.getSeriesWithShortestUniqueLabels();
      // commonLabelParts = labelUtil.getCommonLabelParts();
      commonLabelParts = chartData.series.map(it => it.identifier);

      console.log("commonLabelParts", commonLabelParts);

      seriesLabelParts.forEach(function (labelPart) {
        chartData.series[labelPart.grouping].label = labelPart.grouping;
      });
    }

    function getHeaderTransform() {
      const widthOfAllViolins = Object.keys(currentSeries).length * violinWidth;
      return "translate(" + (margin.left + widthOfAllViolins / 2) + ",20)";
    }

    function drawHeader() {
      svg.append("g").selectAll("text")
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
        .domain(Object.keys(currentSeries).map(seriesKey => {
          return currentSeries[seriesKey].label;
        }));

      //TODO why?
      // svg.selectAll(".d3chart-xAxis").remove();
      svg.append("g")
        .attr("class", "d3chart-axis d3chart-xAxis")
        .call(d3.axisBottom(x))
        .call(rotateLabels)
        .attr("transform", "translate(" + margin.left + ", " + (height - margin.bottom) + ")");
    }

    function drawYAxis(domain, text) {
      const y = d3.scaleLinear()
        .range([height - margin.bottom, margin.top])
        .domain(domain);

      document.getElementById("y-axis_left_label").textContent = text;

      const g = svg.append("g")
        .attr("class", "d3chart-axis d3chart-yAxis")
        .attr("transform", "translate(" + margin.left + ", 0)")
        .call(d3.axisLeft(y));

      g.selectAll(".tick line").classed("d3chart-yAxis-line", true);
      g.selectAll("path").classed("d3chart-yAxis-line", true);
    }

    function drawViolins(domain) {
      svg.selectAll("clipPath").remove();
      svg.select("[clip-path]").remove();

      const violinGroup = svg.append("g");
      createClipPathAroundViolins(violinGroup);
      Object.keys(currentSeries).forEach(function (trace, i) {
        const traceData = currentSeries[trace].data;

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
      Object.keys(chartData.series).forEach(trace => {
        chartData.series[trace].data.sort(d3.ascending);
      });
    }

    function postDraw() {
      chartStyling();
    }

    function calculateViolinWidth() {
      const svgWidth = width - margin.left;
      const numberOfViolins = Object.keys(currentSeries).length;

      if (numberOfViolins * maxViolinWidth > svgWidth) {
        return svgWidth / numberOfViolins;
      }

      return maxViolinWidth;
    }

    function getDomain() {
      const maxInSeries = Object.keys(currentSeries).map(trace => {
        return Math.max(...currentSeries[trace].data)
      });
      const maxValue = Math.max(...maxInSeries);
      const trimValue = dataTrimValue || maxValue;

      return [0, Math.min(maxValue, trimValue)];
    }

    function getGreatestDomainTrace() {
      let maxDomainSize = -1;
      let greatestTrace = [];
      Object.keys(currentSeries).forEach(function (trace) {
        const curTrace = currentSeries[trace].data;
        const domainSize = d3.quantile(curTrace, 0.75) - d3.quantile(curTrace, 0.25);
        if (domainSize > maxDomainSize) {
          maxDomainSize = domainSize;
          greatestTrace = curTrace;
        }
      });
      return greatestTrace;
    }

    function xRange(): number[] {
      return Object.keys(currentSeries).map((_, index: number) => {
        return index * violinWidth + violinWidth / 2
      });
    }

    function addViolin(g, traceData, height, violinWidth, domain) {
      //filter nullable values
      traceData = traceData.filter(it => it != null);

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
        .x( d => x(d['x0']))
        .y0(violinWidth / 2)
        .y1(d => y(d.length));

      const line = d3.line()
        .curve(interpolation)
        .x(d => x(d['x0']))
        .y( d => y(d.length));

      const gPlus = g.append("g");
      const gMinus = g.append("g");

      //TODO ?
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
      Object.keys(chartData.series).forEach(trace => {
        chartData.series[trace].median = calculateMedian(chartData.series[trace].data);
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
      d3.selectAll(".d3chart-xAxis text").each(elem => {
        //TODO temp
        // var labelLength = d3.select(this).node().getComputedTextLength();
        const labelLength = 10;

        if (labelLength > maxLabelLength)
          maxLabelLength = labelLength;

        if (labelLength > violinWidth)
          rotateLabel(maxLabelLength, elem)
      });
    }

    function rotateLabel(maxLabelLength: number, label) {
      margin.bottom = Math.cos(Math.PI / 4) * maxLabelLength + 20;

      d3.selectAll(".d3chart-xAxis text")
        .style("text-anchor", "start")
        .each(function () {
          const x = d3.select(this).attr("x");
          const y = d3.select(this).attr("y");
          // get the translate value
          const t = getTranslation(d3.select(this).attr("transform"));

          const translateX = t[0];
          const translateY = t[1];

          d3.select(label)
            .attr("transform", "translate(" + translateX + "," + translateY + ")rotate(" + 45 + ", " + x + ", " + y + ")");
        });
    }

    function getTranslation(transform) {
      const g = document.createElementNS("http://www.w3.org/2000/svg", "g");
      g.setAttributeNS(null, "transform", transform);
      const matrix = g.transform.baseVal.consolidate().matrix;
      return [matrix.e, matrix.f];
    }

    drawChart(distributionChartData)
  });
}
