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
    var chartBarsComponents = {};
    var chartLegendComponent = OpenSpeedMonitor.ChartComponents.ChartLegend();
    var chartBarScoreComponent = OpenSpeedMonitor.ChartComponents.ChartBarScore();
    var componentMargin = 15;
    var transitionDuration = 500;
    var chartBarsWidth = 700;
    var chartBarsHeight = 400;
    var measurandDataEntries = {};
    var measurandGroupDataMap = {};

    var setData = function (data) {
        measurandDataEntries = (data && data.series) ? extractMeasurandData(data.series) : measurandDataEntries;
        measurandGroupDataMap = (data && data.series) ? extractMeasurandGroupData(data.series) : measurandGroupDataMap;
        chartBarsHeight = calculateChartBarsHeight(measurandDataEntries[0].values.series.length);
        // setDataForHeader(data);
        setDataForLegend(measurandDataEntries);
        setDataForBarScore(data);
        // setDataForSideLabels(data);
        setDataForBars(data);
    };

    var setDataForBarScore = function () {
        chartBarScoreComponent.setData({
            width: chartBarsWidth,
            max: measurandGroupDataMap["LOAD_TIMES"] ? measurandGroupDataMap["LOAD_TIMES"].max : 0
        });
    };

    var setDataForLegend = function () {
        chartLegendComponent.setData({
            entries: measurandDataEntries.map(function (measurandNestEntry) {
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

    var setDataForBars = function () {
        var componentsToRender = {};
        measurandDataEntries.forEach(function (measurandNestEntry) {
            componentsToRender[measurandNestEntry.key] = chartBarsComponents[measurandNestEntry.key] || OpenSpeedMonitor.ChartComponents.ChartBars();
            componentsToRender[measurandNestEntry.key].setData({
                values: measurandNestEntry.values.series,
                color: measurandNestEntry.values.color,
                min: measurandGroupDataMap[measurandNestEntry.values.measurandGroup].min > 0 ? 0 : measurandGroupDataMap[measurandNestEntry.values.measurandGroup].min,
                max: measurandGroupDataMap[measurandNestEntry.values.measurandGroup].max,
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
                    measurandGroup: firstValue.measurandGroup,
                    color: colorScales[unit](firstValue.measurand),
                    series: seriesOfMeasurand
                };
            })
            .entries(series);
    };

    var extractMeasurandGroupData = function (series) {
        return d3.nest()
            .key(function(d) { return d.measurandGroup; })
            .rollup(function (seriesOfMeasurandGroup) {
                var extent = d3.extent(seriesOfMeasurandGroup, function(entry) { return entry.value; });
                return {
                    min: extent[0],
                    max: extent[1]
                };
            }).map(series);
    };

    var calculateChartBarsHeight = function (numberOfMeasurands) {
        var barBand = OpenSpeedMonitor.ChartComponents.ChartBars.BarBand;
        var barGap = OpenSpeedMonitor.ChartComponents.ChartBars.BarGap;
        return numberOfMeasurands * (barBand + barGap) - barGap;
    };

    var render = function () {
        var shouldShowScore = !!measurandGroupDataMap["LOAD_TIMES"];
        var barScorePosY = chartBarsHeight + componentMargin;
        var barScoreHeight = shouldShowScore ? OpenSpeedMonitor.ChartComponents.ChartBarScore.BarHeight + componentMargin : 0;

        renderBars();
        renderBarScore(shouldShowScore, barScorePosY);
        renderLegend(barScorePosY + barScoreHeight);
    };

    var renderBarScore = function (shouldShowScore, posY) {
        var barScore = svg.selectAll(".chart-score-group").data([chartBarScoreComponent]);
        barScore.exit()
            .remove();
        barScore.enter()
            .append("g")
            .attr("class", "chart-score-group")
            .attr("transform", "translate(0, " + posY + ")");
        barScore
            .call(chartBarScoreComponent.render)
            .transition()
            .style("opacity", shouldShowScore ? 1 : 0)
            .duration(transitionDuration)
            .attr("transform", "translate(0, " + posY + ")");
    };

    var renderLegend = function (posY) {
        var legend = svg.selectAll(".chart-legend-group").data([chartLegendComponent]);
        legend.exit()
            .remove();
        legend.enter()
            .append("g")
            .attr("class", "chart-legend-group")
            .attr("transform", "translate(0, " + posY + ")");
        legend.call(chartLegendComponent.render)
            .transition()
            .duration(transitionDuration)
            .attr("transform", "translate(0, " + posY + ")");
    };

    var renderBars = function () {
        var chartBars = svg.selectAll(".bar-charts").data(Object.values(chartBarsComponents));
        chartBars.exit()
            .remove();
        chartBars.enter()
            .append("g")
            .attr("class", "bar-charts");
        chartBars.each(function(chartBarsComponent) {
           chartBarsComponent.render(this);
        });

    };

    return {
        render: render,
        setData: setData
    };

});
