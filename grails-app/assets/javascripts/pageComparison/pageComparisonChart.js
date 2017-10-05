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


"use strict";
var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageComparisonChart = (function (chartIdentifier) {

    var svg = d3.select(chartIdentifier),
        allBarsGroup,
        trafficLightBars,
        transitionDuration = 600,
        headerLine;
    var data = OpenSpeedMonitor.ChartModules.PageComparisonData(svg);
    var chartBarsComponents = {};
    var chartBarScoreComponent = OpenSpeedMonitor.ChartComponents.ChartBarScore();
    // var chartSideLabelsComponent = OpenSpeedMonitor.ChartComponents.ChartSideLabels();
    var chartHeaderComponent = OpenSpeedMonitor.ChartComponents.ChartHeader();

    var setData = function (inputData) {
        data.setData(inputData);
        chartHeaderComponent.setData(data.getDataForHeader());
        chartBarScoreComponent.setData(data.getDataForBarScore());
        setBarChartData();

    };


    var initChart = function () {
        $("#chart-card").removeClass("hidden");

        svg = d3.select(chartIdentifier);

        headerLine = svg.append("g");
        allBarsGroup = svg.append("g");
        trafficLightBars = svg.append("g");
    };

    var setBarChartData = function () {
        var componentsToRender = [];
        var firstPage = OpenSpeedMonitor.ChartComponents.ChartBars();
        firstPage.setData(data.getDataForBars(0));
        var secondPage = OpenSpeedMonitor.ChartComponents.ChartBars();
        secondPage.setData(data.getDataForBars(1));
        componentsToRender.push(secondPage);
        componentsToRender.push(firstPage);
        chartBarsComponents = componentsToRender;
    };

    var render = function () {
        var shouldShowScore = data.hasLoadTimes();
        var componentMargin = OpenSpeedMonitor.ChartModules.PageComparisonData.ComponentMargin;
        var headerHeight = OpenSpeedMonitor.ChartComponents.ChartHeader.Height + componentMargin;
        var barScorePosY = data.getChartBarsHeight() + OpenSpeedMonitor.ChartComponents.common.barBand + componentMargin*2;
        var barScoreHeight = shouldShowScore ? OpenSpeedMonitor.ChartComponents.common.barBand + componentMargin : 0;
        var chartHeight = headerHeight + data.getChartBarsHeight() + barScoreHeight ;

        // var chartHeight = legendPosY + legendHeight + headerHeight;
        svg.transition()
            .duration(transitionDuration)
            .style("height", chartHeight)
            .each("end", rerenderIfWidthChanged);
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
        renderBarScore(svg, shouldShowScore, barScorePosY)

    };
    var rerenderIfWidthChanged = function () {
        if (data.needsAutoResize()) {
            setData({autoWidth: true});
            render();
        }
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

    initChart();

    return {
        setData: setData,
        render: render
    };

});
