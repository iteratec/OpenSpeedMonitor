//= require /bower_components/d3/d3.min.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.CsiBenchmarkChart = (function (chartIdentifier) {

    var labelMapping = {},
        barWidth = 120,
        paddingFactor = .2,
        padding = barWidth * paddingFactor,
        margin = {top: 20, right: 20, bottom: 150, left: 40},
        width,
        height = 700 - margin.top - margin.bottom,
        chartContainer,
        svg;

    var init = function () {
        // make card and buttons visible
        $("#chart-card").removeClass("hidden");
        $(".in-chart-buttons").removeClass("hidden");

        svg = d3.select("#" + chartIdentifier + " svg")
            .attr("class", "d3chart")
            .attr("height", height + margin.top + margin.bottom);
        chartContainer = svg.append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    };

    var getLabelMappings = function () {
        return labelMapping
    };
    var setLabelMappings = function (updatedMappings) {
        for (var key in updatedMappings) {
            labelMapping[key] = updatedMappings[key];
        }
        d3.selectAll(".d3chart-xAxisText").text(function (d) {
            return labelMapping[d.name];
        })
    };

    var drawChart = function (data) {
        if (svg === undefined) {
            init();
        }

        // init labelMapping on startup
        labelMapping = {};
        data.forEach(function (currentElement) {
            labelMapping[currentElement.name] = currentElement.name;
        });

        width = data.length * (barWidth + padding);
        svg.attr("width", width + margin.left + margin.right);

        var xScale = d3.scale.ordinal()
            .rangeRoundBands([0, width], paddingFactor)
            .domain(data.map(function (d) {
                return d.name;
            }));

        var yScale = d3.scale.linear()
            .range([height, 0])
            .domain([0, d3.max(data, function (d) {
                return d.value;
            })]);

        var bars = chartContainer.selectAll("g")
            .data(data, function (d) {
                return d.name + d.value;
            });

        // enter
        var barContainer = bars.enter().append("g")
            .attr("transform", function (d) {
                return "translate(" + xScale(d.name) + "," + yScale(d.value) + ")";
            });
        barContainer.append("rect")
            .attr("class", "d3chart-bar")
            .attr("height", function (d) {
                return height - yScale(d.value);
            })
            .attr("width", barWidth);
        barContainer.append("text")
            .text(function (d) {
                return (d.value + "%");
            })
            .attr("class", "d3chart-barLabel")
            .style("text-anchor", "middle")
            .attr("x", barWidth / 2)
            .attr("y", "1.2em");
        barContainer.append("text")
            .attr("class", "d3chart-xAxisText")
            .text(function (d) {
                return labelMapping[d.name];
            })
            .style("text-anchor", "end")
            .attr("dy", "2em")
            .attr("transform", function (d) {
                return "translate(" + barWidth / 2 + "," + (height - yScale(d.value)) + ") rotate(-45)"
            });

        // exit
        bars.exit()
            .remove();
    };

    return {
        getLabelMappings: getLabelMappings,
        setLabelMappings: setLabelMappings,
        drawChart: drawChart
    };

});
