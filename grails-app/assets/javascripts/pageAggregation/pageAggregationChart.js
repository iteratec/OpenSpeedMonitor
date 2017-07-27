//= require /bower_components/d3/d3.min.js
//= require /chartComponents/chartBars.js
//= require /chartComponents/chartBarScore.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageAggregation = (function (selector) {
    var svg = d3.select(selector);
    var svgChartBars = svg.append("g");
    var svgChartScore = svg.append("g");
    var chartBarsComponents = {};
    var chartBarScoreComponent = OpenSpeedMonitor.ChartComponents.ChartBarScore();

    var setData = function (data) {
        // setDataForHeader(data);
        // setDataForLegend(data);
        setDataForBarScore(data);
        // setDataForSideLabels(data);
        setDataForBars(data);
    };

    var setDataForBarScore = function (data) {
        chartBarScoreComponent.setData({
            width: 700,
            max: d3.max(data.series, function(entry) { return entry.value; })
        });
    };

    var setDataForBars = function (data) {
        var seriesByMeasurand = d3.nest()
            .key(function(d) { return d.measurand; })
            .entries(data.series);
        var newchartBarsComponents = {};
        seriesByMeasurand.forEach(function (measurandData) {
            newchartBarsComponents[measurandData.key] = chartBarsComponents[measurandData.key] || OpenSpeedMonitor.ChartComponents.ChartBars();
            newchartBarsComponents[measurandData.key].setData(measurandData.values);
        });
        chartBarsComponents = newchartBarsComponents;
    };

    var render = function () {
        renderBars();
        chartBarScoreComponent.render(svgChartScore);
    };

    var renderBars = function () {
        var chartBars = svgChartBars.selectAll(".bar-charts").data(Object.values(chartBarsComponents));
        chartBars.enter()
            .append("g")
            .attr("class", "bar-charts");
        chartBars.exit()
            .remove();
        chartBars.each(function(chartBarsComponent) {
           chartBarsComponent.render(this);
        });

    };

    return {
        render: render,
        setData: setData
    };

});
