"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectUserTimings = (function() {
    var resetButtonElement = $(".reset-result-selection");
    var optGroups = [];
    var oldValues = [];

    function OptGroup(optGroupDomElement){
        var optGroupLoadTimes = $(optGroupDomElement).find('.measurand-opt-group-LOAD_TIMES');

        resetButtonElement.on("click", function () {
            OpenSpeedMonitor.domUtils.deselectAllOptions(optGroupLoadTimes, true);
        });

        this.updateOptions = function (newNamesList) {
            if(optGroupLoadTimes){
                OpenSpeedMonitor.domUtils.updateOptionGroupWithUserTimings(optGroupLoadTimes, newNamesList, oldValues);
            }
        }
    }

    function init() {
        $('.measurands-select-opt-groups').each(function (index, optGroupElement) {
            var optGroup = new OptGroup(optGroupElement);
            optGroups.push(optGroup);
        })
    }


    var updateUserTimings = function(userTimings) {
        optGroups.forEach(function (optGroup) {
            optGroup.updateOptions(userTimings);
        });
        oldValues = userTimings;
    };


    init();
    return {
        updateUserTimings: updateUserTimings
    }
})();
