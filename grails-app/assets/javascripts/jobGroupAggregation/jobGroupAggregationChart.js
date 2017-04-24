//= require /bower_components/d3/d3.min.js
//= require /d3/chartLabelUtil
//= require /d3/trafficLightDataProvider
//= require /d3/chartColorProvider
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.JobGroupAggregationHorizontal = (function (chartIdentifier) {

    var svg,
        topMargin = 50,
        svgContainer = $("#" + chartIdentifier),
        outerMargin = 25,
        barHeight = 40,
        barPadding = 10,
        valueMarginInBar = 4,
        colorProvider = OpenSpeedMonitor.ChartColorProvider(),
        absoluteMaxValue = 0,
        absoluteMaxYOffset = 0,
        labelWidths = [],
        xScale = d3.scale.linear(),
        initialBarWidth = 20,
        countTrafficLightBar = 1,
        allBarsGroup,
        trafficLightBars,
        transitionDuration = 600,
        svgHeight,
        filterRules = {},
        actualBarchartData,
        commonLabelParts,
        headerLine,
        barSelected;
    var unitPrecisions = {
        MB: 2
    };

    var drawChart = function (barchartData) {

        if (svg === undefined) {
            initChart();
        }

        initChartData(barchartData);

        initFilterDropdown();
        drawAllBars();

    };

    var initChart = function () {

        $("#chart-card").removeClass("hidden");

        svg = d3.select("#" + chartIdentifier).append("svg")
            .attr("class", "d3chart");

        headerLine = svg.append("g");
        allBarsGroup = svg.append("g");
        trafficLightBars = svg.append("g");

        $(window).resize(drawAllBars);
    };

    var initChartData = function (barchartData) {

        barchartData.series.forEach(function (series) {
            var labelUtil = OpenSpeedMonitor.ChartModules.ChartLabelUtil(series.data, barchartData.i18nMap);
            series.data = labelUtil.getSeriesWithShortestUniqueLabels();
            commonLabelParts = labelUtil.getCommonLabelParts();
            series.data.sort(function (x, y) {
                return d3.ascending(x.value, y.value);
            });
        });

        barchartData.originalSeries = clone(barchartData.series);

        actualBarchartData = barchartData;

    };

    var drawAllBars = function () {

        updateSvgWidth();

        actualBarchartData.series.forEach(function (currentSeries) {
            var seriesData = currentSeries.data;
            var unitName = currentSeries.dimensionalUnit;
            drawSeries(seriesData, unitName);
        });

        drawTrafficLight();

        var headerData = [{headerText: commonLabelParts}];
        drawHeader(headerData);

    };

    var updateSvgWidth = function () {
        svg.attr("width", svgContainer.width());
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
                return "translate(" + parseInt(outerMargin + barPadding + getMaxLabelWidth()) + ")";
            });

        //update

        headerText
            .transition()
            .duration(transitionDuration)
            .attr("transform", function (d) {
                return "translate(" + parseInt(outerMargin + barPadding + getMaxLabelWidth()) + ")";
            });

    }

    var drawSeries = function (seriesData, unitName) {
        var defineXScale = function () {
            var paddingBetweenLabelAndBar = barPadding;
            xScale
                .domain([0, absoluteMaxValue])
                .range([0, svg.attr("width") - outerMargin * 2 - getMaxLabelWidth() - paddingBetweenLabelAndBar]);
        };

        svgHeight = topMargin + (seriesData.length + countTrafficLightBar) * (barHeight + barPadding) + barPadding;
        svg.attr("height", svgHeight);
        absoluteMaxYOffset = (seriesData.length - 1) * (barHeight + barPadding) + barPadding;

        absoluteMaxValue = Math.max(absoluteMaxValue, d3.max(seriesData, function (d) {
            return d.value;
        }));

        var singleBarGroups = allBarsGroup.selectAll("g")
            .data(seriesData, function (d) {
                return d.label + d.measurand;
            });

        // exit ////////////////////////////////////////////////////////////////////////////////////

        singleBarGroups.exit()
            .transition()
            .duration(transitionDuration)
            .attr("width", 0)
            .remove()
            .each(function (elem) {
                labelWidths = labelWidths.filter(function (labelWidth) {
                    return labelWidth.identifier !== elem.label + elem.measurand;
                })
            });

        // enter ////////////////////////////////////////////////////////////////////////////////////

        var singleBarGroupsEnter = singleBarGroups.enter()
            .append("g")
            .attr("cx", 0)
            .attr("transform", function (d, index) {
                var yOffset = (topMargin + index * (barHeight + barPadding) + barPadding);
                return "translate(" + outerMargin + "," + yOffset + ")";
            });

        singleBarGroupsEnter.append("text")
            .attr("y", barHeight / 2)
            .attr("dy", ".35em") //vertical align middle
            .classed("d3chart-label", true)
            .text(function (d) {
                return d.label;
            })
            .each(function (elem) {
                labelWidths.push({identifier: elem.label + elem.measurand, width: this.getBBox().width})
            });

        defineXScale();

        singleBarGroupsEnter.append("rect")
            .attr("transform", "translate(" + parseInt(getMaxLabelWidth() + barPadding) + ", 0)")
            .attr("class", "d3chart-bar")
            .attr("height", barHeight)
            .attr("width", initialBarWidth)
            .attr("fill", function (d, i) {
                var colorscale = colorProvider.getColorscaleForMeasurandGroup(unitName);
                return colorscale(0);
            })
            .on("click", highlightClickedBar);

        singleBarGroupsEnter.append("text")
            .classed("d3chart-value", true)
            .attr("y", barHeight / 2)
            .attr("dx", -valueMarginInBar + getMaxLabelWidth()) //margin right
            .attr("text-anchor", "end")
            .text(function (d) {
                return formatValue(d.value, unitName) + " " + unitName;
            });


        // update ////////////////////////////////////////////////////////////////////////////////////

        singleBarGroups
            .transition()
            .duration(transitionDuration)
            .attr("transform", function (d, index) {
                var yOffset = (topMargin + index * (barHeight + barPadding) + barPadding);
                return "translate(" + outerMargin + "," + yOffset + ")";
            })
            .select("rect")
            .attr("width", function (d) {
                return xScale(d.value);
            });

        singleBarGroups
            .select(".d3chart-value")
            .transition()
            .duration(transitionDuration)
            .attr("x", function (d) {
                var width = this.getBBox().width;
                return Math.max(width + valueMarginInBar, xScale(d.value));
            })
            .attr("opacity", function (d) {
                // Hide labels if they are larger than the rect to draw in
                var textBox = this.getBBox();
                var rectWidth = xScale(d.value);
                if (textBox.width > rectWidth - 3 * valueMarginInBar) {
                    return 0;
                }
                return 1;
            });

        defineXScale();

    };


    var formatValue = function (value, unit) {
        var precision = unitPrecisions[unit] || 0;
        return parseFloat(value).toFixed(precision);
    };

    var getMaxLabelWidth = function () {
        return d3.max(labelWidths, function (d) {
            return d.width;
        });
    };

    var highlightClickedBar = function (d) {
        if (barSelected !== d.grouping) {
            barSelected = d.grouping;
            d3.selectAll(".d3chart-bar").classed("d3chart-faded", true);
            d3.select(this).classed("d3chart-faded", false);
        } else {
            d3.selectAll(".d3chart-bar").classed("d3chart-faded", false);
            barSelected = null;
        }
    };

    var initFilterDropdown = function () {

        var $filterDropdownGroup = $("#filter-dropdown-group");

        // remove old filter
        $filterDropdownGroup.find('.filterRule').remove();

        if ($filterDropdownGroup.hasClass("hidden"))
            $filterDropdownGroup.removeClass("hidden");

        $filterDropdownGroup.find("#all-bars-desc").click(function (e) {
            filterBarChartData(true);
            toogleFilterCheckmarks(e.target);
        });
        $filterDropdownGroup.find("#all-bars-asc").click(function (e) {
            filterBarChartData(false);
            toogleFilterCheckmarks(e.target);
        })
    };

    var clone = function (toClone) {
        return JSON.parse(JSON.stringify(toClone));
    };

    var filterBarChartData = function (desc) {

        actualBarchartData.series = clone(actualBarchartData.originalSeries);

        if (desc) {
            actualBarchartData.series.forEach(function (series) {
                series.data.sort(function (x, y) {
                    return d3.descending(x.value, y.value);
                });
            });
        } else {
            actualBarchartData.series.forEach(function (series) {
                series.data.sort(function (x, y) {
                    return d3.ascending(x.value, y.value);
                });
            });
        }
        drawAllBars()

    };

    var toogleFilterCheckmarks = function (listItem) {
        $('.filterActive').toggleClass("filterInactive filterActive");

        // reset checkmark to 'ascending' if 'Show' gets clicked
        // otherwise set checkmark to the list item one has clicked on
        if (typeof listItem == 'undefined') {
            $('#all-bars-asc > .filterInactive').toggleClass("filterActive filterInactive");
        } else {
            $(listItem).find(".filterInactive").toggleClass("filterActive filterInactive");
        }
    };

    var drawTrafficLight = function () {

        var trafficLightData = OpenSpeedMonitor.ChartModules.TrafficLightDataProvider.getTimeData(absoluteMaxValue);

        var trafficLight = trafficLightBars.selectAll("g")
            .data(trafficLightData, function (d) {
                return d.id
            });

        //exit

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
                var xOffset = outerMargin + getMaxLabelWidth() + xScale(d.lowerBoundary) + barPadding;
                return "translate(" + xOffset + ", " + (topMargin + absoluteMaxYOffset + barHeight + barPadding * 2) + ")";
            });

        trafficLightEnter.append("rect")
            .attr("height", barHeight)
            .attr("width", initialBarWidth)
            .attr("fill", function (d) {
                return d.fill;
            })
            .attr("fill-opacity", function (d) {
                return d.fillOpacity;
            })
            .transition()
            .duration(transitionDuration);

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

        //update

        trafficLight
            .transition()
            .duration(transitionDuration)
            .attr("transform", function (d, index) {
                var xOffset = outerMargin + getMaxLabelWidth() + xScale(d.lowerBoundary) + barPadding;
                return "translate(" + xOffset + ", " + (topMargin + absoluteMaxYOffset + barHeight + barPadding * 2) + ")";
            })
            .select('rect')
            .attr("width", function (d) {
                return xScale(d.upperBoundary - d.lowerBoundary);
            });

        trafficLight
            .select("text")
            .transition()
            .duration(transitionDuration)
            .attr("x", function (d) {
                return xScale(d.upperBoundary - d.lowerBoundary) / 2;
            });
    };

    initChart();

    return {
        drawChart: drawChart
    };

});
