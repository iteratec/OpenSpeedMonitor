//= require /bower_components/d3/d3.min.js
//= require /bower_components/dimple/dist/dimple.v2.2.0.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageAggregation = (function (chartIdentifier) {
    var chart = null,
        chartData = null,
        width = 600,
        height = 600,
        legendPosition = {x: 200, y: 10, width: 100, height: 100},
        margins = {left: 60, right: 100, top: 110, bottom: 150},
        maxWidthPerBar = 150,
        allMeasurandSeries = {},
        filterRules = {},
        svg = null,
        seriesCount = 0,
        xAxis = null,
        yAxes = {},
        showBarLabels = false,
        showXAxis = true,
        showYAxis = true,
        showGridlines = true;

    var init = function () {
        // add eventHandler
        window.onresize = function () {
            calcChartWidth();
            resize();
        };
    };

    var drawChart = function (barchartData) {
        chartData = barchartData;

        // Delete old chart in same container
        d3.select("#" + chartIdentifier).selectAll("svg").remove();
        $("#chart-card").removeClass("hidden");
        var $adjustBarchartButton = $("#adjust-barchart-modal");
        var $downloadAsPngButton = $("#download-as-png-button");
        if ($adjustBarchartButton.hasClass("hidden"))
            $adjustBarchartButton.removeClass("hidden");
        if ($downloadAsPngButton.hasClass("hidden"))
            $downloadAsPngButton.removeClass("hidden");
        initFilterDropdown(chartData.filterRules);

        // Reset fields
        allMeasurandSeries = {};
        yAxes = {};
        xAxis = null;
        height = 600;

        // Create SVG
        svg = dimple.newSvg("#" + chartIdentifier, "100%", height);
        chart = new dimple.chart(svg, null);
        chart.setMargins(margins.left, margins.right, margins.top, margins.bottom);
        svg = svg.node();
        seriesCount = Object.keys(chartData['series']).length;

        // Add x axis
        xAxis = chart.addCategoryAxis("x", "grouping");
        xAxis.title = chartData['groupingLabel'];
        filterRules = chartData.filterRules;

        // Add series
        var seriesId = 0;
        for (var currentSeriesId in chartData.series) {
            var currentSeries = chartData.series[currentSeriesId];

            // get correct y axis
            var yAxis = yAxes[currentSeries['dimensionalUnit']];
            if (!yAxis) {
                yAxis = chart.addMeasureAxis("y", "indexValue");
                yAxis.title = currentSeries['yAxisLabel'];
                yAxes[currentSeries['dimensionalUnit']] = yAxis;
            }

            allMeasurandSeries[currentSeriesId] = [];
            if (currentSeries.stacked === true) {
                var s = chart.addSeries("index", dimple.plot.bar, [xAxis, yAxis]);
                s.data = currentSeries.data;
                // set originData for filtering
                s.originData = s.data;
                allMeasurandSeries[currentSeriesId].push(seriesId);
                seriesId += 1;
            } else {
                var allMeasurands = removeDuplicatesFromArray(currentSeries.data.map(function (value) {
                    return value.index;
                }));
                allMeasurands.forEach(function (measurand) {
                    var s = chart.addSeries("index", dimple.plot.bar, [xAxis, yAxis]);
                    s.data = currentSeries.data.filter(function (datum) {
                        return datum.index === measurand;
                    });
                    s.originData = s.data;
                    allMeasurandSeries[currentSeriesId].push(seriesId);
                    seriesId += 1;
                })
            }
        }

        // set originSeries for filtering
        chart.originSeries = chart.series;

        chart.addLegend(legendPosition.x, legendPosition.y, legendPosition.width, legendPosition.height, "right");
        draw(false);
        calcChartWidth();
        resize();

        postDraw();
    };

    var adjustChart = function () {
        // change size
        width = $("#inputChartWidth").val() + "px";
        height = $("#inputChartHeight").val() + "px";

        // change labels
        xAxis.title = $("#x-axis-label").val();
        $("#y-axis-alias-container").children().each(function () {
            yAxes[$(this).find(".unitInput").val()].title = $(this).find(".labelInput").val()
        });

        // assign colors
        var colorAssignments = [];
        $("#assign-color-container").children().each(function () {
            colorAssignments.push({"label": $(this).find(".colorLabel").data('label'), "color": $(this).find("input").val()});
        });
        assignColor(colorAssignments);

        // Toggle gridlines
        showGridlines = $("#inputShowGridlines").parent().hasClass('active');
        if (showGridlines) {
            d3.select(".dimple-gridline").style("opacity", 1);
        } else {
            d3.select(".dimple-gridline").style("opacity", 0);
        }

        // Toggle yAxis
        showYAxis = $("#inputShowYAxis").parent().hasClass('active');
        if (showYAxis) {
            d3.selectAll(".dimple-axis.dimple-axis-y").style("opacity", 1);
        } else {
            d3.selectAll(".dimple-axis.dimple-axis-y").style("opacity", 0);
        }

        // Toggle xAxis
        showXAxis = $("#inputShowXAxis").parent().hasClass('active');
        if (showXAxis) {
            d3.select(".domain.dimple-custom-axis-line").style("opacity", 1);
            d3.select(".dimple-axis-x").selectAll("line").style("opacity", 1);
        } else {
            d3.select(".domain.dimple-custom-axis-line").style("opacity", 0);
            d3.select(".dimple-axis-x").selectAll("line").style("opacity", 0);
        }

        // Toggle bar labels
        showBarLabels = $("#inputShowBarLabels").parent().hasClass('active');
        if (showYAxis) {
            d3.selectAll(".bar-label").style("opacity", 1);
        } else {
            d3.selectAll(".bar-label").style("opacity", 0);
        }

        resize();

        postDraw();
    };

    var postDraw = function () {
        setLegendNames();
        addBarLabels();
    };

    var setLegendNames = function () {
        chart.legends[0].shapes[0].forEach( function (entry) {
            var legendI18N = entry.firstChild.textContent.replace(entry.textContent, OpenSpeedMonitor.i18n.measurandLabels[entry.textContent]);
            entry.firstChild.textContent = legendI18N;
        });
    };

    var assignColor = function (colorAssignments) {
        colorAssignments.forEach(function (assignment) {
            chart.assignColor(assignment.label, assignment.color);
        });

        // reassign data to series so series are redrawn
        chart.series.forEach(function (s) {
            var seriesData = s.data;
            // fake some data change
            s.data = [{"grouping": s.data[0].grouping, "index": "", "indexValue": 0}];
            draw(false);
            s.data = seriesData;
        });

        draw(false);
    };

    /**
     * Redraws the chart.
     * If dataValid = true, then the chart gets drawn without reprocessing data.
     */
    var draw = function (dataValid) {
        chart.draw(0, dataValid);
    };

    var positionBars = function () {
        // Move Bars side to side
        for (var s in allMeasurandSeries) {
            var i = Object.keys(allMeasurandSeries).indexOf(s);
            allMeasurandSeries[s].forEach(function (seriesId) {
                var currentSeriesRects = d3.selectAll(".dimple-series-group-" + seriesId).selectAll("rect");
                if (currentSeriesRects) {
                    var oldWidth = parseInt(currentSeriesRects.attr("width"));
                    var translateX = i * oldWidth / seriesCount;
                    currentSeriesRects.attr("transform", "translate(" + translateX + ", 0)");
                    currentSeriesRects.attr("width", oldWidth / seriesCount);
                }
            })

        }
    };

    var calcChartWidth = function () {
        var maxWidth, containerWidth;

        containerWidth = $("#" + chartIdentifier).width();
        maxWidth = Object.keys(allMeasurandSeries).length * $(".dimple-axis-x .tick").length * maxWidthPerBar;
        width = containerWidth < maxWidth ? "100%" : "" + maxWidth + "px";
    };

    var initFilterDropdown = function (filterRules) {
        var $filterDropdownGroup = $("#filter-dropdown-group");
        var $customerJourneyHeader = $filterDropdownGroup.find("#customer-journey-header");

        // remove old filter
        $filterDropdownGroup.find('.filterRule').remove();

        if ($filterDropdownGroup.hasClass("hidden"))
            $filterDropdownGroup.removeClass("hidden");

        for (var filterRuleKey in filterRules) {
            if (filterRules.hasOwnProperty(filterRuleKey)) {
                var link = $("<li class='filterRule'><a href='#'>" + filterRuleKey + "</a></li>");
                link.click(function (e) {
                    filterCustomerJourney(e.target.innerText);
                });
                link.insertAfter($customerJourneyHeader);
            }
        }

        $filterDropdownGroup.find("#all-bars-desc").click(function (e) {
            filterCustomerJourney(null, true);
        });
        $filterDropdownGroup.find("#all-bars-asc").click(function (e) {
            filterCustomerJourney(null, false);
        })
    };

    var filterCustomerJourney = function (journeyKey, desc) {
        if (journeyKey && filterRules[journeyKey]) {
            // order axis
            xAxis._orderRules = [];
            xAxis.addOrderRule(filterRules[journeyKey]);

            // remove elemts not in customer Journey
            chart.series.forEach(function (currentSeries) {
                currentSeries.data = currentSeries.originData;
                currentSeries.data = currentSeries.originData.filter(function (element) {
                    return filterRules[journeyKey].indexOf(element.grouping) >= 0;
                });
            });
            // remove series containing no data
            chart.series = chart.originSeries.filter(function (s) {
                return s.data.length > 0;
            });

        } else {
            // reset order rules
            desc == desc || false;
            xAxis._orderRules = [];
            xAxis.addOrderRule(function (a, b) {
                var valueA = parseFloat(a.indexValue[0]);
                var valueB = parseFloat(b.indexValue[0]);

                if (valueA < valueB) {
                    return -1;
                } else if (valueA === valueB) {
                    return 0;
                } else {
                    return 1;
                }
            }, desc);

            chart.series = chart.originSeries;
            chart.series.forEach(function (currentSeries) {
                currentSeries.data = currentSeries.originData;
            });
        }

        draw(false);
        resize();

        addBarLabels();
    };

    var resize = function () {
        if (svg) {
            svg.setAttribute("width", width);
            svg.setAttribute("height", height);
            chart.legends[0].x = parseInt(width) - legendPosition.width - margins.right;

            draw(true);
            positionBars();
        }
    };

    var addBarLabels = function () {
        // save width of rectangles for comparison with label width
        var rectangleWidth = null;

        for (var barIndex in allMeasurandSeries) {
            allMeasurandSeries[barIndex].forEach(function (seriesID, seriesIndex) {
                var currentSeriesLabelClass = "dimple-series-group-" + seriesID.toString() + "-labels";

                // remove labels if they exist and replot them afterwards
                d3.selectAll("." + currentSeriesLabelClass).remove();

                // create labels group container
                chart.svg.append("g").attr("class", currentSeriesLabelClass);

                var currentSeriesRects = d3.selectAll(".dimple-series-group-" + seriesID).selectAll("rect");
                currentSeriesRects[0].forEach(function (rectangle, rectangleIndex) {
                    rectangleWidth = rectangle.width.baseVal.value;

                    // get the unit
                    // stacked/overlayed bars can only be of the same dimensional unit, therefore accessing the first is sufficient
                    var unit = chartData.series[barIndex].dimensionalUnit;
                    // get the measurand value and format it
                    var barchartSeriesDataIndex = seriesIndex * currentSeriesRects[0].length + rectangleIndex;
                    var value = chartData.series[barIndex].data[barchartSeriesDataIndex].indexValue.toString();
                    value = (unit == "%") ? parseFloat(value).toFixed(1) : Math.round(value);

                    // set the label
                    var label = (unit == "#") ? unit + " " + value : value + " " + unit;

                    // append the label
                    d3.selectAll("." + currentSeriesLabelClass).append("text")
                        .attr("x", rectangle.x.baseVal.value + rectangle.width.baseVal.value / 2)
                        .attr("y", rectangle.y.baseVal.value - 10)
                        .text(label)
                        .style("text-anchor", "middle")
                        .attr("class", "bar-label");
                });
                // move labels if necessary
                var currentSeriesLabels = d3.selectAll(".dimple-series-group-" + seriesID + "-labels").selectAll("text");
                var width = parseInt(currentSeriesRects.attr("width"));
                var translateX = barIndex * width;
                currentSeriesLabels.attr("transform", "translate(" + translateX + ", 0)");

                // hide the labels initially
                if (!showBarLabels)
                    currentSeriesLabels.style("opacity", 0);
            });
        }

        // check if labels need to be rotated
        var rotate = false;
        d3.selectAll(".bar-label").each(function () {
            if (d3.select(this).node().getComputedTextLength() > rectangleWidth) {
                rotate = true;
            }
        });

        if (rotate)
            rotateLabels();
    };

    var rotateLabels = function () {
        d3.selectAll(".bar-label")
            .style("text-anchor", "start")
            .each(function () {
                var x = d3.select(this).attr("x");
                var y = d3.select(this).attr("y");
                // get the translate value
                var t = d3.transform(d3.select(this).attr("transform"));
                var translateX = t.translate[0];
                var translateY = t.translate[1];
                // calculate the x value for rotation
                var rotateX = parseFloat(x) + translateX;
                console.log(rotateX);
                d3.select(this)
                    .attr("transform", "translate(" + translateX + "," + translateY + ")rotate(" + -45 + ", " + x + ", " + y + ")");
            });
    };

    var getXLabel = function () {
        return xAxis.title;
    };
    var getYLabels = function () {
        var result = [];
        Object.keys(yAxes).forEach(function (key) {
            result.push({"unit": key, "label": yAxes[key].title})
        });
        return result;
    };
    var getWidth = function () {
        var svgWidth = svg.getAttribute("width");
        if (svgWidth.endsWith("px")) {
            return parseInt(svgWidth);
        } else if (svgWidth.endsWith("%")) {
            return parseInt(svgWidth) * $("#" + chartIdentifier).width() / 100;
        } else {
            return svgWidth;
        }
    };
    var getHeight = function () {
        var svgHeight = svg.getAttribute("height");
        if (svgHeight.endsWith("px")) {
            return parseInt(svgHeight);
        } else if (svgHeight.endsWith("%")) {
            return parseInt(svgHeight) * $("#" + chartIdentifier).height() / 100;
        } else {
            return svgHeight;
        }
    };
    var getColorAssignments = function () {
        var result = [];
        chart.legends[0]._getEntries().forEach(function (legendEntry) {
            result.push({"label": legendEntry.key, "color": legendEntry.fill});
        });
        return result;
    };
    var getShowXAxis = function () {
        return showXAxis;
    };
    var getShowYAxis = function () {
        return showYAxis;
    };
    var getShowGridlines = function () {
        return showGridlines;
    };
    var getShowBarLabels = function () {
        return showBarLabels;
    };

    // returns a new array without removed duplicates
    var removeDuplicatesFromArray = function (array) {
        return array.reduce(function (p, c) {
            if (p.indexOf(c) < 0) p.push(c);
            return p;
        }, []);
    };


    init();
    return {
        adjustChart: adjustChart,
        drawChart: drawChart,
        getColorAssignments: getColorAssignments,
        getHeight: getHeight,
        getWidth: getWidth,
        getXLabel: getXLabel,
        getYLabels: getYLabels,
        getShowXAxis: getShowXAxis,
        getShowYAxis: getShowYAxis,
        getShowGridlines: getShowGridlines,
        getShowBarLabels: getShowBarLabels,
        resize: resize
    };

});
