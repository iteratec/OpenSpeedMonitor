/**
 * Created by nkuhn on 22.02.17.
 */

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.LabelUtils = (function () {

  var identifierDelimitter = " | ";

  var appendLabelAndHeader = function(seriesData){

    var uniqueEntries = getUniqueEntries(seriesData);

    seriesData.forEach(function(series){
      series.label = "";
      if (uniqueEntries.uniquePages.length > 1) { series.label += series.page }
      if (uniqueEntries.uniqueJobGroups.length > 1) { series.label += series.jobGroup }
      if (uniqueEntries.uniqueMeasurands.length > 1) { series.label += series.measurand }
    });

    return seriesData;

  };

  var getUniqueEntries = function(seriesData){

    var uniqueEntries = {
      uniquePages: [],
      uniqueJobGroups: [],
      uniqueMeasurands: []
    };

    seriesData.forEach(function(series){

      var splittedIdentifier = series.identifier.split(identifierDelimitter);
      series.page = splittedIdentifier[0];
      series.jobGroup = splittedIdentifier[1];
      series.measurand = splittedIdentifier[2];

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

    return uniqueEntries;

  };

  var buildShortestUniqueLabel = function(seriesData){
    return appendLabelAndHeader(seriesData);
  };

  return {
    buildShortestUniqueLabel: buildShortestUniqueLabel
  }

})();