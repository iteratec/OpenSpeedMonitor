//= require /bower_components/d3/d3.min.js
"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageAggregationData = (function (svgSelection) {
    var svg = svgSelection;
    var chartSideLabelsWidth = 200;
    var chartBarsWidth = 700;
    var fullWidth = chartSideLabelsWidth + chartBarsWidth;
    var chartBarsHeight = 400;
    var rawSeries = [];
    var allMeasurandDataMap = {};
    var measurandGroupDataMap = {};
    var sideLabelData = [];
    var filterRules = [];
    var activeFilter = "desc";
    var headerText = "";
    var stackBars = false;
    var autoWidth = true;

    var setData = function (data) {
        rawSeries = data.series || rawSeries;
        filterRules = data.filterRules || filterRules;
        activeFilter = data.activeFilter || activeFilter;
        if (data.series || data.filterRules || data.activeFilter) {
            var filteredSeries = filterSeries(rawSeries);
            allMeasurandDataMap = extractMeasurandData(filteredSeries);
            measurandGroupDataMap = extractMeasurandGroupData(filteredSeries);
            var chartLabelUtils = OpenSpeedMonitor.ChartModules.ChartLabelUtil(Object.values(allMeasurandDataMap)[0].series, data.i18nMap);
            headerText = chartLabelUtils.getCommonLabelParts(true);
            sideLabelData = chartLabelUtils.getSeriesWithShortestUniqueLabels(true).map(function (s) { return s.label;});
        }
        stackBars = data.stackBars !== undefined ? data.stackBars : stackBars;
        fullWidth = data.width || fullWidth;
        autoWidth = data.autoWidth !== undefined ? data.autoWidth : autoWidth;
        fullWidth = autoWidth ? getActualSvgWidth() : fullWidth;
        chartSideLabelsWidth = d3.max(OpenSpeedMonitor.ChartComponents.utility.getTextWidths(svg, sideLabelData));
        chartBarsWidth = fullWidth - OpenSpeedMonitor.ChartModules.PageAggregationData.ComponentMargin - chartSideLabelsWidth;
        chartBarsHeight = calculateChartBarsHeight();
    };


    var extractMeasurandData = function (series) {
        var colorProvider = OpenSpeedMonitor.ChartColorProvider();
        var colorScales = {};
        return d3.nest()
            .key(function(d) { return d.measurand; })
            .rollup(function (seriesOfMeasurand) {
                var firstValue = seriesOfMeasurand[0];
                var unit = firstValue.unit;
                colorScales[unit] = colorScales[unit] || colorProvider.getColorscaleForMeasurandGroup(unit);
                seriesOfMeasurand.forEach(function(pageData) {
                    pageData.id = pageData.page + ";" + pageData.jobGroup
                });
                return {
                    id: firstValue.measurand,
                    label: firstValue.measurandLabel,
                    measurandGroup: firstValue.measurandGroup,
                    color: colorScales[unit](firstValue.measurand),
                    series: seriesOfMeasurand
                };
            }).map(series);
    };

    var extractMeasurandGroupData = function (series) {
        return d3.nest()
            .key(function(d) { return d.measurandGroup; })
            .rollup(function (seriesOfMeasurandGroup) {
                var extent = d3.extent(seriesOfMeasurandGroup, function(entry) { return entry.value; });
                return {
                    min: extent[0],
                    max: extent[1]
                };
            }).map(series);
    };

    var filterSeries = function (series) {
        if (activeFilter === "asc" || activeFilter === "desc") {
            var copy = series.slice();
            copy.sort(function (a, b) {
                return activeFilter === "asc" ? a.value - b.value :  b.value - a.value;
            });
            return copy;
        }
        if (!filterRules[activeFilter]) {
            console.error("Unknown filter rule'" + activeFilter + "'");
            return series;
        }
        var filteredSeries = [];
        filterRules[activeFilter].forEach(function(filterEntry) {
            Array.prototype.push.apply(filteredSeries, series.filter(function(datum) {
                return datum.page === filterEntry.page && datum.jobGroup === filterEntry.jobGroup;
            }));
        });
        return filteredSeries;
    };

    var getActualSvgWidth = function() {
        return svg.node().getBoundingClientRect().width;
    };

    var calculateChartBarsHeight = function () {
        var barBand = OpenSpeedMonitor.ChartComponents.ChartBars.BarBand;
        var barGap = OpenSpeedMonitor.ChartComponents.ChartBars.BarGap;
        var numberOfMeasurands = Object.keys(allMeasurandDataMap).length;
        var numberOfPages = d3.max(Object.values(allMeasurandDataMap).map(function(d) { return d.series.length; }));
        var numberOfBars = numberOfPages * (stackBars ? 1 : numberOfMeasurands);
        var gapSize = barGap * ((stackBars || numberOfMeasurands < 2) ? 1 : 2);
        return ((numberOfPages - 1) * gapSize) + numberOfBars * barBand;
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
            max: measurandGroupDataMap["LOAD_TIMES"] ? measurandGroupDataMap["LOAD_TIMES"].max : 0
        };
    };

    var getDataForLegend = function () {
        return {
            entries: Object.values(allMeasurandDataMap).map(function (measurandData) {
                return {
                    id: measurandData.id,
                    color: measurandData.color,
                    label: measurandData.label
                };
            }),
            width: chartBarsWidth
        };
    };

    var getDataForSideLabels = function () {
        return {
            height: chartBarsHeight,
            labels: sideLabelData
        };
    };

    var getAllMeasurands = function () {
        return Object.keys(allMeasurandDataMap);
    };

    var getDataForBars = function (measurand) {
        var measurandData = allMeasurandDataMap[measurand];
        return {
            id: measurandData.id,
            values: measurandData.series,
            color: measurandData.color,
            min: measurandGroupDataMap[measurandData.measurandGroup].min > 0 ? 0 : measurandGroupDataMap[measurandData.measurandGroup].min,
            max: measurandGroupDataMap[measurandData.measurandGroup].max,
            height: chartBarsHeight,
            width: chartBarsWidth
        }
    };

    var hasLoadTimes = function () {
        return !!measurandGroupDataMap["LOAD_TIMES"];
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

    return {
        setData: setData,
        getDataForHeader: getDataForHeader,
        getDataForBarScore: getDataForBarScore,
        getDataForLegend: getDataForLegend,
        getDataForSideLabels: getDataForSideLabels,
        getAllMeasurands: getAllMeasurands,
        getDataForBars: getDataForBars,
        hasLoadTimes: hasLoadTimes,
        needsAutoResize: needsAutoResize,
        getChartBarsHeight: getChartBarsHeight,
        getChartSideLabelsWidth: getChartSideLabelsWidth
    }
});
OpenSpeedMonitor.ChartModules.PageAggregationData.ComponentMargin = 15;
