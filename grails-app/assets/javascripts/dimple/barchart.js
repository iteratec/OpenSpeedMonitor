//= require /bower_components/d3/d3.min.js
//= require /bower_components/dimple/dist/dimple.latest.min.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageAggregation = (function (chartIdentifier) {
    var chart = null,
        width = 600,
        height = 600,
        legendPosition = {x: 200, y: 10, width: 100, height: 100},
        margins = {left: 60, right: 100, top: 110, bottom: 150},
        maxWidthPerBar = 150,
        allMeasurandSeries = {},
        svg = null,
        seriesCount = 0,
        xAxis = null,
        yAxes = {},
        showXAxis = true,
        showYAxis = true,
        showGridlines = true;

    var init = function () {
        // add eventHandler
        window.onresize = function () {
            resize();
        };
    };

    var drawChart = function (barchartData) {
        // Delete old chart in same container
        d3.select("#" + chartIdentifier).selectAll("svg").remove();
        var $adjustBarchartButton = $("#adjust-barchart-modal");
        if ($adjustBarchartButton.hasClass("hidden"))
            $adjustBarchartButton.removeClass("hidden");

        // Reset fields
        allMeasurandSeries = {};
        yAxes = {};
        xAxis = null;
        height = 600;

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
        initWidth();
        resize();
    };

    var adjustChart = function () {
        // change size
        width = $("#inputChartWidth").val() + "px";
        height = $("#inputChartHeight").val() + "px";

        // change labels
        xAxis.title = $("#x-axis-label").val();
        $("#y-axis-alias-container").children().each(function () {
            yAxes[$(this).find(".unitInput").val()].title = $(this).find(".labelInput").val()
        });

        // assign colors
        var colorAssignments = [];
        $("#assign-color-container").children().each(function () {
            colorAssignments.push({"label": $(this).find("label").html(), "color": $(this).find("input").val()});
        });
        assignColor(colorAssignments);

        // Toggle gridlines
        showGridlines = $("#inputShowGridlines").prop("checked");
        if (showGridlines) {
            d3.select(".dimple-gridline").style("opacity", 1);
        } else {
            d3.select(".dimple-gridline").style("opacity", 0);
        }

        // Toggle yAxis
        showYAxis = $("#inputShowYAxis").prop("checked");
        if (showYAxis) {
            d3.selectAll(".dimple-axis.dimple-axis-y").style("opacity", 1);
        } else {
            d3.selectAll(".dimple-axis.dimple-axis-y").style("opacity", 0);
        }

        // Toggle xAxis
        showXAxis = $("#inputShowXAxis").prop("checked");
        if (showXAxis) {
            d3.select(".domain.dimple-custom-axis-line").style("opacity", 1);
            d3.select(".dimple-axis-x").selectAll("line").style("opacity", 1);
        } else {
            d3.select(".domain.dimple-custom-axis-line").style("opacity", 0);
            d3.select(".dimple-axis-x").selectAll("line").style("opacity", 0);
        }

        resize();
    };

    var assignColor = function (colorAssignments) {
        colorAssignments.forEach(function (assignment) {
            chart.assignColor(assignment.label, assignment.color);
        });

        // reassign data to series so series are redrawn
        chart.series.forEach(function (s) {
            var seriesData = s.data;
            // fake some data change
            s.data = [{"grouping": s.data[0].grouping, "index": "", "indexValue": 0}];
            chart.draw();
            s.data = seriesData;
        });

        chart.draw();
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

    var initWidth = function () {
        var maxWidth, containerWidth;

        containerWidth = $("#" + chartIdentifier).width();
        maxWidth = Object.keys(allMeasurandSeries).length * $(".dimple-axis-x .tick").length * maxWidthPerBar;
        width = containerWidth < maxWidth ? "100%" : "" + maxWidth + "px";
    };

    var resize = function () {
        if (svg) {
            svg.setAttribute("width", width);
            svg.setAttribute("height", height);
            chart.legends[0].x = parseInt(width) - legendPosition.width - margins.right;

            // second parameter allows to draw without reprocessing data.
            chart.draw(0, true);
            positionBars();
        }
    };

    var getXLabel = function () {
        return xAxis.title;
    };
    var getYLabels = function () {
        var result = [];
        Object.keys(yAxes).forEach(function (key) {
            result.push({"unit": key, "label": yAxes[key].title})
        });
        return result;
    };
    var getWidth = function () {
        var svgWidth = svg.getAttribute("width");
        if (svgWidth.endsWith("px")) {
            return parseInt(svgWidth);
        } else if (svgWidth.endsWith("%")) {
            return parseInt(svgWidth) * $("#" + chartIdentifier).width() / 100;
        } else {
            return svgWidth;
        }
    };
    var getHeight = function () {
        var svgHeight = svg.getAttribute("height");
        if (svgHeight.endsWith("px")) {
            return parseInt(svgHeight);
        } else if (svgHeight.endsWith("%")) {
            return parseInt(svgHeight) * $("#" + chartIdentifier).height() / 100;
        } else {
            return svgHeight;
        }
    };
    var getColorAssignments = function () {
        var result = [];
        chart.legends[0]._getEntries().forEach(function (legendEntry) {
            result.push({"label": legendEntry.key, "color": legendEntry.fill});
        });
        return result;
    };
    var getShowXAxis = function () {
        return showXAxis;
    };
    var getShowYAxis = function () {
        return showYAxis;
    };
    var getShowGridlines = function () {
        return showGridlines;
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
        getColorAssignments: getColorAssignments,
        getHeight: getHeight,
        getWidth: getWidth,
        getXLabel: getXLabel,
        getYLabels: getYLabels,
        getShowXAxis: getShowXAxis,
        getShowYAxis: getShowYAxis,
        getShowGridlines: getShowGridlines
    };
});
