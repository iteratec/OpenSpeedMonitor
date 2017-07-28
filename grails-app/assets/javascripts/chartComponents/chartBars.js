//= require /bower_components/d3/d3.min.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartComponents = OpenSpeedMonitor.ChartComponents || {};

OpenSpeedMonitor.ChartComponents.ChartBars = (function () {
    var data = null,
        height = null,
        width = null,
        barBand = OpenSpeedMonitor.ChartComponents.ChartBars.BarBand;

    var setData = function (componentData) {
        data = componentData.values;
        height = componentData.height;
        width = componentData.width;
    };

    var render = function (selector) {
        var xScale = d3.scale.linear().range([0, width]);
        var yScale = d3.scale.ordinal().rangeBands([0, height]);

        xScale.domain([0, d3.max(data, function(d) {
            return d.value;
        })]);
        yScale.domain(data.map(function(d) {
            return d.page;
        }));

        var bars = d3.select(selector).selectAll(".bar").data(data, function (d) {
            return d.page;
        });
        renderEnter(bars.enter());
        renderExit(bars.exit());
        renderUpdate(bars, xScale, yScale);
    };

    var renderEnter = function (enterSelection) {
        enterSelection.append("rect")
            .attr("class", "bar");
    };

    var renderUpdate = function (bars, xScale, yScale) {
        bars.attr("x", 0)
            .attr("y", function (d) {
                return yScale(d.page)
            })
            .attr("width", function (d) {
                return xScale(d.value)
            })
            .attr("height", barBand)
            .style("fill", "blue");
    };

    var renderExit = function (exitSelection) {
        exitSelection.remove();
    };

    return {
        render: render,
        setData: setData
    };
});

OpenSpeedMonitor.ChartComponents.ChartBars.BarBand = 40;
OpenSpeedMonitor.ChartComponents.ChartBars.BarGap = 5;