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

OpenSpeedMonitor.BarchartMeasurings = (function () {
    var barchartMeasuringCard = $("#barchartMeasuringCard");
    var additionalMeasurandClone = barchartMeasuringCard.find("#additionalMeasurand-clone");
    var additionalBarClone = barchartMeasuringCard.find("#measurandSeries");

    var init = function () {
        addCloneChangeListener();
    };

    var addCloneChangeListener = function () {
        //We already add the listener to the clones to make sure that they
        //are the first events to be fired, even if there will be additional listeners be added in the future
        additionalBarClone.find(".removeMeasurandButton").click(removeMeasurand);

        $("#addMeasurandButton").click(addMeasurand);
        additionalMeasurandClone.find(".removeMeasurandButton").click(removeMeasurand);
    };

    var addMeasurand = function (e) {
        var clone = additionalMeasurandClone.clone(true, true);
        clone.removeClass("hidden");
        clone.removeAttr("id");
        clone.toggleClass("measurandSeries-clone", true);
        $("#measurands").append(clone);
    };

    var removeMeasurand = function (e) {
        e.preventDefault();
        var $element = $(e.target);
        if ($element.closest(".measurandSeries").find(".addMeasurandRow").length <= 2) {
            $element.closest(".measurandSeries").find(".stackedSelectContainer").addClass("hidden");
        }
        $element.closest(".addMeasurandRow").remove();
    };

    var getValues = function () {
        var result = [];

        var tempMap = {};
        $("#measurands").find("optgroup option:selected").each(function () {
            var seriesLabel = ($(this).parent().attr("label"));
            var series = tempMap[seriesLabel];
            if(!series){
                series = {"measurands":[]};
                tempMap[seriesLabel] = series;
            }
            series["measurands"].push($(this).val())
        });
        $.each(tempMap,function (_,value) {
            result.push(value)
        });
        return result;
    };

    var hasMeasurandSeries = function () {
        var measurandSeries = $('#barchartMeasuringCard > .measurandSeries');
        return measurandSeries.length
    };

    init();
    return {
        getValues: getValues,
        hasMeasurandSeries: hasMeasurandSeries
    };
})();
