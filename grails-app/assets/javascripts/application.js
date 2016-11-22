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
//= require_tree bower_components/jquery
//= require_tree bower_components/jquery-ui
//= require_tree bower_components/bootstrap
//= require_tree bower_components/bootstrap-datepicker
//= require_tree bower_components/chosen
//= require_tree bower_components/clipboard
//= require kickstart/kickstart
//= require kickstart/checkboxes
// = require_self

/**
 * Global namespace for OpenSpeedMonitor application.
 */
var OpenSpeedMonitor = OpenSpeedMonitor || {};

/**
 * Map for URLs to use
 */
OpenSpeedMonitor.urls = OpenSpeedMonitor.urls || {};

/**
 * Loads all registered JavaScript files. Used by microservices.
 * @type {Array}
 */
OpenSpeedMonitor.postLoadUrls = OpenSpeedMonitor.postLoadUrls || [];
OpenSpeedMonitor.postLoadUrls.forEach( function (scriptUrl) {
   $.getScript(scriptUrl);
});

OpenSpeedMonitor.postLoader = (function (){

  var head = document.getElementsByTagName("head")[0];

  var loadJavascript = function(url, async){
    async = async || true;
    var script = document.createElement("script");
    script.setAttribute("src",url);
    script.setAttribute("type","text/javascript");
    script.setAttribute("async",async);
    //script.setAttribute("charset","ISO-8859-1");
    head.appendChild(script);
  }
  var loadStylesheet = function(url){
    var link = document.createElement("link");
    link.rel = "stylesheet";
    link.type = "text/css";
    link.href = url;
    link.media = "all";
    head.appendChild(link);
  }

  return {
    loadJavascript: loadJavascript,
    loadStylesheet: loadStylesheet
  }

})();

/**
 * Global string utilities module.
 * @returns {{
 *      stringToBoolean: publicApi.stringToBoolean}}
 */
OpenSpeedMonitor.stringUtils = function(){
    var publicApi = {
        stringToBoolean: function(string) {

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
        }
    };
    return publicApi;
};

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
OpenSpeedMonitor.clientSideStorageUtils = function(){
    var setCookie = function(key, value, path, shouldExpireInMillisecsFromNow) {
        var expires = new Date();
        expires.setTime(expires.getTime() + shouldExpireInMillisecsFromNow);
        var cookieToSet = key + '=' + btoa(value)  + ';expires=' + expires.toUTCString() + ';path=' + path;
        console.log(cookieToSet);
        document.cookie = cookieToSet;
    };

    var getCookie = function(key) {
        var keyValue = document.cookie.match('(^|;) ?' + key + '=([^;]*)(;|$)');
        return keyValue ? atob(keyValue[2]) : null;
    };

    var setToLocalStorage = function(key, value) {
        try{
            localStorage.setItem(key, value)
        }catch(e){
            console.log('Can\'t write data to local storage: ' + e.message);
        }
    };

    var getFromLocalStorage = function(key) {
        try{
            return localStorage.getItem(key);
        }catch(e){
            console.log('Can\'t read data from local storage: ' + e.message);
        }
        return null;
    };

    /**
     * Reads a whole object in local storage.
     * @param keyObject An object with different properties whose values are the local storage keys.
     * @return object A new object with the same properties as keyObject and values from the local storage.
     */
    var getObjectFromLocalStorage = function(keyObject) {
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
    var setObjectToLocalStorage = function(keyObject, value) {
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

OpenSpeedMonitor.urlUtils = (function() {
	var getAllVars = function() {
		var vars = [];
		var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
		for(var i = 0; i < hashes.length; i++)
		{
			var hash = hashes[i].split('=');
			var key = decodeURIComponent(hash[0]);
			vars.push(key);
			vars[key] = decodeURIComponent(hash[1]);
		}
		return vars;
	};

	var getVar = function(name) {
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
     * @return The list of newly created option elements, sorted by name
     */
    var createOptionsByIdAndName = function (values) {
        var options = [];
        values.sort(function(a, b) {
            return a.name.localeCompare(b.name);
        });
        values.forEach(function(value) {
            options.push($("<option/>", {
                value: value.id,
                text: value.name
            }));
        });
        return options;
    };

    return {
        createOptionsByIdAndName: createOptionsByIdAndName
    };
})();

/**
 * This method will just return the given message. We use it,
 * so you can override it for a specialised delete conformation for the given domain
 */
function domainDeleteConfirmation(message,id,link){
	var confirmMessage = "";
	if(typeof link !== "undefined" && link !== null && link !== ""){
		jQuery.ajax({
			type : 'GET',
			url : link,
			data: {id:id},
			async:   false,
			success: function(result) {
				confirmMessage = message + "<br>" + result;
			},
			error: function(result) {
				confirmMessage = message;
			}});
		return confirmMessage;
	} else{
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

function fireWindowEvent(eventName){
    var event = document.createEvent('Event');
    event.initEvent(eventName, true, true);
    window.dispatchEvent(event);
}

$( document ).ready( function(){

    $('ul.nav li.dropdown').hover(
        function() { $(this).children('.dropdown-menu').stop(true, true).delay(100).fadeIn(); },
        function() { $(this).children('.dropdown-menu').stop(true, true).delay(100).fadeOut(); }
    );
    $('li.dropdown-submenu').hover(
        function() { $(this).children('ul').stop(true, true).delay(100).fadeIn(); },
        function() { $(this).children('ul').stop(true, true).delay(100).fadeOut(); }
    );

});
