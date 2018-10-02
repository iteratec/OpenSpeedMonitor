//= require /node_modules/d3/d3.min.js
//= require /charts/commonChartFunctions.js

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
        svg,
        xScale,
        yScale,
        barSelected;

    var init = function (data) {
        // make card and buttons visible
        $("#chart-card").removeClass("hidden");
        $(".in-chart-buttons").removeClass("hidden");

        svg = d3.select("#" + chartIdentifier + " svg")
            .attr("class", "d3chart")
            .attr("height", height + margin.top + margin.bottom);
        chartContainer = svg.append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        $("#all-bars-desc").on('click', function (e) {
            drawChart(data, 'desc');
            toogleFilterCheckmarks(e.target)
        });
        $("#all-bars-asc").on('click', function (e) {
            drawChart(data, 'asc');
            toogleFilterCheckmarks(e.target)
        })
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

    var drawChart = function (data, sortOrder) {
        if (svg === undefined) {
            init(data);
        }

        // init labelMapping on startup
        labelMapping = {};
        data.forEach(function (currentElement) {
            labelMapping[currentElement.name] = currentElement.name;
        });

        width = data.length * (barWidth + padding);
        svg.attr("width", width + margin.left + margin.right);

        xScale = d3.scale.ordinal()
            .rangeRoundBands([0, width], paddingFactor)
            .domain(data.map(function (d) {
                return d.name;
            }));

        yScale = d3.scale.linear()
            .range([height, 0])
            .domain([0, d3.max(data, function (d) {
                return d.value;
            })]);

        var bars = chartContainer.selectAll("g")
            .data(data, function (d) {
                return d.name + d.value;
            });

        // enter
        var barContainer = bars.enter()
            .append("g")
            .attr("class", "d3chart-bar-container");
        barContainer.append("rect")
            .attr("class", "d3chart-bar")
            .attr("height", function (d) {
                return height - yScale(d.value);
            })
            .attr("width", barWidth)
            .attr("transform", function (d) {
                return "translate(" + 0 +  "," + (yScale(d.value)) + ")"
            })
            .on("click", highlightClickedBar);
        barContainer.append("text")
            .text(function (d) {
                return (d.value + "%");
            })
            .attr("class", "d3chart-barLabel")
            .style("text-anchor", "middle")
            .attr("x", barWidth / 2)
            .attr("y", "1.2em")
            .attr("transform", function (d) {
                return "translate(" + 0 + "," + yScale(d.value) + ")";
            });
        barContainer.append("text")
            .attr("class", "d3chart-xAxisText")
            .text(function (d) {
                return labelMapping[d.name];
            })
            .style("text-anchor", "end")
            .attr("dy", "2em")
            .attr("transform", function (d) {
                return "translate(" + barWidth / 2 + "," + (height) + ") rotate(-45)"
            });

        // exit
        bars.exit()
            .remove();

        // sort bars descending by default
        if (!sortOrder) {
            sortOrder = "desc";
        }
        sortBars(data, sortOrder);
        toogleFilterCheckmarks();
    };

    var sortBars = function (data, order) {
        xScale.domain(data.sort(function (a, b) {
            return (order === "asc") ? a.value - b.value : b.value - a.value;
        })
            .map(function (jobGroup) {
                return jobGroup.name
            }));

        svg.selectAll(".d3chart-bar-container")
            .attr("transform", function (jobGroup) {
                return "translate(" + xScale(jobGroup.name) + ", 0)";
            });
    };

    var toogleFilterCheckmarks = function (listItem) {
        // remove all checkmarks
        $(".filterActive").toggleClass("filterInactive filterActive");

        // reset checkmark to 'descending' if 'Show' gets clicked
        // otherwise set checkmark to the list item one has clicked on
        if (typeof listItem == 'undefined') {
            $('#all-bars-desc > .filterInactive').toggleClass("filterActive filterInactive");
        } else {
            $(listItem).find(".filterInactive").toggleClass("filterActive filterInactive");
        }
    };

    var highlightClickedBar = function (d) {
        if (barSelected !== d.name) {
            barSelected = d.name;
            d3.selectAll(".d3chart-bar").classed("d3chart-faded", true);
            d3.select(this).classed("d3chart-faded", false);
        } else {
            d3.selectAll(".d3chart-bar").classed("d3chart-faded", false);
            barSelected = null;
        }
    };

    return {
        getLabelMappings: getLabelMappings,
        setLabelMappings: setLabelMappings,
        drawChart: drawChart
    };

});
