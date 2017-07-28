//= require /bower_components/d3/d3.min.js
//= require /chartComponents/utility.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartComponents = OpenSpeedMonitor.ChartComponents || {};

OpenSpeedMonitor.ChartComponents.ChartLegend = (function () {
    var svg = null;
    var entryData = [];
    var width = 300;
    var colorPreviewSize = 10;
    var colorPreviewMargin = 5;
    var entryMargin = 20;
    var transitionDuration = 500;
    var eventHandlers = {};

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
            .attr("class", "legend-entry")
            .classed("d3chart-legend-entry", true)
            .style("opacity", 0)
            .on("mouseover", mouseOverEntry)
            .on("mouseout", mouseOutEntry)
            .on("click", clickEntry);
        entryGroup.append('rect')
            .attr('width', colorPreviewSize)
            .attr('height', colorPreviewSize)
            .attr("rx", 2)
            .attr("ry", 2);
        entryGroup.append("text")
            .attr('x', colorPreviewSize + colorPreviewMargin)
            .attr('y', colorPreviewSize);
    };

    var renderUpdate = function(updateSelection) {
        var maxEntryGroupSize = calculateMaxEntryGroupWidth(svg);
        var maxEntriesInRow = Math.floor(width / maxEntryGroupSize);
        var anyIsSelected = isAnyEntrySelected();
        var anyIsHighlighted = isAnyEntryHighlighted();
        updateSelection.each(function(d, i) {
            var x = maxEntryGroupSize * (i % maxEntriesInRow);
            var y = Math.floor(i / maxEntriesInRow) * entryMargin;
            var opacity = (anyIsSelected && !d.selected) || (anyIsHighlighted && !d.highlighted) ? 0.2 : 1;
            d3.select(this)
                .attr("transform", "translate(" + x + "," + y + ")")
                .transition()
                .duration(transitionDuration)
                .style("opacity", opacity);
        });
        updateSelection.select("rect")
            .style('fill', function (d) {
                return d.color;
            });
        updateSelection.select("text")
            .text(function (d) {
                return d.label;
            });
    };

    var renderExit = function (exitSelection) {
        exitSelection
            .transition()
            .duration(transitionDuration)
            .style("opacity", 0)
            .remove();
    };

    var calculateMaxEntryGroupWidth = function (svgForEstimation) {
        var labels = entryData.map(function (d) {
            return d.label;
        });
        var labelWidths = OpenSpeedMonitor.ChartComponents.utility.getTextWidths(svgForEstimation, labels);
        return d3.max(labelWidths) + colorPreviewSize + entryMargin + colorPreviewMargin;
    };

    var mouseOverEntry = function (legendEntry) {
        if (isAnyEntrySelected()) {
            return;
        }
        entryData.forEach(function (entry) {
            entry.highlighted = entry.id === legendEntry.id;
        });
        render();
        callEventHandler("highlight", legendEntry);
    };

    var mouseOutEntry = function (legendEntry) {
        if (isAnyEntrySelected()) {
            return;
        }
        legendEntry.highlighted = false;
        render();
        callEventHandler("highlight", legendEntry);
    };

    var clickEntry = function (legendEntry) {
        entryData.forEach(function (entry) {
            entry.selected = (entry.id === legendEntry.id) ? !entry.selected : false;
            entry.highlighted = false;
        });
        render();
        callEventHandler("select", legendEntry);
    };

    var isAnyEntrySelected = function () {
        return entryData.some(function(entry) {
            return !!entry.selected;
        });
    };

    var isAnyEntryHighlighted = function () {
        return entryData.some(function(entry) {
            return !!entry.highlighted;
        });
    };

    var callEventHandler = function (eventType, legendEntry) {
        if (eventHandlers[eventType]) {
            eventHandlers[eventType](legendEntry);
        }
    };

    var registerEventHandler = function (eventType, eventHandler) {
        eventHandlers[eventType] = eventHandler;
    };

    var estimateHeight = function (svgForEstimation) {
        svgForEstimation = svg || svgForEstimation;
        var maxEntryGroupSize = calculateMaxEntryGroupWidth(svgForEstimation);
        var maxEntriesInRow = Math.floor(width / maxEntryGroupSize);
        return Math.floor(entryData.length / maxEntriesInRow) * entryMargin;
    };

    return {
        render: render,
        setData: setData,
        on: registerEventHandler,
        estimateHeight: estimateHeight
    };

});
