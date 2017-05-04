//= require _timeRangePicker.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectIntervalTimeframeCard = (function () {

  var cardElement = $("#select-interval-timeframe-card");
  var timeFrameSelectElement = $("#timeframeSelect");
  var intervalSelectElement = $("#selectedIntervalHtmlId");
  var timeFramePickerElement = $("#timeframe-picker");
  var timeFramePicker = null;
  var comparativeTimeFramePickerContainer = $("#timeframe-picker-previous-container");
  var comparativeTimeFramePickerElement = $("#timeframe-picker-previous");
  var comparativeTimeFramePicker = null;

  var clientStorage = OpenSpeedMonitor.clientSideStorageUtils();
  var clientStorageTimeFramePresetKey = "de.iteratec.osm.result.dashboard.timeframeselection";
  var clientStorageIntervalKey = "de.iteratec.osm.result.dashboard.intervalselection";
  var clientStorageTimeFrameFromKey = 'de.iteratec.osm.result.dashboard.from';
  var clientStorageTimeFrameToKey = 'de.iteratec.osm.result.dashboard.to';
  var manualTimeFrameSelection = "0"; // manual selection
  var defaultIntervalSelection = "-1"; // Raw data
  var defaultTimeFramePreselect = (3 * 24 * 60 * 60).toString(); // 3 days
  var isSavedDashboard = OpenSpeedMonitor.urlUtils.getVar("dashboardID") !== undefined;



  var init = function () {
    // initialize controls with values. Either from presets, from URL, from local storage or defaults
    intervalSelectElement.val(defaultValueForInterval());
    var timeFramePreselection = defaultValueForTimeFramePreselection();
    timeFrameSelectElement.val(timeFramePreselection);
    timeFramePicker = OpenSpeedMonitor.timeRangePicker(timeFramePickerElement);
    if (comparativeEnabled()) {
      comparativeTimeFramePicker = OpenSpeedMonitor.timeRangePicker(comparativeTimeFramePickerElement);
    }

    setTimeFramePreselection(timeFramePreselection, true);

    registerEvents();
    triggerTimeFrameChanged(); // initial event
  };

  var defaultValueForInterval = function () {
    if (isSavedDashboard) {
      return intervalSelectElement.val();
    }
    return OpenSpeedMonitor.urlUtils.getVar("selectedInterval") ||
      clientStorage.getFromLocalStorage(clientStorageIntervalKey) ||
      defaultIntervalSelection;
  };

  var defaultValueForStart = function () {
    if (isSavedDashboard) {
            return timeFramePicker.getStart().toISOString();
    }
    return OpenSpeedMonitor.urlUtils.getVar("from") ||
      clientStorage.getFromLocalStorage(clientStorageTimeFrameFromKey);
  };

  var defaultValueForEnd = function () {
    if (isSavedDashboard) {
            return timeFramePicker.getEnd().toISOString();
    }
    return OpenSpeedMonitor.urlUtils.getVar("to") ||
      clientStorage.getFromLocalStorage(clientStorageTimeFrameToKey);
  };

  var defaultValueForTimeFramePreselection = function () {
    if (isSavedDashboard) {
      return timeFrameSelectElement.val();
    }
    return OpenSpeedMonitor.urlUtils.getVar("selectedTimeFrameInterval") ||
      clientStorage.getFromLocalStorage(clientStorageTimeFramePresetKey) ||
      defaultTimeFramePreselect;
  };

  var registerEvents = function () {
    intervalSelectElement.on("change", function () {
      clientStorage.setToLocalStorage(clientStorageIntervalKey, this.value);
    });

    timeFrameSelectElement.on("change", function () {
      clientStorage.setToLocalStorage(clientStorageTimeFramePresetKey, this.value);
      setTimeFramePreselection(this.value);
    });

    timeFramePickerElement.on("rangeChanged", function (ev, from, to) {
      clientStorage.setToLocalStorage(clientStorageTimeFrameFromKey, from.toISOString());
      clientStorage.setToLocalStorage(clientStorageTimeFrameToKey, to.toISOString());
      clientStorage.setToLocalStorage(clientStorageTimeFramePresetKey, manualTimeFrameSelection);
      timeFrameSelectElement.val(manualTimeFrameSelection);
      triggerTimeFrameChanged();
    });
    if (comparativeEnabled()){
      registerComparativeEvents();
    }

  };

  var registerComparativeEvents = function () {
    $("#addComparativeTimeFrame").on("click", function (ev) {
      $("#comparativeTimeFrameButton").toggleClass("hidden");
      $(".comparison-initially-hidden").toggleClass("hidden");
      $('#timeframe-picker').toggleClass("col-md-offset-4");
    });
    $("#removeComparativeTimeFrame").on("click", function (ev) {
      $("#comparativeTimeFrameButton").toggleClass("hidden");
        $(".comparison-initially-hidden").toggleClass("hidden");
      $('#timeframe-picker').toggleClass("col-md-offset-4");
    });
  };

  var setTimeFramePreselection = function (timeFrameInSecs, suppressEvent) {
    timeFrameInSecs = parseInt(timeFrameInSecs);

    var from;
    var to;

    if (isNaN(timeFrameInSecs) || timeFrameInSecs <= 0) { // manual time selection
      from = defaultValueForStart();
      to = defaultValueForEnd();
    }else {
      to = new Date();
      from = new Date(to.getTime() - (timeFrameInSecs * 1000));
      var oneDayInSecs = 24 * 60 * 60;
      if (timeFrameInSecs > oneDayInSecs) {
        to.setHours(23, 59, 59, 999);
        from.setHours(0, 0, 0, 0);
      }
    }

    timeFramePicker.setRange(from, to);
    if (comparativeEnabled()){
      setComparativeTimeFramePreselection();
    }

    if (!suppressEvent) {
      cardElement.trigger("timeFrameChanged", [getTimeFrame()]);
    }

  };
  var setComparativeTimeFramePreselection = function () {
      var mainTimeFrame = getTimeFrame();
      var interval = mainTimeFrame[1] - mainTimeFrame[0];
      var toComparative = mainTimeFrame[0];
      var fromComparative = new Date(toComparative - interval);
      comparativeTimeFramePicker.setRange(fromComparative, toComparative);
  };

  var getTimeFrame = function () {
    return timeFramePicker.getRange();
  };

  var getComparativeTimeFrame = function () {
    return comparativeEnabled() && !comparativeTimeFramePickerContainer.hasClass("hidden") ? comparativeTimeFramePicker.getRange() : null;
  };

  var triggerTimeFrameChanged = function () {
    cardElement.trigger("timeFrameChanged", [getTimeFrame()]);
  };

  var comparativeEnabled = function () {
    return cardElement.data("comparativeEnabled");
  };

  init();
  return {
    getTimeFrame: getTimeFrame,
    getComparativeTimeFrame: getComparativeTimeFrame
  }

})();
