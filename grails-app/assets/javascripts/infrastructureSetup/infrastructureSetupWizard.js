"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.InfrastructureSetupWizard = OpenSpeedMonitor.InfrastructureSetupWizard || {};

OpenSpeedMonitor.InfrastructureSetupWizard.Wizard = (function () {

    var form = $("#setServersForm");
    var serverSelectBox = $("#serverSelect");
    var wptKeyInputInfo = $("#wptKeyInfo");
    var customServerInfo = $("#customServerInfo");
    var finishButton = $("#finishButton");
    var serverNameField = $("#serverName");
    var serverUrlField = $("#serverUrl");
    var apiKeyPrompt = $('#apiKeyPrompt')
    var lastCustomServerName, lastCustomServerUrl;

    var spinner = OpenSpeedMonitor.Spinner("#chart-container");

    var init = function () {
        serverSelectBox.change(updateInputFields);
        finishButton.click(function () {
            spinner.start();
        });
        form.validator({
          feedback: {
            success: 'fa-check',
            error: 'fa-times'
          }
        });
        updateInputFields();
    };

    var updateInputFields = function () {

      var OFFICIAL_WPT_URL = "www.webpagetest.org";
      var isOfficialWptServer = serverSelectBox.val() == OFFICIAL_WPT_URL;

      if (isOfficialWptServer){
        lastCustomServerName = serverNameField.val();
        lastCustomServerUrl = serverUrlField.val();
        serverNameField.val(OFFICIAL_WPT_URL);
        serverUrlField.val('http://' + OFFICIAL_WPT_URL);
        form.validator('validate');
      } else {
        serverNameField.val(lastCustomServerName);
        serverUrlField.val(lastCustomServerUrl);
      }

      serverNameField.prop('disabled', isOfficialWptServer);
      serverUrlField.prop('disabled', isOfficialWptServer);
      wptKeyInputInfo.toggleClass("hidden", !isOfficialWptServer);
      customServerInfo.toggleClass("hidden", isOfficialWptServer);
      apiKeyPrompt.toggleClass("hidden", !isOfficialWptServer);

    };

    init();

    return {}
})();

