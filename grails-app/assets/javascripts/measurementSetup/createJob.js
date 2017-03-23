"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.MeasurementSetupWizard = OpenSpeedMonitor.MeasurementSetupWizard || {};

OpenSpeedMonitor.MeasurementSetupWizard.CreateJobCard = (function () {

    var predefinedCronSelectBox = $("#selectExecutionSchedule");
    var cronStringInputField = $("#inputCronString")
    var hiddenFieldForCronString = $("#executionSchedule")

    var init = function () {
        // set change listeners
        predefinedCronSelectBox.change(updateCronStringFromPredefined);
        cronStringInputField.keyup(updateHiddenField);

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
            predefinedCronSelectBox.val(predefinedCronSelectBox.find("option:eq(1)").val())
        }
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
    }

    init();
    return {}
})();