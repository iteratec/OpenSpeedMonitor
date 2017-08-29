//= require /bower_components/d3/d3.min.js
//= require common.js
//= require utility.js
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
    var transitionDuration = OpenSpeedMonitor.ChartComponents.common.transitionDuration;
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
        updateSelection
            .attr("transform", function(d, i) {
                var x = maxEntryGroupSize * (i % maxEntriesInRow);
                var y = Math.floor(i / maxEntriesInRow) * entryMargin;
                return "translate(" + x + "," + y + ")";
            })
            .transition()
            .duration(transitionDuration)
            .style("opacity", function(d) {
                return (anyIsSelected && !d.selected) || (anyIsHighlighted && !d.highlighted) ? 0.2 : 1;
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

    var mouseOverEntry = function (data) {
        if (isAnyEntrySelected()) {
            return;
        }
        entryData.forEach(function (entry) {
            entry.highlighted = entry.id === data.id;
        });
        render();
        callEventHandler("highlight", {
            id: data.id,
            highlighted: true,
            anyHighlighted: true
        });
    };

    var mouseOutEntry = function (data) {
        if (isAnyEntrySelected()) {
            return;
        }
        entryData.forEach(function (entry) {
            if (entry.id === data.id) {
                entry.highlighted = false;
            }
        });
        render();
        callEventHandler("highlight", {
            id: data.id,
            highlighted: false,
            anyHighlighted: false
        });
    };

    var clickEntry = function (data) {
        var idIsSelected = false;
        entryData.forEach(function (entry) {
            entry.selected = (entry.id === data.id) ? !entry.selected : false;
            entry.highlighted = false;
            if (entry.id === data.id) {
                idIsSelected = entry.selected;
            }
        });
        render();
        callEventHandler("select", {
            id: data.id,
            selected: idIsSelected,
            anySelected: isAnyEntrySelected()
        });
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
        estimateHeight: estimateHeight,
        clickEntry: clickEntry,
        mouseOverEntry: mouseOverEntry,
        mouseOutEntry: mouseOutEntry
    };

});
