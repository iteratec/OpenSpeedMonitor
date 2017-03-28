"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.MeasurementSetupWizard = OpenSpeedMonitor.MeasurementSetupWizard || {};

OpenSpeedMonitor.MeasurementSetupWizard.Wizard = (function () {

    var formSubmissonButton = $("#createJobTabCreationButton");
    var setJobGroupCardValid = false;
    var scriptCardIsValid = false;
    var createJobCardValid = false;
    var progressBar = $("#setupWizardProgressBar");
    var scriptDiv = $("#createScript");
    var jobGroupDiv = $("#setJobGroup");
    var jobDiv = $("#createJob");
    var setJobGroupTabNextButton = $("#setJobGroubTabNextButton");
    var createScriptTabNextButton = $("#createScriptTabNextButton");
    var locationAndConnectivityDiv = $("#selectLocationAndConnectivity");

    var init = function () {
        initTabEventListeners();
        initTabNavigation();
    }

    var initTabEventListeners = function () {
        jobGroupDiv.on('changed', function () {
            var jobGroupCard = OpenSpeedMonitor.MeasurementSetupWizard.CreateJobGroupCard;
            setJobGroupCardValid = jobGroupCard.isValid();
            var scriptCard = OpenSpeedMonitor.MeasurementSetupWizard.CreateScriptCard;
            if (scriptCard) {
                scriptCard.setDefaultScriptName(jobGroupCard.getJobGroup());
            }
            validateForm();
        })
        scriptDiv.on('changed', function () {
            scriptCardIsValid = OpenSpeedMonitor.MeasurementSetupWizard.CreateScriptCard.isValid();
            validateForm();
            setDefaultJobName();
        });
        locationAndConnectivityDiv.on("changed", function() {
            setDefaultJobName();
        })
        jobDiv.on('changed', function () {
            createJobCardValid = OpenSpeedMonitor.MeasurementSetupWizard.CreateJobCard.isValid();
            validateForm();
        })
    }

    var setDefaultJobName = function () {
        if (!OpenSpeedMonitor.MeasurementSetupWizard.CreateJobCard) {
            return;
        }
        var scriptCard = OpenSpeedMonitor.MeasurementSetupWizard.CreateScriptCard;
        var scriptName = scriptCard ? scriptCard.getScriptName() : "";

        var locationConnectivityCard = OpenSpeedMonitor.MeasurementSetupWizard.SelectLocationAndConnectivity;
        var browser = locationConnectivityCard ? locationConnectivityCard.getBrowserOrLocationName() : "";

        var defaultJobName = (scriptName + " " + browser).trim();
        OpenSpeedMonitor.MeasurementSetupWizard.CreateJobCard.setDefaultJobName(defaultJobName);
    }

    var initTabNavigation = function () {
        setJobGroupTabNextButton.click(function () {
            $("#setJobGroupTab").parent().toggleClass("active");
            $("#createScriptTab").parent().toggleClass("active");

            if (!$("#createScriptTab").parent().hasClass("wasActive"))
                progressBar.css("width", "37.5%");

            $("#createScriptTab").parent().addClass("wasActive");
        });
        createScriptTabNextButton.click(function () {
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
        return setJobGroupCardValid && scriptCardIsValid && createJobCardValid;
    }

    var validateForm = function () {
        setJobGroupTabNextButton.prop('disabled', !setJobGroupCardValid);
        createScriptTabNextButton.prop('disabled', !scriptCardIsValid);
        formSubmissonButton.prop('disabled', !allCardsValid());
    }

    init();

    return {}
})();

