//= require /bower_components/d3/d3.min.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.distributionChart = (function () {
    var svgContainer = null,
        chart = null,
        chartData = null,
        width = 600,
        height = 600,
        margin = {top: 10, right: 30, bottom: 10, left: 100},
        violinWidth = 100,
        resolution = 20,
        interpolation = 'basis';

    var init = function () {
        svgContainer = document.querySelector("#svg-container")
    };

    var drawChart = function (distributionChartData) {
        chartData = distributionChartData;

        // delete old chart in same container
        d3.select(svgContainer).selectAll("svg").remove();

        // make the chart visible
        $("#chart-card").removeClass("hidden");

        // create the svg which contains the chart
        var svg = d3.select(svgContainer)
                    .append("svg")
                    .attr("width", "100%")
                    .attr("height", height);

        var domain = getDomain();

        // create the domain for the y axis
        var y = d3.scale.linear()
                  .range([height-margin.bottom, margin.top])
                  .domain(domain);

        // create the yAxis
        var yAxis = d3.svg.axis()
                      .scale(y)
                      .orient("left");

        // add the violins
        Object.keys(chartData.series).forEach( function (trace, i) {
            var traceData = chartData.series[trace].sort(d3.ascending);

            var g = svg.append("g")
                       .attr("transform", "translate(" + (i * violinWidth + margin.left) + ",0)");

            addViolin(g, traceData, height, violinWidth, domain, 0.25, "#0000FF");
        });

        svg.append("g")
           .attr('class', 'axis')
           .attr("transform", "translate(" + margin.left + ",0)")
           .call(yAxis);
    };

    var getDomain = function () {
        var maxValues = [];
        var minValues = [];
        Object.keys(chartData.series).forEach( function (trace) {
            maxValues.push(Math.max.apply(null, chartData.series[trace]));
            minValues.push(Math.min.apply(null, chartData.series[trace]).toString());
        });

        return [Math.min.apply(null, minValues), Math.max.apply(null, maxValues)]
    };

    var addViolin = function (svg, traceData, height, violinWidth, domain, violinColor) {
        var data = d3.layout.histogram()
                     .bins(resolution)
                     .frequency(0)
                     (traceData);

        // y is now the horizontal axis because of the violin beeing a 90 degree rotated histogram
        var y = d3.scale.linear()
                  .range([violinWidth/2, 0])
                  .domain([0, d3.max(data, function(d) { return d.y; })]);

        // x is now the vertical axis because of the violin beeing a 90 degree rotated histogram
        var x = d3.scale.linear()
                  .range([height, 0])
                  .domain(domain)
                  .nice();

        var area = d3.svg.area()
                     .interpolate(interpolation)
                     .x(function (d) { return x(d.x); })
                     .y0(violinWidth/2)
                     .y1(function (d) { return y(d.y); });

        var line = d3.svg.line()
                     .interpolate(interpolation)
                     .x(function (d) { return x(d.x); })
                     .y(function (d) { return y(d.y); });

        var gPlus = svg.append("g");
        var gMinus = svg.append("g");

        gPlus.append("path")
             .datum(data)
             .attr("class", "area")
             .attr("d", area)
             .style("fill", violinColor);

        gPlus.append("path")
             .datum(data)
             .attr("class", "violin")
             .attr("d", line)
             .style("stroke", violinColor);

        gMinus.append("path")
              .datum(data)
              .attr("class", "area")
              .attr("d", area)
              .style("fill", violinColor);

        gMinus.append("path")
              .datum(data)
              .attr("class", "violin")
              .attr("d", line)
              .style("stroke", violinColor);

        gPlus.attr("transform", "rotate(90, 0, 0)  translate(0, -" + violinWidth + ")");
        gMinus.attr("transform", "rotate(90, 0, 0) scale(1, -1)");
    };

    init();

    return {
        drawChart: drawChart
    };
})();