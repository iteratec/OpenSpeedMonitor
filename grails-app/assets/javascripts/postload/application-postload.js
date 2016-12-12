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
 * This class contains all global js functionality of osm, that is not necessary for above-the-fold content.
 * This script is loaded after window.load event.
 *
 * This script Calls custom Event PostLoadedScriptArrived. An event handler is registered in _common/_postloadInitializedJS.gsp
 * which publishes PostLoaded functionality under namespace OpenSpeedMonitor.postLoaded by instantiate an instance of class PostLoaded.
 *
 * @param dataFromGsp This contains all init data and is filled in _common/_postloadInitializedJS.gsp
 * @constructor
 */
//= require bower_components/spin.js/spin.js
//= require_self
var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.postLoaded = (function() {
    var setDeleteConfirmationInformations = function(link){
        setTimeout (function () {
            //TODO find out why this. doesn't work
            var text = domainDeleteConfirmation( OpenSpeedMonitor.i18n.deletionConfirmMessage, OpenSpeedMonitor.postLoaded.idOfItemToDelete, link);
            $('#DeleteModal').find('p').html(text);
            },
            10
        );
    };

    return {
        setDeleteConfirmationInformations: setDeleteConfirmationInformations
    };
})();

$('.dropdown-toggle').dropdown();
fireWindowEvent('PostLoadedScriptArrived');
