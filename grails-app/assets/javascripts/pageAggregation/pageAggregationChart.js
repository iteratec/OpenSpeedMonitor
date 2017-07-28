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
    var componentMargin = 15;
    var chartBarsWidth = 700;
    var chartBarsHeight = 400;
    var measurandDataNest = {};

    var setData = function (data) {
        measurandDataNest = (data && data.series) ? extractMeasurandData(data.series) : measurandDataNest;
        chartBarsHeight = calculateChartBarsHeight(measurandDataNest[0].values.series.length);
        // setDataForHeader(data);
        setDataForLegend(measurandDataNest);
        setDataForBarScore(data);
        // setDataForSideLabels(data);
        setDataForBars(data);
    };

    var setDataForBarScore = function (data) {
        chartBarScoreComponent.setData({
            width: chartBarsWidth,
            max: d3.max(data.series, function(entry) { return entry.value; })
        });
    };

    var setDataForLegend = function () {
        chartLegendComponent.setData({
            entries: measurandDataNest.map(function (measurandNestEntry) {
                var measurandValue = measurandNestEntry.values;
                return {
                    id: measurandValue.id,
                    color: measurandValue.color,
                    label: measurandValue.label
                };
            }),
            width: chartBarsWidth
        });
    };

    var setDataForBars = function (data) {
        var componentsToRender = {};
        measurandDataNest.forEach(function (measurandNestEntry) {
            componentsToRender[measurandNestEntry.key] = chartBarsComponents[measurandNestEntry.key] || OpenSpeedMonitor.ChartComponents.ChartBars();
            componentsToRender[measurandNestEntry.key].setData({
                values: measurandNestEntry.values.series,
                color: measurandNestEntry.values.color,
                height: chartBarsHeight,
                width: chartBarsWidth
            });
        });
        chartBarsComponents = componentsToRender;
    };

    var extractMeasurandData = function (series) {
        var colorProvider = OpenSpeedMonitor.ChartColorProvider();
        var colorScales = {};
        return d3.nest()
            .key(function(d) { return d.measurand; })
            .rollup(function (seriesOfMeasurand) {
                var firstValue = seriesOfMeasurand[0];
                var unit = firstValue.unit;
                colorScales[unit] = colorScales[unit] || colorProvider.getColorscaleForMeasurandGroup(unit);
                return {
                    id: firstValue.measurand,
                    label: firstValue.measurandLabel,
                    color: colorScales[unit](firstValue.measurand),
                    series: seriesOfMeasurand
                };
            })
            .entries(series);
    };

    var calculateChartBarsHeight = function (numberOfMeasurands) {
        var barBand = OpenSpeedMonitor.ChartComponents.ChartBars.BarBand;
        var barGap = OpenSpeedMonitor.ChartComponents.ChartBars.BarGap;
        return numberOfMeasurands * (barBand + barGap) - barGap;
    };

    var render = function () {
        renderBars();
        var svgChartScoreY = chartBarsHeight + componentMargin;
        var svgChartLegendY = svgChartScoreY + OpenSpeedMonitor.ChartComponents.ChartBarScore.BarHeight + componentMargin;
        svgChartScore
            .call(chartBarScoreComponent.render)
            .transition()
            .duration(transitionDuration)
            .attr("transform", "translate(0, " + svgChartScoreY + ")");
        svgChartLegend
            .call(chartLegendComponent.render)
            .transition()
            .duration(transitionDuration)
            .attr("transform", "translate(0, " + svgChartLegendY + ")");
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
