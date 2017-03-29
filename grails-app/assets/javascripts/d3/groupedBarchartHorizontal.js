//= require /bower_components/d3/d3.min.js
//= require /d3/chartLabelUtil
//= require /d3/trafficLightDataProvider
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageAggregationHorizontal = (function (chartIdentifier) {

    var svg,
        topMargin = 50,
        outerMargin = 25,
        barHeight = 40,
        barPadding = 10,
        valueMarginInBar = 4,
        colorPalette = d3.scale.category20(),
        width,
        height,
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
        flattenedData;

    var drawChart = function (barchartData) {

        if (svg === undefined) {
            initChart();
        }

        initChartData(barchartData);

        // initFilterDropdown();
        drawAllBars();

    };

    var initChart = function () {

        $("#chart-card").removeClass("hidden");
        width = parseInt($("#" + chartIdentifier).width(), 10);

        svg = d3.select("#" + chartIdentifier).append("svg")
            .attr("class", "d3chart")
            .attr("height", 400)
            .attr("width", width);
        var margin = {top: 20, right: 20, bottom: 30, left: 40};
            width = +svg.attr("width") - margin.left - margin.right;
            height = +svg.attr("height") - margin.top - margin.bottom;
            allBarsGroup = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        headerLine = svg.append("g");
        trafficLightBars = svg.append("g");

        window.onresize = function () {
            // calcChartWidth();
            // resize();
        };

    };

    var initChartData = function (barchartData) {

        barchartData.series.forEach(function (series) {
            var labelUtil = OpenSpeedMonitor.ChartModules.ChartLabelUtil(series.data, barchartData.i18nMap);
            series.data = labelUtil.getSeriesWithShortestUniqueLabels();
            commonLabelParts = labelUtil.getCommonLabelParts();
            series.data.sort(function (x, y) {
                return d3.descending(x.value, y.value);
            });
        });

        barchartData.originalSeries = clone(barchartData.series);

        actualBarchartData = barchartData;

    };

    var drawAllBars = function () {
        var dataMap = {};
        var serieses = [];
        console.log(actualBarchartData);
        var measurandInSeries = {};
        var units = {};
        $.each(actualBarchartData.series, function (i,series) {
            serieses.push(i);
            if(units[series.dimensionalUnit] === undefined){
                units[series.dimensionalUnit] = [];
            }
            measurandInSeries[i] = [];
            $.each(series.data, function (_,datum) {
                var currentDatum = dataMap[datum.grouping];
                if(currentDatum === undefined ){
                    currentDatum = {};
                    currentDatum["bars"] = [];
                    currentDatum["grouping"] = datum.grouping;
                    dataMap[datum.grouping] = currentDatum;
                }
                var bar = {};
                bar["measurand"] = datum.measurand;
                bar["value"] = datum.value;
                bar["series"] = i;
                bar["unit"] = series.dimensionalUnit;
                bar["grouping"] = datum.grouping;
                currentDatum["bars"].push(bar);
                units[series.dimensionalUnit].push(datum.value);

                if($.inArray(datum.measurand, measurandInSeries[i]) == -1){
                    measurandInSeries[i].push(datum.measurand);
                }
            })
        });
        var groupings = Object.keys(dataMap);
        $.each(units, function (k,v) {
            v.sort(function(a, b) { return a > b ? 1 : -1});
        });
        var flattenedData = Object.keys(dataMap).map(function (key) {
            return dataMap[key];
        });
        var y0 = d3.scale.ordinal()
            .rangeRoundBands([0, height], .1);

        var y1 = d3.scale.ordinal();
        $.each(flattenedData, function (_,d) {
            console.log(d);
            d.bars.sort(function(a, b) { return a.value < b.value ? 1 : -1});
        });


        var seriesColorScale = d3.scale.category10();

        var measurands = $.unique(Array.prototype.concat.apply([], actualBarchartData.series.map(function (series) {
            return series.data.map(function (datum) {
                return datum.measurand
            })
        })));
        var map = {};
        var measurandMap = {};
        $.each(measurands, function (i,measurand) {
            measurandMap[measurand] = i;
        });

        $.each(serieses, function (_,series) {
            var domain = measurandInSeries[series].map(function (measurand) {
               return measurandMap[measurand];
            });
            if(domain.length==1) domain.push(domain[0]);
            map[series] = d3.scale.linear().domain(domain)
                .range([d3.rgb(seriesColorScale(series).toString()), d3.rgb(seriesColorScale(series).toString()).brighter()]);
        });

        var innerSeriesColorScale = function (series, measurand) {
            return map[series](measurandMap[measurand]);
        };

        y0.domain(groupings);
        y1.domain(serieses).rangeRoundBands([0,y0.rangeBand()]);

        var unitScales = {};
        $.each(units, function (unit,values) {
            var scale = d3.scale.linear().rangeRound([0, width]);
            scale.domain([0,d3.max(values)]);
            unitScales[unit] = scale;
        });
        var select1 = allBarsGroup.selectAll("g").data(flattenedData);

        select1.enter().append("g");
        select1.exit().remove();
        select1.each(function (group) {
            var rects = d3.select(this).selectAll("rect").data(group.bars);
                rects.enter().append("rect");
                rects.exit().remove();
        });

        allBarsGroup.selectAll("g").attr("transform", function (d) {return "translate(0,"+y0(d.grouping)+")"});

        console.log(flattenedData);

        var valuesMap = {};
        $.each(flattenedData, function (_,group) {
            var actualGroupMap =  {};
            valuesMap[group.grouping] = actualGroupMap;
            $.each(group.bars, function (_,bar) {
                var actualSeriesList = actualGroupMap[bar.series];
                if(actualSeriesList === undefined){
                    actualSeriesList = [];
                    actualGroupMap[bar.series] = actualSeriesList;
                }
                if($.inArray(actualSeriesList, bar.value) == -1) {
                    actualSeriesList.push(bar.value)
                }
            });
            $.each(actualGroupMap, function (_,v) {
                v.sort(function(a, b) { return a > b ? 1 : -1})
            })
        });

        var xFunction = function (d) {
            var valueList = valuesMap[d.grouping][d.series];
            var index = valueList.indexOf(d.value);
            if(index >0){
                return unitScales[d.unit](valueList[index-1]);
            } else{
                return 0;
            }
        };

        d3.selectAll("rect")
            .attr("x", function(d){
                return xFunction(d);
            })
            .attr("y", function(d) {return  y1(d.series); })
            .attr("width", function(d) { return  unitScales[d.unit](d.value) -xFunction(d) ; })
            .attr("height", function() { return y1.rangeBand() })
            .attr("name", function (d) {return d.measurand})
            .attr("text","abc")
            .attr("fill", function(d) {return innerSeriesColorScale(d.series,d.measurand); })
            .on("mouseover", function(hoverRect) {
                d3.selectAll("rect").each(function (rect) {
                    if(rect.measurand != hoverRect.measurand){
                        d3.select(this).transition()
                            .duration(150).style("opacity", 0.2).attr("width", function(d) { return  unitScales[d.unit](d.value) -xFunction(d) ; }).attr("x", function (d) {
                            return xFunction(d)
                        });
                    } else{
                        this.parentNode.appendChild(this);
                        d3.select(this).transition().duration(200).style("opacity", 1).attr("x",0).attr("width", function(d) { return  unitScales[d.unit](d.value) });
                    }
                });
            })
            .on("mouseout", function(d, i) {
                d3.selectAll("rect").each(function (rect) {
                    if(d.measurand === rect.measurand){
                        d3.select(this).transition().duration(200).attr("width", function(d) { return  unitScales[d.unit](d.value) -xFunction(d) ; }).attr("x", function (d) {
                            return xFunction(d)
                        });
                    } else{
                        d3.select(this).transition().duration(200).style("opacity", 1);
                    }

                })
            });

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
    }

    var filterCustomerJourney = function (journeyKey, desc) {

        actualBarchartData.series = clone(actualBarchartData.originalSeries);

        if (journeyKey && filterRules[journeyKey]) {

            // remove elements not in customer Journey from each series
            actualBarchartData.series.forEach(function (series) {
                series.data = series.data.filter(function (element) {
                    return filterRules[journeyKey].indexOf(element.grouping) >= 0;
                });
            });
            // remove series containing no data after first filter
            actualBarchartData.series = actualBarchartData.series.filter(function (series) {
                return series.data.length > 0;
            });
            // sort series
            actualBarchartData.series.forEach(function (series) {
                series.data.sort(function (x, y) {
                    return filterRules[journeyKey].indexOf(x.grouping) - filterRules[journeyKey].indexOf(y.grouping);
                });
            });

        } else {
            desc == desc || false;
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
        }
        drawAllBars()

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


    initChart();

    return {
        drawChart: drawChart
    };

});
