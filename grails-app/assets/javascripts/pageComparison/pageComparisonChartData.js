//= require /bower_components/d3/d3.min.js
"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageComparisonData = (function (svgSelection) {
    var svg = svgSelection;
    var chartSideLabelsWidth = 0;
    var chartBarsWidth = 700;
    var fullWidth = chartSideLabelsWidth + chartBarsWidth;
    var chartBarsHeight = 40;
    var allPageData = [];
    var sideLabelData = [];
    var rawSeries = [];
    var headerText = "";
    var autoWidth = true;
    var dataAvailalbe = false;
    var hasLoadTime = false;
    var max = 0;
    var i18n = {};

    var setData = function (data) {
        rawSeries = data || rawSeries;
        i18n = data.i18nMap || i18n;
        if (data.series) {
            filterData();
            var chartLabelUtils = OpenSpeedMonitor.ChartModules.ChartLabelUtil(createLabelFilterData(), data.i18nMap);
            headerText = chartLabelUtils.getCommonLabelParts(true);
            sideLabelData = chartLabelUtils.getSeriesWithShortestUniqueLabels(true).map(function (s) { return s.label;});
        }
        fullWidth = data.width || fullWidth;
        autoWidth = data.autoWidth !== undefined ? data.autoWidth : autoWidth;
        fullWidth = getActualSvgWidth();
        chartBarsWidth = fullWidth - chartSideLabelsWidth - OpenSpeedMonitor.ChartModules.PageComparisonData.ComponentMargin;
        chartBarsHeight = calculateChartBarsHeight();
        dataAvailalbe = data.series ? true : dataAvailalbe;
    };

    var createLabelFilterData = function () {
      return [].concat.apply([], allPageData.map(function (groups) {
          return groups.map(function (page) {
              return {
                  page: filterPageName(page.id),
                  jobGroup: filterJobGroup(page.id),
                  id: page.id
              }
          })
      }))
    };

    var filterData = function(){
        allPageData = [];
        var newMax = -1;
        hasLoadTime = false;
        var colors = OpenSpeedMonitor.ChartColorProvider().getColorScaleForComparison();
        rawSeries.series.forEach(function (series,comparisonIndex) {
            series.data.forEach(function (dataElement, pageIndex) {
                allPageData[pageIndex] = allPageData[pageIndex] || [];
                var id = dataElement.grouping+comparisonIndex;
                var add = {
                    id: id,
                    label: dataElement.grouping,
                    value: dataElement.value,
                    unit: series.dimensionalUnit,
                    color: colors(id)
                };
                if(series.dimensionalUnit === "ms") hasLoadTime = true;
                allPageData[pageIndex].push(add);
                if(dataElement.value > newMax) newMax = dataElement.value;
            })
        });
        max = newMax;
    };

    var filterPageName = function (grouping) {
        return grouping.substring(grouping.lastIndexOf("|")+1,grouping.length).trim()
    };

    var filterJobGroup= function (grouping) {
        return grouping.substring(0, grouping.lastIndexOf("|")).trim()
    };

    var getActualSvgWidth = function() {
        return svg.node().getBoundingClientRect().width;
    };

    var calculateChartBarsHeight = function () {
        var barBand = OpenSpeedMonitor.ChartComponents.common.barBand;
        var barGap = OpenSpeedMonitor.ChartComponents.common.barGap;
        var numberOfBars = allPageData[0]? allPageData[0].length: 0;
        return (numberOfBars * barGap) + (numberOfBars * barBand);
    };

    var getDataForHeader = function () {
        return {
            width: fullWidth,
            text: headerText
        };
    };

    var getDataForBarScore = function () {
        return {
            width: chartBarsWidth,
            min: 0,
            max: max
        };
    };

    var getDataForSideLabels = function () {
        return {
            height: chartBarsHeight,
            labels: sideLabelData
        };
    };


    var getDataForBars = function (firstOrSecond) {
        var series = allPageData[firstOrSecond];
        return {
            id: "page"+firstOrSecond,
            individualColors: true,
            values: series,
            min: 0,
            max: max,
            width: chartBarsWidth,
            height: chartBarsHeight
        }
    };

    var getDataForLegend = function () {
        return {
            entries: allPageData[0].map(function (page, i) {
                return [
                    extractLegendEntry(page),
                    extractLegendEntry(allPageData[1][i])
                ]
            }),
            width: chartBarsWidth
        };
    };

    var extractLegendEntry = function (entry) {
        return {
            id: entry.id,
            color: entry.color,
            label: entry.label
        }
    };

    var getComparisonAmount = function () {
        return allPageData[0].length
    };

    var needsAutoResize = function () {
        return autoWidth && (Math.abs(getActualSvgWidth() - fullWidth) >= 1);
    };

    var getChartBarsHeight = function () {
        return chartBarsHeight;
    };

    var getChartSideLabelsWidth = function () {
        return chartSideLabelsWidth;
    };


    var isDataAvailable = function () {
        return dataAvailalbe;
    };

    var hasLoadTimes = function () {
        return hasLoadTime;
    };

    return {
        setData: setData,
        getDataForHeader: getDataForHeader,
        getDataForBarScore: getDataForBarScore,
        getDataForSideLabels: getDataForSideLabels,
        isDataAvailable: isDataAvailable,
        getDataForBars: getDataForBars,
        needsAutoResize: needsAutoResize,
        getChartBarsHeight: getChartBarsHeight,
        getChartSideLabelsWidth: getChartSideLabelsWidth,
        getComparisonAmount: getComparisonAmount,
        hasLoadTimes: hasLoadTimes,
        getDataForLegend: getDataForLegend
    }
});
OpenSpeedMonitor.ChartModules.PageComparisonData.ComponentMargin = 15;
