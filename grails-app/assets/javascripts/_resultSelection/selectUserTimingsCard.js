"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectUserTimings = (function () {
    var resetButtonElement = $(".reset-result-selection");

    var updateUserTimings = function (userTimings) {
        $('.measurands-select-opt-groups').each(function (index, optGroupElement) {
            var optGroupUserTimings = $(optGroupElement).find('.measurand-opt-group-USER_TIMINGS');

            if (optGroupUserTimings) {
                resetButtonElement.on("click", function () {
                    OpenSpeedMonitor.domUtils.deselectAllOptions(optGroupUserTimings, true);
                });
                if (userTimings) {
                    OpenSpeedMonitor.domUtils.updateSelectOptions(optGroupUserTimings, userTimings, userTimings);
                    if( userTimings.length > 0){
                        optGroupUserTimings.show();
                    }else{
                        optGroupUserTimings.hide();
                    }
                }
            }
        })
    };

    function init(){
        $('.measurands-select-opt-groups').each(function (index, optGroupElement) {
            var optGroupUserTimings = $(optGroupElement).find('.measurand-opt-group-USER_TIMINGS');
            optGroupUserTimings.hide();
        });
    }

    init();
    return {
        updateUserTimings: updateUserTimings
    }
})();
