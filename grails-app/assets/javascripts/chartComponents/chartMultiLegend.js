//= require /bower_components/d3/d3.min.js
//= require common.js
//= require utility.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartComponents = OpenSpeedMonitor.ChartComponents || {};

OpenSpeedMonitor.ChartComponents.ChartMultiLegend = (function () {
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

        var entries = svg.selectAll(".legend-entry").data(entryData, function(e) { return e['entries'][0].id + e['entries'][1].id });
        renderExit(entries.exit());
        renderEnter(entries.enter());
        renderUpdate(entries);
    };

    var renderEnter = function (entrySelection) {
        var entryGroup = entrySelection.append("g")
            .attr("class", "legend-entry")
            .classed("d3chart-legend-entry", true);

        var gradient = entryGroup.append("defs")
            .append("linearGradient")
            .attr("id", "gradient")
            .attr("name", "gradient")
            .attr("x1", "0%")
            .attr("y1", "0%")
            .attr("x2", "100%")
            .attr("y2", "100%")
            .attr("spreadMethod", "pad");

        gradient.append("stop")
            .attr("name","firstStop")
            .attr("offset", "0%")
            .attr("stop-color", "#fff")
            .attr("stop-opacity", 1);

        gradient.append("stop")
            .attr("id","secondStop")
            .attr("offset", "100%")
            .attr("stop-color", "#fff")
            .attr("stop-opacity", 1);

        entryGroup.append('rect')
            .attr('width', colorPreviewSize)
            .attr('height', colorPreviewSize)
            .attr('name', 'colorPreview');

        entryGroup.append("text")
            .attr('x', colorPreviewSize + colorPreviewMargin)
            .attr('y', colorPreviewSize)
            .attr('name', 'common');

        entryGroup.append("text")
            .attr('x', colorPreviewSize + colorPreviewMargin)
            .attr('y', colorPreviewSize)
            .attr('name', 'firstText')
            .on("mouseover", function (d) {
                mouseOverEntry(d['entries'][0])
            })
            .on("mouseout", function (d) {
                mouseOutEntry(d['entries'][0])
            })
            .on("click", function (d) {
                clickEntry(d['entries'][0])
            });

        entryGroup.append("text")
            .attr('font-family', 'FontAwesome')
            .attr('x', colorPreviewSize + colorPreviewMargin)
            .attr('y', colorPreviewSize)
            .attr('name', 'compareSymbol')
            .text(function() { return '\uf07e' });

        entryGroup.append("text")
            .attr('x', colorPreviewSize + colorPreviewMargin)
            .attr('y', colorPreviewSize)
            .attr('name', 'secondText')
            .on("mouseover", function (d) {
                mouseOverEntry(d['entries'][1])
            })
            .on("mouseout", function (d) {
                mouseOutEntry(d['entries'][1])
            })
            .on("click", function (d) {
                clickEntry(d['entries'][1])
            });
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
            .duration(transitionDuration);
        updateSelection.select("[name=colorPreview]")
            .style('fill', function (d,i) {
                var parent = d3.select(d3.select(this).node().parentNode);
                parent.selectAll("[name=firstStop]")
                    .attr("stop-color", d['entries'][0].color);
                parent.selectAll("[name=secondStop]")
                    .attr("stop-color", d['entries'][1].color);
                parent.selectAll("[name=gradient]")
                    .attr("id","gradient"+i);
                return "url(#gradient"+i+")";
            })
            .transition()
            .duration(transitionDuration)
            .style('opacity', function (d) {
                return d3.max([opacityFunction(anyIsSelected, anyIsHighlighted,d['entries'][0]), opacityFunction(anyIsSelected,anyIsHighlighted,d['entries'][1])])

            });
        updateSelection.select("[name=common]")
            .text(function (d) {
                return d.common ;
            })
            .transition()
            .duration(transitionDuration)
            .style('opacity', function (d) {
                return d3.max([opacityFunction(anyIsSelected, anyIsHighlighted,d['entries'][0]), opacityFunction(anyIsSelected,anyIsHighlighted,d['entries'][1])]);
            });
        updateSelection.select("[name=firstText]")
            .text(function (d) {
                return d['entries'][0].label ;
            })
            .attr('x', function(d){
                var commonWidth = d3.select(d3.select(this).node().parentNode).select("[name=common]").node().getBBox().width;
                var commonWidthSpace = d.common !== ""? colorPreviewMargin:0;
                return commonWidth + colorPreviewSize + colorPreviewMargin + commonWidthSpace
            })
            .transition()
            .duration(transitionDuration)
            .style('opacity', function (d) {
                return opacityFunction(anyIsSelected,anyIsHighlighted,d['entries'][0]);
            });

        var symbolWidth = updateSelection.select("[name=compareSymbol]").node().getBBox().width;

        updateSelection.select("[name=compareSymbol")
            .attr('x', function(){
                var firstText = d3.select(d3.select(this).node().parentNode).select("[name=firstText]");
                var firstWidth = firstText.node().getBBox().width;
                var firstTextPosition = parseFloat(firstText.attr('x'));
                return firstTextPosition + firstWidth + colorPreviewMargin
            })
            .transition()
            .duration(transitionDuration)
            .style('opacity', function (d) {
                return d3.min([opacityFunction(anyIsSelected, anyIsHighlighted,d['entries'][0]), opacityFunction(anyIsSelected,anyIsHighlighted,d['entries'][1])])
            });

        updateSelection.select("[name=secondText")
            .text(function (d) {
                return d['entries'][1].label ;
            })
            .attr('x', function(){
                var symbolX = d3.select(d3.select(this).node().parentNode).select("[name=compareSymbol]").attr('x');
                return parseFloat(symbolX) + symbolWidth + colorPreviewMargin
            })
            .transition()
            .duration(transitionDuration)
            .style('opacity', function (d) {
                return opacityFunction(anyIsSelected,anyIsHighlighted,d['entries'][1]);
            });
    };

    var opacityFunction = function (anyIsSelected, anyIsHighlighted, d) {
        return (anyIsSelected && !d.selected) || (anyIsHighlighted && !d.highlighted) ? 0.2 : 1;
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
            return d['common']+d['entries'][0].label+ "<>" + d['entries'][1].label;
        });
        var labelWidths = OpenSpeedMonitor.ChartComponents.utility.getTextWidths(svgForEstimation, labels);
        return d3.max(labelWidths) + colorPreviewSize + entryMargin + colorPreviewMargin;
    };

    var mouseOverEntry = function (data) {
        if (isAnyEntrySelected()) {
            return;
        }
        entryData.forEach(function (entry) {
            entry['entries'][0].highlighted = entry['entries'][0].id === data.id;
            entry['entries'][1].highlighted = entry['entries'][1].id === data.id;
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
            var firstEntry = entry['entries'][0];
            var secondEntry = entry['entries'][1];
            if (firstEntry.id === data.id) {
                firstEntry.highlighted = false;
            }
            if (secondEntry.id === data.id) {
                secondEntry.highlighted = false;
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
            var firstEntry = entry['entries'][0];
            var secondEntry = entry['entries'][1];
            firstEntry.selected = (firstEntry.id === data.id) ? !firstEntry.selected : false;
            secondEntry.selected = (secondEntry.id === data.id) ? !secondEntry.selected : false;
            firstEntry.highlighted = false;
            secondEntry.highlighted = false;
            if (firstEntry.id === data.id) {
                idIsSelected = firstEntry.selected;
            }
            if (secondEntry.id === data.id) {
                idIsSelected = secondEntry.selected;
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
            return !!entry['entries'][0].selected || !!entry['entries'][1].selected;
        });
    };

    var isAnyEntryHighlighted = function () {
        return entryData.some(function(entry) {
            return !!entry['entries'][0].highlighted || !!entry['entries'][1].highlighted;
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
