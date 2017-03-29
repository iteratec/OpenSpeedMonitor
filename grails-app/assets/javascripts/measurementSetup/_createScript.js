"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.MeasurementSetupWizard = OpenSpeedMonitor.MeasurementSetupWizard || {};

OpenSpeedMonitor.MeasurementSetupWizard.CreateScriptCard = (function () {

    var nextButton = $("#createScriptTabNextButton")
    var scriptNameInput = $("#inputScriptName");
    var navigationScriptInput = $(".CodeMirror textarea")
    var scriptNameFormGroup = $("#scriptNameFormGroup");
    var navigationScriptFormGroup = $("#navigationScriptFormGroup");
    var inputValid = false;
    var existingScriptNames = [];
    var scriptNavPill = $("#createScriptTab");
    var scriptDiv = $("#createScript");
    var defaultScriptName = "";
    var defaultNavigationScript = $("#navigationScript").data("default-script").trim();
    var navigationScriptHelpBlock = $("#navigationScriptHelpBlock");

    var init = function () {
        scriptNameInput.keyup(function() { validateInputs(); });
        navigationScriptInput.keyup(function() { validateInputs(); });
        nextButton.click(function() { validateInputs(); });
        getExistingScriptNames();

        if (!String(OpenSpeedMonitor.script.codemirrorEditor.getContent()).trim()) {
            OpenSpeedMonitor.script.codemirrorEditor.setNewContent(defaultNavigationScript);
        }
    }

    var validateScriptName = function (isInitialCheck) {
        var currentScriptName = scriptNameInput.val();
        var isDuplicateName = isExistingScriptName(currentScriptName);

        scriptNameFormGroup.toggleClass("has-error", isDuplicateName || (!currentScriptName && !isInitialCheck));
        $("#scriptNameHelpBlock").toggleClass("hidden", !isDuplicateName);

        return currentScriptName && !isDuplicateName;
    }

    var validateNavigationScript = function (isInitialCheck) {
        var errors = OpenSpeedMonitor.script.codemirrorEditor.getErrors();
        var scriptContent = String(OpenSpeedMonitor.script.codemirrorEditor.getContent()).trim();
        var emptyOrDefault = !scriptContent || (scriptContent === defaultNavigationScript);
        var navigationScriptValid = (!errors || errors.length === 0) && !emptyOrDefault;

        navigationScriptHelpBlock.toggleClass("hidden", !emptyOrDefault);

        navigationScriptFormGroup.toggleClass("has-error", !navigationScriptValid && !isInitialCheck);
        return navigationScriptValid;
    }

    var validateInputs = function (isInitialCheck) {
        inputValid = validateScriptName(isInitialCheck) && validateNavigationScript(isInitialCheck);
        var cardWasActive = scriptNavPill.parent().hasClass("wasActive");
        scriptNavPill.toggleClass("failureText", !inputValid && !isInitialCheck && cardWasActive);
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
            validateInputs();
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
OpenSpeedMonitor.MeasurementSetupWizard.CreateScriptCard.validate(true);
