//= require /bower_components/d3/d3.min.js
//= require common.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartComponents = OpenSpeedMonitor.ChartComponents || {};

OpenSpeedMonitor.ChartComponents.ChartHeader = (function () {
    var textData = "";
    var width = 500;
    var height = OpenSpeedMonitor.ChartComponents.ChartHeader.Height;
    var transitionDuration = OpenSpeedMonitor.ChartComponents.common.transitionDuration;

    var setData = function (data) {
        textData = (data.text || data.text === "") ? data.text : textData;
        width = data.width || width;
    };

    var render = function (svg) {
      var text = svg.selectAll(".header-text").data([textData], function (d) { return d; });
      text.exit()
          .transition()
          .duration(transitionDuration)
          .style("opacity", 0)
          .remove();
      text.enter()
          .append("text")
          .classed("header-text", true)
          .attr("text-anchor", "middle")
          .attr("dominant-baseline", "alphabetic")
          .text(function (d) {
              return d;
          })
          .style("opacity", 0);
      text
          .attr("x", width/2)
          .attr("y", height)
          .transition()
          .duration(transitionDuration)
          .style("opacity", 1);
    };

    return {
        render: render,
        setData: setData
    };
});
OpenSpeedMonitor.ChartComponents.ChartHeader.Height = 40;

