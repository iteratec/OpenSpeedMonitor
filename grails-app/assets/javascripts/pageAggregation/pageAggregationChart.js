//= require /bower_components/d3/d3.min.js
//= require /d3/chartLabelUtil
//= require /d3/trafficLightDataProvider
//= require /d3/chartColorProvider
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageAggregation = (function (chartIdentifier) {

    var svg,
        svgDefinitions,
        margin,
        barHeight = 40,
        barPadding = 10,
        svgContainer = $("#" + chartIdentifier),
        width,
        height,
        labelWidths = [],
        barXOffSet,
        allBarsGroup,
        trafficLightBars,
        transitionDuration = 250,
        filterRules = {},
        actualBarchartData,
        commonLabelParts,
        headerLine,
        transformedData,
        groupings,
        valueLabelOffset = 5,
        unitScales,
        units,
        legend,
        inFrontSwitchButton = $("#inFrontButton"),
        inFront,
        absoluteMaxYOffset = 0,
        absoluteMaxValue = 0,
        selectedSeries = "",
        colorScales = {},
        unitFactors = {
            ms: 1,
            s: 1000
        },
        measurandOrder = {
            ms: {
                order: 1,
                measurands: {
                    fullyLoadedTimeInMillisecs: 6,
                    docCompleteTimeInMillisecs: 3,
                    loadTimeInMillisecs: 4,
                    startRenderInMillisecs: 2,
                    firstByteInMillisecs: 1,
                    visuallyCompleteInMillisecs: 5,
                    domTimeInMillisecs: 0
                }
            },
            s: {
                order: 1,
                measurands: {
                    fullyLoadedTimeInMillisecs: 6,
                    docCompleteTimeInMillisecs: 3,
                    loadTimeInMillisecs: 4,
                    startRenderInMillisecs: 2,
                    firstByteInMillisecs: 1,
                    visuallyCompleteInMillisecs: 5,
                    domTimeInMillisecs: 0
                }
            },
            "#": {
                order: 2,
                measurands: {
                    fullyLoadedRequestCount: 2,
                    docCompleteRequests: 1
                }
            },
            KB: {
                order: 3,
                measurands: {
                    fullyLoadedIncomingBytes: 2,
                    docCompleteIncomingBytes: 1
                }
            },
            MB: {
                order: 3,
                measurands: {
                    fullyLoadedIncomingBytes: 2,
                    docCompleteIncomingBytes: 1
                }
            },
            "%": {
                order: 4,
                measurands: {
                    csByWptDocCompleteInPercent: 1,
                    csByWptVisuallyCompleteInPercent: 2
                }
            },
            "": {
                order: 5,
                measurands: {
                    speedIndex: 1
                }
            }
        },
        highestMeasurand,
        descending = true,
        customerJourneyFilter;

    var drawChart = function (barchartData) {

        if (svg === undefined) {
            initChart();
        }

        initChartData(barchartData);

        initFilterDropdown();
        drawAllBars();
    };

    var countBars = function () {
        if (inFront) {
            return transformedData.length
        } else {
            return transformedData.reduce(function (count, d) {
                return count + d.bars.length;
            }, 0);
        }
    };

    /**
     * Set global margins and width, height initially.
     * Initialize svg and its main <g> elements.
     * Adds event handlers for besides/infront switcher.
     */
    var initChart = function () {

        $("#chart-card").removeClass("hidden");

        margin = {top: 50, right: 20, bottom: 20, left: 20};

        svg = d3.select("#" + chartIdentifier).append("svg")
            .attr("class", "d3chart");
        svgDefinitions = svg.append("defs");
        createDiagonalHatchPattern();

        allBarsGroup = svg.append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
        headerLine = svg.append("g");
        trafficLightBars = svg.append("g");
        legend = svg.append("g");

        addBesideInFrontSwitchEventHandlers();
        $(window).resize(drawAllBars);

    };

    var initChartData = function (barchartData) {
        barchartData.series.forEach(function (series) {
            if(series.data.length === 0) return; //we simply ignore empty series
            var labelUtil = OpenSpeedMonitor.ChartModules.ChartLabelUtil(series.data, barchartData.i18nMap);
            series.data = labelUtil.getSeriesWithShortestUniqueLabels(true);
            commonLabelParts = labelUtil.getCommonLabelParts(true);
            series.data.sort(function (x, y) {
                return d3.descending(x.value, y.value);
            });
        });

        barchartData.originalSeries = clone(barchartData.series);

        actualBarchartData = barchartData;
    };

    var setHighestMeasurand = function () {
        var measurand = {
            unit: transformedData[0].bars[0].unit,
            measurand: transformedData[0].bars[0].measurand,
            originalMeasurandName: transformedData[0].bars[0].originalMeasurandName
        };
        $.each(transformedData, function (_, data) {
            $.each(data.bars, (function (_, a) {
                var aMeasurandGroup = measurandOrder[a.unit];
                var bMeasurandGroup = measurandOrder[measurand.unit];
                if (aMeasurandGroup["order"] >= bMeasurandGroup["order"]) {
                    if (aMeasurandGroup.measurands[a.originalMeasurandName] > bMeasurandGroup.measurands[measurand.originalMeasurandName]) {
                        measurand.unit = a.unit;
                        measurand.measurand = a.measurand;
                        measurand.originalMeasurandName = a.originalMeasurandName
                    }
                }
            }));
        });

        highestMeasurand = measurand.measurand;
    };

    var sortGroups = function () {
        if (customerJourneyFilter) {
            sortGroupsByJourney();
        } else {
            sortGroupsByHighestMeasurand();
        }
    };

    var sortGroupsByJourney = function () {
        var filter = actualBarchartData.filterRules[customerJourneyFilter];
        transformedData.sort(function (a, b) {
            var aIndex = filter.indexOf(a.grouping);
            var bIndex = filter.indexOf(b.grouping);
            return aIndex - bIndex;
        })

    };

    var sortGroupsByHighestMeasurand = function () {
        var compare = sortFunction();
        transformedData.sort(function (a, b) {
            var aValue = $.grep(a.bars, function (e) {
                return e.measurand == highestMeasurand;
            })[0].value;
            var bValue = $.grep(b.bars, function (e) {
                return e.measurand == highestMeasurand;
            })[0].value;
            return compare(aValue, bValue)
        });
    };

    var createDiagonalHatchPattern = function () {
        svgDefinitions.append("pattern")
            .attr({
                "id": "diagonalHatch",
                "width": "5",
                "height": "5",
                "patternTransform": "rotate(45 0 0)",
                "patternUnits": "userSpaceOnUse"
            })
            .append("line")
            .attr({
                "x1": "0",
                "y1": "0",
                "x2": "0",
                "y2": "5",
                "style": "stroke:#558BBF; stroke-width:4"
            });
    };

    var createColorScale = function () {
        var colorProvider = OpenSpeedMonitor.ChartColorProvider();
        var units = {};
        $.each(transformedData, function (_, d) {
            $.each(d.bars, function (_, bar) {
                var unitMeasurands = units[bar.unit];
                if (!unitMeasurands) {
                    unitMeasurands = [];
                    units[bar.unit] = unitMeasurands;
                }
                if ($.inArray(bar.measurand, unitMeasurands) == -1) unitMeasurands.push(bar.measurand);
            })
        });
        $.each(units, function (key, value) {
            var currentMeasurandGroupOrder = measurandOrder[key];
            value.sort(function (a, b) {
                return currentMeasurandGroupOrder[a] < currentMeasurandGroupOrder[b];
            });
        });
        //we iterate over all measurands and get the color, so the provider will register our order of colors for later use
        $.each(units, function (key, value) {
            var currentColorScale = colorProvider.getColorscaleForMeasurandGroup(key);
            colorScales[key] = currentColorScale;
            $.each(value, function (_, measurand) {
                currentColorScale(measurand);
            })
        })
    };

    var drawAllBars = function () {
        updateSvgWidth();
        transformData();
        setHighestMeasurand();
        sortGroups();
        createColorScale();
        groupings = transformedData.map(function (d) {
            return d.grouping;
        });

        barXOffSet = measureComponent($("<text>" + getLongestGroupName() + valueLabelOffset + "</text>"), function (d) {
            return d.width();
        });
        updateInFrontSwitch();
        if (inFrontSwitchButton.hasClass("active")) {
            drawBarsInFrontOfEachOther();
        } else {
            drawBarsBesideEachOther();
        }

        var headerData = [{headerText: commonLabelParts}];
        drawHeader(headerData);
        drawTrafficLight();
        drawLegend();
        updateSvgHeight();
        appendMouseEvents();
    };

    var drawLegend = function () {
        var measurands = getAllMeasurandsWithUnit();
        var replaceRegex = /\(.*\)/g;
        var legendRectSize = 10;
        var legendSpacing = 5;
        var longestString = "";
        //we doesn't want to show additional information within parenthesis, but we need the original name for later action
        //between the legend and bars. So we find the longest shortened name, but won't touch the original data
        $.each(measurands, function (_, d) {
            var simplified = d.measurand.replace(replaceRegex, "");
            if (simplified.length > longestString.length) {
                longestString = simplified;
            }
        });
        var maxWidth = measureComponent($("<text>" + longestString + "</text>"), function (d) {
            return d.width();
        });
        var legendMargin = 20;
        var leftPadding = margin.left + barXOffSet;
        var legendSpace = parseInt(svg.style("width")) - leftPadding - margin.right;
        var maxEntriesInRow = Math.floor(legendSpace / (maxWidth + legendRectSize + legendSpacing + legendMargin));

        var yPosition = calculateLegendYPosition();

        var comparativeLegendEntries = createLegendEntriesForComparativeTimeframe();
        if (comparativeTimeframeIsEnabled())
            measurands = measurands.concat(comparativeLegendEntries);

        var entries = legend.selectAll("g").data(measurands);
        entries.enter()
            .append("g")
            .attr("transform", "translate(" + leftPadding + "," + yPosition + ")")
            .classed("d3chart-legend-entry", true)
            .each(function () {
                var line = d3.select(this);
                line.append('rect')
                    .attr('width', legendRectSize)
                    .attr('height', legendRectSize);
                line.append("text")
                    .attr('x', legendRectSize + legendSpacing)
                    .attr('y', legendRectSize);
            });
        entries.exit().remove();

        //Update
        legend.selectAll("g").each(function (d, i) {
            var line = d3.select(this);
            line.select("rect").style('fill', function (d) {
                return d.fill || colorScales[d.unit](d.measurand);
            });
            line.select("text").text(function (d) {
                return d.measurand.replace(replaceRegex, "")
            });
            var x = (maxWidth + legendRectSize + legendSpacing + legendMargin) * (i % maxEntriesInRow);
            var y = Math.floor(i / maxEntriesInRow) * legendMargin;
            line.attr("transform", "translate(" + x + "," + y + ")");
        });

        legend.transition().duration(transitionDuration).attr("transform", "translate(" + leftPadding + "," + yPosition + ")")
    };

    var createLegendEntriesForComparativeTimeframe = function () {
        var good = OpenSpeedMonitor.ChartColorProvider().getColorscaleForTrafficlight()("good");
        var bad = OpenSpeedMonitor.ChartColorProvider().getColorscaleForTrafficlight()("bad");

        var legendEntries = [
            {
                'measurand': actualBarchartData.i18nMap.comparativeImprovement,
                'fill': good
            },{
                'measurand': actualBarchartData.i18nMap.comparativeDeterioration,
                'fill': bad
            }
        ];

        return legendEntries;
    };

    var comparativeTimeframeIsEnabled =  function () {
        var anyPageHasComparativeValue = actualBarchartData.series[0].data.reduce( function (result, d) {
            var hasValueComparative = d.valueComparative !== null;
            return result || hasValueComparative;
        }, false);

        return anyPageHasComparativeValue;
    };

    var calculateLegendYPosition = function () {
        var y = absoluteMaxYOffset;
        if (shouldDrawTrafficLight()) {
            y += trafficLightBars.node().getBBox().height + barPadding
        }
        return y
    };

    var getLongestGroupName = function () {
        var longestString = "";
        $.each(transformedData, function (_, d) {
            if (d.label.length > longestString.length) {
                longestString = d.label;
            }
        });
        return longestString;
    };

    var measureComponent = function (component, fn) {
        var el = component.clone();
        el.css("visibility", 'hidden');
        el.css("position", 'absolut');
        el.appendTo('body');
        var result = fn(el);
        el.remove();
        return result
    };

    var updateInFrontSwitch = function () {
        var needToBeBeside = !canBeInFront();
        inFrontSwitchButton.toggleClass("disabled", needToBeBeside);
        if (needToBeBeside) {
            inFrontSwitchButton.removeClass("active");
        }
    };

    var transformData = function () {
        units = {};
        var dataMap = {};
        $.each(actualBarchartData.series, function (i, series) {
            if ((series.data.length !== 0) && units[series.dimensionalUnit] === undefined) {
                units[series.dimensionalUnit] = [];
            }
            $.each(series.data, function (_, datum) {
                var currentDatum = dataMap[datum.grouping];
                if (currentDatum === undefined) {
                    currentDatum = {};
                    currentDatum["bars"] = [];
                    currentDatum["grouping"] = datum.grouping;
                    currentDatum["label"] = datum.label;
                    dataMap[datum.grouping] = currentDatum;
                }
                var bar = {};
                bar["measurand"] = datum.measurand;
                bar["originalMeasurandName"] = datum.originalMeasurandName.replace("Uncached", "");
                bar["value"] = datum.value;
                bar["comparativeDifference"] = datum.valueComparative !== null ? datum.value - datum.valueComparative : 0;
                bar["unit"] = series.dimensionalUnit;
                bar["grouping"] = datum.grouping;
                currentDatum["bars"].push(bar);
                units[series.dimensionalUnit].push(datum.value);
            })
        });

        $.each(units, function (k, v) {
            v.sort(function (a, b) {
                return a > b ? 1 : -1
            });
        });
        transformedData = Object.keys(dataMap).map(function (key) {
            return dataMap[key];
        });
    };

    var canBeInFront = function () {
        return Object.keys(units).length == 1
    };

    var getAllMeasurands = function () {
        var measurands = [];
        $.each(transformedData, function (_, d) {
            $.each(d.bars, function (_, bar) {
                if ($.inArray(bar.measurand, measurands) == -1) measurands.push(bar.measurand);
            })
        });
        return measurands;
    };

    var getAllMeasurandsWithUnit = function () {
        var visitedMeasurands = [];
        var measurands = [];
        $.each(transformedData, function (_, d) {
            $.each(d.bars, function (_, bar) {
                var measurandObject = {measurand: bar.measurand, unit: bar.unit};
                if ($.inArray(bar.measurand, visitedMeasurands) == -1) {
                    measurands.push(measurandObject);
                    visitedMeasurands.push(bar.measurand)
                }
            })
        });
        return measurands;
    };

    var addBesideInFrontSwitchEventHandlers = function () {
        var besideButton = $("#besideButton");
        var inFrontButton = $("#inFrontButton");
        besideButton.click(function () {
            drawBarsBesideEachOther();
            drawTrafficLight();
            drawLegend();
        });
        inFrontButton.click(function () {
            if (canBeInFront()) {
                drawBarsInFrontOfEachOther();
                drawTrafficLight();
                drawLegend();
            }
        });
    };

    var sortBarsInGroupByValue = function () {
        $.each(transformedData, function (_, data) {
            data.bars.sort(function (a, b) {
                return a.value < b.value
            });
        });
    };

    var sortBarsInGroupByMeasurandOrder = function () {
        var compareFunction = sortFunction();
        if (descending) {
            compareFunction = function (a, b) {
                return a < b;
            }
        } else {
            compareFunction = function (a, b) {
                return a > b;
            }
        }
        $.each(transformedData, function (_, data) {
            data.bars.sort(function (a, b) {
                var aMeasurandGroup = measurandOrder[a.unit];
                var bMeasurandGroup = measurandOrder[b.unit];
                if (aMeasurandGroup["order"] == bMeasurandGroup["order"]) {
                    return compareFunction(aMeasurandGroup.measurands[a.originalMeasurandName], bMeasurandGroup.measurands[b.originalMeasurandName])
                } else {
                    return aMeasurandGroup["order"] > bMeasurandGroup["order"]
                }
            });
        });
    };

    var sortFunction = function () {
        if (descending) {
            return function (a, b) {
                return b - a;
            }
        } else {
            return function (a, b) {
                return a - b;
            }
        }
    };

    var drawBarsInFrontOfEachOther = function () {
        inFront = true;
        sortBarsInGroupByValue();
        updateSvgHeight();

        var outerYScale = d3.scale.ordinal()
            .domain(groupings)
            .rangeRoundBands([0, height], .05);

        unitScales = createUnitScales();
        enterBarGroups();

        allBarsGroup.selectAll(".barGroup").transition().duration(transitionDuration)
            .attr("transform", function (d) {
                return "translate(0," + outerYScale(d.grouping) + ")"
            });

        var innerYScale = d3.scale.ordinal()
            .domain([0])
            .rangeRoundBands([0, outerYScale.rangeBand()]);

        //Update Bar Container
        var bars = d3.selectAll(".bar");
        bars.transition().duration(transitionDuration)
            .attrTween("transform", function (d, i, a) {
                return d3.interpolateString(a, "translate(" + barXOffSet + ",0)");
            });

        //Update actual bars
        d3.selectAll(".bar").each(function (bar) {

            //Update Rectangle Position and Size
            var barWidth = unitScales[bar.unit](bar.value);
            d3.select(this).select("rect").attr("fill", colorScales[bar.unit](bar.measurand)).transition()
                .attr("width", barWidth)
                .attr("height", innerYScale.rangeBand());

            //Update Bar Label
            updateBarLabel(bar, this, barWidth, innerYScale);
        });

        //Update Group Labels
        var groupLabelY = innerYScale.rangeBand() / 2;
        d3.selectAll(".barGroup").each(function (d) {
            d3.select(this).select(".barGroupLabel").text(d.label).transition().duration(transitionDuration).attr("y", groupLabelY);
        })


    };

    var drawBarsBesideEachOther = function () {
        inFront = false;
        sortBarsInGroupByMeasurandOrder();
        var measurands = getAllMeasurands();
        updateSvgHeight();

        var outerYScale = d3.scale.ordinal()
            .rangeRoundBands([0, height], .05)
            .domain(groupings);

        var innerYScale = d3.scale.ordinal()
            .domain(measurands)
            .rangeRoundBands([0, outerYScale.rangeBand()]);

        var actualBarHeight = innerYScale.rangeBand();

        unitScales = createUnitScales();

        enterBarGroups();

        allBarsGroup.selectAll(".barGroup").transition().duration(transitionDuration)
            .attr("transform", function (d) {
                return "translate(0," + outerYScale(d.grouping) + ")";
            });

        //Update Bar Container
        var bars = d3.selectAll(".bar");
        bars.transition()
            .attrTween("transform", function (d, i) {
                var currentY = d3.select(this).attr("transform");
                if (!currentY) {
                    currentY = "translate(" + barXOffSet + ",0)"
                }
                return d3.interpolateString(currentY, "translate(" + barXOffSet + "," + innerYScale(d.measurand) + ")");
            });

        //Update actual bars
        d3.selectAll(".bar").each(function (bar) {
            var value = unitScales[bar.unit](bar.value);

            //Update Rectangle Position and Size
            var barX = unitScales[bar.unit](0);
            var barWidth = value - barX;
            var rect = d3.select(this).select("rect");
            var barFill = comparativeTimeframeIsEnabled() ? colorScales[bar.unit](2) : colorScales[bar.unit](bar.measurand);
            rect.attr("height", actualBarHeight)
                .attr("x", barX)
                .attr("fill", barFill)
                .transition().duration(transitionDuration)
                .attr("width", barWidth);

            updateBarLabel(bar, this, barWidth, innerYScale);

            var comparativeBarEnd = unitScales[bar.unit](bar.comparativeDifference);
            var x = d3.min([comparativeBarEnd, barX]);
            var comparativeBarWidth = d3.max([comparativeBarEnd, barX]) - x;

            var trafficLightColorscale = OpenSpeedMonitor.ChartColorProvider().getColorscaleForTrafficlight();
            var comparativeBarFill = "";
            if (bar.unit == "%") {
                comparativeBarFill = trafficLightColorscale(bar.comparativeDifference < 0 ? "bad" : "good");
            } else {
                comparativeBarFill = trafficLightColorscale(bar.comparativeDifference < 0 ? "good" : "bad");
            }

            d3.select(this).select(".d3chart-comparative-indicator")
                .attr("x", x)
                .attr("height", actualBarHeight)
                .attr("width", comparativeBarWidth)
                .transition().duration(transitionDuration)
                .attr("fill", comparativeBarFill);

            updateComparativeBarLabel(bar, this, comparativeBarWidth, innerYScale, comparativeBarEnd);
        });

        //Update Group Labels
        d3.selectAll(".barGroup").each(function (d) {
            var groupLabelY = innerYScale.rangeBand() * (d.bars.length) / 2;
            d3.select(this).select(".barGroupLabel").text(d.label).transition().duration(transitionDuration).attr("y", groupLabelY);
        })
    };

    var formatValue = function (value) {
        var precision = 2;
        return parseFloat(value).toFixed(precision);
    };

    var updateBarLabel = function (bar, barElement, barWidth, innerYScale) {
        var textY = innerYScale.rangeBand() / 2;
        var textLabel = d3.select(barElement).select(".d3chart-value");
        var text = "" + formatValue(bar.value) + " " + bar.unit;
        var textTrans = textLabel.transition().duration(transitionDuration)
            .text(text)
            .attr("y", textY);
        if (!checkIfTextWouldFitRect(text, textLabel, barWidth)) {
            textTrans
                .attr("visibility", "hidden");
        } else {
            textTrans
                .attr("x", unitScales[bar.unit](bar.value) - valueLabelOffset)
                .attr("visibility", "");
        }
    };

    var updateComparativeBarLabel = function(bar, barElement, barWidth, innerYScale, position) {
        var textY = innerYScale.rangeBand() / 2;
        var textLabel = d3.select(barElement).select(".d3chart-comparative-value");
        var sign = bar.comparativeDifference > 0 ? "+" : "";
        var text = sign + formatValue(bar.comparativeDifference) + " " + bar.unit;

        var visibility = checkIfTextWouldFitRect(text, textLabel, barWidth) ? "" : "hidden";
        var labelOffset = bar.comparativeDifference > 0 ? -valueLabelOffset : valueLabelOffset;
        textLabel
            .classed("d3chart-text-start", bar.comparativeDifference < 0)
            .transition().duration(transitionDuration)
            .text(text)
            .attr("visibility", visibility)
            .attr("x", position + labelOffset)
            .attr("y", textY);
    };

    var updateSvgHeight = function () {
        height = countBars() * barHeight;
        var newHeight = height + margin.top + margin.bottom + barHeight + barPadding + legend.node().getBBox().height;
        if (shouldDrawTrafficLight()) newHeight += barHeight + barPadding;
        svg.attr("height", newHeight);
        absoluteMaxYOffset = height + margin.top + margin.bottom;
    };

    var updateSvgWidth = function () {
        var fullWidth = svgContainer.width();
        svg.attr("width", fullWidth);
        width = fullWidth - margin.left - margin.right;
    };

    var createUnitScales = function () {
        unitScales = {};

        var comparativeDifferences = transformedData.map(function (element) {
            return element.bars[0].comparativeDifference;
        });

        $.each(units, function (unit, values) {
            var maxValueForThisUnit = d3.max(values);
            var minValueForThisUnit = d3.min(comparativeDifferences);
            var scale = d3.scale.linear()
                .rangeRound([0, width - barXOffSet])
                .domain([minValueForThisUnit, maxValueForThisUnit]);
            unitScales[unit] = scale;
            absoluteMaxValue = Math.max(absoluteMaxValue, maxValueForThisUnit);
        });
        return unitScales;
    };

    var enterBarGroups = function () {
        var barGroup = allBarsGroup.selectAll(".barGroup").data(transformedData, function (bar) {
            return bar.grouping;
        });

        barGroup.enter().append("g")
            .attr("class", "barGroup")
            .append("text")
            .attr("class", "barGroupLabel")
            .attr("alignment-baseline", "central");
        barGroup.exit().remove();

        barGroup.each(function (group) {
            var bars = d3.select(this).selectAll(".bar").data(group.bars, function (bar) {
                return bar.grouping + bar.measurand;
            });
            bars.enter().append("g").attr("class", "bar").each(function (d) {
                d3.select(this).append("rect").classed("d3chart-bar-clickable", true);
                d3.select(this).append("rect").classed("d3chart-comparative-indicator", true);
                d3.select(this).append("text").classed("d3chart-value", true);
                d3.select(this).append("text").classed("d3chart-comparative-value", true);
            });
            bars.exit().remove();
        });
    };

    var appendMouseEvents = function () {
        d3.selectAll(".bar")
            .on("mouseover", mouseOver())
            .on("mouseout", mouseOut())
            .on("click", click());

        d3.selectAll(".d3chart-legend-entry")
            .on("mouseover", mouseOver())
            .on("mouseout", mouseOut())
            .on("click", click());
    };

    var mouseOver = function () {
        return function (hoverBar) {
            if (selectedSeries == "") {
                d3.selectAll(".bar").each(function (bar) {
                    if (bar.measurand != hoverBar.measurand) {
                        changeBarOpacity(this, 0.2);
                    } else {
                        highlightBar(this, bar);
                    }
                });
                d3.selectAll(".d3chart-legend-entry").each(function (entry) {
                    var opacity = 1;
                    if (entry.measurand != hoverBar.measurand) opacity = 0.2;
                    changeBarOpacity(this, opacity);
                });
            }
        }
    };

    var mouseOut = function () {
        return function () {
            if (selectedSeries == "") {
                d3.selectAll(".bar").each(function (bar) {
                    var text = d3.select(this).select("text");
                    var rect = d3.select(this).select("rect");
                    var rectSize = parseInt(rect.attr("width"));
                    var textSize = text.node().getBBox().width;
                    if (rectSize < textSize) {
                        text.classed("d3chart-value-out", false).classed("d3chart-value", true);
                        text.transition().duration(transitionDuration).attr("x", 0).attr("visibility", "hidden");
                    } else {
                        text.transition().duration(transitionDuration).style("opacity", 1);
                    }
                    rect.transition().duration(transitionDuration).style("opacity", 1);
                });
                d3.selectAll(".d3chart-legend-entry").each(function (entry) {
                    changeBarOpacity(this, 1);
                });
            }
        };
    };

    var changeBarOpacity = function (bar, opacity) {
        d3.select(bar).select("rect").transition().duration(transitionDuration).style("opacity", opacity);
        d3.select(bar).select("text").transition().duration(transitionDuration).style("opacity", opacity);
    };

    var highlightBar = function (barDom, barData) {
        var rect = d3.select(barDom).select("rect");
        var text = d3.select(barDom).select("text");
        var rectSize = parseInt(rect.attr("width"));
        var textSize = text.node().getBBox().width;

        if (rectSize < textSize) {
            var position = unitScales[barData.unit](barData.value) + valueLabelOffset;
            text.classed("d3chart-value", false).classed("d3chart-value-out", true);
            text.transition().duration(transitionDuration).attr("x", position).attr("visibility", "visible").style("opacity", 1);
        } else {
            text.transition().duration(transitionDuration).style("opacity", 1)
        }
        rect.transition().duration(transitionDuration).style("opacity", 1);
    };

    var click = function () {
        return function (hoverBar) {
            if (selectedSeries == "") {
                selectedSeries = hoverBar.measurand;
                mouseOver()(hoverBar);
            } else if (selectedSeries == hoverBar.measurand) {
                selectedSeries = "";
                mouseOver()(hoverBar);
            } else {
                selectedSeries = "";
                mouseOver()(hoverBar);
                selectedSeries = hoverBar.measurand;
            }
        }
    };

    var checkIfTextWouldFitRect = function (text, textLabel, barWidth) {
        var clone = $("<text>" + text + "</text>");
        clone.text(text);
        clone.addClass("d3chart-value");
        var width = measureComponent(clone, function (d) {
            return d.width()
        });
        return width < (barWidth - valueLabelOffset);
    };


    var drawHeader = function (headerData) {
        var headerText = headerLine.selectAll("text")
            .data(headerData, function (d) {
                return d.headerText;
            });

        // exit

        headerText.exit()
            .transition()
            .duration(transitionDuration)
            .remove();

        // enter

        headerText.enter().append("text")
            .text(function (d) {
                return d.headerText;
            })
            .attr("y", barPadding + barHeight / 2)
            .attr("dy", ".35em") //vertical align middle
            .attr("cx", 0)
            .attr("transform", function (d) {
                return "translate(" + parseInt(barXOffSet + margin.left) + ")";
            });

        //update

        headerText
            .transition()
            .duration(transitionDuration)
            .attr("transform", function (d) {
                return "translate(" + parseInt(barXOffSet + margin.left) + ")";
            });

    };


    var getMaxLabelWidth = function () {
        return d3.max(labelWidths, function (d) {
            return d.width;
        });
    };

    var initFilterDropdown = function () {

        filterRules = actualBarchartData.filterRules;

        var $filterDropdownGroup = $("#filter-dropdown-group");
        var $customerJourneyHeader = $filterDropdownGroup.find("#customer-journey-header");

        // remove old filter
        $filterDropdownGroup.find('.filterRule').remove();

        if ($filterDropdownGroup.hasClass("hidden"))
            $filterDropdownGroup.removeClass("hidden");

        for (var filterRuleKey in filterRules) {
            if (filterRules.hasOwnProperty(filterRuleKey)) {
                var link = $("<li class='filterRule'><a href='#'><i class='fa fa-check filterInactive' aria-hidden='true'></i>" + filterRuleKey + "</a></li>");
                link.click(function (e) {
                    filterCustomerJourney(e.target.innerText);
                    toogleFilterCheckmarks(e.target);
                });
                link.insertAfter($customerJourneyHeader);
            }
        }

        $filterDropdownGroup.find("#all-bars-desc").click(function (e) {
            filterCustomerJourney(null, true);
            toogleFilterCheckmarks(e.target);
        });
        $filterDropdownGroup.find("#all-bars-asc").click(function (e) {
            filterCustomerJourney(null, false);
            toogleFilterCheckmarks(e.target);
        })
    };

    var clone = function (toClone) {
        return JSON.parse(JSON.stringify(toClone));
    };

    var filterCustomerJourney = function (journeyKey, desc) {
        customerJourneyFilter = journeyKey;
        descending = desc;
        actualBarchartData.series = clone(actualBarchartData.originalSeries);
        if (journeyKey && filterRules[journeyKey]) {

            removeElementsNotInCustomerJourney(journeyKey);
            removeEmptySeries();
            descending = true;
        }
        drawAllBars()
    };

    var removeElementsNotInCustomerJourney = function(journeyKey){
        actualBarchartData.series.forEach(function (series) {
            series.data = series.data.filter(function (element) {
                return filterRules[journeyKey].indexOf(element.grouping) >= 0;
            });
        });
    };

    var removeEmptySeries = function(){
        actualBarchartData.series = actualBarchartData.series.filter(function (series) {
            return series.data.length > 0;
        });
    };

    var toogleFilterCheckmarks = function (listItem) {
        $('.filterActive').toggleClass("filterInactive filterActive");

        // reset checkmark to 'descending' if 'Show' gets clicked
        // otherwise set checkmark to the list item one has clicked on
        if (typeof listItem == 'undefined') {
            $('#all-bars-desc > .filterInactive').toggleClass("filterActive filterInactive");
        } else {
            $(listItem).find(".filterInactive").toggleClass("filterActive filterInactive");
        }
    };

    var drawTrafficLight = function () {
        if (shouldDrawTrafficLight()) {
            drawTrafficLightForTimings();
        } else {
            trafficLightBars.selectAll("*").remove();
        }
    };

    var shouldDrawTrafficLight = function () {
        var unitNames = Object.keys(units);
        return unitNames.length == 1 && (unitNames[0] == "ms" || unitNames[0] == "s")
    };

    var drawTrafficLightForTimings = function () {
        var unitName = Object.keys(units);
        var unitFactor = unitFactors[unitName[0]];
        var trafficLightData = OpenSpeedMonitor.ChartModules.TrafficLightDataProvider.getTimeData(absoluteMaxValue*unitFactor);
        var microsecsXScale = unitScales[unitName[0]];

        var trafficLight = trafficLightBars.selectAll("g")
            .data(trafficLightData, function (d) {
                return d.id
            });

        // exit

        trafficLight.exit()
            .transition()
            .duration(transitionDuration)
            .attr("width", 0)
            .remove();

        // enter

        var trafficLightEnter = trafficLight.enter()
            .append("g")
            .attr("cx", 0)
            .attr("transform", function (d, index) {
                var xOffset = barXOffSet + microsecsXScale(d.lowerBoundary)/unitFactor + 2 * barPadding;
                return "translate(" + xOffset + ", " + absoluteMaxYOffset + ")";
            });

        trafficLightEnter.append("rect")
            .attr("height", barHeight)
            .attr("width", OpenSpeedMonitor.ChartModules.TrafficLightDataProvider.initialBarWidth)
            .attr("fill", function (d) {
                return d.fill;
            })
            .attr("fill-opacity", function (d) {
                return d.fillOpacity;
            });
        trafficLightEnter.append("text")
            .attr("class", function (d) {
                return d.cssClass;
            })
            .text(function (d) {
                return d.name;
            })
            .attr("y", barHeight / 2)
            .attr("dy", ".35em") //vertical align middle
            .attr("text-anchor", "middle");

        // update

        var trafficLightTransition = trafficLight
            .transition()
            .duration(transitionDuration);

        trafficLightTransition
            .attr("transform", function (d, index) {
                var xOffset = barXOffSet + microsecsXScale(d.lowerBoundary)/unitFactor + 2 * barPadding;
                return "translate(" + xOffset + ", " + absoluteMaxYOffset + ")";
            })
            .select('rect')
            .attr("width", function (d) {
                return (microsecsXScale(d.upperBoundary - d.lowerBoundary) - microsecsXScale(0)) / unitFactor;
            });

        trafficLightTransition
            .select('text')
            .attr("x", function (d) {
                return (microsecsXScale(d.upperBoundary - d.lowerBoundary) - microsecsXScale(0)) / unitFactor / 2;
            });

    };


    initChart();

    return {
        drawChart: drawChart
    };

});
