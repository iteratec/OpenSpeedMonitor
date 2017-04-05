//= require /bower_components/d3/d3.min.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.distributionChart = (function () {
    var svgContainer = null,
        chart = null,
        chartData = null,
        originalSeries = null,
        width = 600,
        height = 600,
        margin = {top: 10, right: 30, bottom: 50, left: 100},
        maxViolinWidth = 150,
        violinWidth = null,
        mainDataResolution = 30,
        interpolation = 'basis',
        toggleLogarithmicYAxisButton = null,
        dataTrimValue = null,
        logarithmicYAxis = false;

    var init = function () {
        svgContainer = document.querySelector("#svg-container");
        // TODO: Toggled out because the functionality has a bug right now (see Ticket [IT-1612])
        // toggleLogarithmicYAxisButton = document.querySelector("#toggle-logarithmic-y-axis");

        dataTrimValue = document.querySelector("#data-trim-value");
        dataTrimValue.addEventListener('change', function() {
            drawChart(chartData);
        });

        // TODO: Toggled out because the functionality has a bug right now (see Ticket [IT-1612])
        // toggleLogarithmicYAxisButton.addEventListener('click', function () {
        //     logarithmicYAxis = !logarithmicYAxis;
        //     drawChart(chartData);
        // });
    };

    var drawChart = function (distributionChartData) {
        chartData = distributionChartData;
        sortSeriesDataAscending();
        if (originalSeries === null) {
            originalSeries = chartData.series;

            dataTrimValue.value = getDomain()[1];

            // sort the violins after descending median as default
            sortByMedian();
            var sortedSeries = {};
            chartData.sortingRules.desc.forEach(function (trace) {
                sortedSeries[trace] = originalSeries[trace];
            });
            chartData.series = sortedSeries;
        }

        // delete old chart in same container
        d3.select(svgContainer).selectAll("svg").remove();
        initFilterDropdown(chartData.filterRules);

        // create the svg which contains the chart
        var svg = d3.select(svgContainer)
                    .append("svg")
                    .attr("class", "d3chart")
                    .attr("width", "100%")
                    .attr("height", height);

        violinWidth = calculateViolinWidth();

        var domain = getDomain();

        // create the scales for both axis
        var y = d3.scale.linear()
                  .range([height - margin.bottom, margin.top])
                  .domain(domain);

        var logY = d3.scale.log()
                     .range([height - margin.bottom, margin.top])
                     .domain(domain);

        var x = d3.scale.ordinal()
                  .range(xRange())
                  .domain(Object.keys(chartData.series));

        // create both axis
        var yAxis = null;
        if (logarithmicYAxis)
            yAxis = d3.svg.axis()
                .scale(logY)
                .orient('left')
                .tickFormat(d3.format('g'));
        else
            yAxis = d3.svg.axis()
                      .scale(y)
                      .orient("left");

        var xAxis = d3.svg.axis()
                      .scale(x)
                      .orient("bottom");

        // add the violins
        Object.keys(chartData.series).forEach( function (trace, i) {
            var traceData = chartData.series[trace].data;

            var g = svg.append("g")
                       .attr("class", "d3chart-violin")
                       .attr("transform", "translate(" + (i * violinWidth + margin.left) + ",0)");

            addViolin(g, traceData, height - margin.bottom, violinWidth, domain);
        });

        svg.append("g")
           .attr("class", "d3chart-axis d3chart-yAxis")
           .attr("transform", "translate(" + margin.left + ", 0)")
           .call(yAxis);

        svg.append("g")
           .attr("class", "d3chart-axis d3chart-xAxis")
           .attr("transform", "translate(" + margin.left + ", " + ( height - margin.bottom ) + ")")
           .call(xAxis);

        postDraw()
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
        var svgWidth = svgContainer.clientWidth - margin.left;
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
            var domainSize = curTrace[curTrace.length - 1] - curTrace[0];
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

    var addViolin = function (svg, traceData, height, violinWidth, domain) {
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
        var x = null;
        if (logarithmicYAxis)
            x = d3.scale.log()
                .range([height, 0])
                .domain(domain)
                .nice();
        else
            x = d3.scale.linear()
                  .range([height, 0])
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

        var gPlus = svg.append("g");
        var gMinus = svg.append("g");

        var violinColor = "#808080";

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
                var link = $("<li class='filterRule'><a href='#'><i class='fa fa-check filterInactive' aria-hidden='true'></i>" + filterRuleKey + "</a></li>");
                link.click(function (e) {
                    filterCustomerJourney(e.target.innerText);
                    toogleFilterCheckmarks(e.target);
                });
                link.insertAfter($customerJourneyHeader);
            }
        }

        $filterDropdownGroup.find("#all-violins-desc").click(function (e) {
            filterCustomerJourney(null, true);
            toogleFilterCheckmarks(e.target);
        });
        $filterDropdownGroup.find("#all-violins-asc").click(function (e) {
            filterCustomerJourney(null, false);
            toogleFilterCheckmarks(e.target);
        })
    };

    var filterCustomerJourney = function (journeyKey, desc) {
        var filteredAndSortedSeries = {};

        if (journeyKey) {
            var journey = chartData.filterRules[journeyKey];

            journey.forEach( function (trace) {
                filteredAndSortedSeries[trace] = originalSeries[trace];
            });
        } else {
            var sortingOrder = desc ? "desc" : "asc";
            chartData.sortingRules[sortingOrder].forEach( function (trace) {
                filteredAndSortedSeries[trace] = originalSeries[trace];
            });
        }

        chartData.series = filteredAndSortedSeries;
        drawChart(chartData);
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

        // rotate the labels if necessary
        rotateLabels()
    };

    var rotateLabels = function () {
        var rotate = false;
        d3.selectAll(".d3chart-xAxis text").each(function() {
            if (d3.select(this).node().getComputedTextLength() > violinWidth) {
                rotate = true;
            }
        });

        if (rotate) {
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
