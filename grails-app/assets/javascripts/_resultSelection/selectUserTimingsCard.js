"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectUserTimings = (function () {
    var resetButtonElement = $(".reset-result-selection");

    var updateUserTimings = function (userTimings) {
        loopOverOptGroups(executeUpdate, userTimings);
    };

    function init() {
        loopOverOptGroups(executeInit);
    }

    function loopOverOptGroups(executeOperation, userTimings) {
        $('.measurand-select').each(function (index, optGroupElement) {
            var optGroupUserTimings = $(optGroupElement).find('.measurand-opt-group-USER_TIMINGS');
            if (optGroupUserTimings) {
                executeOperation(optGroupUserTimings, userTimings);
                if (userTimings && userTimings.length > 0) {
                    optGroupUserTimings.show();
                } else {
                    optGroupUserTimings.hide();
                }
            }
        });
    }

    function executeInit(optGroupUserTimings) {
        resetButtonElement.on("click", function () {
            OpenSpeedMonitor.domUtils.deselectAllOptions(optGroupUserTimings, true);
        });
    }

    function executeUpdate(optGroupUserTimings, userTimings) {
        var selectedOptions = [];
        optGroupUserTimings.find('option:selected').each(function (index, option){
            selectedOptions.push(option.value);
        });
        OpenSpeedMonitor.domUtils.updateSelectOptions(optGroupUserTimings, userTimings, null);
        selectedOptions.forEach(function (value) {
            optGroupUserTimings.find('option').each(function (index2, option){
                if(option.value == value){
                    option.selected = true;
                }
            });
        });
    }

    init();
    return {
        updateUserTimings: updateUserTimings
    }
})();
