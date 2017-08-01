//= require /bower_components/d3/d3.min.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartComponents = OpenSpeedMonitor.ChartComponents || {};

OpenSpeedMonitor.ChartComponents.utility = (function () {

    var getTextWidths = function (svgForEstimation, texts) {
        var widths = [];
        svgForEstimation.selectAll('.invisible-text-to-measure')
            .data(texts)
            .enter()
            .append("text")
            .attr("opacity", 0)
            .text(function(d) { return d; })
            .each(function() {
                widths.push(this.getComputedTextLength());
                this.remove();
            });
        return widths;
    };

    return {
        getTextWidths: getTextWidths
    }
})();
