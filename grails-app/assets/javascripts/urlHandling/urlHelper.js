"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};
OpenSpeedMonitor.ChartModules.UrlHandling = OpenSpeedMonitor.ChartModules.UrlHandling || {};

OpenSpeedMonitor.ChartModules.UrlHandling.UrlHelper = (function () {

    var getUrlParameter = function () {
        var parameterMap = {};
        var currentUrl = window.location.href;

        // remove html anchor if exists
        var anchorIndex = currentUrl.indexOf('#');
        if (anchorIndex > 0) {
            currentUrl = currentUrl.replace(/#\w*/, "")
        }

        var paramIndex = currentUrl.indexOf('?');
        if (paramIndex < 0)
            return parameterMap;

        var parameters = currentUrl.slice(paramIndex + 1).split('&');
        for (var i = 0; i < parameters.length; i++) {
            var parameterPair = parameters[i].split('=');
            var parameterKey = parameterPair[0];
            var parameterValue = decodeURIComponent(parameterPair[1] || "");
            if (!parameterMap[parameterKey]) {
                parameterMap[parameterKey] = parameterValue;
            } else if (parameterMap[parameterKey].constructor === Array) {
                parameterMap[parameterKey].push(parameterValue);
            } else {
                parameterMap[parameterKey] = [parameterMap[parameterKey], parameterValue];
            }
        }
        return parameterMap;
    };

    return {
        getUrlParameter: getUrlParameter
    }
})();
