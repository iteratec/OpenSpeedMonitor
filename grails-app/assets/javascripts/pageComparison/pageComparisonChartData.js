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
    var aggregationValue = "avg";
    var max = 0;
    var i18n = {};

    var setData = function (data) {
        transformAndMergeData(data);
        aggregationValue = data.aggregationValue !== undefined ? data.aggregationValue : aggregationValue;
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
        // chartSideLabelsWidth = d3.max(OpenSpeedMonitor.ChartComponents.utility.getTextWidths(svg, sideLabelData));
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

    var addAggregationToSeriesEntry = function(jobGroup, page, measurand, aggregationValue, value, valueComparative) {
        rawSeries.forEach(function (it) {
            if(it.jobGroup === jobGroup && it.page === page && it.measurand === measurand) {
                it[aggregationValue] = value;
                if (valueComparative) {it[aggregationValue+'Comparative'] = valueComparative}
            }
        })
    };

    var transformAndMergeData = function(data) {
        if(data.series && !rawSeries.length > 0) {
            rawSeries = data.series || rawSeries;
            rawSeries.forEach(function(it){
                it[data.series[0].aggregationValue] = it.value;
                delete it.value;
                if(data.hasComparativeData) {
                    it[data.series[0].aggregationValue+'Comparative'] = it.valueComparative;
                    delete it.valueComparative;
                }
            })
        }
        if(data.series && rawSeries && !rawSeries[0].hasOwnProperty(data.series[0].aggregationValue)) {
            data.series.forEach(function(it){
                if(data.hasComparativeData) {
                    addAggregationToSeriesEntry(it.jobGroup, it.page, it.measurand, data.series[0].aggregationValue, it.value, it.valueComparative);
                } else {
                    addAggregationToSeriesEntry(it.jobGroup, it.page, it.measurand, data.series[0].aggregationValue, it.value);
                }
            })
        }
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
                    showLabelOnTop: true,
                    value: dataElement[aggregationValue],
                    unit: series.dimensionalUnit
                };
                if(series.dimensionalUnit === "ms") hasLoadTime = true;
                allPageData[index].push(add);
                if(dataElement[aggregationValue] > newMax) newMax = dataElement[aggregationValue];
                ++index;
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
        var colorProvider = OpenSpeedMonitor.ChartColorProvider().getColorscaleForMeasurandGroup("ms");
        var series = allPageData[firstOrSecond];
        return {
            id: "page"+firstOrSecond,
            color: colorProvider(firstOrSecond),
            values: series,
            min: 0,
            max: max,
            width: chartBarsWidth,
            height: chartBarsHeight
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
        hasLoadTimes: hasLoadTimes
    }
});
OpenSpeedMonitor.ChartModules.PageComparisonData.ComponentMargin = 15;
