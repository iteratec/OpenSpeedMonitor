//= require /bower_components/d3/d3.min.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartComponents = OpenSpeedMonitor.ChartComponents || {};

OpenSpeedMonitor.ChartComponents.ChartHeader = (function () {
    var textData = "";
    var width = 500;
    var height = OpenSpeedMonitor.ChartComponents.ChartHeader.Height;
    var transitionDuration = 500;

    var setData = function (data) {
        textData = data.text || textData;
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
          .attr("dominant-baseline", "middle")
          .text(function (d) {
              return d;
          })
          .style("opacity", 0);
      text
          .transition()
          .duration(transitionDuration)
          .style("opacity", 1)
          .attr("x", width/2)
          .attr("y", height/2);
    };

    return {
        render: render,
        setData: setData
    };
});
OpenSpeedMonitor.ChartComponents.ChartHeader.Height = 40;

