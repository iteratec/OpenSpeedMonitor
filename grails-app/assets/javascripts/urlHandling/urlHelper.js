"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.UrlHandling = OpenSpeedMonitor.ChartModules.UrlHandling || {};

OpenSpeedMonitor.ChartModules.UrlHandling.UrlHelper = (function () {
    var getUrlParameter = function () {
        var vars = [], hash;
        var currentUrl = window.location.href;

        // remove html anchor if exists
        var anchorIndex = currentUrl.indexOf('#');
        if (anchorIndex > 0) {
            currentUrl = currentUrl.replace(/#\w*/, "")
        }

        var paramIndex = currentUrl.indexOf('?');
        if (paramIndex < 0)
            return vars;

        var hashes = currentUrl.slice(paramIndex + 1).split('&');
        for (var i = 0; i < hashes.length; i++) {
            hash = hashes[i].split('=');
            var currentValue = vars[hash[0]];
            if (currentValue == null) {
                vars.push(hash[0]);
                vars[hash[0]] = hash[1];
            } else if (currentValue.constructor === Array) {
                vars[hash[0]].push(hash[1]);
            } else {
                vars[hash[0]] = [vars[hash[0]], hash[1]]
            }
        }
        return vars;
    };

    return {
        getUrlParameter: getUrlParameter
    }
})();