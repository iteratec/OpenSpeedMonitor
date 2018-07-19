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
//= require_tree node_modules/jquery
//= require_tree node_modules/jquery-ui-dist
//= require_tree node_modules/jquery-migrate
//= require_tree node_modules/bootstrap
//= require_tree node_modules/bootstrap-colorpicker/dist/js/
//= require_tree node_modules/chosen-js
//= require_tree node_modules/clipboard
//= require ${grails.util.Environment.currentEnvironment == grails.util.Environment.PRODUCTION ? 'node_modules/vue/dist/vue.min.js' : 'node_modules/vue/dist/vue.js'}
//= require kickstart/checkboxes
//= require spinner
//= require node_modules/bootstrap-validator/js/validator
//= require_self

delete $.fn.datepicker; // so it can be replaced by air-datepicker

/**
 * Global namespace for OpenSpeedMonitor application.
 */
var OpenSpeedMonitor = OpenSpeedMonitor || {};

/**
 * Map for URLs to use
 */
OpenSpeedMonitor.urls = OpenSpeedMonitor.urls || {};

/**
 * Map for i18n to use
 */
OpenSpeedMonitor.i18n = OpenSpeedMonitor.i18n || {};


/**
 * Loads all registered JavaScript files. Used by microservices.
 * @type {Array}
 */
OpenSpeedMonitor.postLoadUrls = OpenSpeedMonitor.postLoadUrls || [];
OpenSpeedMonitor.postLoadUrls.forEach(function (scriptUrl) {
    $.getScript(scriptUrl);
});

OpenSpeedMonitor.postLoader = (function () {
    var postloadedScripts = {};
    var onLoadedHandlers = {};

    var head = document.getElementsByTagName("head")[0];

    var loadJavascript = function (url, name) {
        var script = document.createElement("script");
        script.onload = function () {
            postloadedScripts[name] = true;
            checkCallHandler(name);
            fireWindowEvent("" + name + "Loaded");
        };
        script.setAttribute("src", url);
        script.setAttribute("type", "text/javascript");
        script.setAttribute("async", "async");
        head.appendChild(script);
    };

    var loadStylesheet = function (url) {
        var link = document.createElement("link");
        link.rel = "stylesheet";
        link.type = "text/css";
        link.href = url;
        link.media = "all";
        head.appendChild(link);
    };

    var onLoaded = function(dependencies, callback) {
        if (typeof dependencies === 'string' || dependencies instanceof String) {
            dependencies = [dependencies];
        }
        var dependencyId = dependencies.sort().join(";");
        if (!onLoadedHandlers[dependencyId]) {
            onLoadedHandlers[dependencyId] = [];
        }
        if (onLoadedHandlers[dependencyId].indexOf(callback) < 0) {
            onLoadedHandlers[dependencyId].push(callback);
        }
        if (allDependenciesAreLoaded(dependencies)) {
            callback();
        }
    };

    var checkCallHandler = function(nameOfNewLoaded) {
        Object.keys(onLoadedHandlers).forEach(function(dependencyId) {
            var dependencies = dependencyId.split(";");
            if (dependencies.indexOf(nameOfNewLoaded) < 0) {
                return;
            }
            if (allDependenciesAreLoaded(dependencies)) {
                onLoadedHandlers[dependencyId].forEach(function(callback) {
                    callback();
                });
            }
        });
    };

    var allDependenciesAreLoaded = function (dependencies) {
        return dependencies.every(function (d) { return !!postloadedScripts[d]; });
    };

    return {
        loadJavascript: loadJavascript,
        loadStylesheet: loadStylesheet,
        onLoaded: onLoaded
    }

})();

/**
 * Global string utilities module.
 */
OpenSpeedMonitor.stringUtils = (function () {
    var stringToBoolean = function (string) {
        if (!string) {
            return false;
        }
        switch (string.toLowerCase()) {
            case "true":
            case "yes":
            case "1":
            case "on":
                return true;
            case "false":
            case "no":
            case "0":
            case "off":
            case null:
                return false;
            default:
                return false;
        }
    };

    var isNumeric = function (string) {
        return !isNaN(string);
    };

    return {
        isNumeric: isNumeric,
        stringToBoolean: stringToBoolean
    };
})();

