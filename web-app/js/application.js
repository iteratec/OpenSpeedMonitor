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

if (typeof jQuery !== 'undefined') {
	(function($) {
		$('#spinner').ajaxStart(function() {
			$(this).fadeIn();
		}).ajaxStop(function() {
			$(this).fadeOut();
		});
	})(jQuery);
}

jQuery('ul.nav li.dropdown').hover(function() {
  jQuery(this).children('.dropdown-menu').stop(true, true).delay(100).fadeIn();
}, function() {
  jQuery(this).children('.dropdown-menu').stop(true, true).delay(100).fadeOut();
});

jQuery('li.dropdown-submenu').hover(function() {
  jQuery(this).children('ul').stop(true, true).delay(100).fadeIn();
}, function() {
  jQuery(this).children('ul').stop(true, true).delay(100).fadeOut();
});

function stringToBoolean(string) {
	if(!string) return false;
	switch(string.toLowerCase()){
		case "true": case "yes": case "1": case "on": return true;
		case "false": case "no": case "0": case "off": case null: return false;
		default: return false;
	}
}
function setCookie(key, value, path, shouldExpireInMillisecsFromNow) {
	var expires = new Date();
	expires.setTime(expires.getTime() + shouldExpireInMillisecsFromNow);
	var cookieToSet = key + '=' + btoa(value)  + ';expires=' + expires.toUTCString() + ';path=' + path;
	console.log(cookieToSet);
	document.cookie = cookieToSet;
}
function getCookie(key) {
	var keyValue = document.cookie.match('(^|;) ?' + key + '=([^;]*)(;|$)');
	return keyValue ? atob(keyValue[2]) : null;
}
function setToLocalStorage(key, value) {
	try{
		localStorage.setItem(key, value)
	}catch(e){
		console.log('Can\'t write data to local storage: ' + e.message);
	}
}
function getFromLocalStorage(key) {
	try{
		return localStorage.getItem(key);
	}catch(e){
		console.log('Can\'t read data from local storage: ' + e.message);
	}
	return null;
}
$.extend({
	getUrlVars: function(){
		var vars = [], hash;
		var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
		for(var i = 0; i < hashes.length; i++)
		{
			var hash = hashes[i].split('=');
			var key = decodeURIComponent(hash[0]);
			vars.push(key);
			vars[key] = decodeURIComponent(hash[1]);
		}
		return vars;
	},
	getUrlVar: function(name){
		return $.getUrlVars()[name];
	}
});
function domainDeleteConfirmation(message,id){
    var defaultMessage = "Are you sure?"
    console.log("def");
    return confirm(message ? message : defaultMessage);

}
