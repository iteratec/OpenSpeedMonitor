//= require /bower_components/d3/d3.min.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageAggregation = (function (selector) {
    var svg = d3.select(selector);
    var chartBars = [];

    var setData = function (data) {
        // setDataForHeader(data);
        // setDataForLegend(data);
        // setDataForBarScore(data);
        // setDataForSideLabels(data);
        setDataForBars(data);
    };

    var setDataForBars = function (data) {
        var seriesByMeasurand = d3.nest()
            .key(function(d) { return d.measurand; })
            .entries(data.series);
        console.log(seriesByMeasurand);
    };

    var render = function () {
        svg.append("circle")
           .attr("cx", 30)
           .attr("cy", 30)
           .attr("r", 20);
    };

    return {
        render: render,
        setData: setData
    };

});
