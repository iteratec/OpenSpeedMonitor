//= require /bower_components/d3/d3.min.js
//= require /bower_components/dimple/dist/dimple.latest.min.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageAggregation = (function (chartIdentifier) {
    var chart = null,
        height = 600,
        legendPosition = {x: 200, y: 10, width: 100, height: 100},
        margins = {left: 60, right: 100, top: 110, bottom: 150},
        maxWidthPerBar = 150,
        allMeasurandSeries = {},
        svg = null,
        seriesCount = 0,
        xAxis = null,
        yAxes = {};

    var init = function () {
        // add eventHandler
        window.onresize = function () {
            resize();
        };
    };

    var drawChart = function (barchartData) {
        // Delete old chart in same container
        d3.select("#" + chartIdentifier).selectAll("svg").remove();
        if($("#adjust-barchart-modal").hasClass("hidden"))
            $("#adjust-barchart-modal").removeClass("hidden");

        allMeasurandSeries = {};
        yAxes = {};
        xAxis = null;

        svg = dimple.newSvg("#" + chartIdentifier, "100%", height);
        chart = new dimple.chart(svg, null);
        chart.setMargins(margins.left, margins.right, margins.top, margins.bottom);
        svg = svg.node();
        seriesCount = Object.keys(barchartData['series']).length;

        xAxis = chart.addCategoryAxis("x", "grouping");
        xAxis.title = barchartData['groupingLabel'];

        var seriesId = 0;
        for (var currentSeriesId in barchartData.series) {
            var currentSeries = barchartData.series[currentSeriesId];

            // get correct y axis
            var yAxis = yAxes[currentSeries['dimensionalUnit']];
            if (!yAxis) {
                yAxis = chart.addMeasureAxis("y", "indexValue");
                yAxis.title = currentSeries['dimensionalUnit'];
                yAxes[currentSeries['dimensionalUnit']] = yAxis;
            }

            allMeasurandSeries[currentSeriesId] = [];
            if (currentSeries.stacked === true) {
                var s = chart.addSeries("index", dimple.plot.bar, [xAxis, yAxis]);
                s.data = currentSeries.data;
                allMeasurandSeries[currentSeriesId].push(seriesId);
                seriesId += 1;
            } else {
                var allMeasurands = removeDuplicatesFromArray(currentSeries.data.map(function (value) {
                    return value.index;
                }));
                allMeasurands.forEach(function (measurand) {
                    var s = chart.addSeries("index", dimple.plot.bar, [xAxis, yAxis]);
                    s.data = currentSeries.data.filter(function (datum) {
                        return datum.index === measurand;
                    });
                    allMeasurandSeries[currentSeriesId].push(seriesId);
                    seriesId += 1;
                })
            }
        }

        chart.addLegend(legendPosition.x, legendPosition.y, legendPosition.width, legendPosition.height, "right");
        chart.draw();
        resize();
    };

    var adjustChart = function () {
        var xAxisLabel = $("#x-axis-label").val();
        xAxis.title = xAxisLabel;
        chart.draw(0, true);
    };


    var positionBars = function () {
        // Move Bars side to side
        for (var s in allMeasurandSeries) {
            var i = Object.keys(allMeasurandSeries).indexOf(s);
            allMeasurandSeries[s].forEach(function (seriesId) {
                var currentSeriesRects = d3.selectAll(".dimple-series-group-" + seriesId).selectAll("rect");
                var oldWidth = parseInt(currentSeriesRects.attr("width"));
                var translateX = i * oldWidth / seriesCount;
                currentSeriesRects.attr("transform", "translate(" + translateX + ", 0)");
                currentSeriesRects.attr("width", oldWidth / seriesCount);
            })

        }
    };

    var resize = function () {
        var svgWidth, maxWidth, containerWidth;

        containerWidth = $("#" + chartIdentifier).width();
        maxWidth = Object.keys(allMeasurandSeries).length * $(".dimple-axis-x .tick").length * maxWidthPerBar;
        svgWidth = containerWidth < maxWidth ? "100%" : "" + maxWidth + "px";

        svg.setAttribute("width", svgWidth);

        chart.legends[0].x = (containerWidth < maxWidth) ? (containerWidth - legendPosition.width - margins.right) : (maxWidth - legendPosition.width - margins.right);
        // second parameter allows to draw without reprocessing data.
        chart.draw(0, true);

        positionBars();
    };

    var getXLabel = function () {
        return xAxis.title;
    };

    // returns a new array without removed duplicates
    var removeDuplicatesFromArray = function (array) {
        return array.reduce(function (p, c) {
            if (p.indexOf(c) < 0) p.push(c);
            return p;
        }, []);
    };

    init();
    return {
        adjustChart: adjustChart,
        drawChart: drawChart,
        getXLabel: getXLabel
    };
});