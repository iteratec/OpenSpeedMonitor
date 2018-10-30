//= require /node_modules/d3/d3.min.js
"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.JobGroupAggregationData = (function (svgSelection) {
    var svg = svgSelection;
    var sideLabelData = [];
    var rawSeries = [];
    var orderedSeries = [];
    var activeFilter = "desc";
    var headerText = "";
    var i18n = {};
    var chartSideLabelsWidth = 200;
    var chartBarsWidth = 700;
    var chartBarsHeight = 400;
    var fullWidth = chartSideLabelsWidth + chartBarsWidth;
    var dataAvailable = false;
    var labelList = [];
    var aggregationValue = "avg";

    var setData = function (data) {
        aggregationValue = data.aggregationValue !== undefined ? data.aggregationValue : aggregationValue;
        transformAndMergeData(data);
        activeFilter = data.activeFilter || activeFilter;
        orderedSeries = orderData(rawSeries) || orderedSeries;
        i18n = data.i18nMap || i18n;
        labelList = !orderedSeries ? labelList : [];
        if (orderedSeries.groupData) {
            orderedSeries.groupData.forEach(function (element) {
                labelList.push({
                    jobGroup: element.jobGroup,
                    measurand: rawSeries.measurand
                });
            });
        }
        var chartLabelUtils = OpenSpeedMonitor.ChartModules.ChartLabelUtil(labelList, data.i18nMap);
        headerText = chartLabelUtils.getCommonLabelParts(false);
        headerText += headerText ? ' - '+getAggregationValueLabel() : getAggregationValueLabel();
        sideLabelData = chartLabelUtils.getSeriesWithShortestUniqueLabels(true).map(function (s) {
            return s.label;
        });
        fullWidth = getActualSvgWidth();
        chartSideLabelsWidth = d3.max(OpenSpeedMonitor.ChartComponents.utility.getTextWidths(svg, sideLabelData));
        chartBarsWidth = fullWidth - 2*OpenSpeedMonitor.ChartComponents.common.ComponentMargin - chartSideLabelsWidth;
        chartBarsHeight = calculateChartBarsHeight();
        dataAvailable = rawSeries.groupData ? true : dataAvailable;
    };

    var resetData = function () {
        rawSeries = []
    };

    var getDataForHeader = function () {
        return {
            width: fullWidth,
            text: headerText
        };
    };

    var addAggregationToSeriesEntry = function(jobGroup, aggregationValue, value, valueComparative) {
        rawSeries.groupData.forEach(function (it) {
            if(it.jobGroup === jobGroup) {
                it[aggregationValue] = value;
                if (valueComparative) {it[aggregationValue+'Comparative'] = valueComparative}
            }
        })
    };

    var transformAndMergeData = function(data) {
        if(data.groupData && !rawSeries.groupData) {
            rawSeries = data || rawSeries;
            rawSeries.groupData.forEach(function(it){
                it[data.groupData[0].aggregationValue] = it.value;
                delete it.value;
                if(data.hasComparativeData) {
                    it[data.groupData[0].aggregationValue+'Comparative'] = it.valueComparative;
                    delete it.valueComparative;
                }
            })
        }
        if(data.groupData && rawSeries && !rawSeries.groupData[0].hasOwnProperty(data.groupData[0].aggregationValue)) {
            data.groupData.forEach(function(it){
                if(data.hasComparativeData) {
                    addAggregationToSeriesEntry(it.jobGroup, data.groupData[0].aggregationValue, it.value, it.valueComparative);
                } else {
                    addAggregationToSeriesEntry(it.jobGroup, data.groupData[0].aggregationValue, it.value);
                }
            })
        }
    };

    var getAggregationValueLabel = function () {
        if (aggregationValue === 'avg') {
            return 'Average'
        }
        else if (aggregationValue === 'median' || aggregationValue === '50'){
            return 'Percentile: 50%'
        }
        else {
            return "Percentile: " + aggregationValue + "%"
        }
    };

    var getDataForBarScore = function () {
        var minVal = 0;
        var maxVal = 0;
        if (rawSeries.measurandGroup === "LOAD_TIMES") {
            var extent = d3.extent(rawSeries.groupData, function (entry) {
                return entry[aggregationValue];
            });
            minVal = Math.min(extent[0], 0);
            maxVal = Math.max(extent[1], 0);
        }
        return {
            width: chartBarsWidth,
            min: minVal,
            max: maxVal
        };
    };

    var getDataForSideLabels = function () {
        return {
            height: chartBarsHeight,
            labels: sideLabelData
        };
    };

    var getDataForBars = function () {
        if (rawSeries.groupData) {
            var extent = d3.extent(rawSeries.groupData, function (entry) {
                return entry[aggregationValue];
            });
            return {
                values: getMappedValues(rawSeries),
                min: Math.min(extent[0], 0),
                max: Math.max(extent[1], 0),
                height: chartBarsHeight,
                width: chartBarsWidth
            }
        } else return [];
    };

    var orderData = function (unorderedSeries) {
        var compareFunction = (activeFilter === "asc") ? d3.ascending : d3.descending;
        if (unorderedSeries.groupData) {
            unorderedSeries.groupData.sort(function (a, b) {
                return compareFunction(a[aggregationValue], b[aggregationValue]);
            })
        }
        return unorderedSeries;
    };

    var getMappedValues = function (rawValues) {
        var mappedValues = [];
        rawValues.groupData.forEach(function (series) {
            var mappedSeries = {};
            mappedSeries.id = series.jobGroup;
            mappedSeries.value = series[aggregationValue];
            mappedSeries.unit = rawValues.unit;
            mappedValues.push(mappedSeries);
        });
        return mappedValues;
    };

    var getActualSvgWidth = function () {
        return svg.node().getBoundingClientRect().width;
    };

    var hasLoadTimes = function () {
        return rawSeries.measurandGroup === "LOAD_TIMES";
    };

    var getChartBarsHeight = function () {
        return chartBarsHeight;
    };

    var getChartSideLabelsWidth = function () {
        return chartSideLabelsWidth;
    };

    var calculateChartBarsHeight = function () {
        var barBand = OpenSpeedMonitor.ChartComponents.common.barBand;
        var barGap = OpenSpeedMonitor.ChartComponents.common.barGap;
        var numberOfBars = rawSeries.groupData ? rawSeries.groupData.length : 0;
        return ((numberOfBars - 1) * barGap) + numberOfBars * barBand;
    };

    var isDataAvailable = function () {
        return dataAvailable;
    };

    return {
        setData: setData,
        resetData: resetData,
        getDataForHeader: getDataForHeader,
        getDataForBarScore: getDataForBarScore,
        getDataForSideLabels: getDataForSideLabels,
        isDataAvailable: isDataAvailable,
        getDataForBars: getDataForBars,
        hasLoadTimes: hasLoadTimes,
        getChartBarsHeight: getChartBarsHeight,
        getChartSideLabelsWidth: getChartSideLabelsWidth
    }
});
