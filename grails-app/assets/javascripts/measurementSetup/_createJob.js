"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.MeasurementSetupWizard = OpenSpeedMonitor.MeasurementSetupWizard || {};

OpenSpeedMonitor.MeasurementSetupWizard.CreateJobCard = (function () {

    var predefinedCronSelectBox = $("#selectExecutionSchedule");
    var cronStringInputField = $("#executionSchedule");
    var cronInputValid = true;
    var jobNameInput = $("#inputJobName");
    var inputsValid = false;
    var jobDiv = $("#createJob");
    var defaultJobName = "";
    var executionScheduleFormGroup = $("#executionScheduleFormGroup");
    var cronInputHelpBlock = $("#cronInputHelpBlock");
    var createJobNavPill = $('#createJobTab');


    var init = function () {
        // set change listeners
        predefinedCronSelectBox.on('change', updateCronStringFromPredefined);
        cronStringInputField.on('keyup', validateCronExpression);
        jobNameInput.on('change', function() { validateInputs(); });
        jobNameInput.on('keyup', function() { validateInputs(); });

        // init value
        var initValue = cronStringInputField.val()
        if (initValue) {
            // check if init value is a predefined cron string
            if (isPredefinedCronString(initValue)) {
                predefinedCronSelectBox.val(initValue);
                predefinedCronSelectBox.trigger("chosen:updated");
            } else {
                predefinedCronSelectBox.val("");
                cronStringInputField.val(initValue);
            }
        } else {
            // set default value.
            predefinedCronSelectBox.val(predefinedCronSelectBox.find("option:eq(1)").val());
            updateCronStringFromPredefined();
        }
    }

    var isPredefinedCronString = function (cronString) {
        return predefinedCronSelectBox.find("option").is(function (index, elem) {
            return $(elem).val() === cronString;
        });
    }

    var updateCronStringFromPredefined = function () {
        var selectedValue = predefinedCronSelectBox.val();
        cronStringInputField.prop("readonly", !!selectedValue);
        if (!!selectedValue) {
            cronStringInputField.val(selectedValue);
        }
        validateCronExpression();
    }

    var validateInputs = function () {
        inputsValid = cronInputValid;
        var cardWasActive = createJobNavPill.parent().hasClass("wasActive");

        createJobNavPill.toggleClass("failureText", !inputsValid && cardWasActive);

        informListeners();
    }

    var validateCronExpression = function () {
        $.ajax({
            url: OpenSpeedMonitor.urls.cronExpressionNextExecution,
            data: { cronExpression: cronStringInputField.val()},
            dataType: "text",
            type: 'GET',
            success: function (data) {
                processCronExpressionValidation(true, prettyCron.toString(cronStringInputField.val()));
            },
            error: function (e, status) {
                if (status === "error") {
                    processCronExpressionValidation(false, e.responseText);
                } else {
                    console.error(e);
                }
            }
        });
    }

    var processCronExpressionValidation = function (isValid, helpText) {
        cronInputValid = isValid;
        executionScheduleFormGroup.toggleClass("has-error", !isValid);
        cronInputHelpBlock.text(helpText);
        validateInputs();
    }

    var informListeners = function () {
        var event = new Event('changed')
        jobDiv.get(0).dispatchEvent(event);
    }

    var isValid = function () {
        return inputsValid;
    }

    var setDefaultJobName = function (newDefault) {
        var oldDefault = defaultJobName;
        defaultJobName = newDefault;
        if (jobNameInput.val() === oldDefault) {
            jobNameInput.val(defaultJobName);
            validateInputs();
        }
    }

    init();
    return {
        isValid: isValid,
        setDefaultJobName: setDefaultJobName,
        validateCronExpression: validateCronExpression
    }
})();
OpenSpeedMonitor.MeasurementSetupWizard.CreateJobCard.validateCronExpression();
