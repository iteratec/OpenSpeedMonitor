"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.InfrastructureSetupWizard = OpenSpeedMonitor.InfrastructureSetupWizard || {};

OpenSpeedMonitor.InfrastructureSetupWizard.Wizard = (function () {

    var serverSelectBox = $("#serverSelect");
    var WPTKeyInputFields = $("#WPTKeyFields");
    var CustomServerFields = $("#CustomServerFields");
    var WPTKeyInputInfo = $("#WPTKeyInfo");
    var CustomServerInfo = $("#CustomServerInfo");
    var finishButton = $("#finishButton");
    var WPTKeyField = $("#inputWPTKey");
    var ServerNameField = $("#inputServerName");
    var ServerAddressField = $("#inputServerAddress");
    var invalidAddressText = $("#invalidAddress");
    var progressBar = $("#setupWizardProgressBar");

    var spinner = OpenSpeedMonitor.Spinner("#chart-container");

    var init = function () {
        serverSelectBox.change(updateInputFields);
        WPTKeyField.on('input propertychange paste',validate);
        ServerNameField.on('input propertychange paste',validate);
        ServerAddressField.on('input propertychange paste',validate);
        finishButton.click(function () {
            spinner.start();
        });
        validate();
        updateInputFields();
    };

    var validate = function() {
        if (serverSelectBox.val() == "WPTServer") {
            finishButton.prop('disabled', !WPTKeyField.val());
        }
        else {
            var validURL = ValidURL(ServerAddressField.val());
            finishButton.prop('disabled', !(ServerNameField.val() && validURL));
            if (validURL || !ServerAddressField.val()) {
                invalidAddressText.addClass("hidden");
            }
            else {
                invalidAddressText.removeClass("hidden");
            }
        }
    };

    function ValidURL(str) {
        var pattern = new RegExp('^(https?:\\/\\/)?'+ // protocol
            '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|'+ // domain name
            '((\\d{1,3}\\.){3}\\d{1,3}))'+ // OR ip (v4) address
            '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*'+ // port and path
            '(\\?[;&a-z\\d%_.~+=-]*)?'+ // query string
            '(\\#[-a-z\\d_]*)?$','i'); // fragment locater
        return pattern.test(str);
    }

    var updateInputFields = function () {
        if (serverSelectBox.val() == "WPTServer") {
            WPTKeyInputFields.removeClass("hidden");
            CustomServerFields.addClass("hidden");
            WPTKeyInputInfo.removeClass("hidden");
            CustomServerInfo.addClass("hidden");
        } else {
            // newJobGroup selected
            WPTKeyInputFields.addClass("hidden");
            CustomServerFields.removeClass("hidden");
            WPTKeyInputInfo.addClass("hidden");
            CustomServerInfo.removeClass("hidden");
        }
        validate();
    };

    init();

    return {}
})();