/**
 * Global module providing functionalities for storage of data in the browser.
 *
 * @returns {{
 *      setCookie: setCookie,
 *      getCookie: getCookie,
 *      setToLocalStorage: setToLocalStorage,
 *      getFromLocalStorage: getFromLocalStorage,
 *      getObjectFromLocalStorage: getObjectFromLocalStorage,
 *      setObjectToLocalStorage: setObjectToLocalStorage}}
 */
OpenSpeedMonitor.clientSideStorageUtils = function () {
    var setCookie = function (key, value, path, shouldExpireInMillisecsFromNow) {
        var expires = new Date();
        expires.setTime(expires.getTime() + shouldExpireInMillisecsFromNow);
        var cookieToSet = key + '=' + btoa(value) + ';expires=' + expires.toUTCString() + ';path=' + path;
        console.log(cookieToSet);
        document.cookie = cookieToSet;
    };

    var getCookie = function (key) {
        var keyValue = document.cookie.match('(^|;) ?' + key + '=([^;]*)(;|$)');
        return keyValue ? atob(keyValue[2]) : null;
    };

    var setToLocalStorage = function (key, value) {
        try {
            localStorage.setItem(key, value)
        } catch (e) {
            console.log('Can\'t write data to local storage: ' + e.message);
        }
    };

    var getFromLocalStorage = function (key) {
        try {
            return localStorage.getItem(key);
        } catch (e) {
            console.log('Can\'t read data from local storage: ' + e.message);
        }
        return null;
    };

    /**
     * Reads a whole object in local storage.
     * @param keyObject An object with different properties whose values are the local storage keys.
     * @return object A new object with the same properties as keyObject and values from the local storage.
     */
    var getObjectFromLocalStorage = function (keyObject) {
        var hasValues = false;
        var values = {};
        for (var property in keyObject) {
            if (keyObject.hasOwnProperty(property)) {
                var value = getFromLocalStorage(keyObject[property]);
                hasValues = hasValues || value !== null;
                values[property] = value;
            }
        }
        return hasValues ? values : null;
    };

    /**
     * Saves a whole object in local storage
     * @param keyObject An object whose properties are the same as in value. The value of each property is the local storage key.
     * @param value The object to save with the same properties as in keyObject.
     */
    var setObjectToLocalStorage = function (keyObject, value) {
        for (var property in keyObject) {
            if (keyObject.hasOwnProperty(property)) {
                setToLocalStorage(keyObject[property], value[property]);
            }
        }
    };

    return {
        setCookie: setCookie,
        getCookie: getCookie,
        setToLocalStorage: setToLocalStorage,
        getFromLocalStorage: getFromLocalStorage,
        getObjectFromLocalStorage: getObjectFromLocalStorage,
        setObjectToLocalStorage: setObjectToLocalStorage
    };
};

OpenSpeedMonitor.urlUtils = (function () {
    var getAllVars = function () {
        var vars = [];
        var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
        for (var i = 0; i < hashes.length; i++) {
            var hash = hashes[i].split('=');
            var key = decodeURIComponent(hash[0]);
            vars.push(key);
            vars[key] = decodeURIComponent(hash[1]);
        }
        return vars;
    };

    var getVar = function (name) {
        return getAllVars()[name];
    };

    return {
        getAllVars: getAllVars,
        getVar: getVar
    };
})();

