//= require pageComparisonChart.js
//= require pageComparisonGuiHandling.js

//= require /urlHandling/urlHelper.js
"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.UrlHandling = OpenSpeedMonitor.ChartModules.UrlHandling || {};

OpenSpeedMonitor.ChartModules.UrlHandling.PageComparison = (function () {

    var getTimeFrame = function (map) {
        map["from"] = $("#fromDatepicker").val();
        map["to"] = $("#toDatepicker").val();
    };

    var getMeasurands = function (map) {
        var measurands = [];
        var measurandObjects = $('.measurandSeries');
        $.each(measurandObjects, function (_, currentSeries) {
            var currentMeasurands = [$(currentSeries).find(".firstMeasurandSelect").val()];
            $(currentSeries).find(".additionalMeasurand").each(function (_, additionalMeasurand) {
                currentMeasurands.push($(additionalMeasurand).val());
            });
            var json = JSON.stringify({
                "stacked": $(currentSeries).find(".stackedSelect").val(),
                "values": currentMeasurands
            });
            measurands.push(json);
        });
        map['measurand'] = measurands
    };
    var addHandler = function () {
        $('#graphButtonHtmlId').on('click', updateUrl);
    };

    var updateUrl = function () {
        var map = {};
        getTimeFrame(map);
        getMeasurands(map);
        var path = "show?" + $.param(map, true);
        window.history.pushState("object or string", "Title", path);
    };

    var setSelections = function () {
        var params = OpenSpeedMonitor.ChartModules.UrlHandling.UrlHelper.getUrlParameter();
        if (params && Object.keys(params).length > 0) {
            setMeasurands(params);
            setJobGroupsAndPages(params);
            if(params.selectedFolder != null && params.selectedPages != null){
                clickShowButton();
            }
        }

    };

    var setJobGroupsAndPages = function (params) {
        var selectedFolderParam = params['selectedFolder'];
        var selectedPagesParam = params['selectedPages'];
        // if there is only one selectedPage or selectedFolder put it into an new array
        if(typeof selectedPagesParam === 'string') {
            selectedPagesParam = [selectedPagesParam]
        }
        if(typeof selectedFolderParam === 'string') {
            selectedFolderParam = [selectedFolderParam]
        }

        if (selectedFolderParam !== undefined && selectedFolderParam != null && selectedPagesParam !== undefined && selectedPagesParam != null) {
            var values = [];
            selectedFolderParam.forEach(function (currentFolder) {
               selectedPagesParam.forEach(function (currentPage) {
                   values.push({'jobGroupId1': currentFolder, 'jobGroupId2': currentFolder, 'pageId1': currentPage, 'pageId2': currentPage})
               })
            });
            OpenSpeedMonitor.PageComparisonSelection.setValues(values);
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

    var init = function () {
        setSelections();
        addHandler();
    };

    return {
        setSelections: setSelections,
        init: init
    };
});
