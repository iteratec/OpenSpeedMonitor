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
        existingJobGroupSelectBox.on('change', updateJobGroupNameFromExisting);
        jobGroupNameInputField.on('keyup', updateHiddenField);
        nextButton.on('click', validateInputs);

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
        return jobGroupName && existingJobGroupSelectBox.find("option").is(function (index, elem) {
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

    var validateInputs = function (isInitialCheck) {
        var currentInput = jobGroupNameInputField.val();
        var isDuplicateName = !existingJobGroupSelectBox.val() && isExistingJobGroup(currentInput);
        inputValid = currentInput && !isDuplicateName;
        $("#jobGroupFormGroup").toggleClass("has-error", !inputValid && !isInitialCheck);
        $('#setJobGroupTab').toggleClass("failureText",  !inputValid && !isInitialCheck);
        $("#jobGroupNameHelpBlock").toggleClass("hidden", !isDuplicateName);
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
OpenSpeedMonitor.MeasurementSetupWizard.CreateJobGroupCard.validate(true);
