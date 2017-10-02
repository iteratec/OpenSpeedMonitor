//= require /bower_components/d3/d3.min.js
"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageComparisonData = (function (svgSelection) {
    var svg = svgSelection;
    var chartSideLabelsWidth = 0;
    var chartBarsWidth = 700;
    var fullWidth = chartSideLabelsWidth + chartBarsWidth;
    var chartBarsHeight = 400;
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
            // headerText = chartLabelUtils.getCommonLabelParts(true);
            // sideLabelData = chartLabelUtils.getSeriesWithShortestUniqueLabels(true).map(function (s) { return s.label;});
        }
        fullWidth = data.width || fullWidth;
        autoWidth = data.autoWidth !== undefined ? data.autoWidth : autoWidth;
        fullWidth = getActualSvgWidth();
        // chartSideLabelsWidth = d3.max(OpenSpeedMonitor.ChartComponents.utility.getTextWidths(svg, sideLabelData));
        chartBarsWidth = fullWidth - chartSideLabelsWidth - OpenSpeedMonitor.ChartModules.PageComparisonData.ComponentMargin;
        chartBarsHeight = calculateChartBarsHeight();
        dataAvailalbe = data.series ? true : dataAvailalbe;
    };

    var filterData = function(){
        allPageData = [];
        var newMax = -1;
        hasLoadTime = false;
        rawSeries.series.forEach(function (series) {
            var index = 0;
            series.data.forEach(function (dataElement) {
                allPageData[index] = allPageData[index] || [];
                var add = {
                    id: dataElement.grouping,
                    label: filterPageName(dataElement.grouping),
                    value: dataElement.value,
                    unit: series.dimensionalUnit
                };
                if(series.dimensionalUnit === "ms") hasLoadTime = true
                allPageData[index].push(add);
                if(dataElement.value > newMax) newMax = dataElement.value
            })
        });
        max = newMax;
    };

    var filterPageName = function (grouping) {
        return grouping.substring(grouping.lastIndexOf("|")+1,grouping.length).trim()
    };

    var getActualSvgWidth = function() {
        return svg.node().getBoundingClientRect().width;
    };

    var calculateChartBarsHeight = function () {
        var barBand = OpenSpeedMonitor.ChartComponents.common.barBand;
        var barGap = OpenSpeedMonitor.ChartComponents.common.barGap;
        var numberOfBars = allPageData[0].length;
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
        var colorProvider = OpenSpeedMonitor.ChartColorProvider().getColorscaleForMeasurandGroup("ms");
        var series = [];
        allPageData.forEach(function (data) {
            series.push(data[firstOrSecond]);
        });
        return {
            id: "page"+firstOrSecond,
            color: colorProvider(firstOrSecond),
            values: series,
            min: 0,
            max: max,
            width: chartBarsWidth
        }
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
        hasLoadTimes: hasLoadTimes
    }
});
OpenSpeedMonitor.ChartModules.PageComparisonData.ComponentMargin = 15;
