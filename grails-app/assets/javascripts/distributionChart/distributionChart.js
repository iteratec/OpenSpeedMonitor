//= require bower_components/d3/d3.min.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.distributionChart = (function () {
    var chart = null,
        chartData = null;

    var init = function () {
        var chartContainer = document.querySelector("#chart-container")
    };

    var drawChart = function (distributionChartData) {
        var chartData = distributionChartData;
    };

    init();

    return {
        drawChart: drawChart
    };
})();