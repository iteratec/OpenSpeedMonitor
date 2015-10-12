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
/**
 * This class contains all js functionality of osm, that is not necessary for above-the-fold content.
 * Gets loaded after window.load event and is published globally under namespace POSTLOADED.
 * Calls custom Event PostLoadedScriptArrived. An event handler is registered in _common/_postloadInitializedJS.gsp
 * which publishes PostLoaded functionality.
 *
 * @param dataFromGsp This contains all init data and is filled in _common/_postloadInitializedJS.gsp
 * @constructor
 */
function PostLoaded(dataFromGsp){

    this.i18n_duplicatePrompt = dataFromGsp.i18n_duplicatePrompt
    this.i18n_duplicateSuffix = dataFromGsp.i18n_duplicateSuffix
    this.deletionConfirmMessage = dataFromGsp.deletionConfirmMessage
    this.idOfItemToDelete = dataFromGsp.idOfItemToDelete

    this.init = function(){

    }
    this.promptForDuplicateName = function() {
        var newName = prompt(this.i18n_duplicatePrompt, $('input#label').val() + this.i18n_duplicateSuffix);
        if (newName != null && newName != '') {
            $('input#label').val(newName);
            return true;
        } else {
            return false;
        }
    }
    this.setDeleteConfirmationInformations = function(link){
        setTimeout(function(){
            var opts = {
                lines: 10, // The number of lines to draw
                length: 3, // The length of each line
                width: 2, // The line thickness
                radius: 5, // The radius of the inner circle
                corners: 1, // Corner roundness (0..1)
                rotate: 0, // The rotation offset
                direction: 1, // 1: clockwise, -1: counterclockwise
                color: '#000', // #rgb or #rrggbb or array of colors
                speed: 1, // Rounds per second
                trail: 60, // Afterglow percentage
                shadow: true, // Whether to render a shadow
                hwaccel: false, // Whether to use hardware acceleration
                className: 'spinner', // The CSS class to assign to the spinner
                zIndex: 2e9, // The z-index (defaults to 2000000000)
                top: 'auto', // Top position relative to parent in px
                left: '50%' // Left position relative to parent in px
            };
            var spinner = new Spinner(opts).spin(document.getElementById('spinner-position'));
            var text = domainDeleteConfirmation(this.deletionConfirmMessage, this.idOfItemToDelete, link);
            spinner.stop();
            $('#DeleteModal').find('p').html(text);
        },1);
    }
    this.init()
}
fireWindowEvent('PostLoadedScriptArrived');