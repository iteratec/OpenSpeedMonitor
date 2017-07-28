//= require /bower_components/d3/d3.min.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartComponents = OpenSpeedMonitor.ChartComponents || {};

OpenSpeedMonitor.ChartComponents.ChartLegend = (function () {
    var svg = null;
    var entryData = [];
    var width = 300;
    var colorPreviewSize = 10;
    var entryMargin = 20;

    var setData = function (data) {
        width = data.width || 300;
        entryData = data.entries || entryData;
    };

    var render = function (d3SelectionToRenderOn) {
        svg = d3SelectionToRenderOn || svg;
        if (!svg) {
            return;
        }

        var entries = svg.selectAll(".legend-entry").data(entryData, function(e) { return e.id });
        renderExit(entries.exit());
        renderEnter(entries.enter());
        renderUpdate(entries);
    };

    var renderEnter = function (entrySelection) {
        var entryGroup = entrySelection.append("g")
            .attr("class", "legend-entry");
        entryGroup.append('rect')
            .attr('width', colorPreviewSize)
            .attr('height', colorPreviewSize);
        entryGroup.append("text")
            .attr('x', colorPreviewSize * 2)
            .attr('y', colorPreviewSize);
    };

    var renderUpdate = function(updateSelection) {
        var maxEntryGroupSize = d3.max(getLabelWidths(svg, entryData)) + 2 * colorPreviewSize + entryMargin;
        var maxEntriesInRow = Math.floor(width / maxEntryGroupSize);
        updateSelection.select("rect")
            .style('fill', function (d) {
                return d.color;
            });
        updateSelection.select("text")
            .text(function (d) {
                return d.label;
            });
        updateSelection.each(function(d, i) {
            var updateGroup = d3.select(this);
            var x = maxEntryGroupSize * (i % maxEntriesInRow);
            var y = Math.floor(i / maxEntriesInRow) * entryMargin;
            updateGroup.attr("transform", "translate(" + x + "," + y + ")");
        });
    };

    var renderExit = function (exitSelection) {
        exitSelection.remove();
    };

    var getLabelWidths = function (entryData) {
        var widths = [];
        svg.selectAll('.invisible-text-to-measure')
            .data(entryData)
            .enter()
            .append("text")
            .attr("opacity", 0)
            .text(function(d) { return d.label })
            .each(function() {
                widths.push(this.getComputedTextLength());
                this.remove()
            });
        return widths;
    };

    return {
        render: render,
        setData: setData
    };

});
