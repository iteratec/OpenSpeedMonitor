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

function removeSelectedPageMapping(removePageMappingLink, csiConfId){

    var nameOfSelectedPage = pageMappingDiagram.selectedName;

    if(nameOfSelectedPage != ""){

        if(confirm(POSTLOADED.i18n_deletePageMappingConfirmation + "\n\n" + nameOfSelectedPage) == false){
            return;
        }
        var btnRemovePageMapping = $('#removePageMapping');
        btnRemovePageMapping.prop("disabled",true);

        var rowDiv = document.createElement("div");
        rowDiv.classList.add("row-fluid");
        var spanDiv = document.createElement("div");
        spanDiv.classList.add("span10");

        var spinnerDiv = document.createElement("div");
        spinnerDiv.classList.add("span1");
        spinnerDiv.id = "spinner-position";
        spinnerDiv.classList.add("spinner-content-spinner");

        var messageDiv = document.createElement("div");
        messageDiv.classList.add("span11");
        messageDiv.classList.add("spinner-content-message");
        messageDiv.classList.add("text-info");
        messageDiv.classList.add("text-left");
        messageDiv.id="spinner-message";

        spanDiv.appendChild(spinnerDiv);
        spanDiv.appendChild(messageDiv);
        rowDiv.appendChild(spanDiv);

        var mappingDeletionList = document.getElementById('page-mapping-deletions');
        mappingDeletionList.insertBefore(rowDiv, mappingDeletionList.childNodes[0]);

        var spinnerParent = document.getElementById('spinner-position');
        var spinner = POSTLOADED.getSmallSpinner('#3a87ad', '0', '0');
        spinnerParent.appendChild(spinner.el);

        var strongMessage = document.createElement("strong");
        var textNodeWaitingToFinish = document.createTextNode(
            POSTLOADED.i18n_deletePageMappingProcessing + ": " + nameOfSelectedPage
        );
        strongMessage.appendChild(textNodeWaitingToFinish);
        messageDiv.appendChild(strongMessage);

        $.ajax({
            type: 'POST',
            url: removePageMappingLink,
            data: {
                csiConfId: csiConfId,
                pageName: nameOfSelectedPage
            },
            success: function(data) {
                pageMappingDiagram.clearGraph();
                for(var i = 0; i < graphData.lines.length; i++) {
                    if(graphData.lines[i].name == nameOfSelectedPage) {
                        graphData.lines.splice(i, 1);
                        break;
                    }
                }
                pageMappingDiagram = createMultiLineGraph(graphData, 'page_csi_mappings', true, null, legendEntryClickCallback);
                btnRemovePageMapping.prop("disabled",false);
                var textNodeDone = document.createTextNode(data);
                strongMessage.removeChild(textNodeWaitingToFinish);
                strongMessage.appendChild(textNodeDone);
                spinner.stop();
                return false;
            },
            error: function(result, textStatus, errorThrown) {
                spinner.stop();
                btnRemovePageMapping.prop("disabled",false);
                var textNodeDone = document.createTextNode(textStatus);
                strongMessage.removeChild(textNodeWaitingToFinish);
                strongMessage.appendChild(textNodeDone);
                return false;
            }
        });
    }
};