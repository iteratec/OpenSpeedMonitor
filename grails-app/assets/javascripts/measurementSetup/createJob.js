"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.MeasurementSetupWizard = OpenSpeedMonitor.MeasurementSetupWizard || {};

OpenSpeedMonitor.MeasurementSetupWizard.CreateJobCard = (function () {

    var predefinedCronSelectBox = $("#selectExecutionSchedule");
    var cronStringInputField = $("#executionSchedule");
    var cronInputValid = true;
    var jobNameInput = $("#inputJobName");
    var jobNameValid = true;
    var existingJobNames;
    var inputsValid = false;
    var jobDiv = $("#createJob");

    var init = function () {
        // set change listeners
        predefinedCronSelectBox.change(updateCronStringFromPredefined);
        cronStringInputField.keyup(validateCronInput);
        jobNameInput.change(validateJobNameInput);
        jobNameInput.keyup(validateJobNameInput);

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
        validateCronInput()
    }

    var validateCronInput = function () {
        cronInputValid = isValidCronInput(cronStringInputField.val());
        $("#executionScheduleFormGroup").toggleClass("has-error", !cronInputValid);
        validateInputs();
    }

    var validateJobNameInput = function () {
        jobNameValid = false;

        var currentJobName = jobNameInput.val();
        if (!currentJobName) {
            $("#jobNameFormGroup").addClass("has-error");
        } else if (isExistingJobName(currentJobName)) {
            $("#jobNameFormGroup").addClass("has-error");
            $("#jobNameHelpBlock").removeClass("hidden");
        } else {
            $("#jobNameHelpBlock").addClass("hidden");
            $("#jobNameFormGroup").removeClass("has-error");
            jobNameValid = true;
        }

        validateInputs();
    }

    var isExistingJobName = function (jobName) {
        return existingJobNames.indexOf(jobName) >= 0;
    }

    var isValidCronInput = function (cronString) {
        return !!cronString;
    }

    var validateInputs = function () {
        inputsValid = cronInputValid && jobNameValid;
        $('#createJobTab').toggleClass("failureText", !inputsValid);

        informListeners();
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

    init();
    return {
        isValid: isValid
    }
})();
