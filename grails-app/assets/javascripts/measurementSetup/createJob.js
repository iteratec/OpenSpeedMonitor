"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.MeasurementSetupWizard = OpenSpeedMonitor.MeasurementSetupWizard || {};

OpenSpeedMonitor.MeasurementSetupWizard.CreateJobCard = (function () {

    var predefinedCronSelectBox = $("#selectExecutionSchedule");
    var cronStringInputField = $("#inputCronString");
    var hiddenFieldForCronString = $("#executionSchedule");
    var cronInputValid = true;
    var jobNameInput = $("#inputJobName");
    var jobNameValid = true;
    var existingJobNames;

    var init = function () {
        // set change listeners
        predefinedCronSelectBox.change(updateCronStringFromPredefined);
        cronStringInputField.keyup(updateHiddenField);
        jobNameInput.change(validateJobNameInput);
        jobNameInput.keyup(validateJobNameInput);

        // init value
        var initValue = hiddenFieldForCronString.val()
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
        if (selectedValue) {
            cronStringInputField.val(selectedValue)
            cronStringInputField.addClass("hidden")
        } else {
            // custom cron string
            cronStringInputField.removeClass("hidden")
        }
        updateHiddenField()
    }

    var updateHiddenField = function () {
        hiddenFieldForCronString.val(cronStringInputField.val())
        validateCronInput()
    }

    var validateCronInput = function () {
        cronInputValid = false;

        var currentCronInput = hiddenFieldForCronString.val();
        if (!isValidCronInput(currentCronInput)) {
            $("#executionScheduleFormGroup").addClass("has-error");
        } else {
            $("#executionScheduleFormGroup").removeClass("has-error");
            cronInputValid = true;
        }

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
        var inputsValid = cronInputValid && jobNameValid;

        if (!inputsValid) {
            $('#createJobTab').addClass("failureText")
        } else {
            $('#createJobTab').removeClass("failureText")
        }

        OpenSpeedMonitor.MeasurementSetupWizard.CreateMeasurementForm.setErrors('createJobCard', !inputsValid);
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

    init();
    return {}
})();