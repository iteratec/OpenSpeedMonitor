//= require bower_components/d3/d3.min.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.DistributionChart = (function () {

    var init = function () {
        var chartContainer = document.querySelector("#chart-container")
    };

    init();

    return {
        init: init
    };
});