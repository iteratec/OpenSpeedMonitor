"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.MeasurementSetupWizard = OpenSpeedMonitor.MeasurementSetupWizard || {};

OpenSpeedMonitor.MeasurementSetupWizard.CreateMeasurementForm = (function () {

    var formSubmissonButton = $("#createJobTabCreationButton");
    var createJobGroupCardValid = false;
    var scriptCardIsValid = false;
    var createJobCardValid = false;
    var progressBar = $("#setupWizardProgressBar");
    var scriptDiv = $("#createScript");
    var jobGroupDiv = $("#setJobGroup");
    var jobDiv = $("#createJob");

    var init = function () {
        initTabEventListeners();
        initTabNavigation();
    }

    var initTabEventListeners = function () {
        scriptDiv.get(0).addEventListener('changed', function () {
            scriptCardIsValid = OpenSpeedMonitor.MeasurementSetupWizard.CreateScriptCard.isValid();
            validateForm();
        });
        jobGroupDiv.get(0).addEventListener('changed',function () {
            createJobGroupCardValid = OpenSpeedMonitor.MeasurementSetupWizard.CreateJobGroupCard.isValid();
            validateForm();
        })
        jobDiv.get(0).addEventListener('changed',function () {
            createJobCardValid = OpenSpeedMonitor.MeasurementSetupWizard.CreateJobCard.isValid();
            validateForm();
        })
    }

    var initTabNavigation = function () {
        $("#setJobGroubTabNextButton").click(function () {
            $("#setJobGroupTab").parent().toggleClass("active");
            $("#createScriptTab").parent().toggleClass("active");

            if (!$("#createScriptTab").parent().hasClass("wasActive"))
                progressBar.css("width", "37.5%");

            $("#createScriptTab").parent().addClass("wasActive");
        });
        $("#createScriptTabNextButton").click(function () {
            $("#createScriptTab").parent().toggleClass("active");
            $("#selectLocationAndConnectivityTab").parent().toggleClass("active");

            if (!$("#selectLocationAndConnectivityTab").parent().hasClass("wasActive"))
                progressBar.css("width", "62.5%");

            $("#selectLocationAndConnectivityTab").parent().addClass("wasActive");
        });
        $("#selectLocationAndConnectivityTabNextButton").click(function () {
            $("#selectLocationAndConnectivityTab").parent().toggleClass("active");
            $("#createJobTab").parent().toggleClass("active");

            if (!$("#createJobTab").parent().hasClass("wasActive"))
                progressBar.css("width", "100%");

            $("#createJobTab").parent().addClass("wasActive");
        });

        $("#createScriptTabPreviousButton").click(function () {
            $("#createScriptTab").parent().toggleClass("active");
            $("#setJobGroupTab").parent().toggleClass("active");
        });
        $("#selectLocationAndConnectivityTabPreviousButton").click(function () {
            $("#selectLocationAndConnectivityTab").parent().toggleClass("active");
            $("#createScriptTab").parent().toggleClass("active");
        });
        $("#createJobTabPreviousButton").click(function () {
            $("#createJobTab").parent().toggleClass("active");
            $("#selectLocationAndConnectivityTab").parent().toggleClass("active");
        });
    }

    var allCardsValid = function () {
        return createJobGroupCardValid && scriptCardIsValid && createJobCardValid;
    }

    var validateForm = function () {
        if (allCardsValid()) {
            // enable submit button
            formSubmissonButton.prop('disabled', false);
        } else {
            // disable submit button
            formSubmissonButton.prop('disabled', true);
        }
    }

    init();

    return {
    }
})();