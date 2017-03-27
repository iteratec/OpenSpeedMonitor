"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.MeasurementSetupWizard = OpenSpeedMonitor.MeasurementSetupWizard || {};

OpenSpeedMonitor.MeasurementSetupWizard.CreateScriptCard = (function () {

    var nextButton = $("#createScriptTabNextButton")
    var scriptNameInput = $("#inputScriptName");
    var navigationScriptInput = $(".CodeMirror textarea")
    var scriptNameFormGroup = $("#scriptNameFormGroup");
    var navigationScriptFormGroup = $("#navigationScriptFormGroup");
    var scriptNameValid = false;
    var inputValid = false;
    var navigationScriptValid = false;
    var existingScriptNames;
    var scriptNavPill = $("#createScriptTab");
    var scriptDiv = $("#createScript");
    var defaultScriptName = "";

    var init = function () {
        scriptNameInput.keyup(validateScriptName);
        navigationScriptInput.keyup(validateNavigationScript);
        nextButton.click(validateScriptName);
        getExistingScriptNames();
    }

    var validateScriptName = function () {
        scriptNameValid = false;

        var currentScriptName = scriptNameInput.val();
        if (!currentScriptName) {
            scriptNameFormGroup.addClass("has-error");
        } else if (isExistingScriptName(currentScriptName)) {
            scriptNameFormGroup.addClass("has-error");
            $("#scriptNameHelpBlock").removeClass("hidden");
        } else {
            $("#scriptNameHelpBlock").addClass("hidden");
            scriptNameFormGroup.removeClass("has-error");
            scriptNameValid = true;
        }
        validateInputs();
    }

    var validateNavigationScript = function () {
        navigationScriptValid = false;

        var errors = OpenSpeedMonitor.script.codemirrorEditor.getErrors();
        if (!errors || errors.length === 0) {
            navigationScriptFormGroup.removeClass("has-error")
            navigationScriptValid = true;
        } else {
            navigationScriptFormGroup.addClass("has-error")
        }

        validateInputs();
    }

    var validateInputs = function () {
        inputValid = scriptNameValid && navigationScriptValid;
        if (inputValid) {
            scriptNavPill.removeClass("failureText");
        } else {
            scriptNavPill.addClass("failureText");
        }
        informListeners();
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

    var informListeners = function () {
        var event = new Event('changed')
        scriptDiv.get(0).dispatchEvent(event);
    }

    var isValid = function () {
        return inputValid;
    }

    var setDefaultScriptName = function (newDefault) {
        var oldDefault = defaultScriptName;
        defaultScriptName = newDefault;
        if (getScriptName() === oldDefault) {
            scriptNameInput.val(defaultScriptName);
            validateScriptName();
        }
    }

    var getScriptName = function () {
        return scriptNameInput.val();
    }

    init();
    return {
        isValid: isValid,
        setDefaultScriptName: setDefaultScriptName,
        getScriptName: getScriptName,
        validate: validateInputs
    }
})();
OpenSpeedMonitor.MeasurementSetupWizard.CreateScriptCard.validate();
