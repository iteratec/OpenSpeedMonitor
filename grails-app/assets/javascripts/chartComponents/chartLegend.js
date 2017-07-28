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
    var transitionDuration = 200;
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
            .on("mouseover", mouseOverEntry)
            .on("mouseout", mouseOutEntry)
            .on("click", clickEntry);
        entryGroup.append('rect')
            .attr('width', colorPreviewSize)
            .attr('height', colorPreviewSize);
        entryGroup.append("text")
            .attr('x', colorPreviewSize * 2)
            .attr('y', colorPreviewSize);
    };

    var renderUpdate = function(updateSelection) {
        var maxEntryGroupSize = d3.max(getLabelWidths(entryData)) + 2 * colorPreviewSize + entryMargin;
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
                .attr("opacity", opacity);
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
                this.remove();
            });
        return widths;
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

    return {
        render: render,
        setData: setData,
        on: registerEventHandler
    };

});
