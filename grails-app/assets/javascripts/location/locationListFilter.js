/*
 * OpenSpeedMonitor (OSM)
 * Copyright 2014 iteratec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.locationListFilter = (function () {
    var storageUtils = OpenSpeedMonitor.clientSideStorageUtils();
    var filterTextLocalStorage = 'de.iteratec.osm.location.list.filter';
    var filterTextInput = $("#elementFilter");

    function init() {
        var filterText = storageUtils.getFromLocalStorage(filterTextLocalStorage);
        filterTextInput.val(filterText);
        filterTextInput.change(saveState);
        filterTextInput.keyup(saveState);
    }

    var saveState = function () {
        storageUtils.setToLocalStorage(filterTextLocalStorage, filterTextInput.val());
    };

    init();
})();
