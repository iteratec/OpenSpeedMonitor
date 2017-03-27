"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.MeasurementSetupWizard = OpenSpeedMonitor.MeasurementSetupWizard || {};

OpenSpeedMonitor.MeasurementSetupWizard.CreateMeasurementForm = (function () {

    var formSubmissonButton = $("#createJobTabCreationButton");
    var createJobGroupCardHasErrors = false;
    var createScriptCardHasErrors = false;
    var createJobCardHasErrors = false;

    var allCardsValid = function () {
        return !createJobGroupCardHasErrors && !createScriptCardHasErrors && !createJobCardHasErrors;
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
            case 'createJobCard':
                createJobCardHasErrors = hasErrors;
        }

        validateForm();
    }

    return {
        setErrors: setErrors
    }
})();