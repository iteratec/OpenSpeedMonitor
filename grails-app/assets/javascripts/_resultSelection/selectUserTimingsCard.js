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
        $('.measurands-select-opt-groups').each(function (index, optGroupElement) {
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
        OpenSpeedMonitor.domUtils.updateSelectOptions(optGroupUserTimings, userTimings, null);
    }

    init();
    return {
        updateUserTimings: updateUserTimings
    }
})();
