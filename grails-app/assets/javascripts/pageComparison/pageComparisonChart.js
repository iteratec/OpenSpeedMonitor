//= require /bower_components/d3/d3.min.js
//= require /d3/chartLabelUtil
//= require /d3/trafficLightDataProvider
//= require_self

"use strict";
var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageComparisonChart = (function (chartIdentifier) {

    var svg,
        topMargin = 50,
        outerMargin = 25,
        barHeight = 40,
        barPadding = 10,
        valueMarginInBar = 4,
        colorPalette = d3.scale.category20(),
        width,
        absoluteMaxValue = 0,
        xScale,
        initialBarWidth = 20,
        countTrafficLightBar = 1,
        allBarsGroup,
        trafficLightBars,
        transitionDuration = 600,
        svgHeight,
        headerLine,
        commonLabelParts,
        unitName,
        absoluteMaxYOffset;


    var initChart = function () {

        $("#chart-card").removeClass("hidden");
        width = parseInt($("#" + chartIdentifier).width(), 10);

        svg = d3.select("#" + chartIdentifier).append("svg")
            .attr("class", "d3chart")
            .attr("width", width);

        headerLine = svg.append("g");
        allBarsGroup = svg.append("g");
        trafficLightBars = svg.append("g");
        xScale = d3.scale.linear().range([0, width - outerMargin * 2 - barPadding])

    };

    var drawChart = function (barchartData) {
        if (svg === undefined) {
            initChart();
        }

        initChartData(barchartData);
        drawBars(barchartData.series);
        var headerData = [{headerText: commonLabelParts}];
        drawHeader(headerData);
        drawTrafficLight();
    };

    var drawHeader = function (headerData) {
        var headerText = headerLine.selectAll("text")
            .data(headerData, function (d) {
                return d.headerText;
            });

        // exit
        headerText.exit()
            .remove();

        // enter

        headerText.enter().append("text")
            .text(function (d) {
                return d.headerText;
            })
            .attr("y", barPadding + barHeight / 2)
            .attr("dy", ".35em") //vertical align middle
            .attr("cx", 0)
            .attr("transform", function (d) {
                return "translate(" + parseInt(outerMargin + barPadding) + ")";
            });

        //update

        headerText
            .attr("transform", function (d) {
                return "translate(" + parseInt(outerMargin + barPadding) + ")";
            });

    };

    var initChartData = function (barchartData) {
        var allData = [];
        barchartData.series.forEach(function (current) {
            current.data.forEach(function (currentData) {
                allData.push(currentData);
            })
        });
        var labelUtil = OpenSpeedMonitor.ChartModules.ChartLabelUtil(allData, barchartData.i18nMap);
        labelUtil.getSeriesWithShortestUniqueLabels();
        commonLabelParts = labelUtil.getCommonLabelParts();
        absoluteMaxYOffset = (barchartData.series.length - 1) * (barHeight + barPadding) + barPadding;

        unitName = barchartData.series[0].dimensionalUnit;
    };


    var drawBars = function (series) {
        absoluteMaxValue = d3.max(series, function (d) {
            return d3.max(d.data, function (d1) {
                return d1.value;
            })
        });

        xScale.domain([0, absoluteMaxValue]);

        svgHeight = topMargin + (series.length + countTrafficLightBar) * (barHeight + barPadding) + barPadding;
        svg.attr("height", svgHeight);

        var outerData = allBarsGroup.selectAll("g.outer")
            .data(series);
        // enter /////////////////////////////////////////////////////////////////////////////////
        outerData.enter()
            .append("g")
            .attr("class", "outer")
            .attr("transform", function (d, index) {
                var yOffset = (topMargin + index * (barHeight + barPadding) + barPadding);
                return "translate(" + outerMargin + "," + yOffset + ")";
            });
        var outerG = allBarsGroup.selectAll("g.outer");
        var innerData = outerG.selectAll(".inner")
            .data(function (d) {
                return d.data.sort(function (a, b) {
                    return b.value - a.value
                })
            }, function (d, index) {
                return "" + index + "-" + d.value + "-" + d.label;
            });

        var innerG = innerData.enter().append("g").attr("class", "inner");
        // bar
        innerG.append("rect")
            .attr("transform", "translate(" + barPadding + ", 0)")
            .attr("height", barHeight)
            .attr("width", initialBarWidth)
            .attr("fill", function (d, i) {
                return colorPalette(0);
            })
            .attr("opacity", function (d, index) {
                return index * 0.5 + 0.4;
            })
            .call(updateRect);
        // value text
        innerG.append("text")
            .attr("class", "d3chart-value")
            .attr("y", barHeight / 2)
            .attr("dy", ".35em") //vertical align middle
            .call(updateText);

        // exit //////////////////////////////////////////////////////////////////////////////////
        outerData.exit().remove();
        innerData.exit().remove();

        // update //////////////////////////////////////////////////////////////////////////////
        innerG.selectAll("rect").call(updateRect);
        innerG.selectAll("text").call(updateText);

    };

    var updateText = function (selection) {
        selection
            .text(function (d) {
                var label = d.label ? d.label + ": " : "";
                return label + (Math.round(d.value)) + " " + unitName;
            })
            .attr("x", function (d) {
                var width = this.getBBox().width;
                return Math.max(width + valueMarginInBar, xScale(d.value));
            });
    };
    var updateRect = function (selection) {
        selection
            .transition()
            .duration(transitionDuration)
            .attr("width", function (d) {
                return xScale(d.value)
            });
    };

    var drawTrafficLight = function () {

        var trafficLightData = OpenSpeedMonitor.ChartModules.TrafficLightDataProvider.getTimeData(absoluteMaxValue);

        var trafficLight = trafficLightBars.selectAll("g")
            .data(trafficLightData, function (d) {
                return d.id
            });

        //exit

        trafficLight.exit()
            .transition()
            .duration(transitionDuration)
            .attr("width", 0)
            .remove();

        // enter

        var trafficLightEnter = trafficLight.enter()
            .append("g")
            .attr("cx", 0)
            .attr("transform", function (d, index) {
                var xOffset = outerMargin + xScale(d.lowerBoundary) + barPadding;
                return "translate(" + xOffset + ", " + (topMargin + absoluteMaxYOffset + barHeight + barPadding * 2) + ")";
            });

        trafficLightEnter.append("rect")
            .attr("height", barHeight)
            .attr("width", initialBarWidth)
            .attr("fill", function (d) {
                return d.fill;
            })
            .attr("fill-opacity", function (d) {
                return d.fillOpacity;
            })
            .transition()
            .duration(transitionDuration)
            .attr("width", function (d) {
                return xScale(d.upperBoundary - d.lowerBoundary);
            });
        trafficLightEnter.append("text")
            .attr("class", function (d) {
                return d.cssClass;
            })
            .text(function (d) {
                return d.name;
            })
            .attr("y", barHeight / 2)
            .attr("dy", ".35em") //vertical align middle
            .attr("x", function (d) {
                return xScale(d.upperBoundary - d.lowerBoundary) / 2;
            })
            .attr("text-anchor", "middle");

        //update

        trafficLight
            .transition()
            .duration(transitionDuration)
            .attr("transform", function (d, index) {
                var xOffset = outerMargin + xScale(d.lowerBoundary) + barPadding;
                return "translate(" + xOffset + ", " + (topMargin + absoluteMaxYOffset + barHeight + barPadding * 2) + ")";
            })
            .select('rect')
            .attr("width", function (d) {
                return xScale(d.upperBoundary - d.lowerBoundary);
            });

    };

    initChart();

    return {
        drawChart: drawChart
    };

});
