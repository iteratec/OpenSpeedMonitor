//= require_self

/*
 * OpenSpeedMonitor (OSM)
 * Copyright 2014 iteratec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.PageComparisonSelection = (function () {
    var init = function () {
        $(".addMeasurandButton").click(addPageComparisonRow);

        addEventListeners();

        $("#pageComparisonSelectionCard select option[value='']").attr("disabled", true);
        $("#pageComparisonSelectionCard select").on("change", enabaleOrDisableShowButton);

        enabaleOrDisableShowButton();
    };

    var addEventListeners = function () {
        var $firstJobGroupSelect = $("#firstJobGroupSelect");
        var $secondJobGroupSelect = $("#secondJobGroupSelect");

        $firstJobGroupSelect.on("change", updatePageListener);
        $secondJobGroupSelect.on("change", updatePageListener);

        $firstJobGroupSelect.on("change", selectionChangeListener);
    };

    var enabaleOrDisableShowButton = function () {
        var notAllOptionsSelected = $("#pageComparisonSelectionCard select option:selected").filter(function () {
                return !$(this).val();
            }).length > 0;
        $("#graphButtonHtmlId").attr("disabled", notAllOptionsSelected)
    };

    var addPageComparisonRow = function (event) {
        var clone = $("#measurandSeries-clone").clone();
        clone.find(".removeMeasurandButton").removeClass("hidden");
        clone.find(".removeMeasurandButton").click(removeComparisonRow);
        clone.find(".addMeasurandButton").click(addPageComparisonRow);
        clone.find("#firstJobGroupSelect").on("change", selectionChangeListener);
        clone.find("#firstJobGroupSelect").on("change", updatePageListener);
        clone.find("#secondJobGroupSelect").on("change", updatePageListener);
        clone.find("select").on("change", enabaleOrDisableShowButton);
        clone.removeAttr("id");
        if (event) {
            clone.insertAfter($(event.target).closest(".addPageComparisonRow"));
        } else {
            clone.insertAfter("#measurandSeries-clone")
        }
    };

    var removeComparisonRow = function (event) {
        $(event.target).closest(".addPageComparisonRow").remove();
    };

    var selectionChangeListener = function (event) {
        var secondJobGroupSelect = $(event.target).closest(".addPageComparisonRow").find("#secondJobGroupSelect");
        secondJobGroupSelect.val($(event.target).val());
        secondJobGroupSelect.change();
        enabaleOrDisableShowButton();
    };

    var updatePageListener = function (event) {
        var selectBoxToChange = $(event.target).parent().find(".pageSelect");
        var url = OpenSpeedMonitor.urls.pageComparisonGetPages;
        var selectedTimeframe = OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame();
        var queryArgs = {
            'from': selectedTimeframe[0].toISOString(),
            'to': selectedTimeframe[1].toISOString(),
            'jobGroupIds': $(event.target).val(),
            'caller': null
        };
        $.ajax({
            url: url,
            type: 'GET',
            data: queryArgs,
            dataType: "json",
            success: function (data) {
                var currentSelection = selectBoxToChange.val();
                selectBoxToChange.find("option[value!='']").remove();
                if (data && data.length > 0) {
                    data.forEach(function (d) {
                        selectBoxToChange.append("<option value='" + d.id + "'>" + d.name + "</option>")
                    });
                    currentSelection = data.some(function (d) {
                        return d.id == currentSelection;
                    }) ? currentSelection : "";
                    selectBoxToChange.val(currentSelection);
                } else {
                    selectBoxToChange.val("");
                }
            },
            error: function (e, statusText) {
                if (statusText != "abort") {
                    throw e;
                }
            },
            traditional: true // grails compatible parameter array encoding
        });
    };

    var getValues = function () {
        var result = [];

        $(".addPageComparisonRow").each(function () {
            var currentRow = $(this);
            var currentComparision = {};

            currentComparision['jobGroupId1'] = currentRow.find("#firstJobGroupSelect").val();
            currentComparision['jobGroupId2'] = currentRow.find("#secondJobGroupSelect").val();
            currentComparision['pageId1'] = currentRow.find("#firstPageSelect").val();
            currentComparision['pageId2'] = currentRow.find("#secondPageSelect").val();

            result.push(currentComparision);
        });

        return result;
    };

    var setValues = function (values) {
        for (var i = 1; i < values.length; i++) {
            addPageComparisonRow(null)
        }
        var comparisonRows = $(".addPageComparisonRow");
        values.forEach(function (currentComparison, index) {
            var currentRow = $(comparisonRows[index]);
            currentRow.find("#firstJobGroupSelect").val(currentComparison['jobGroupId1']);
            currentRow.find("#firstPageSelect").val(currentComparison['pageId1']);
            currentRow.find("#firstJobGroupSelect").change();
            currentRow.find("#secondJobGroupSelect").val(currentComparison['jobGroupId2']);
            currentRow.find("#secondPageSelect").val(currentComparison['pageId2']);
            currentRow.find("#secondJobGroupSelect").change();
        })
    };

    var getJobGroups = function () {
        var result = [];
        $(".addPageComparisonRow").each(function () {
            var currentRow = $(this);
            result.push(currentRow.find("#firstJobGroupSelect").val());
            result.push(currentRow.find("#secondJobGroupSelect").val());
        });
        return result.filter(function (value, index, self) {
            return self.indexOf(value) === index;
        })
    };

    var getPages = function () {
        var result = [];
        $(".addPageComparisonRow").each(function () {
            var currentRow = $(this);
            result.push(currentRow.find("#firstPageSelect").val());
            result.push(currentRow.find("#secondPageSelect").val());
        });
        return result.filter(function (value, index, self) {
            return self.indexOf(value) === index;
        })
    };

    init();
    return {
        getValues: getValues,
        setValues: setValues,
        getPages: getPages,
        getJobGroups: getJobGroups
    };
})();
