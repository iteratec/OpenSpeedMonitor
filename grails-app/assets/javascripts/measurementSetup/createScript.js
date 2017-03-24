"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.MeasurementSetupWizard = OpenSpeedMonitor.MeasurementSetupWizard || {};

OpenSpeedMonitor.MeasurementSetupWizard.CreateScriptCard = (function () {

    var nextButton = $("#createScriptTabNextButton")
    var scriptNameInput = $("#inputScriptName");
    var scriptNameValid = false;
    var existingScriptNames;

    var init = function () {
        scriptNameInput.change(validateScriptName);
        scriptNameInput.keyup(validateScriptName);
        nextButton.click(validateInput);
        getExistingScriptNames();
    }

    var validateScriptName = function () {
        scriptNameValid = false;

        var currentScriptName = scriptNameInput.val();
        if (!currentScriptName) {
            $("#scriptNameFormGroup").addClass("has-error");
        } else if (isExistingScriptName(currentScriptName)) {
            $("#scriptNameFormGroup").addClass("has-error");
            $("#scriptNameHelpBlock").removeClass("hidden");
        } else {
            $("#scriptNameHelpBlock").addClass("hidden");
            $("#scriptNameFormGroup").removeClass("has-error");
            scriptNameValid = true;
        }
        validateInput();
    }

    var validateInput = function () {
        if (scriptNameValid) {
            $('#createScriptTab').removeClass("failureText");
        } else {
            $('#createScriptTab').addClass("failureText");
        }
        OpenSpeedMonitor.MeasurementSetupWizard.CreateMeasurementForm.setErrors('createScriptCard', !scriptNameValid)
    }

    var isExistingScriptName = function (scriptName) {
        return existingScriptNames.indexOf(scriptName) >= 0;
    }

    var getExistingScriptNames = function () {
        var url = OpenSpeedMonitor.urls.getScriptNames;

        $.ajax({
            url: url,
            type: 'GET',
            dataType: "json",
            success: function (data) {
                existingScriptNames = data;
            },
            error: function (e) {
                throw e;
            },
            traditional: true // grails compatible parameter array encoding
        });
    }

    init();
    return {}
})();