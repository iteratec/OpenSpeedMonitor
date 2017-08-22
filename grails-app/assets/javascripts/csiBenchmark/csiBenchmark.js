//= require /urlHandling/urlHelper.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.UrlHandling = OpenSpeedMonitor.ChartModules.UrlHandling || {};

OpenSpeedMonitor.ChartModules.UrlHandling.CsiBenchmark = (function () {

    var getTimeFrame = function (map) {
            map["setFromHour"] = ($('#setFromHour:checked').length>0) ? "on" :"";
            map["setToHour"] =  ($('#setToHour:checked').length>0) ? "on" :"";
            map["from"] = $("#fromDatepicker").val();
            map["fromHour"] = $("#startDateTimePicker").find(".input-group.bootstrap-timepicker.time-control").find(".form-control").val();
            map["to"] = $("#toDatepicker").val();
            map["toHour"] = $("#endDateTimePicker").find(".input-group.bootstrap-timepicker.time-control").find(".form-control").val()
          };

    var getJobGroup = function (map) {
        map["selectedFolder"] = $("#folderSelectHtmlId").val();
    };

    var addHandler = function () {
        $('#graphButtonHtmlId').on('click', updateUrl);
    };

    var updateUrl = function () {
        var map = {};
        getTimeFrame(map);
        getJobGroup(map);
        var path = "show?" + $.param(map, true);
        window.history.pushState("object or string", "Title", path);
    };

    var setSelections = function () {
        var params = OpenSpeedMonitor.ChartModules.UrlHandling.UrlHelper.getUrlParameter();
        // setTrim(params);
        if (params && Object.keys(params).length > 0) {
            setJobGroups(params);
            if(params.selectedFolder != null && params.selectedPages != null){
                clickShowButton();
            }
        }

    };
    var setMultiSelect = function (id, values) {
        $("#" + id).val(values);
        $("#" + id).trigger("change")
    };
    var setJobGroups = function (params) {
        var selectedFolderParam = params['selectedFolder'];
        if (selectedFolderParam !== undefined && selectedFolderParam != null) {
            setMultiSelect("folderSelectHtmlId", selectedFolderParam);
        }
    };

    var clickShowButton = function () {
        $("#graphButtonHtmlId").click()
    };

    var timecardResolved = false;
    var barChartResolved = false;
    var markTimeCardAsResolved = function () {
        timecardResolved = true;
        if (allLoaded()) {
           init()
        }
    };
    var markBarChartAsResolved = function () {
        barChartResolved = true;
        if (allLoaded()) {
            init()
        }
    };
    var allLoaded = function () {
        return timecardResolved && barChartResolved;
    };

    var init = function () {
        setSelections();
        addHandler();
    };

    var initWaitForPostload = function () {
        $(window).on("selectIntervalTimeframeCardLoaded", function () {
            markTimeCardAsResolved();
        });
        $(window).on("barchartLoaded", function () {
            markBarChartAsResolved();
        });
    };

    return {
        setSelections: setSelections,
        init: initWaitForPostload
    };
});
