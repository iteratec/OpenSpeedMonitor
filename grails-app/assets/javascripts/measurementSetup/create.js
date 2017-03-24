//= require createJob.js
//= require createJobGroup.js
//= require createScript.js
//= require self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.MeasurementSetupWizard = OpenSpeedMonitor.MeasurementSetupWizard || {};

OpenSpeedMonitor.MeasurementSetupWizard.CreateMeasurementForm = (function () {

    var formSubmissonButton = $("#createJobTabCreationButton");
    var createJobGroupCardHasErrors = false;
    var createScriptCardHasErrors = false;

    var allCardsValid = function () {
        return !createJobGroupCardHasErrors && !createScriptCardHasErrors;
    }

    var validateForm = function () {
        if (allCardsValid()) {
            // enable submit button
            formSubmissonButton.prop('disabled', false);
        } else {
            // disable submit button
            formSubmissonButton.prop('disabled', true);
        }
    }

    var setErrors = function (caller, hasErrors) {
        switch (caller) {
            case 'createJobGroupCard':
                createJobGroupCardHasErrors = hasErrors;
                break;
            case 'createScriptCard':
                createScriptCardHasErrors = hasErrors;
                break;
        }

        validateForm();
    }

    return {
        setErrors: setErrors
    }
})();