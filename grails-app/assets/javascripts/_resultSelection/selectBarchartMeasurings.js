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

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.BarchartMeasurings = (function () {
    var barchartMeasuringCard = $("#barchartMeasuringCard");
    var additionalMeasurandClone = barchartMeasuringCard.find("#additionalMeasurand-clone");
    var additionalBarClone = barchartMeasuringCard.find("#measurandSeries-clone");

    var init = function () {
        addCloneChangeListener();
        addBar();
        barchartMeasuringCard.find("#addMeasurandSeriesButton").click(addBar);
    };

    var addCloneChangeListener = function () {
        //We already add the listener to the clones to make sure that they
        //are the first events to be fired, even if there will be additional listeners be added in the future
        additionalBarClone.find(".firstMeasurandSelect").change(selectionChangeListener);
        additionalBarClone.find(".addMeasurandButton").click(addMeasurand);
        additionalBarClone.find(".removeMeasurandButton").click(removeMeasurand);
        additionalBarClone.find(".removeMeasurandSeriesButton").click(removeSeries);

        additionalMeasurandClone.find(".addMeasurandButton").click(addMeasurand);
        additionalMeasurandClone.find(".removeMeasurandButton").click(removeMeasurand);
    };

    var selectionChangeListener = function (e) {
        var $selectElement = $(e.target);
        var selectedOptionGroupLabel = $selectElement.find(":selected").parent().attr('label');
        var additionalMeasurands = $selectElement.closest(".panel").find(".additionalMeasurand");
        additionalMeasurands.each(function (index, currentMeasurand) {
            enableDisableSelectionGroup(currentMeasurand, selectedOptionGroupLabel);
        })
    };

    var enableDisableSelectionGroup = function (targetSelectElement, activeOptionGroupLabel) {
        var $targetSelectElement = $(targetSelectElement);
        var validSelectionInTargetElement = activeOptionGroupLabel === $targetSelectElement.find(":selected").parent().attr("label");

        $targetSelectElement.find("optgroup").each(function (index, group) {
            var $group = $(group);
            if ($group.attr("label") === activeOptionGroupLabel) {
                $group.attr("disabled", false);
                if (!validSelectionInTargetElement) {
                    $(targetSelectElement).val($group.find("option:first").val());
                }
            } else {
                $group.attr("disabled", true);
            }
        })
    };

    var addBar = function () {
        var clone = additionalBarClone.clone(true, true);
        clone.removeClass("hidden");
        clone.removeAttr("id");
        clone.addClass("measurandSeries");
        clone.insertBefore(additionalBarClone);
    };

    var addMeasurand = function (e) {
        var clone = additionalMeasurandClone.clone(true, true);
        clone.removeClass("hidden");
        clone.removeAttr("id");
        clone.insertAfter($(e.target).closest(".addMeasurandRow"));

        var selectedOptGroupLabel = clone.closest(".panel").find(".firstMeasurandSelect :selected").parent().attr("label");
        enableDisableSelectionGroup(clone.find("select"), selectedOptGroupLabel);

        // Make stacked selection visible
        var stackedSelectContainer = $(e.target).closest(".panel").find(".stackedSelectContainer");
        if (stackedSelectContainer.hasClass("hidden")) {
            stackedSelectContainer.removeClass("hidden")
        }
    };

    var removeMeasurand = function (e) {
        var $element = $(e.target);
        if ($element.closest(".panel").find(".addMeasurandRow").length <= 2) {
            $element.closest(".panel").find(".stackedSelectContainer").addClass("hidden");
        }
        $element.closest(".addMeasurandRow").remove();
    };

    var removeSeries = function (e) {
        var $element = $(e.target);
        $element.closest(".panel").remove();
    };

    var getValues = function () {
        var result = [];

        $(".measurandSeries").each(function () {
            var currentSeries = {};
            var measurands = [];

            $(this).find(".addMeasurandRow select").each(function () {
                measurands.push($(this).val());
            });

            currentSeries['measurands'] = measurands;
            currentSeries['stacked'] = $(this).find(".stackedSelect").val() === "stacked";
            result.push(currentSeries);
        });

        return result;
    };

    init();
    return {
        getValues: getValues
    };
})();
