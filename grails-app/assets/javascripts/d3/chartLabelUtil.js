/**
 * Created by nkuhn on 22.02.17.
 */

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.SeriesLabels = function (series) {

  var identifierDelimitter = " | ";

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

      var splittedIdentifier = series.grouping.split(identifierDelimitter);
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
      if (uniqueEntries.uniquePages.length > 1) { series.label += series.page }
      if (uniqueEntries.uniqueJobGroups.length > 1) { series.label += series.jobGroup }
      if (uniqueEntries.uniqueMeasurands.length > 1) { series.label += series.measurand }
    });

  };

  var getCommonLabelParts = function () {
    var commonPartsHeader = ""
    if (uniqueEntries.uniqueMeasurands.length == 1) { commonPartsHeader += "Measurand: " + uniqueEntries.uniqueMeasurands[0] + identifierDelimitter }
    if (uniqueEntries.uniquePages.length == 1) { commonPartsHeader += "Page: " + uniqueEntries.uniquePages[0] + identifierDelimitter }
    if (uniqueEntries.uniqueJobGroups.length == 1) { commonPartsHeader += "Job Group: " + uniqueEntries.uniqueJobGroups[0] + identifierDelimitter }
    if (commonPartsHeader.indexOf(identifierDelimitter) !== -1) {
      commonPartsHeader = commonPartsHeader.substr(0, commonPartsHeader.length - identifierDelimitter.length);
    }
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