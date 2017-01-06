"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.UrlHandling = OpenSpeedMonitor.ChartModules.UrlHandling || {};

OpenSpeedMonitor.ChartModules.UrlHandling.ChartSwitch = (function (eventResultDashboardLink, pageAggregationLink, tabularResultLink) {


    var getJobGroup = function (map) {
        map["selectedFolder"] = $("#folderSelectHtmlId").val();
    };

    var getBrowser = function (map) {
        var browserSelect = $("#selectedBrowsersHtmlId");
        if(browserSelect!= null){
            map["selectedBrowsers"] = browserSelect.val();
            map["selectedAllBrowsers"]=$("#selectedAllBrowsers").prop("checked");
        }
    };

    var getLocation = function (map) {
        var selectedLocations = $("#selectedLocationsHtmlId_chosen");
        if(selectedLocations != null){
            map["selectedAllLocations"]= $("#selectedAllLocations").prop("checked");
        }
    };

    var getConnectivity = function (map) {
        var selectedConnectivities = $("#selectedConnectivityProfilesHtmlId");
        if(selectedConnectivities != null){
            map["selectedConnectivityProfiles"] = selectedConnectivities.val();
            map["selectedAllConnectivityProfiles"] = $("#selectedAllConnectivityProfiles").prop("checked");
        }
    };

    var getPage = function (map) {
        map["selectedPages"] = $("#pageSelectHtmlId").val();
    };

    var getTimeFrame = function (map) {
        map["setFromHour"] = ($('#setFromHour:checked').length>0) ? "on" :"";
        map["setToHour"] =  ($('#setToHour:checked').length>0) ? "on" :"";
        map["from"] = $("#fromDatepicker").val();
        map["fromHour"] = $("#startDateTimePicker").find(".input-group.bootstrap-timepicker.time-control").find(".form-control").val();
        map["to"] = $("#toDatepicker").val();
        map["toHour"] = $("#endDateTimePicker").find(".input-group.bootstrap-timepicker.time-control").find(".form-control").val()
    };



    var updateUrls = function () {
        var map = {hideGraph:true};
        getTimeFrame(map);
        getJobGroup(map);
        getPage(map);
        getBrowser(map);
        getLocation(map);
        getConnectivity(map);

        updateUrl("#eventResultMainMenu",eventResultDashboardLink+"?"+$.param(map, true));
        updateUrl("#tabularResultMainMenu",tabularResultLink+"?"+$.param(map, true));
        updateUrl("#pageAggregationMainMenu",pageAggregationLink+"?"+$.param(map, true));
    };
    
    var updateUrl = function (selector, newUrl) {
        $(selector).find("a").attr("href",newUrl)
    };

    var init = function () {
        $('#folderSelectHtmlId').on('change', updateUrls);
        $('#pageSelectHtmlId').on('change', updateUrls);
        $('#timeframeSelect').on('change', updateUrls);
        $('#select-interval-timeframe-card').find('.form-control').on('change', updateUrls);
    };

    return {
        getJobGroup: getJobGroup,
        getPage: getPage,
        getTimeFrame:getTimeFrame,
        updateUrls:updateUrls,
        init:init
    };

});