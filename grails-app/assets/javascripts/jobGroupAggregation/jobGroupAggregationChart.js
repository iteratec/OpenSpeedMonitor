//= require /node_modules/d3/d3.min.js
//= require /chartComponents/chartBars.js
//= require /chartComponents/chartBarScore.js
//= require /d3/chartColorProvider.js
//= require /chartComponents/chartLegend.js
//= require /chartComponents/chartSideLabels.js
//= require /chartComponents/chartHeader.js
//= require /d3/chartLabelUtil.js
//= require /jobGroupAggregation/jobGroupAggregationChartData.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.JobGroupAggregationHorizontal = (function (selector) {
    var svg = d3.select(selector);
    var chartBars = OpenSpeedMonitor.ChartComponents.ChartBars();
    var chartBarScoreComponent = OpenSpeedMonitor.ChartComponents.ChartBarScore();
    var chartSideLabelsComponent = OpenSpeedMonitor.ChartComponents.ChartSideLabels();
    var chartHeaderComponent = OpenSpeedMonitor.ChartComponents.ChartHeader();
    var data = OpenSpeedMonitor.ChartModules.JobGroupAggregationData(svg);
    var transitionDuration = OpenSpeedMonitor.ChartComponents.common.transitionDuration;
    var highlightedGroupId = null;

    var setData = function (inputData) {
        data.setData(inputData);
        chartHeaderComponent.setData(data.getDataForHeader());
        chartBarScoreComponent.setData(data.getDataForBarScore());
        chartSideLabelsComponent.setData(data.getDataForSideLabels());
        chartBars.setData(data.getDataForBars());
        chartBars.on("click", function () {
            highlightedGroupId = this.click.arguments["0"].id;
            toggleBarHighlight(highlightedGroupId);
        });
    };

    var resetData = function () {
        data.resetData()
    };

    var render = function () {
        if (data.isDataAvailable()) {
            var shouldShowScore = data.hasLoadTimes();
            var componentMargin = OpenSpeedMonitor.ChartComponents.common.ComponentMargin;
            var headerHeight = OpenSpeedMonitor.ChartComponents.ChartHeader.Height + componentMargin;
            var barScorePosY = data.getChartBarsHeight() + componentMargin;
            var barScoreHeight = shouldShowScore ? OpenSpeedMonitor.ChartComponents.common.barBand + componentMargin : 0;
            var chartHeight = barScorePosY + barScoreHeight + headerHeight;

            svg.transition()
              .duration(transitionDuration);

            var svgName = selector.substr(1);
            document.getElementById(svgName).setAttribute("height",chartHeight);
            renderHeader(svg);
            renderSideLabels(svg, headerHeight);

            var contentGroup = svg.selectAll(".bars-content-group").data([1]);
            contentGroup.enter()
                .append("g")
                .classed("bars-content-group", true);
            contentGroup.attr("transform",
                "translate(" + (data.getChartSideLabelsWidth() + componentMargin) + ", " + headerHeight + ")");
            renderBars(contentGroup);
            renderBarScore(contentGroup, shouldShowScore, barScorePosY);
        }
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

    var renderSideLabels = function (svg, posY) {
        var sideLabels = svg.selectAll(".side-labels-group").data([chartSideLabelsComponent]);
        sideLabels.exit()
            .remove();
        sideLabels.enter()
            .append("g")
            .classed("side-labels-group", true);
        sideLabels
            .attr("transform", "translate(0, " + posY + ")")
            .call(chartSideLabelsComponent.render)
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

    var renderBars = function (svg) {
        var bars = svg.selectAll(".chart-bars").data([chartBars]);
        bars.exit()
            .remove();
        bars.enter()
            .append("g")
            .attr("class", "chart-bars")
        bars
            .call(chartBars.render)
            .transition()
            .style("opacity", 1)
            .duration(transitionDuration)
    };

    var toggleBarHighlight = function (highlightGroupId) {
        chartBars.setData({highLightId: highlightGroupId});
        render();
    };

    return {
        render: render,
        setData: setData,
        resetData: resetData
    };

});
