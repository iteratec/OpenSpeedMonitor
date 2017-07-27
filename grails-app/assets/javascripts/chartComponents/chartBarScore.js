//= require /bower_components/d3/d3.min.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartComponents = OpenSpeedMonitor.ChartComponents || {};

OpenSpeedMonitor.ChartComponents.ChartBarScore = (function () {
    var width = 300;
    var initialBarWidth = 0;
    var transitionDuration = 1000;
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

    var setData = function (data) {
        max = data.max || 0;
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
    };

    var render = function (svg) {
        var scale = d3.scale.linear().rangeRound([0, width]).domain([0, max]);
        var scoreBars = svg.selectAll(".scoreBar").data(barsToRender, function (d) { return d.id });
        renderEnter(scoreBars.enter(), scale);
        renderExit(scoreBars.exit());
        renderUpdate(scoreBars, scale);
    };

    var renderEnter = function(enterSelection, scale) {
        var barGroup = enterSelection
            .append("g")
            .attr("class", "scoreBar")
            .attr("transform", function (d) {
                return "translate(" + scale(d.start) + ", 0)";
            });
        barGroup.append("rect")
            .attr("height", barHeight)
            .attr("width", initialBarWidth)
            .attr("fill", function (d) { return d.fill; })
        barGroup.append("text")
            .attr("class", function (d) { return d.cssClass; })
            .text(function (d) { return d.label; })
            .attr("y", barHeight / 2)
            .attr("dy", ".35em") //vertical align middle
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
            });
    };

    var renderExit = function (exitSelection) {
        exitSelection.transition()
            .duration(transitionDuration)
            .attr("width", 0)
            .remove();
    };

    return {
        render: render,
        setData: setData
    };

});

OpenSpeedMonitor.ChartComponents.ChartBarScore.BarHeight = 40;
