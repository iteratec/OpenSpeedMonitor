//= require /bower_components/d3/d3.min.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageAggregationHorizontal = (function (chartIdentifier) {

  var svg = d3.select("#" + chartIdentifier).append("svg"),
    margin = 25,
    barHeight = 40,
    barPadding = 10,
    valueMargin = 4,
    colorPalette = d3.scale.category20(),
    width,
    height,
    absoluteMaxValue = 0,
    absoluteMaxYOffset = 0,
    labelWidth = 0,
    xScale = d3.scale.linear();

  var init = function () {

    // add eventHandler
    window.onresize = function () {
      // calcChartWidth();
      // resize();
    };

  };

  var drawChart = function (barchartData) {

    $("#chart-card").removeClass("hidden");

    var seriesData = barchartData.series[0].data;
    var unitName = barchartData.series[0].dimensionalUnit;

    width = parseInt($("#" + chartIdentifier).width(), 10);
    var countTrafficLightBar = 1;
    height = (seriesData.length + countTrafficLightBar) * (barHeight + barPadding) + barPadding;

    svg
      .attr("width", width)
      .attr("height", height);

    drawSeries(seriesData, unitName);

    drawTrafficLight();

  };

  var drawSeries = function (seriesData, unitName) {

    absoluteMaxValue = Math.max(absoluteMaxValue, d3.max(seriesData, function(d) { return d.indexValue; }));

    xScale
      .domain([0, absoluteMaxValue])
      .range([0, width - margin*2 - labelWidth]);

    var allBars = svg.append("g");

    var singleBar = allBars.selectAll("g")
      .data(seriesData)
      .enter()
      .append("g")
      .attr("cx",0)
      .attr("transform", function(d, index) {
        var yOffset = (index * (barHeight + barPadding) + barPadding);
        absoluteMaxYOffset = Math.max(absoluteMaxYOffset, yOffset)
        return "translate(" + margin + "," + yOffset + ")";
      });

    singleBar.append("text")
      .attr("y", barHeight / 2)
      .attr("dy", ".35em") //vertical align middle
      .text(function(d){ return d.grouping; }).each(function() {
        labelWidth = Math.ceil(Math.max(labelWidth, this.getBBox().width))
      });

    singleBar.append("rect")
      .attr("transform", "translate("+labelWidth+", 0)")
      .attr("height", barHeight)
      .attr("width", function (d) {
        return xScale(d.indexValue)
      })
      .attr("fill", function(d,i) { return colorPalette(0); });

    singleBar.append("text")
      .attr("class", "value")
      .attr("y", barHeight / 2)
      .attr("dx", -valueMargin + labelWidth) //margin right
      .attr("dy", ".35em") //vertical align middle
      .attr("text-anchor", "end")
      .text(function(d){
        return (Math.round(d.indexValue)) + " " + unitName;
      })
      .attr("x", function(d){
        var width = this.getBBox().width;
        return Math.max(width + valueMargin, xScale(d.indexValue));
      });
  };

  var drawTrafficLight = function(){

    var trafficLightBars = svg.append("g");

    var trafficLightData = getTrafficLightData();

    var trafficLight = trafficLightBars.selectAll("g")
      .data(trafficLightData)
      .enter()
      .append("g")
      .attr("cx",0);

    trafficLight.append("rect")
      .attr("transform", function(d, index){
        var xOffset = margin + labelWidth + xScale(d.lowerBoundary);
        return "translate(" + xOffset + ", " + (absoluteMaxYOffset + barHeight + barPadding*2) + ")";
      })
      .attr("height", barHeight)
      .attr("width", function (d) {
        return xScale(d.upperBoundary - d.lowerBoundary)
      })
      .attr("fill", function(d) { return d.fill; })
      .attr("fill-opacity", function(d) { return d.fillOpacity; });

    trafficLight.append("text")

  };

  var getTrafficLightData = function () {
    var trafficLightData = []
    var goodLoadtime = 1000,
      badLoadtime = 3000;

    if (absoluteMaxValue > badLoadtime) {
      trafficLightData = [
        {"lowerBoundary": 0, "upperBoundary": goodLoadtime, "fill": "green", "fillOpacity": "0.3"},
        {"lowerBoundary": goodLoadtime, "upperBoundary": badLoadtime, "fill": "yellow", "fillOpacity": "0.3"},
        {"lowerBoundary": badLoadtime, "upperBoundary": absoluteMaxValue, "fill": "red", "fillOpacity": "0.3"}
      ]
    } else if (absoluteMaxValue > goodLoadtime) {
      trafficLightData = [
        {"lowerBoundary": 0, "upperBoundary": goodLoadtime, "fill": "green", "fillOpacity": "0.3"},
        {"lowerBoundary": goodLoadtime, "upperBoundary": absoluteMaxValue, "fill": "yellow", "fillOpacity": "0.3"},
      ]
    } else if (absoluteMaxValue > 0) {
      trafficLightData = [
        {"lowerBoundary": 0, "upperBoundary": absoluteMaxValue, "fill": "green", "fillOpacity": "0.3"},
      ]
    }
    return trafficLightData;
  };

  init();

  return {
    drawChart: drawChart
  };

});
