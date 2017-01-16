"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.UrlHandling = OpenSpeedMonitor.ChartModules.UrlHandling || {};

OpenSpeedMonitor.ChartModules.UrlHandling.ChartSwitch = (function (eventResultDashboardLink, pageAggregationLink, tabularResultLink) {

    var oldParameter;
    var getUrlParameter = function () {
        var vars = [], hash;
        var currentUrl = window.location.href;

        // remove html anchor if exists
        var anchorIndex = currentUrl.indexOf('#');
        if (anchorIndex > 0) {
            currentUrl = currentUrl.replace(/#\w*/, "")
        }

        var paramIndex = currentUrl.indexOf('?');
        if (paramIndex < 0)
            return vars;

        var hashes = currentUrl.slice(paramIndex + 1).split('&');
        for (var i = 0; i < hashes.length; i++) {
            hash = hashes[i].split('=');
            var currentValue = vars[hash[0]];
            if (currentValue == null) {
                vars.push(hash[0]);
                vars[hash[0]] = hash[1];
            } else if (currentValue.constructor === Array) {
                vars[hash[0]].push(hash[1]);
            } else {
                vars[hash[0]] = [vars[hash[0]], hash[1]]
            }
        }
        return vars;
    };

    var getJobGroup = function (map) {
        var folder = $("#folderSelectHtmlId").val();
        if(folder != null) map["selectedFolder"] = folder
    };

    var getBrowser = function (map) {
        var browserSelect = $("#selectedBrowsersHtmlId");
        if(browserSelect!= null){
            var selectedBrowser = browserSelect.val();
            if(selectedBrowser != null) map["selectedBrowsers"] = selectedBrowser;
            var selectedAllBrosers = $("#selectedAllBrowsers").prop("checked");
            if(selectedAllBrosers != null) map["selectedAllBrowsers"] = selectedAllBrosers;
        }
    };

    var getLocation = function (map) {
        var selectedLocations = $("#selectedLocationsHtmlId_chosen");
        if(selectedLocations != null){
            var selectedAllLocations = $("#selectedAllLocations").prop("checked");
            if(selectedAllLocations) map["selectedAllLocations"] = selectedAllLocations;
        }
    };

    var getConnectivity = function (map) {
        var selectedConnectivities = $("#selectedConnectivityProfilesHtmlId");
        if(selectedConnectivities != null){
            var connectivities = selectedConnectivities.val();
            if (connectivities != null) map["selectedConnectivityProfiles"] = connectivities;
            var allConnectivies = $("#selectedAllConnectivityProfiles").prop("checked");
            if (allConnectivies) map["selectedAllConnectivityProfiles"] = allConnectivies;
        }
    };

    var getPage = function (map) {
        var pages = $("#pageSelectHtmlId").val();
        if(pages) map["selectedPages"] = pages;
    };

    var getTimeFrame = function (map) {
        map["setFromHour"] = ($('#setFromHour:checked').length>0) ? "on" :"";
        map["setToHour"] =  ($('#setToHour:checked').length>0) ? "on" :"";
        map["from"] = $("#fromDatepicker").val();
        map["fromHour"] = $("#startDateTimePicker").find(".input-group.bootstrap-timepicker.time-control").find(".form-control").val();
        map["to"] = $("#toDatepicker").val();
        map["toHour"] = $("#endDateTimePicker").find(".input-group.bootstrap-timepicker.time-control").find(".form-control").val();
    };



    var updateUrls = function (withCurrentSelection) {
        var updatedMap = $.extend({},oldParameter);
        if(withCurrentSelection){
            getTimeFrame(updatedMap);
            getJobGroup(updatedMap);
            getPage(updatedMap);
            getBrowser(updatedMap);
            getLocation(updatedMap);
            getConnectivity(updatedMap);
        }
        if(updatedMap["selectedFolder"] == null){
            updatedMap = {};
        } else{
            if (updatedMap["selectedInterval"] == null) updatedMap["selectedInterval"] = 60;
            if (updatedMap["selectedTimeFrameInterval"] == null) updatedMap["selectedTimeFrameInterval"] = 0;
            if (updatedMap["selectedAggrGroupValuesUnCached"] == null) updatedMap["selectedAggrGroupValuesUnCached"] = "docCompleteTimeInMillisecsUncached";
            if (updatedMap["includeNativeConnectivity"] == null) updatedMap["includeNativeConnectivity"] = "false";
            if (updatedMap["includeCustomConnectivity"] == null) updatedMap["includeCustomConnectivity"] = "false";
            if (updatedMap["selectedTimeFrameInterval"] == null) updatedMap["selectedTimeFrameInterval"] = 0;
        }
        updateUrl("#pageAggregationMainMenu",pageAggregationLink+"?"+$.param(updatedMap, true));
        updateUrl("#eventResultMainMenu",eventResultDashboardLink+"?"+$.param(updatedMap, true));
        updateUrl("#tabularResultMainMenu",tabularResultLink+"?"+$.param(updatedMap, true));
    };


    var updateUrl = function (selector, newUrl) {
        $(selector).find("a").attr("href",newUrl)
    };

    var init = function () {
        oldParameter = {};
        var urlParameter = getUrlParameter();
        oldParameter["selectedFolder"] = urlParameter["selectedFolder"];
        oldParameter["selectedBrowsers"] = urlParameter["selectedBrowsers"];
        oldParameter["selectedAllBrowsers"] = urlParameter["selectedAllBrowsers"];
        oldParameter["selectedAllLocations"] = urlParameter["selectedAllLocations"];
        oldParameter["selectedConnectivityProfiles"] = urlParameter["selectedConnectivityProfiles"];
        oldParameter["selectedAllConnectivityProfiles"] = urlParameter["selectedAllConnectivityProfiles"];
        oldParameter["selectedPages"] = urlParameter["selectedPages"];
        oldParameter["setFromHour"] = urlParameter["setFromHour"];
        oldParameter["setToHour"] = urlParameter["setToHour"];
        oldParameter["from"] = urlParameter["from"];
        oldParameter["fromHour"] = decodeURIComponent(urlParameter["fromHour"]);
        oldParameter["to"] = urlParameter["to"];
        oldParameter["toHour"] = decodeURIComponent(urlParameter["toHour"]);
        $('#graphButtonHtmlId').on('click', updateUrls(true));
        updateUrls(false);
    };

    return {
        getJobGroup: getJobGroup,
        getPage: getPage,
        getTimeFrame:getTimeFrame,
        updateUrls:updateUrls,
        init:init
    };
});