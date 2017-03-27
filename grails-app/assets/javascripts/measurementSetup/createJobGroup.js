"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.MeasurementSetupWizard = OpenSpeedMonitor.MeasurementSetupWizard || {};

OpenSpeedMonitor.MeasurementSetupWizard.CreateJobGroupCard = (function () {

    var existingJobGroupSelectBox = $("#jobGroupSelect");
    var jobGroupNameInputField = $("#inputNewJobGroupName");
    var hiddenFieldForJobGroupName = $("#jobGroupName");
    var nextButton = $("#setJobGroubTabNextButton");
    var inputValid = false;
    var jobGroupDiv = $("#setJobGroup");

    var init = function () {
        // set change listeners
        existingJobGroupSelectBox.change(updateJobGroupNameFromExisting);
        jobGroupNameInputField.keyup(updateHiddenField);
        nextButton.click(validateInputs);

        // init value
        var initValue = hiddenFieldForJobGroupName.val();
        if (initValue && isExistingJobGroup(initValue)) {
            // check if init value is an existing job group
            existingJobGroupSelectBox.val(initValue);
            existingJobGroupSelectBox.trigger("chosen:updated");
        } else {
            existingJobGroupSelectBox.val("");
            jobGroupNameInputField.removeClass("hidden")
            jobGroupNameInputField.val(initValue);
        }
    }

    var isExistingJobGroup = function (jobGroupName) {
        return existingJobGroupSelectBox.find("option").is(function (index, elem) {
            return $(elem).val() === jobGroupName;
        });
    }

    var updateJobGroupNameFromExisting = function () {
        var selectedValue = existingJobGroupSelectBox.val();
        if (selectedValue) {
            jobGroupNameInputField.val(selectedValue);
            jobGroupNameInputField.addClass("hidden");
        } else {
            // newJobGroup selected
            jobGroupNameInputField.removeClass("hidden");
            jobGroupNameInputField.val("");
        }
        updateHiddenField()
    }

    var updateHiddenField = function () {
        hiddenFieldForJobGroupName.val(jobGroupNameInputField.val());
        validateInputs();
    }

    var validateInputs = function () {
        inputValid = false;
        var currentInput = jobGroupNameInputField.val();
        if (!currentInput) {
            $("#jobGroupFormGroup").addClass("has-error");
            $("#jobGroupNameHelpBlock").addClass("hidden");
            $('#setJobGroupTab').addClass("failureText")
        } else if (!jobGroupNameInputField.hasClass("hidden") && isExistingJobGroup(currentInput)) {
            $("#jobGroupFormGroup").addClass("has-error");
            $("#jobGroupNameHelpBlock").removeClass("hidden");
            $('#setJobGroupTab').addClass("failureText");
        }
        else {
            $("#jobGroupNameHelpBlock").addClass("hidden");
            $("#jobGroupFormGroup").removeClass("has-error");
            $('#setJobGroupTab').removeClass("failureText");
            inputValid = true;
        }
        informListeners();
    }

    var informListeners = function () {
        var event = new Event('changed')
        jobGroupDiv.get(0).dispatchEvent(event);
    }

    var isValid = function () {
        return inputValid;
    }

    var getJobGroup = function () {
        return jobGroupNameInputField.val();
    }

    init();
    return {
        isValid: isValid,
        getJobGroup: getJobGroup,
        validate: validateInputs
    }
})();
OpenSpeedMonitor.MeasurementSetupWizard.CreateJobGroupCard.validate();
