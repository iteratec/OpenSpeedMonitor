//= require /node_modules/d3/d3.min.js
//= require common.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartComponents = OpenSpeedMonitor.ChartComponents || {};

OpenSpeedMonitor.ChartComponents.ChartBarScore = (function () {
    var width = 300;
    var transitionDuration = OpenSpeedMonitor.ChartComponents.common.transitionDuration;
    var barHeight = OpenSpeedMonitor.ChartComponents.common.barBand;
    var availableScoreBars = [
        {
            id: "good",
            fill: "#bbe2bb",
            label: "GOOD",
            cssClass: "d3chart-good",
            end: 1000
        },
        {
            id: "okay",
            fill: "#f9dfba",
            label: "OK",
            cssClass: "d3chart-ok",
            end: 3000
        },
        {
            id: "bad",
            fill: "#f5d1d0",
            label: "BAD",
            cssClass: "d3chart-bad"
        }
    ];
    var barsToRender = [];
    var max = 0;
    var min = 0;

    var setData = function (data) {
        max = data.max || 0;
        min = data.min || 0;
        width = data.width || width;
        barsToRender = [];
        var lastBarEnd = 0;
        for (var curScoreBar = 0; curScoreBar < availableScoreBars.length; curScoreBar++) {
            var bar = $.extend({}, availableScoreBars[curScoreBar]);
            barsToRender.push(bar);
            bar.start = lastBarEnd;
            if (!bar.end || max < bar.end || !availableScoreBars[curScoreBar + 1]) {
                bar.end = max;
                break;
            }
            lastBarEnd = bar.end;
        }
        barsToRender.reverse();
    };

    var render = function (svg, isAggregationValueChange) {
        var scale = d3.scale.linear().rangeRound([0, width]).domain([min, max]);
        var scoreBars = svg.selectAll(".scoreBar").data(barsToRender, function (d) { return d.id });
        renderExit(scoreBars.exit());
        renderEnter(scoreBars.enter());
        renderUpdate(scoreBars, scale, isAggregationValueChange);
    };

    var renderEnter = function(enterSelection) {
        var barGroup = enterSelection
            .append("g")
            .attr("class", "scoreBar");
        barGroup.append("rect")
            .attr("height", barHeight)
            .attr("fill", function (d) { return d.fill; });
        barGroup.append("text")
            .attr("class", function (d) { return d.cssClass; })
            .text(function (d) { return d.label; })
            .style("opacity", 0)
            .attr("y", barHeight / 2)
            .attr("dominant-baseline", "middle")
            .attr("text-anchor", "middle");
    };

    var renderUpdate = function (updateSelection, scale, isAggregationValueChange) {
        var xTransition = updateSelection;
        if (isAggregationValueChange) {
            xTransition = updateSelection.transition().duration(transitionDuration)
        }

        xTransition.select('rect')
            .attr("width", function (d) {
                return scale(d.end) - scale(d.start);
            })
            .attr("x", function (d) {
                return scale(d.start);
            });
        xTransition.select('text')
            .attr("x", function (d) {
                return (scale(d.end) + scale(d.start)) / 2;
            })
            .style("opacity", function(d) {
                return ((this.getComputedTextLength() + 20) > ((scale(d.end) - scale(d.start)) / 2)) ? 0 : 1;
            });
    };

    var renderExit = function (exitSelection) {
        exitSelection.remove();
    };

    return {
        render: render,
        setData: setData
    };

});
