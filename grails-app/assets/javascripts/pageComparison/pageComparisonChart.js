//= require /bower_components/d3/d3.min.js
//= require /d3/chartLabelUtil
//= require /d3/trafficLightDataProvider
//= require /d3/chartColorProvider
//= require_self
//= require /chartComponents/utility
//= require /chartComponents/common
//= require /chartComponents/chartHeader
//= require /chartComponents/chartBarScore
//= require /chartComponents/chartBars
//= require /chartComponents/chartMultiLegend.js

"use strict";
var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageComparisonChart = (function (chartIdentifier) {

    var svg = d3.select(chartIdentifier);
    var transitionDuration = OpenSpeedMonitor.ChartComponents.common.transitionDuration;
    var data = OpenSpeedMonitor.ChartModules.PageComparisonData(svg);
    var chartBarsComponents = {};
    var chartBarScoreComponent = OpenSpeedMonitor.ChartComponents.ChartBarScore();
    var chartLegendComponent = OpenSpeedMonitor.ChartComponents.ChartMultiLegend();
    var chartHeaderComponent = OpenSpeedMonitor.ChartComponents.ChartHeader();
    var highlightedIds = "";
    var anyHighlighted = false;
    var selectedIds = "";
    var anySelected = false;


    chartLegendComponent.on("select", function (selectEvent) {
        selectedIds = selectEvent.ids;
        anySelected = selectEvent.anySelected;
        render()
    });

    chartLegendComponent.on("highlight", function (highlightEvent) {
        highlightedIds = highlightEvent.ids;
        anyHighlighted = highlightEvent.anyHighlighted;
        render()
    });

    var setData = function (inputData) {
        data.setData(inputData);
        chartHeaderComponent.setData(data.getDataForHeader());
        chartBarScoreComponent.setData(data.getDataForBarScore());
        chartLegendComponent.setData(data.getDataForLegend());
        setBarChartData();
        registerBarEvents();
    };

    var setBarChartData = function () {
        var componentsToRender = [];
        var firstPage = OpenSpeedMonitor.ChartComponents.ChartBars();
        firstPage.setData(data.getDataForBars(0));
        firstPage.setOpacitiyFunction(determineBarOpacity);
        var secondPage = OpenSpeedMonitor.ChartComponents.ChartBars();
        secondPage.setData(data.getDataForBars(1));
        secondPage.setOpacitiyFunction(determineBarOpacity);
        componentsToRender.push(secondPage);
        componentsToRender.push(firstPage);
        chartBarsComponents = componentsToRender;
    };

    var registerBarEvents = function () {
        chartBarsComponents.forEach(function (barGroup) {
            barGroup.on("click", function (bar) {
                chartLegendComponent.clickEntry([bar.id]);
            });
            barGroup.on("mouseover", function (bar) {
                chartLegendComponent.mouseOverEntry([bar.id])
            });
            barGroup.on("mouseout", function (bar) {
                chartLegendComponent.mouseOutEntry([bar.id])
            });
        });
    };

    var determineBarOpacity = function (d) {
        var opacity = 1;
        if(anyHighlighted){
            opacity = 0.2;
            if(highlightedIds.indexOf(d.id) > -1) opacity = 1;
        }
        if(anySelected){
            opacity = 0.2;
            if(selectedIds.indexOf(d.id) > -1) opacity = 1;
        }
        return opacity
    };

    var render = function () {
        var shouldShowScore = data.hasLoadTimes();
        var componentMargin = OpenSpeedMonitor.ChartModules.PageComparisonData.ComponentMargin;
        var headerHeight = OpenSpeedMonitor.ChartComponents.ChartHeader.Height + componentMargin;
        var barScorePosY = data.getChartBarsHeight() + OpenSpeedMonitor.ChartComponents.common.barBand + componentMargin*2;
        var barScoreHeight = shouldShowScore ? OpenSpeedMonitor.ChartComponents.common.barBand + componentMargin : 0;
        var legendPosY = barScorePosY;
        var legendHeight = chartLegendComponent.estimateHeight(svg) + componentMargin;
        var chartHeight = headerHeight + data.getChartBarsHeight() + barScoreHeight + legendHeight + 20;
        svg.transition()
            .duration(transitionDuration)
            .style("height", chartHeight);
        var contentGroup = svg.selectAll(".bars-content-group").data([1]);
        contentGroup.enter()
            .append("g")
            .classed("bars-content-group", true);

        contentGroup
            .transition()
            .duration(transitionDuration)
            .attr("transform", "translate(" + (data.getChartSideLabelsWidth()) + ", " + headerHeight + ")");


        renderHeader(svg);
        renderBars(contentGroup);
        renderBarScore(svg, shouldShowScore, barScorePosY);
        renderLegend(contentGroup, legendPosY);
    };

    var renderLegend = function (svg, posY) {
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

    var renderBars = function (contentGroup) {
        var chartBarsGroup = contentGroup.selectAll(".chart-bar-group").data([1]);
        chartBarsGroup.enter()
            .append("g")
            .attr("class", "chart-bar-group");
        var chartBars = chartBarsGroup.selectAll(".chart-bars").data(chartBarsComponents);
        chartBars.exit()
            .transition()
            .duration(transitionDuration)
            .attr("transform", "translate(0, 0)")
            .style("opacity", 0)
            .remove();
        chartBars.enter()
            .append("g")
            .attr("class", "chart-bars");
        chartBars
            .each(function (chartBarsComponent) {
                chartBarsComponent.render(d3.select(this));
            });
    };

    var renderHeader = function (svg) {
        var header = svg.selectAll(".header-group").data([chartHeaderComponent]);
        header.exit()
            .remove();
        header.enter()
            .append("g")
            .classed("header-group", true);
        header.call(chartHeaderComponent.render);
    };
    var renderBarScore = function (svg, shouldShowScore, posY) {
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

    return {
        setData: setData,
        render: render
    };

});
