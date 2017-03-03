/**
 * Created by nkuhn on 22.02.17.
 */

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.ChartLabelUtil = function (series) {

  var delimitter = " | ";

  var seriesData,
    uniqueEntries;

  var  init = function () {
    seriesData = series;
    deduceUniqueEntries();
  };

  var deduceUniqueEntries = function(){

    uniqueEntries = {
      uniquePages: [],
      uniqueJobGroups: [],
      uniqueMeasurands: []
    };

    seriesData.forEach(function(series){

      var splittedIdentifier = series.grouping.split(delimitter);
      series.page = splittedIdentifier[0];
      series.jobGroup = splittedIdentifier[1];

      if (uniqueEntries.uniquePages.indexOf(series.page) == -1) {
        uniqueEntries.uniquePages.push(series.page);
      }
      if (uniqueEntries.uniqueJobGroups.indexOf(series.jobGroup) == -1) {
        uniqueEntries.uniqueJobGroups.push(series.jobGroup);
      }
      if (uniqueEntries.uniqueMeasurands.indexOf(series.measurand) == -1) {
        uniqueEntries.uniqueMeasurands.push(series.measurand);
      }

    });

  };

  var appendUniqueLabels = function(){

    seriesData.forEach(function(series){
      series.label = "";
      if (uniqueEntries.uniquePages.length > 1) { series.label += series.page + delimitter }
      if (uniqueEntries.uniqueJobGroups.length > 1) { series.label += series.jobGroup + delimitter }
      if (uniqueEntries.uniqueMeasurands.length > 1) { series.label += series.measurand + delimitter }
      series.label = cutTrailingDelimitter(series.label)
    });

  };

  var cutTrailingDelimitter = function (toCutFrom) {
    if (toCutFrom.indexOf(delimitter) !== -1) {
      toCutFrom = toCutFrom.substr(0, toCutFrom.length - delimitter.length);
    }
    return toCutFrom;
  };
  var getCommonLabelParts = function () {
    var commonPartsHeader = ""
    if (uniqueEntries.uniqueMeasurands.length == 1) { commonPartsHeader += "Measurand: " + uniqueEntries.uniqueMeasurands[0] + delimitter }
    if (uniqueEntries.uniquePages.length == 1) { commonPartsHeader += "Page: " + uniqueEntries.uniquePages[0] + delimitter }
    if (uniqueEntries.uniqueJobGroups.length == 1) { commonPartsHeader += "Job Group: " + uniqueEntries.uniqueJobGroups[0] + delimitter }
    commonPartsHeader = cutTrailingDelimitter(commonPartsHeader);
    return commonPartsHeader;
  };

  var getSeriesWithShortestUniqueLabels = function(){
    appendUniqueLabels(seriesData);
    return seriesData;
  };

  init();

  return {
    getSeriesWithShortestUniqueLabels: getSeriesWithShortestUniqueLabels,
    getCommonLabelParts: getCommonLabelParts,
  }

};