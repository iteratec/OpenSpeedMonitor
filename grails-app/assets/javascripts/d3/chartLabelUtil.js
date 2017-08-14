/**
 * Created by nkuhn on 22.02.17.
 */

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.ChartLabelUtil = function (series, i18nMap) {
    var groupingDelimiter = " | ";
    var delimiter = ", ";

    var seriesData;
    var i18nData;
    var uniquePages = [];
    var uniqueJobGroups = [];
    var uniqueMeasurands = [];

    var init = function () {
        seriesData = series;
        deduceUniqueEntries();
        i18nData = i18nMap || {};
    };

    var deduceUniqueEntries = function () {
        seriesData.forEach(function (series) {
            if (series.grouping && !series.page && !series.jobGroup) {
                var splittedIdentifier = series.grouping.split(groupingDelimiter);
                series.page = splittedIdentifier[0];
                series.jobGroup = splittedIdentifier[1];
            }

            if (series.page && uniquePages.indexOf(series.page) === -1) {
                uniquePages.push(series.page);
            }
            if (series.jobGroup && uniqueJobGroups.indexOf(series.jobGroup) === -1) {
                uniqueJobGroups.push(series.jobGroup);
            }
            if (series.measurand && uniqueMeasurands.indexOf(series.measurand) === -1) {
                uniqueMeasurands.push(series.measurand);
            }

        });

    };

    var setLabelInSeriesData = function (omitMeasurands) {
        seriesData.forEach(function (series) {
            var labelParts = [];
            if (uniquePages.length > 1) {
                labelParts.push(series.page);
            }
            if (uniqueJobGroups.length > 1) {
                labelParts.push(series.jobGroup);
            }
            if (!omitMeasurands && uniqueMeasurands.length > 1) {
                labelParts.push(series.measurand);
            }
            series.label = labelParts.join(delimiter);
        });

    };

    var getCommonLabelParts = function (omitMeasurands) {
        var commonPartsHeader = [];
        if (uniqueJobGroups.length === 1) {
            commonPartsHeader.push(uniqueJobGroups[0]);
        }
        if (uniquePages.length === 1) {
            commonPartsHeader.push(uniquePages[0]);
        }
        if (uniqueMeasurands.length === 1 && !omitMeasurands) {
            commonPartsHeader.push(uniqueMeasurands[0]);
        }
        return commonPartsHeader.join(delimiter);
    };

    var getSeriesWithShortestUniqueLabels = function () {
        setLabelInSeriesData(seriesData);
        return seriesData;
    };

    init();

    return {
        getSeriesWithShortestUniqueLabels: getSeriesWithShortestUniqueLabels,
        getCommonLabelParts: getCommonLabelParts
    }

};
