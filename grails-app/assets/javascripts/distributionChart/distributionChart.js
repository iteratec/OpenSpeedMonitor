//= require /node_modules/d3/d3.min.js
//= require /d3/chartLabelUtil
//= require /d3/chartColorProvider

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.distributionChart = (function () {
    var svg = null,
        chartData = null,
        width = 600,
        height = 600,
        margin = {top: 50, right: 0, bottom: 70, left: 100},
        maxViolinWidth = 150,
        violinWidth = null,
        mainDataResolution = 30,
        interpolation = 'basis',
        dataTrimValue = null,
        commonLabelParts,
        colorProvider = OpenSpeedMonitor.ChartColorProvider();

    var init = function () {
        dataTrimValue = document.querySelector("#data-trim-value");
        dataTrimValue.addEventListener('change', function() {
            draw();
        });

        $(window).on('resize', drawUpdatedSize);
    };

    var drawUpdatedSize = function () {
        var domain = getDomain();

        width = svg.node().getBoundingClientRect().width;
        violinWidth = calculateViolinWidth();

        drawXAxis();
        drawViolins(domain);
        svg.select("#header-text").attr("transform", getHeaderTransform());
        chartStyling();
    };

    var drawChart = function (distributionChartData) {
        chartData = distributionChartData;
        sortSeriesDataAscending();

        assignShortLabels();

        // sort the violins after descending median as default
        sortByMedian();
        var sortedSeries = {};
        chartData.sortingRules.desc.forEach(function (trace) {
            sortedSeries[trace] = chartData.series[trace];
        });
        chartData.series = sortedSeries;

        initFilterDropdown(chartData.filterRules);
        draw();
    };

    var draw = function () {
        initSvg();
        violinWidth = calculateViolinWidth();

        var domain = getDomain();

        drawXAxis();
        drawYAxis(domain,chartData.i18nMap['measurand'] + " [" + chartData.dimensionalUnit + "]");
        drawViolins(domain);
        drawHeader();

        postDraw();
    };

    var initSvg = function () {
        if (svg)
            svg.remove();

        svg = d3.select("#svg-container").append("svg")
            .attr("class", "d3chart")
            .attr("height", height)
            .attr("width", "100%");
        width = svg.node().getBoundingClientRect().width;
    };

    var assignShortLabels = function () {
        var seriesLabelParts = Object.keys(chartData.series).map(function (traceLabel) {
            return {grouping: traceLabel};
        });

        var labelUtil = OpenSpeedMonitor.ChartModules.ChartLabelUtil(seriesLabelParts, chartData.i18nMap);
        labelUtil.getSeriesWithShortestUniqueLabels();
        commonLabelParts = labelUtil.getCommonLabelParts();

        seriesLabelParts.forEach(function (labelPart) {
            chartData.series[labelPart.grouping].label = labelPart.label;
        });
    };

    var getHeaderTransform = function() {
        var widthOfAllViolins = Object.keys(chartData.series).length * violinWidth;
        return "translate(" + (margin.left + widthOfAllViolins / 2) + ",20)";
    };

    var drawHeader = function () {
        svg.append("g").selectAll("text")
            .data([commonLabelParts])
            .enter()
            .append("text")
            .text(commonLabelParts)
            .attr("id", "header-text")
            .attr("transform", getHeaderTransform());
    };

    var drawXAxis = function () {
        var x = d3.scale.ordinal()
            .range(xRange())
            .domain(Object.keys(chartData.series).map(function (seriesKey) {
                return chartData.series[seriesKey].label;
            }));

        var xAxis = d3.svg.axis()
            .scale(x)
            .orient("bottom");

        svg.selectAll(".d3chart-xAxis").remove();
        svg.append("g")
            .attr("class", "d3chart-axis d3chart-xAxis")
            .call(xAxis)
            .call(rotateLabels)
            .attr("transform", "translate(" + margin.left + ", " + ( height - margin.bottom ) + ")");
    };

    var drawYAxis = function (domain, text) {
        var y = d3.scale.linear()
            .range([height - margin.bottom, margin.top])
            .domain(domain);

        $("#y-axis_left_label").remove();
        $("#svg-container").append("<div id=\"y-axis_left_label\" style=\"left: 45px;position: absolute;white-space: nowrap;transform: translate(-50%, 0%) rotate(-90deg);top: 50%;\">"+text+"</div>");

        var yAxis = d3.svg.axis()
                .scale(y)
                .orient("left");

        var g = svg.append("g")
            .attr("class", "d3chart-axis d3chart-yAxis")
            .attr("transform", "translate(" + margin.left + ", 0)")
            .call(yAxis);

        g.selectAll(".tick line").classed("d3chart-yAxis-line", true);
        g.selectAll("path").classed("d3chart-yAxis-line", true);
    };

    var drawViolins = function (domain) {
        svg.selectAll("clipPath").remove();
        svg.select("[clip-path]").remove();

        var violinGroup = svg.append("g");
        createClipPathAroundViolins(violinGroup);
        Object.keys(chartData.series).forEach(function (trace, i) {
            var traceData = chartData.series[trace].data;

            var g = violinGroup.append("g")
                .attr("class", "d3chart-violin")
                .attr("transform", "translate(" + (i * violinWidth + margin.left) + ",0)");

            addViolin(g, traceData, height - margin.bottom, violinWidth, domain);
        });
    };

    var createClipPathAroundViolins = function (violinGroup) {
        var clipPathId = "violin-clip";
        svg
            .append("clipPath")
            .attr("id", clipPathId)
            .append("rect")
            .attr("x", margin.left)
            .attr("y", margin.top)
            .attr("width", width - margin.left - margin.right)
            .attr("height", height - margin.top - margin.bottom);
        violinGroup.attr("clip-path", "url(#" + clipPathId + ")");
    };

    var sortSeriesDataAscending = function() {
        Object.keys(chartData.series).forEach( function (trace, i) {
            chartData.series[trace].data.sort(d3.ascending);
        });
    };

    var postDraw = function () {
        chartStyling();
        toogleFilterCheckmarks();
    };

    var calculateViolinWidth = function () {
        var svgWidth = width - margin.left;
        var numberOfViolins = Object.keys(chartData.series).length;

        if (numberOfViolins * maxViolinWidth > svgWidth) {
            return svgWidth / numberOfViolins;
        }

        return maxViolinWidth;
    };

    var getDomain = function () {
        var maxValues = [];
        Object.keys(chartData.series).forEach( function (trace) {
            maxValues.push(Math.max.apply(null, chartData.series[trace].data));
        });
        var maxValue = Math.max.apply(null, maxValues);
        var trimValue = dataTrimValue.value || maxValue;

        return [0, Math.min(maxValue, trimValue)];
    };

    var getGreatestDomainTrace = function () {
        var maxDomainSize = -1;
        var greatestTrace = [];
        Object.keys(chartData.series).forEach( function (trace) {
            var curTrace = chartData.series[trace].data;
            var domainSize = d3.quantile(curTrace, 0.75) - d3.quantile(curTrace, 0.25);
            if (domainSize > maxDomainSize) {
                maxDomainSize = domainSize;
                greatestTrace = curTrace;
            }
        });
        return greatestTrace;
    };

    var xRange = function () {
        var range = [];
        Object.keys(chartData.series).forEach( function (trace, i) {
            range.push(i * violinWidth + violinWidth/2);
        });

        return range;
    };

    var addViolin = function (g, traceData, height, violinWidth, domain) {
        var resolution = histogramResolutionForTraceData(traceData);

        var data = d3.layout.histogram()
                     .bins(resolution)
                     .frequency(0)
                     (traceData);

        // y is now the horizontal axis because of the violin being a 90 degree rotated histogram
        var y = d3.scale.linear()
                  .range([violinWidth/2, 0])
                  .domain([0, d3.max(data, function(d) { return d.y; })]);

        // x is now the vertical axis because of the violin being a 90 degree rotated histogram
        var x = d3.scale.linear()
                  .range([height, margin.top])
                  .domain(domain)
                  .nice();

        var area = d3.svg.area()
                     .interpolate(interpolation)
                     .x(function (d) { return x(d.x); })
                     .y0(violinWidth/2)
                     .y1(function (d) { return y(d.y); });

        var line = d3.svg.line()
                     .interpolate(interpolation)
                     .x(function (d) { return x(d.x); })
                     .y(function (d) { return y(d.y); });

        var gPlus = g.append("g");
        var gMinus = g.append("g");


        var colorscale = colorProvider.getColorscaleForMeasurandGroup(chartData.dimensionalUnit);
        var violinColor = colorscale(0);

        gPlus.append("path")
             .datum(data)
             .attr("class", "d3chart-violinArea")
             .attr("d", area)
             .style("fill", violinColor);

        gPlus.append("path")
             .datum(data)
             .attr("class", "d3chart-violinOutline")
             .attr("d", line)
             .style("stroke", violinColor);

        gMinus.append("path")
              .datum(data)
              .attr("class", "d3chart-violinArea")
              .attr("d", area)
              .style("fill", violinColor);

        gMinus.append("path")
              .datum(data)
              .attr("class", "d3chart-violinOutline")
              .attr("d", line)
              .style("stroke", violinColor);

        gPlus.attr("transform", "rotate(90, 0, 0)  translate(0, -" + violinWidth + ")");
        gMinus.attr("transform", "rotate(90, 0, 0) scale(1, -1)");
    };

    var histogramResolutionForTraceData = function (traceData) {
        var greatestDomainTrace = getGreatestDomainTrace();
        var quantile25 = d3.quantile(greatestDomainTrace, 0.25);
        var quantile75 = d3.quantile(greatestDomainTrace, 0.75);
        var binSize = (quantile75 - quantile25) / mainDataResolution;
        return Math.floor((traceData[traceData.length -1] - traceData[0]) / binSize);
    };

    var sortByMedian = function () {
        Object.keys(chartData.series).forEach( function (trace) {
            chartData.series[trace].median = calculateMedian(chartData.series[trace].data);
        });

        chartData.sortingRules = {};

        chartData.sortingRules.desc = Object.keys(chartData.series).sort( function (a, b) {
            return chartData.series[b].median - chartData.series[a].median;
        });

        chartData.sortingRules.asc = Object.keys(chartData.series).sort( function (a, b) {
            return chartData.series[a].median - chartData.series[b].median;
        });
    };

    var calculateMedian = function (arr) {
        // for safety reasons sort the array
        arr.sort(function(a, b) {
            return a - b;
        });

        var i = arr.length/2;
        var med = (arr.length % 2 === 0) ? arr[i-1] : (arr[Math.floor(i)-1] + arr[Math.floor(i)])/2;

        return med;
    };

    var initFilterDropdown = function (filterRules) {
        var $filterDropdownGroup = $("#filter-dropdown-group");
        var $customerJourneyHeader = $filterDropdownGroup.find("#customer-journey-header");

        // remove old filter
        $filterDropdownGroup.find('.filterRule').remove();

        for (var filterRuleKey in filterRules) {
            if (filterRules.hasOwnProperty(filterRuleKey)) {
                var link = $("<li class='filterRule'><a href='#'><i class='fas fa-check filterInactive' aria-hidden='true'></i>" + filterRuleKey + "</a></li>");
                link.on('click', function (e) {
                    filterCustomerJourney(e.target.innerText);
                    toogleFilterCheckmarks(e.target);
                });
                link.insertAfter($customerJourneyHeader);
            }
        }

        $filterDropdownGroup.find("#all-violins-desc").on('click', function (e) {
            filterCustomerJourney(null, true);
            toogleFilterCheckmarks(e.target);
        });
        $filterDropdownGroup.find("#all-violins-asc").on('click', function (e) {
            filterCustomerJourney(null, false);
            toogleFilterCheckmarks(e.target);
        })
    };

    var filterCustomerJourney = function (journeyKey, desc) {
        var filteredAndSortedSeries = {};

        if (journeyKey) {
            var journey = chartData.filterRules[journeyKey];

            journey.forEach( function (trace) {
                filteredAndSortedSeries[trace] = chartData.series[trace];
            });
        } else {
            var sortingOrder = desc ? "desc" : "asc";
            chartData.sortingRules[sortingOrder].forEach( function (trace) {
                filteredAndSortedSeries[trace] = chartData.series[trace];
            });
        }

        chartData.series = filteredAndSortedSeries;
        draw();
    };

    var toogleFilterCheckmarks = function (listItem) {
        // remove all checkmarks
        $(".filterActive").toggleClass("filterInactive filterActive");

        // reset checkmark to 'descending' if 'Show' gets clicked
        // otherwise set checkmark to the list item one has clicked on
        if (typeof listItem == 'undefined') {
            $('#all-violins-desc > .filterInactive').toggleClass("filterActive filterInactive");
        } else {
            $(listItem).find(".filterInactive").toggleClass("filterActive filterInactive");
        }
    };

    var chartStyling = function () {
        // remove the xAxis lines
        d3.select(".d3chart-axis.d3chart-xAxis > path.domain").remove();
        d3.selectAll(".d3chart-axis.d3chart-xAxis g > line").remove();
    };

    var rotateLabels = function () {
        var rotate = false;
        var maxLabelLength = -1;
        d3.selectAll(".d3chart-xAxis text").each(function() {
            var labelLength = d3.select(this).node().getComputedTextLength();

            if (labelLength > maxLabelLength)
                maxLabelLength = labelLength;

            if (labelLength > violinWidth)
                rotate = true;

            margin.bottom = d3.select(this).node().getBoundingClientRect().height + 20;
        });

        if (rotate) {
            margin.bottom = Math.cos(Math.PI / 4) * maxLabelLength + 20;

            d3.selectAll(".d3chart-xAxis text")
                .style("text-anchor", "start")
                .each(function () {
                    var x = d3.select(this).attr("x");
                    var y = d3.select(this).attr("y");
                    // get the translate value
                    var t = d3.transform(d3.select(this).attr("transform"));
                    var translateX = t.translate[0];
                    var translateY = t.translate[1];

                    d3.select(this)
                        .attr("transform", "translate(" + translateX + "," + translateY + ")rotate(" + 45 + ", " + x + ", " + y + ")");
                });
        }
    };

    init();

    return {
        drawChart: drawChart
    };
})();
