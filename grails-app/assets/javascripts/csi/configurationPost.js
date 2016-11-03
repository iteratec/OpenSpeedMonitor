/*
 * OpenSpeedMonitor (OSM)
 * Copyright 2014 iteratec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//= require defaultMappingCsvValidator.js
//= require deleteCsiConfigValidation.js
//= require pageMappingDeletion.js
//= require_self

function changeCsiConfiguration(id) {
    window.location.href=POSTLOADED.link_CsiConfigurationConfigurations + "/" + id;
}

function callControllerActionWithId(controllerActionLink) {
    controllerActionLink += "/" + actualCsiConfigurationId;

    window.location.href=controllerActionLink;
}
function copyCsiConfiguration(csiConfigurations) {

    var linkToCopyCsiConfig = promptForNewName(
        POSTLOADED.link_CsiConfigurationSaveCopy, POSTLOADED.i18n_nameAlreadyExistMsg, csiConfigurations);

    if(linkToCopyCsiConfig) {
        var runningSpinner;
        $.ajax({
            url: linkToCopyCsiConfig,
            beforeSend: function() {
                var spinnerParent = document.getElementById('copyCsiConfigurationSpinner');
                runningSpinner = POSTLOADED.getLargeSpinner('#000', '50%', '50%');
                spinnerParent.appendChild(runningSpinner.el);
            },
            complete: function(xhr, textStatus) {

                $('#headerCsiConfLabel').text(xhr.responseJSON.newCsiConfigLabel);
                actualCsiConfigurationId = xhr.responseJSON.newCsiConfigId;
                actualCsiConfigurationLabel = xhr.responseJSON.newCsiConfigLabel;
                allCsiConfigurations = xhr.responseJSON.allCsiConfigurations;

                $('#uploadBrowserConnectivityWeightsCsiConfigurationId').val(actualCsiConfigurationId);
                $('#uploadPageWeightsCsiConfigurationId').val(actualCsiConfigurationId);
                $('#uploadHourOfDayWeightsCsiConfigurationId').val(actualCsiConfigurationId);

                // refreshCsiConfigurationSwitchMenu();
                runningSpinner.stop();

            }
        })
    }
}
/**
 * Asks for label of new csi config. If label is empty or a config with that label already exists
 * link will be broken (this method delivers false).
 * Otherwise previous and new label is added to links href before it can be followed.
 * @param anchor-link
 *          Link where the onclick-handler links to
 * @param nameExistsErrorMessage
 *          Internationalized error message.
 * @returns {boolean}
 *          True if new label chosen by user is ok, False otherwise.
 */
function promptForNewName(anchorLink, nameExistsErrorMessage, csiConfigurations) {

    var actualLabel = $('#headerCsiConfLabel').text();
    var newName = prompt(
        POSTLOADED.i18n_duplicatePrompt,
        actualLabel + POSTLOADED.i18n_duplicateSuffix
    );
    if (newName === null || newName === '') {
        return false;
    }

    for (var i = 0; i < csiConfigurations.length; i++) {
        var config = csiConfigurations[i];
        var configName = config[1];
        if (configName == newName) {
            alert(nameExistsErrorMessage);
            return false;
        }
    }

    anchorLink += '?'+'label='+newName+'&sourceCsiConfigLabel='+actualLabel
    return anchorLink;
}