OpenSpeedMonitor.domUtils = (function () {
    /**
     * Creates option elements by a list of values which all contain an 'id' and a 'name'
     * @param values Array with objects containing id and name
     * @return Array The list of newly created option elements, sorted by name
     */
    var createOptionsByIdAndName = function (values) {
        var options = [];
        if (!values) {
            return [];
        }
        values.sort(function (a, b) {
            return a.name.localeCompare(b.name);
        });
        values.forEach(function (value) {
            if (value && value.id && value.name) {
                options.push($("<option/>", {
                    value: value.id,
                    text: value.name
                }));
            }
        });
        return options;
    };
    /**
     * Gets all values of all option elements in a select element
     * @param selectElement The select element with options as children
     * @returns Array An array with all values of all options
     */
    var getAllOptionValues = function (selectElement) {
        return $.map($(selectElement).find("option"), function (option) {
            return option.value;
        });
    };

    var hasAllOptionsSelected = function (selectElement) {
        selectElement = $(selectElement);
        return selectElement.find(":not(:selected)").length == 0 && selectElement.find("option").length > 0;
    };

    /**
     * Updates a select element with new values
     * @param selectElement The select element to update
     * @param idAndNameList A list of objects with id and name
     * @param noResultsText If set, a disabled option with this text is appended if the idAndNameList is empty
     */
    var updateSelectOptions = function (selectElement, idAndNameList, noResultsText) {
        selectElement = $(selectElement);
        var selection = selectElement.val();
        selectElement.empty();
        selectElement.append(OpenSpeedMonitor.domUtils.createOptionsByIdAndName(idAndNameList));
        if (!selectElement.children().length && noResultsText) {
            selectElement.append($("<option/>", {disabled: "disabled", text: noResultsText}));
        }
        selectElement.val(selection);
    };

    /**
     * Deselects all selected options in a select element
     * @param selectElement The select element
     * @param avoidEvent Optional. If true, no change event will be triggered
     */
    var deselectAllOptions = function (selectElement, avoidEvent) {
        selectElement = $(selectElement);
        selectElement.find("option:selected").prop('selected', false);
        if (!avoidEvent) {
            selectElement.trigger("change");
        }
    };

    return {
        createOptionsByIdAndName: createOptionsByIdAndName,
        getAllOptionValues: getAllOptionValues,
        hasAllOptionsSelected: hasAllOptionsSelected,
        updateSelectOptions: updateSelectOptions,
        deselectAllOptions: deselectAllOptions,
    };
})();

/**
 * This method will just return the given message. We use it,
 * so you can override it for a specialised delete conformation for the given domain
 */
function domainDeleteConfirmation(message, id, link) {
    var confirmMessage = "";
    if (typeof link !== "undefined" && link !== null && link !== "") {
        jQuery.ajax({
            type: 'GET',
            url: link,
            data: {id: id},
            async: false,
            success: function (result) {
                confirmMessage = message + "<br>" + result;
            },
            error: function (result) {
                confirmMessage = message;
            }
        });
        return confirmMessage;
    } else {
        return message;
    }

}


/**
 * Necessary to prevent Chosen dropdown from being cut off in tabs
 * @see http://stackoverflow.com/a/21375637
 * TODO: after first selection dropdown of chosen select disappears after click. So one have to hold the mouse button while selection :(
 */
function fixChosen() {
    var els = $(".chosen");
    els.on("chosen:showing_dropdown", function (event, chosen) {
        $(this).parents("div").css("overflow", "visible");
    });
    els.on("chosen:hiding_dropdown", function () {
        var $parent = $(this).parents("div");
        // See if we need to reset the overflow or not.
        var noOtherExpanded = $('.chosen-with-drop', $parent).length == 0;
        if (noOtherExpanded)
            $parent.css("overflow", "");
        //$('.tab-content').scrollTop(($('.tab-content').height()*2));
    });
}

function makeChosenAccessibleForBootstrapValidation(){
    //bootstraps validation highlighting only works, of the element to be highlighted has the class "form-control"
    //since the chosen plugin provides us with this form, we have no other way then adding the class dynamically
    $(".chosen-single").addClass("form-control");
}


function fireWindowEvent(eventName) {
    var event = document.createEvent('Event');
    event.initEvent(eventName, true, true);
    window.dispatchEvent(event);
}

$("#main-navbar .dropdown-toggle").on('click', function (event) {
    event.preventDefault();
    var parent = $(this).parent();
    var wasOpen = parent.hasClass("open");
    $("#main-navbar .dropdown.open").removeClass("open").attr('aria-expanded', 'false');
    if (!wasOpen) {
        parent.addClass("open");
        $(this).trigger('focus').attr('aria-expanded', 'true')
    }
});

/**
 * Adds a cron validator to the form. The cron input field must define the attribute: data-cron, to use this.
 * @param $form the form which holds a cron input
 * @param $outputElement will receive a text which describws the entered cron
 */
function addCronValidatorToForm($form, $outputElement){
    $form.validator({
        custom: {
            'cron': function() {
                var error = "";
                $.ajax({
                    url: OpenSpeedMonitor.urls.cronExpressionNextExecution,
                    data: { cronExpression: cronStringInputField.val()},
                    dataType: "text",
                    type: 'GET',
                    async:false,
                    success: function (data) {
                        $outputElement.text(prettyCron.toString(cronStringInputField.val()));
                    },
                    error: function (e, status) {
                        if (status === "error") {
                            error = e.responseText;
                            $outputElement.text("");
                        } else {
                            console.error(e);
                        }
                    }
                });
                return error;
            }
        }
    });
}
