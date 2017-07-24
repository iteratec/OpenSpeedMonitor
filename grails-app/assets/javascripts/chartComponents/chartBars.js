//= require /bower_components/d3/d3.min.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartComponents = OpenSpeedMonitor.ChartComponents || {};

OpenSpeedMonitor.ChartComponents.ChartBars = (function (selector) {
    var svg = d3.select(selector);

    var setData = function (data) {
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
