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
    var allMeasurandDataMap = {};
    var measurandGroupDataMap = {};
    var sideLabelData = [];
    var dataOrder = [];
    var filterRules = [];
    var rawSeries = [];
    var activeFilter = "desc";
    var headerText = "";
    var stackBars = false;
    var autoWidth = true;
    var i18n = {};

    var setData = function (data) {
        rawSeries = data.series || rawSeries;
        filterRules = data.filterRules || filterRules;
        activeFilter = data.activeFilter || validateActiveFilter(activeFilter);
        i18n = data.i18nMap || i18n;
        if (data.series || data.filterRules || data.activeFilter) {
            var filteredSeries = filterSeries(rawSeries);
            Array.prototype.push.apply(filteredSeries, extractComparativeValuesAsSeries(filteredSeries))
            allMeasurandDataMap = extractMeasurandData(filteredSeries);
            measurandGroupDataMap = extractMeasurandGroupData(filteredSeries);
            dataOrder = createDataOrder();
            var chartLabelUtils = OpenSpeedMonitor.ChartModules.ChartLabelUtil(dataOrder, data.i18nMap);
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

    var validateActiveFilter = function(activeFilter) {
        return (activeFilter === "asc" || activeFilter === "desc" || filterRules[activeFilter]) ? activeFilter : "desc";
    };

    var extractComparativeValuesAsSeries = function(series) {
        var comparativeSeries = [];
        series.forEach(function(datum) {
            if (!datum.valueComparative) {
                return;
            }
            var difference = datum.value - datum.valueComparative;
            var isImprovement = difference < 0 && datum.measurandGroup !== "PERCENTAGES";
            var measurandSuffix = isImprovement ? "improvement" : "deterioration";
            var label = isImprovement ? (i18n.comparativeImprovement || "improvement") : (i18n.comparativeDeterioration || "deterioration");
            comparativeSeries.push({
                jobGroup: datum.jobGroup,
                page: datum.page,
                unit: datum.unit,
                measurandGroup: datum.measurandGroup,
                measurand: datum.measurand + "_" + measurandSuffix,
                measurandLabel: label,
                value: difference,
                isImprovement: isImprovement,
                isDeterioration: !isImprovement
            });
        });
        return comparativeSeries;
    };

    var extractMeasurandData = function (series) {
        var measurandDataMap = d3.nest()
            .key(function(d) { return d.measurand; })
            .rollup(function (seriesOfMeasurand) {
                var firstValue = seriesOfMeasurand[0];
                var unit = firstValue.unit;
                seriesOfMeasurand.forEach(function(value) {
                    value.id = createSeriesValueId(value);
                });
                return {
                    id: firstValue.measurand,
                    label: firstValue.measurandLabel,
                    measurandGroup: firstValue.measurandGroup,
                    isImprovement: firstValue.isImprovement,
                    isDeterioration: firstValue.isDeterioration,
                    unit: unit,
                    series: seriesOfMeasurand
                };
            }).map(series);
        return applyColorsToMeasurandData(measurandDataMap);
    };

    var applyColorsToMeasurandData = function (measurandDataMap) {
        var colorProvider = OpenSpeedMonitor.ChartColorProvider();
        var colorScales = {};
        var measurands = sortByMeasurandOrder(Object.keys(measurandDataMap));
        measurands.forEach(function(measurand) {
            var measurandData = measurandDataMap[measurand];
            if (measurandData.isImprovement || measurandData.isDeterioration) {
                measurandData.color = colorProvider.getColorscaleForTrafficlight()(measurandData.isImprovement ? "good" : "bad");
            } else {
                var unit = measurandData.unit;
                colorScales[unit] = colorScales[unit] || colorProvider.getColorscaleForMeasurandGroup(unit);
                measurandData.color = colorScales[unit](measurand)
            }
        });
        return measurandDataMap;
    };

    var sortByMeasurandOrder = function (measurandList) {
        var measurandOrder = OpenSpeedMonitor.ChartModules.PageAggregationData.MeasurandOrder;
        measurandList.sort(function(a, b) {
            var idxA = measurandOrder.indexOf(a);
            var idxB = measurandOrder.indexOf(b);
            if (idxA < 0) {
                return (idxB < 0) ? 0 : 1;
            }
            return (idxB < 0) ? -1 : (idxA - idxB);
        });
        return measurandList;
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

    var createDataOrder = function () {
        var filter = (activeFilter === "asc" || activeFilter === "desc") ? createSortFilter(activeFilter) : filterRules[activeFilter];
        return filter.map(function (datum) {
            return {
                page: datum.page,
                jobGroup: datum.jobGroup,
                id: createSeriesValueId(datum)
            };
        });
    };

    var filterSeries = function (series) {
        if (activeFilter === "asc" || activeFilter === "desc") {
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

    var createSortFilter = function (ascOrdDesc) {
        var seriesForSorting = getMeasurandDataForSorting().series.slice();
        var compareFunction = (ascOrdDesc === "asc") ? d3.ascending : d3.descending;
        seriesForSorting.sort(function(a, b) {
            return compareFunction(a.value, b.value);
        });
        var longestExistingSeries = Object.values(allMeasurandDataMap).reduce(function(curFilter, measurandData) {
            return (measurandData.series.length > curFilter.length) ? measurandData.series : curFilter;
        }, []);
        if (seriesForSorting.length < longestExistingSeries.length) {
            addMissingValuesToSeries(seriesForSorting, longestExistingSeries);
        }
        return seriesForSorting.map(function (value) {
            return {
                page: value.page,
                jobGroup: value.jobGroup,
                id: value.id
            }
        });
    };

    var addMissingValuesToSeries = function(series, allValues) {
        var existingIds = {};
        series.forEach(function (value) {
            existingIds[value.id] = true;
        });
        Array.prototype.push.apply(series, allValues.filter(function(value) {
            return !existingIds[value.id];
        }));
        return series;
    };

    var getMeasurandDataForSorting = function () {
        var measurandOrder = OpenSpeedMonitor.ChartModules.PageAggregationData.MeasurandOrder;
        for (var i = 0; i < measurandOrder.length; i++) {
            var curMeasurandData = allMeasurandDataMap[measurandOrder[i]];
            if (curMeasurandData) {
                return curMeasurandData;
            }
        }
        return Object.values(curMeasurandData)[0];
    };

    var getActualSvgWidth = function() {
        return svg.node().getBoundingClientRect().width;
    };

    var calculateChartBarsHeight = function () {
        var barBand = OpenSpeedMonitor.ChartComponents.ChartBars.BarBand;
        var barGap = OpenSpeedMonitor.ChartComponents.ChartBars.BarGap;
        var numberOfMeasurands = Object.keys(allMeasurandDataMap).length;
        var numberOfBars = dataOrder.length * (stackBars ? 1 : numberOfMeasurands);
        var gapSize = barGap * ((stackBars || numberOfMeasurands < 2) ? 1 : 2);
        return ((dataOrder.length - 1) * gapSize) + numberOfBars * barBand;
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
            min: measurandGroupDataMap["LOAD_TIMES"] ? Math.min(measurandGroupDataMap["LOAD_TIMES"].min, 0) : 0,
            max: measurandGroupDataMap["LOAD_TIMES"] ? Math.max(measurandGroupDataMap["LOAD_TIMES"].max, 0) : 0
        };
    };

    var getDataForLegend = function () {
        return {
            entries: sortByMeasurandOrder(Object.keys(allMeasurandDataMap)).map(function (measurand) {
                var measurandData = allMeasurandDataMap[measurand];
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
            values: getSortedValuesForBars(measurandData.series),
            color: measurandData.color,
            min: Math.min(measurandGroupDataMap[measurandData.measurandGroup].min, 0),
            max: Math.max(measurandGroupDataMap[measurandData.measurandGroup].max, 0),
            height: chartBarsHeight,
            width: chartBarsWidth,
            forceSignInLabel: measurand.isDeterioration
        }
    };

    var getSortedValuesForBars = function (series) {
        var seriesMap = {};
        series.forEach(function (value) {
            seriesMap[value.id] = value;
        });
        return dataOrder.map(function (filterEntry) {
            return seriesMap[filterEntry.id] || {
                page: filterEntry.page,
                jobGroup: filterEntry.jobGroup,
                id: filterEntry.id,
                value: null
            }
        });
    };

    var createSeriesValueId = function(value) {
        return value.page + ";" + value.jobGroup;
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

    var hasStackedBars = function () {
        return stackBars;
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
        getChartSideLabelsWidth: getChartSideLabelsWidth,
        hasStackedBars: hasStackedBars,
        sortByMeasurandOrder: sortByMeasurandOrder
    }
});
OpenSpeedMonitor.ChartModules.PageAggregationData.ComponentMargin = 15;
OpenSpeedMonitor.ChartModules.PageAggregationData.MeasurandOrder = [
    "CS_BY_WPT_VISUALLY_COMPLETE",
    "CS_BY_WPT_DOC_COMPLETE",
    "FULLY_LOADED_INCOMING_BYTES",
    "DOC_COMPLETE_INCOMING_BYTES",
    "FULLY_LOADED_REQUEST_COUNT",
    "DOC_COMPLETE_REQUESTS",
    "FULLY_LOADED_TIME",
    "VISUALLY_COMPLETE",
    "VISUALLY_COMPLETE_99",
    "VISUALLY_COMPLETE_95",
    "VISUALLY_COMPLETE_90",
    "VISUALLY_COMPLETE_85",
    "CONSISTENTLY_INTERACTIVE",
    "FIRST_INTERACTIVE",
    "SPEED_INDEX",
    "DOC_COMPLETE_TIME",
    "LOAD_TIME",
    "START_RENDER",
    "DOM_TIME",
    "FIRST_BYTE"
];
