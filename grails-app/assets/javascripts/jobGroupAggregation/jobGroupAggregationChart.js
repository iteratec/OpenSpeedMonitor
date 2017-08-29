//= require /bower_components/d3/d3.min.js
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
    var chartBarsComponents = {};
    var chartBars = OpenSpeedMonitor.ChartComponents.ChartBars();
    var chartBarScoreComponent = OpenSpeedMonitor.ChartComponents.ChartBarScore();
    var chartSideLabelsComponent = OpenSpeedMonitor.ChartComponents.ChartSideLabels();
    var chartHeaderComponent = OpenSpeedMonitor.ChartComponents.ChartHeader();
    var data = OpenSpeedMonitor.ChartModules.JobGroupAggregationData(svg);
    var transitionDuration = 500;

    var setData = function (inputData) {
        data.setData(inputData);
        chartHeaderComponent.setData(data.getDataForHeader());
        chartBarScoreComponent.setData(data.getDataForBarScore());
        chartSideLabelsComponent.setData(data.getDataForSideLabels());
        chartBars.setData(data.getDataForBars());
    };

    var setDataForBars = function () {
        if (!chartBarsComponents) {
            var component = OpenSpeedMonitor.ChartComponents.ChartBars();
            component.on("mouseover", function () {
                // chartLegendComponent.mouseOverEntry({id: measurand});
            });
            component.on("mouseout", function () {
                // chartLegendComponent.mouseOutEntry({id: measurand});
            });
            component.on("click", function () {
                // chartLegendComponent.clickEntry({id: measurand});
            });
            chartBarsComponents = component;
        }
        var componentsToRender = chartBarsComponents;
        componentsToRender.setData(data.getDataForBars());
        chartBarsComponents = componentsToRender;
    };

    var render = function () {
        var shouldShowScore = data.hasLoadTimes();
        var componentMargin = OpenSpeedMonitor.ChartModules.JobGroupAggregationData.ComponentMargin;
        var headerHeight = OpenSpeedMonitor.ChartComponents.ChartHeader.Height + componentMargin;
        var barScorePosY = data.getChartBarsHeight() + componentMargin;
        var barScoreHeight = shouldShowScore ? OpenSpeedMonitor.ChartComponents.ChartBarScore.BarHeight + componentMargin : 0;
        var chartHeight = barScorePosY + barScoreHeight + headerHeight;

        svg
            .transition()
            .duration(transitionDuration)
            .style("height", chartHeight)
            .each("end", rerenderIfWidthChanged);

        renderHeader(svg);
        renderSideLabels(svg, headerHeight);

        var contentGroup = svg.selectAll(".bars-content-group").data([1]);
        contentGroup.enter()
            .append("g")
            .classed("bars-content-group", true);
        contentGroup
            .transition()
            .duration(transitionDuration)
            .attr("transform", "translate(" + (data.getChartSideLabelsWidth() + componentMargin) + ", " + headerHeight + ")");
        renderBars(contentGroup);
        renderBarScore(contentGroup, shouldShowScore, barScorePosY);
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

    var rerenderIfWidthChanged = function () {
        if (data.needsAutoResize()) {
            setData({autoWidth: true});
            render();
        }
    };

    // var toggleBarComponentHighlight = function (measurandToHighlight, anyHighlighted, doHighlight) {
    //     Object.keys(chartBarsComponents).forEach(function(measurand) {
    //         var isRestrained = anyHighlighted && !(doHighlight && measurand === measurandToHighlight);
    //         chartBarsComponents[measurand].setData({isRestrained: isRestrained});
    //     });
    //     render();
    // };

    return {
        render: render,
        setData: setData
    };

});
