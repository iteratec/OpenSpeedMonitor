//= require /bower_components/d3/d3.min.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartComponents = OpenSpeedMonitor.ChartComponents || {};

OpenSpeedMonitor.ChartComponents.ChartBarScore = (function () {
    var width = 300;
    var transitionDuration = 500;
    var barHeight = OpenSpeedMonitor.ChartComponents.ChartBarScore.BarHeight;
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

    var render = function (svg) {
        var scale = d3.scale.linear().rangeRound([0, width]).domain([min, max]);
        var scoreBars = svg.selectAll(".scoreBar").data(barsToRender, function (d) { return d.id });
        renderExit(scoreBars.exit(), scale);
        renderEnter(scoreBars.enter(), scale);
        renderUpdate(scoreBars, scale);
    };

    var renderEnter = function(enterSelection, scale) {
        var barGroup = enterSelection
            .append("g")
            .attr("class", "scoreBar");
        barGroup.append("rect")
            .attr("height", barHeight)
            .attr("width", 0)
            .attr("fill", function (d) { return d.fill; });
        barGroup.append("text")
            .attr("class", function (d) { return d.cssClass; })
            .text(function (d) { return d.label; })
            .style("opacity", 0)
            .attr("y", barHeight / 2)
            .attr("dominant-baseline", "middle")
            .attr("text-anchor", "middle");
    };

    var renderUpdate = function (updateSelection, scale) {
        var transition = updateSelection
            .transition()
            .duration(transitionDuration);
        transition.attr("transform", function (d) {
                return "translate(" + scale(d.start) + ", 0)";
            });
        transition.select('rect')
            .attr("width", function (d) {
                return scale(d.end) - scale(d.start);
            });
        transition.select('text')
            .attr("x", function (d) {
                return (scale(d.end) - scale(d.start)) / 2;
            })
            .style("opacity", function(d) {
                return ((this.getComputedTextLength() + 20) > ((scale(d.end) - scale(d.start)) / 2)) ? 0 : 1;
            });
    };

    var renderExit = function (exitSelection, scale) {
        var exitTransition = exitSelection.transition()
            .duration(transitionDuration);
        exitTransition
            .select('rect')
            .attr("width", 0);
        exitTransition
            .select('text')
            .style("opacity", 0)
            .attr("x", 0);
        exitTransition
            .attr("transform", function (d) {
                return "translate(" + scale(d.start) + ", 0)";
            })
            .remove();
    };

    return {
        render: render,
        setData: setData,
    };

});

OpenSpeedMonitor.ChartComponents.ChartBarScore.BarHeight = 40;
