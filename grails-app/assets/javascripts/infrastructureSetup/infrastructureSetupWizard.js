"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.InfrastructureSetupWizard = OpenSpeedMonitor.InfrastructureSetupWizard || {};

OpenSpeedMonitor.InfrastructureSetupWizard.Wizard = (function () {

    var serverSelectBox = $("#serverSelect");
    var wptKeyInputFields = $("#wptKeyFields");
    var customServerFields = $("#customServerFields");
    var wptKeyInputInfo = $("#wptKeyInfo");
    var customServerInfo = $("#customServerInfo");
    var finishButton = $("#finishButton");
    var wptKeyField = $("#inputWptKey");
    var serverNameField = $("#inputServerName");
    var serverAddressField = $("#inputServerAddress");
    var invalidAddressText = $("#invalidAddress");
    var progressBar = $("#setupWizardProgressBar");

    var spinner = OpenSpeedMonitor.Spinner("#chart-container");

    var init = function () {
        serverSelectBox.change(updateInputFields);
        wptKeyField.on('input propertychange paste',validate);
        serverNameField.on('input propertychange paste',validate);
        serverAddressField.on('input propertychange paste',validate);
        finishButton.click(function () {
            spinner.start();
        });
        validate();
        updateInputFields();
    };

    var validate = function() {
        if (serverSelectBox.val() == "WPTServer") {
            finishButton.prop('disabled', !wptKeyField.val());
        }
        else {
            var validUrl = validateUrl(serverAddressField.val());
            finishButton.prop('disabled', !(serverNameField.val() && validUrl));
            invalidAddressText.toggleClass("hidden",validUrl || !serverAddressField.val());
        }
    };

    var validateUrl = function (value) {
        var pattern = new RegExp('^(https?:\\/\\/)?'+ // protocol
            '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)*(([a-z\\d]([a-z\\d-]*[a-z\\d])*))|' + // domain name
            '((\\d{1,3}\\.){3}\\d{1,3}))'+ // OR ip (v4) address
            '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*'+ // port and path
            '(\\?[;&a-z\\d%_.~+=-]*)?'+ // query string
            '(\\#[-a-z\\d_]*)?$','i'); // fragment locater
        return pattern.test(value);
    }

    var updateInputFields = function () {
        var isWptServer = serverSelectBox.val() == "WPTServer";
        wptKeyInputInfo.toggleClass("hidden", !isWptServer);
        wptKeyInputFields.toggleClass("hidden", !isWptServer);
        customServerInfo.toggleClass("hidden", isWptServer);
        customServerFields.toggleClass("hidden", isWptServer);
        validate();
    };

    init();

    return {}
})();

