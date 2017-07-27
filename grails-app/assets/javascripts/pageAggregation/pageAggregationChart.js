//= require /bower_components/d3/d3.min.js
//= require /chartComponents/chartBars.js
//= require /chartComponents/chartBarScore.js
//= require /d3/chartColorProvider.js
//= require /chartComponents/chartLegend.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageAggregation = (function (selector) {
    var svg = d3.select(selector);
    var svgChartBars = svg.append("g");
    var svgChartScore = svg.append("g");
    var svgChartLegend = svg.append("g");
    var chartBarsComponents = {};
    var chartLegendComponent = OpenSpeedMonitor.ChartComponents.ChartLegend();
    var chartBarScoreComponent = OpenSpeedMonitor.ChartComponents.ChartBarScore();
    var barComponentWidth = 700;

    var setData = function (data) {
        // setDataForHeader(data);
        setDataForLegend(data);
        setDataForBarScore(data);
        // setDataForSideLabels(data);
        setDataForBars(data);
    };

    var setDataForBarScore = function (data) {
        chartBarScoreComponent.setData({
            width: barComponentWidth,
            max: d3.max(data.series, function(entry) { return entry.value; })
        });
    };

    var setDataForLegend = function (data) {
        var colorProvider = OpenSpeedMonitor.ChartColorProvider();
        var colorScales = {};
        var labelsAndColors = {};
        data.series.forEach(function(entry) {
           if (labelsAndColors[entry.measurand]) {
               return;
           }
           colorScales[entry.unit] = colorScales[entry.unit] || colorProvider.getColorscaleForMeasurandGroup(entry.unit);
           labelsAndColors[entry.measurand] = {
               id: entry.measurand,
               label: entry.measurandLabel,
               color: colorScales[entry.unit](entry.measurand)
           };
        });
        chartLegendComponent.setData({
            entries: Object.values(labelsAndColors),
            width: barComponentWidth
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
        chartLegendComponent.render(svgChartLegend);
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
