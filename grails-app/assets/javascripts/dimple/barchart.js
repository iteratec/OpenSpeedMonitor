//= require /bower_components/d3/d3.min.js
//= require /bower_components/dimple/dist/dimple.latest.min.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageAggregation = function (chartIdentifier) {
    var chart = null,
        height = 400,
        legendPosition = {x: 200, y: 10, width: 380, height: 20},
        maxWidthPerBar = 300,
        series = null,
        svg = null;

    var init = function () {
        // add eventHandler
        window.onresize = function () {
            resize();
        };
    };

    var drawChart = function (barchartData) {
        // Delete old chart in same container
        d3.select("#" + chartIdentifier).selectAll("svg").remove();

        svg = dimple.newSvg("#" + chartIdentifier, "100%", height);
        chart = new dimple.chart(svg, null);
        chart.setMargins("60px", "30px", "110px", "70px");
        svg = svg.node();

        chart.data = barchartData.data;
        setYValueAccessor(barchartData.yValueAccessor, barchartData.yValueUnit);

        addXCategory(barchartData.xGroupings);

        barchartData.stackedAttributes.forEach(function (stack) {
            addStackAttribute(stack)
        });

        chart.addLegend(legendPosition.x, legendPosition.y, legendPosition.width, legendPosition.height);
        chart.addSeries(series, dimple.plot.bar);
        chart.draw();
        resize();
    };

    addXCategory = function (category) {
        if (series == null) {
            series = []
        }
        category.forEach(function (c) {
            series.push(c);
        });
        chart.addCategoryAxis("x", category);
    };

    setYValueAccessor = function (accessor, unit) {
        var axis = chart.addMeasureAxis("y", accessor);
        axis.title = accessor + " (" + unit + ")"
    };

    addStackAttribute = function (stackAccessor) {
        if (series == null) {
            series = []
        }
        series.push(stackAccessor);
    };

    var resize = function () {
        var svgWidth, maxWidth, containerWidth;

        containerWidth = $("#" + chartIdentifier).width();
        maxWidth = $(".dimple-series-0").length * maxWidthPerBar;
        svgWidth = containerWidth < maxWidth ? "100%" : "" + maxWidth + "px";

        svg.setAttribute("width", svgWidth);

        // second parameter here allows you to draw without reprocessing data.
        chart.draw(0, true);
    };
    
    init();
    return {
        drawChart: drawChart
    };
};