"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.MeasurementSetupWizard = OpenSpeedMonitor.MeasurementSetupWizard || {};

OpenSpeedMonitor.MeasurementSetupWizard.CreateJobCard = (function () {

    var predefinedCronSelectBox = $("#selectExecutionSchedule");
    var cronStringInputField = $("#executionSchedule");
    var cronInputValid = true;
    var jobNameInput = $("#inputJobName");
    var existingJobNames = [];
    var inputsValid = false;
    var jobDiv = $("#createJob");
    var defaultJobName = "";
    var jobNameHelpBlock = $("#jobNameHelpBlock");
    var executionScheduleFormGroup = $("#executionScheduleFormGroup");
    var cronInputHelpBlock = $("#cronInputHelpBlock")

    var init = function () {
        // set change listeners
        predefinedCronSelectBox.change(updateCronStringFromPredefined);
        cronStringInputField.keyup(validateCronExpression);
        jobNameInput.change(function() { validateInputs(); });
        jobNameInput.keyup(function() { validateInputs(); });

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

        getExistingJobNames();
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

    var validateJobNameInput = function () {
        var currentJobName = jobNameInput.val();
        var isDuplicateName = isExistingJobName(currentJobName);

        $("#jobNameFormGroup").toggleClass("has-error", !currentJobName);
        jobNameHelpBlock.toggleClass("hidden", !isDuplicateName);

        return currentJobName && !isDuplicateName;
    }

    var isExistingJobName = function (jobName) {
        return existingJobNames.indexOf(jobName) >= 0;
    }

    var validateInputs = function (isInitialCheck) {
        inputsValid = cronInputValid && validateJobNameInput();

        $('#createJobTab').toggleClass("failureText", !inputsValid && !isInitialCheck);

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

    var getExistingJobNames = function () {
        var url = OpenSpeedMonitor.urls.getJobNames;

        $.ajax({
            url: url,
            type: 'GET',
            dataType: "json",
            success: function (data) {
                existingJobNames = data;
            },
            error: function (e) {
                throw e;
            },
            traditional: true // grails compatible parameter array encoding
        });
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
            validateJobNameInput();
        }
    }

    init();
    return {
        isValid: isValid,
        setDefaultJobName: setDefaultJobName,
        validate: validateInputs
    }
})();
OpenSpeedMonitor.MeasurementSetupWizard.CreateJobCard.validate(true);
