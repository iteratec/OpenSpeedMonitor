//= require /bower_components/d3/d3.min.js
//= require /chartComponents/utility.js
//= require common.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartComponents = OpenSpeedMonitor.ChartComponents || {};

OpenSpeedMonitor.ChartComponents.ChartSideLabels = (function () {
    var height = 500;
    var labelData = [];
    var transitionDuration = OpenSpeedMonitor.ChartComponents.common.transitionDuration;
    var svg = null;

    var setData = function (data) {
        height = data.height || height;
        labelData = data.labels || labelData;
    };

    var render = function (d3SelectionToRenderOn) {
        svg = d3SelectionToRenderOn || svg;
        if (!svg) {
            return;
        }

        var yScale = d3.scale.ordinal().rangeBands([0, height]).domain(labelData);
        var labels = svg.selectAll(".side-label").data(labelData, function (d) { return d; });
        labels.exit()
            .transition()
            .duration(transitionDuration)
            .style("opacity", 0)
            .remove();
        labels.enter()
            .append("text")
            .classed("side-label", true)
            .style("opacity", 0)
            .attr("dominant-baseline", "middle")
        labels
            .transition()
            .duration(transitionDuration)
            .text(function (d) {
                return d;
            })
            .style("opacity", 1)
            .attr("transform", function (d) {
                return "translate(0, " + (yScale(d) + (yScale.rangeBand() / 2)) +")";
            });
    };

    var estimateWidth = function (svgForEstimation, labelsToEstimate) {
        svgForEstimation = svg || svgForEstimation;
        labelsToEstimate = labelsToEstimate || labelData;
        return d3.max(OpenSpeedMonitor.ChartComponents.utility.getTextWidths(svgForEstimation, labelsToEstimate));
    };

    return {
        render: render,
        setData: setData,
        estimateWidth: estimateWidth
    };

});
