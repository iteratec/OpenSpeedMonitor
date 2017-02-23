//= require /bower_components/d3/d3.min.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageAggregationHorizontal = (function (chartIdentifier) {

  var svg,
    outerMargin = 25,
    barHeight = 40,
    barPadding = 10,
    valueMarginInBar = 4,
    colorPalette = d3.scale.category20(),
    width,
    absoluteMaxValue = 0,
    absoluteMaxYOffset = 0,
    labelWidth = 0,
    xScale = d3.scale.linear(),
    initialBarWidth = 20,
    countTrafficLightBar = 1,
    allBarsGroup,
    trafficLightBars,
    transitionDuration = 600,
    svgHeight,
    trafficLightBarOpacity = 0.4;

  var init = function () {

    $("#chart-card").removeClass("hidden");
    width = parseInt($("#" + chartIdentifier).width(), 10);

    svg = d3.select("#" + chartIdentifier).append("svg")
      .attr("width", width);

    allBarsGroup = svg.append("g");
    trafficLightBars = svg.append("g");

    window.onresize = function () {
      // calcChartWidth();
      // resize();
    };

  };

  var drawChart = function (barchartData) {

    if (svg === undefined){
      init();
    }

    var seriesData = barchartData.series[0].data;
    var unitName = barchartData.series[0].dimensionalUnit;

    drawSeries(seriesData, unitName);

    drawTrafficLight();

  };

  var drawSeries = function (seriesData, unitName) {

    var setWidthOfBarByData = function(selection) {
      selection
        .attr("width", function (d) {
          return xScale(d.indexValue)
        })
    };
    var defineXScale = function () {
      var paddingBetweenLabelAndBar = barPadding;
      xScale
        .domain([0, absoluteMaxValue])
        .range([0, width - outerMargin*2 - labelWidth - paddingBetweenLabelAndBar]);
    };

    svgHeight = (seriesData.length + countTrafficLightBar) * (barHeight + barPadding) + barPadding;
    svg.attr("height", svgHeight);
    absoluteMaxYOffset = (seriesData.length - 1) * (barHeight + barPadding) + barPadding;

    absoluteMaxValue = Math.max(absoluteMaxValue, d3.max(seriesData, function(d) { return d.indexValue; }));

    var singleBarGroups = allBarsGroup.selectAll("g")
      .data(seriesData, function(d) { return d.grouping; });

    // enter

    var singleBarGroupsEnter = singleBarGroups.enter()
      .append("g")
      .attr("cx",0)
      .attr("transform", function(d, index) {
        var yOffset = (index * (barHeight + barPadding) + barPadding);
        return "translate(" + outerMargin + "," + yOffset + ")";
      });

    singleBarGroupsEnter.append("text")
      .attr("y", barHeight / 2)
      .attr("dy", ".35em") //vertical align middle
      .text(function(d){ return d.grouping; }).each(function() {
        labelWidth = Math.ceil(Math.max(labelWidth, this.getBBox().width))
      });

    defineXScale();

    singleBarGroupsEnter.append("rect")
      .attr("transform", "translate(" + parseInt(labelWidth+barPadding) + ", 0)")
      .attr("height", barHeight)
      .attr("width", initialBarWidth)
      .attr("fill", function(d,i) { return colorPalette(0); })
      .transition()
      .duration(transitionDuration)
      .call(setWidthOfBarByData);

    singleBarGroupsEnter.append("text")
      .attr("class", "value")
      .attr("y", barHeight / 2)
      .attr("dx", -valueMarginInBar + labelWidth) //margin right
      .attr("dy", ".35em") //vertical align middle
      .attr("text-anchor", "end")
      .text(function(d){
        return (Math.round(d.indexValue)) + " " + unitName;
      })
      .attr("x", function(d){
        var width = this.getBBox().width;
        return Math.max(width + valueMarginInBar, xScale(d.indexValue));
      });

    // update

    singleBarGroups
      .transition()
      .duration(transitionDuration)
      .attr("transform", function(d, index) {
        var yOffset = (index * (barHeight + barPadding) + barPadding);
        return "translate(" + outerMargin + "," + yOffset + ")";
      })

    // exit

    singleBarGroups.exit()
      .transition()
      .duration(transitionDuration)
      .attr("width", 0)
      .remove();

  };

  var drawTrafficLight = function(){

    var trafficLightData = getTrafficLightData();

    var trafficLight = trafficLightBars.selectAll("g")
      .data(trafficLightData, function(d){ return d.id });

    //enter

    var trafficLightEnter = trafficLight.enter()
      .append("g")
      .attr("cx",0)
      .attr("transform", function(d, index){
        var xOffset = outerMargin + labelWidth + xScale(d.lowerBoundary) + barPadding;
        return "translate(" + xOffset + ", " + (absoluteMaxYOffset + barHeight + barPadding*2) + ")";
      });

    trafficLightEnter.append("rect")
      .attr("height", barHeight)
      .attr("width", initialBarWidth)
      .attr("fill", function(d) { return d.fill; })
      .attr("fill-opacity", function(d) { return d.fillOpacity; })
      .transition()
      .duration(transitionDuration)
      .attr("width", function (d) {
        return xScale(d.upperBoundary - d.lowerBoundary);
      });
    trafficLightEnter.append("text")
      .attr("class", function(d){
        return d.cssClass;
      })
      .text(function(d){
        return d.name;
      })
      .attr("y", barHeight / 2)
      .attr("dy", ".35em") //vertical align middle
      .attr("x", function(d){
        return xScale(d.upperBoundary - d.lowerBoundary) / 2;
      })
      .attr("text-anchor", "mid");

    //update

    trafficLight
      .transition()
      .duration(transitionDuration)
      .attr("transform", function(d, index){
        var xOffset = outerMargin + labelWidth + xScale(d.lowerBoundary) + barPadding;
        return "translate(" + xOffset + ", " + (absoluteMaxYOffset + barHeight + barPadding*2) + ")";
      });

    //exit

    trafficLight.exit()
      .transition()
      .duration(transitionDuration)
      .attr("width", 0)
      .remove();

  };

  var getTrafficLightData = function () {
    var trafficLightData = []
    var goodLoadtime = 1000,
      badLoadtime = 3000;

    if (absoluteMaxValue > badLoadtime) {
      trafficLightData = [
        {id: "all_good", lowerBoundary: 0, upperBoundary: goodLoadtime, fill: "green", fillOpacity: trafficLightBarOpacity, name: "GOOD", cssClass: "good"},
        {id: "all_ok", lowerBoundary: goodLoadtime, upperBoundary: badLoadtime, fill: "yellow", fillOpacity: trafficLightBarOpacity, name: "OK", cssClass: "ok"},
        {id: "all_bad", lowerBoundary: badLoadtime, upperBoundary: absoluteMaxValue, fill: "red", fillOpacity: trafficLightBarOpacity, name: "BAD", cssClass: "bad"}
      ]
    } else if (absoluteMaxValue > goodLoadtime) {
      trafficLightData = [
        {id: "justGoodAndOk_good", lowerBoundary: 0, upperBoundary: goodLoadtime, fill: "green", fillOpacity: trafficLightBarOpacity, name: "GOOD", cssClass: "good"},
        {id: "justGoodAndOk_ok", lowerBoundary: goodLoadtime, upperBoundary: absoluteMaxValue, fill: "yellow", fillOpacity: trafficLightBarOpacity, name: "OK", cssClass: "ok"},
      ]
    } else if (absoluteMaxValue > 0) {
      trafficLightData = [
        {id: "justGood_good", lowerBoundary: 0, upperBoundary: absoluteMaxValue, fill: "green", fillOpacity: trafficLightBarOpacity, name: "GOOD", cssClass: "good"},
      ]
    }
    return trafficLightData;
  };

  init();

  return {
    drawChart: drawChart
  };

});
