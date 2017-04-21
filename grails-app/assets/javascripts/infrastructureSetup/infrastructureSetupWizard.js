"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.InfrastructureSetupWizard = OpenSpeedMonitor.InfrastructureSetupWizard || {};

OpenSpeedMonitor.InfrastructureSetupWizard.Wizard = (function () {

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
        initTabNavigation();
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

    init();

    return {}
})();

