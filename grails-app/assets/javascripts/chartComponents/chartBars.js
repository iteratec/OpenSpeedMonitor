//= require /bower_components/d3/d3.min.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartComponents = OpenSpeedMonitor.ChartComponents || {};

OpenSpeedMonitor.ChartComponents.ChartBars = (function () {
    var data = null;
    var height = null;
    var width = null;
    var barBand = OpenSpeedMonitor.ChartComponents.ChartBars.BarBand;
    var barColor = null;
    var transitionDuration = 1000;

    var setData = function (componentData) {
        data = componentData.values;
        height = componentData.height;
        width = componentData.width;
        barColor = componentData.color;
    };

    var render = function (selector) {
        var xScale = d3.scale.linear().range([0, width]);
        var yScale = d3.scale.ordinal().rangeBands([0, height]);

        xScale.domain([0, d3.max(data, function(d) { return d.value; })]);
        yScale.domain(data.map(function(d) { return d.page; }));

        var bars = d3.select(selector).selectAll(".bar").data(data, function (d) {
            return d.page;
        });
        renderEnter(bars.enter(), yScale);
        renderExit(bars.exit());
        renderUpdate(bars, xScale, yScale);
    };

    var renderEnter = function (enterSelection) {
        enterSelection.append("rect")
            .attr("class", "bar")
            .attr("width", 0)
            .attr("height", barBand);
    };

    var renderUpdate = function (updateSelection, xScale, yScale) {
        updateSelection.attr("x", 0)
            .style("fill", barColor)
        .transition()
            .duration(transitionDuration)
            .attr("y", function (d) { return yScale(d.page) })
            .attr("width", function (d) { return xScale(d.value) });
    };

    var renderExit = function (exitSelection) {
        var exitTransition = exitSelection
            .transition()
            .duration(transitionDuration);
        exitTransition
            .attr("width", 0)
            .remove();
    };

    return {
        render: render,
        setData: setData
    };
});

OpenSpeedMonitor.ChartComponents.ChartBars.BarBand = 40;
OpenSpeedMonitor.ChartComponents.ChartBars.BarGap = 5;