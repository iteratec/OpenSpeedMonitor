"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.UrlHandling = OpenSpeedMonitor.ChartModules.UrlHandling || {};

OpenSpeedMonitor.ChartModules.UrlHandling.PageAggregation = (function () {

  var getUrlParameter = function () {
    var vars = [], currentParam;
    var hashIndex = window.location.href.indexOf("#");
    var toIndex;
    if(hashIndex>0){
      toIndex = hashIndex
    } else{
      toIndex = window.location.href.length;
    }
    var params = window.location.href.slice(window.location.href.indexOf('?') + 1, toIndex).split('&');
    for (var i = 0; i < params.length; i++) {
      currentParam = params[i].split('=');
      var currentValue = vars[currentParam[0]];
      if (currentValue == null) {
        vars.push(currentParam[0]);
        vars[currentParam[0]] = currentParam[1];
      } else if (currentValue.constructor === Array) {
        vars[currentParam[0]].push(currentParam[1]);
      } else {
        vars[currentParam[0]] = [vars[currentParam[0]], currentParam[1]]
      }
    }
    return vars;
  };

  var getTimeFrame = function (map) {
    map["from"] = $("#fromDatepicker").val();
    map["fromHour"] = $("#startDateTimePicker").find(".input-group.bootstrap-timepicker.time-control").find(".form-control").val();
    map["to"] = $("#toDatepicker").val();
    map["toHour"] = $("#endDateTimePicker").find(".input-group.bootstrap-timepicker.time-control").find(".form-control").val()
  };

  var getJobGroup = function (map) {
    map["selectedFolder"] = $("#folderSelectHtmlId").val();
  };

  var getPage = function (map) {
    map["selectedPages"] = $("#pageSelectHtmlId").val();
  };

  var getMeasurands = function (map) {
    var measurands = [];
    var measurandObjects = $('.measurandSeries');
    $.each(measurandObjects, function (_,currentSeries) {
      var currentMeasurands = [$(currentSeries).find(".firstMeasurandSelect").val()];
      $(currentSeries).find(".additionalMeasurand").each(function(_,additionalMeasurand){
        currentMeasurands.push($(additionalMeasurand).val());
      });
      var json = JSON.stringify({"stacked":$(currentSeries).find(".stackedSelect").val(),"values":currentMeasurands});
      measurands.push(json);
    });
    map['measurand'] = measurands
  };

  var addHandler = function () {
    $('#folderSelectHtmlId').on('change', updateUrl);
    $('#pageSelectHtmlId').on('change', updateUrl);
    $('#timeframeSelect').on('change', updateUrl);
    $('#select-interval-timeframe-card').find('.form-control').on('change',updateUrl);
    $(".firstMeasurandSelect").on('change', updateUrl);
    $(".additionalMeasurand").on('change', updateUrl);
    $(".stackedSelect").on('change', updateUrl);
    $("#addMeasurandSeriesButton").on('click', updateUrl);
    $(".removeMeasurandSeriesButton").on('click', updateUrl);
    $(".removeMeasurandButton").on('click', updateUrl);
    $(".addMeasurandButton").on('click', updateUrl);
  };

  var updateUrl = function () {
    var map = {};
    getTimeFrame(map);
    getJobGroup(map);
    getPage(map);
    getMeasurands(map);
    var path = "show?"+$.param(map,true);
    window.history.pushState("object or string", "Title", path);
  };

  var setSelections = function () {
    var params = getUrlParameter();
    setJobGroups(params);
    setPages(params);
    setMeasurands(params);
    // setTrim(params);
    if (params != null) {
      clickShowButton();
    }

  };
  var setMultiSelect = function (id, values) {
    $("#" + id).val(values);
  };
  var setJobGroups = function (params) {
    var selectedFolderParam = params['selectedFolder'];
    if (selectedFolderParam !== undefined && selectedFolderParam != null) {
      setMultiSelect("folderSelectHtmlId", selectedFolderParam);
    }
  };
  var setPages = function (params) {
    var selectedPagesParam = params['selectedPages'];
    if (selectedPagesParam !== undefined && selectedPagesParam != null){
      setMultiSelect("pageSelectHtmlId", selectedPagesParam);
    }
  };

  var clickShowButton = function () {
    $("#graphButtonHtmlId").click()
  };

  var setMeasurands = function (params) {
    var measurandGroups = params['measurand'];
    if (measurandGroups == undefined || measurandGroups == null) {
      return;
    }
    var currentGroup;
    if (measurandGroups.constructor === Array) {
      currentGroup = JSON.parse(decodeURIComponent(measurandGroups.shift()));
      addMeasurands(currentGroup, 0);
      var addButton = $("#addMeasurandSeriesButton");
      var length = measurandGroups.length;
      for (var i = 0; i < length; i++) {
        addButton.click();
        addMeasurands(JSON.parse(decodeURIComponent(measurandGroups.shift())), i + 1);
      }
    } else {
      currentGroup = JSON.parse(decodeURIComponent(measurandGroups));
      addMeasurands(currentGroup, 0);
    }
  };

  var addMeasurands = function (measurands, index) {
    var firstSelect = $(".firstMeasurandSelect").eq(index);
    firstSelect.val(measurands['values'].shift());
    var length = measurands['values'].length;
    var currentPanel = firstSelect.closest(".panel");
    var currentAddButton = currentPanel.find(".addMeasurandButton");
    for (var i = 0; i < length; i++) {
      currentAddButton.click();
      currentPanel.find(".additionalMeasurand").eq(i).val(measurands['values'].shift());
      currentAddButton = currentPanel.find(".addMeasurandButton").eq(i + 1);
    }
    currentPanel.find(".stackedSelect").val(measurands['stacked'])

  };

  var setTrim = function (params) {
    // $("#appendedInputBelowLoadTimes").val(params["trimAboveLoadTimes"]);
    // $("#appendedInputAboveLoadTimes").val(params["trimAboveLoadTimes"]);
    // $("#appendedInputBelowRequestCounts").val(params["trimBelowRequestCounts"]);
    // $("#appendedInputAboveRequestCounts").val(params["trimAboveRequestCounts"]);
    // $("#appendedInputBelowRequestSizes").val(params["trimBelowRequestSizes"]);
    // $("#appendedInputAboveRequestSizes").val(params["trimAboveRequestSizes"]);
  };


  var timecardResolved = false;
  var barChartResolved = false;
  var markTimeCardAsResolved = function () {
    timecardResolved = true;
    if (allLoaded()) {
      setSelections();
      addHandler();
      updateUrl()
    }
  };
  var markBarChartAsResolved = function () {
    barChartResolved = true;
    if (allLoaded()) {
      setSelections();
      addHandler();
      updateUrl()
    }
  };
  var allLoaded = function () {
    return timecardResolved && barChartResolved;
  };

  var init = function () {
    $(window).on("selectIntervalTimeframeCardLoaded", function () {
      markTimeCardAsResolved();
    });
    $(window).on("barchartLoaded", function () {
      markBarChartAsResolved();
    });
  };

  return {
    setSelections: setSelections,
    clickShowButton: clickShowButton,
    init: init
  };
});