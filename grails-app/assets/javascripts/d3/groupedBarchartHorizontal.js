//= require /bower_components/d3/d3.min.js
//= require /d3/chartLabelUtil
//= require /d3/trafficLightDataProvider
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.ChartModules = OpenSpeedMonitor.ChartModules || {};

OpenSpeedMonitor.ChartModules.PageAggregationHorizontal = (function (chartIdentifier) {

    var svg,
        margin,
        barHeight = 40,
        barPadding = 10,
        valueMarginInBar = 4,
        colorPalette = d3.scale.category20(),
        width,
        height,
        absoluteMaxValue = 0,
        absoluteMaxYOffset = 0,
        labelWidths = [],
        barXOffSet,
        xScale = d3.scale.linear(),
        initialBarWidth = 20,
        countTrafficLightBar = 1,
        allBarsGroup,
        trafficLightBars,
        transitionDuration = 200,
        svgHeight,
        filterRules = {},
        actualBarchartData,
        commonLabelParts,
        headerLine,
        transformedData,
        groupings,
        valueLabelOffset = 5,
        unitScales,
        units;

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
        margin = {top: 40, right: 20, bottom: 20, left: 20};
            width = +svg.attr("width") - margin.left - margin.right;
            height = +svg.attr("height") - margin.top - margin.bottom;
            allBarsGroup = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        headerLine = svg.append("g");
        trafficLightBars = svg.append("g");
        createInFrontSwitch();
        window.onresize = function () {
            // calcChartWidth();
            // resize();
        };

    };

    var initChartData = function (barchartData) {

        barchartData.series.forEach(function (series) {
            var labelUtil = OpenSpeedMonitor.ChartModules.ChartLabelUtil(series.data, barchartData.i18nMap);
            series.data = labelUtil.getSeriesWithShortestUniqueLabels(true);
            commonLabelParts = labelUtil.getCommonLabelParts();
            series.data.sort(function (x, y) {
                return d3.descending(x.value, y.value);
            });
        });

        barchartData.originalSeries = clone(barchartData.series);

        actualBarchartData = barchartData;

    };
    
    var getLongestGroupName = function() {
        var longestString = "";
        $.each(transformedData, function (_,d) {
            if(d.label.length > longestString.length){
                longestString = d.label;
            }
        });
        return longestString;
    };

    var measureComponent = function(component, fn){
        var el = component.clone();
        el.css("visibility",'hidden');
        el.css("position",'absolut');
        el.appendTo('body');
        var result = fn(el);
        el.remove();
        return result
    };

    var drawAllBars = function () {
        transformData();
        barXOffSet = measureComponent($("<text>"+getLongestGroupName()+valueLabelOffset+"</text>"),function (d) {
            return d.width();
        });
        updateInFrontSwitch();
        drawBarsBesideEachOther();
        var headerData = [{headerText: commonLabelParts}];
        drawHeader(headerData);

    };

    var updateInFrontSwitch = function () {
      if(canBeInFront()){
          $("#inFrontButton").removeClass("disabled")
      }  else{
          $("#inFrontButton").addClass("disabled")
      }
    };
    
    var transformData = function () {
        units = {};
        var dataMap = {};
        $.each(actualBarchartData.series, function (i,series) {
            if(units[series.dimensionalUnit] === undefined){
                units[series.dimensionalUnit] = [];
            }
            $.each(series.data, function (_,datum) {
                var currentDatum = dataMap[datum.grouping];
                if(currentDatum === undefined ){
                    currentDatum = {};
                    currentDatum["bars"] = [];
                    currentDatum["grouping"] = datum.grouping;
                    currentDatum["label"] = datum.label;
                    dataMap[datum.grouping] = currentDatum;
                }
                var bar = {};
                bar["measurand"] = datum.measurand;
                bar["value"] = datum.value;
                bar["unit"] = series.dimensionalUnit;
                bar["grouping"] = datum.grouping;
                currentDatum["bars"].push(bar);
                units[series.dimensionalUnit].push(datum.value);
            })
        });
        groupings = Object.keys(dataMap);
        $.each(units, function (k,v) {
            v.sort(function(a, b) { return a > b ? 1 : -1});
        });
        transformedData = Object.keys(dataMap).map(function (key) {
            return dataMap[key];
        });

        $.each(transformedData, function (_,data) {
            data.bars.sort(function(a, b) { return a.value < b.value});
        })
    };

    var recreateOrder = function () {
        d3.selectAll(".barGroup").each(function(){d3.select(this).selectAll(".baar").sort(function(a, b) { return a.value < b.value})});
    };

    var canBeInFront = function () {
        return Object.keys(units).length == 1
    };

    var getAllMeasurands = function () {
        return $.unique(Array.prototype.concat.apply([], actualBarchartData.series.map(function (series) {
            return series.data.map(function (datum) {
                return datum.measurand
            })
        })));
    };

    var createInFrontSwitch = function () {
        if(!$("#besideSwitches").length){
            var input = $('<div class="btn-group pull-left" data-toggle="buttons" id="besideSwitches"></div>');
            var besideButton = $('<label class="btn btn-default active" id="besideButton"><input type="radio" name="mode" >Beside</label>');
            var inFrontButton = $('<label class="btn btn-default" id="inFrontButton"><input type="radio" name="mode" >In Front</label>');
            besideButton.click(function () {
                drawBarsBesideEachOther();
            });
            inFrontButton.click(function () {
                if(canBeInFront()){
                    drawBarsInFrontOfEachOther();
                }
            });
            besideButton.appendTo(input);
            inFrontButton.appendTo(input);
            //TODO move all css stuff in a file
            var css = {
                'padding': '5px 10px',
                'padding-top': '5px',
                'padding-right': '10px',
                'padding-bottom': '5px',
                'padding-left': '10px',
                'font-size': '12px',
                'line-height': '1.5'
            };
            inFrontButton.css(css);
            besideButton.css(css);
            input.prependTo($("#filter-dropdown-group"));
        }

    };

    var drawBarsInFrontOfEachOther = function () {
        var y0 = d3.scale.ordinal()
            .rangeRoundBands([0, height], .1);

        var seriesColorScale = d3.scale.category20();

        y0.domain(groupings);

        unitScales = createUnitScales();
        enterBarGroups();

        allBarsGroup.selectAll(".barGroup").attr("transform", function (d) {return "translate(0,"+y0(d.grouping)+")"});

        var y1 = d3.scale.ordinal();
        y1.domain([0]).rangeRoundBands([0,y0.rangeBand()]);

        appendMouseEvents()
        //Update Bar Container
        var bars = d3.selectAll(".baar");
        bars.transition()
            .attrTween("transform", function(d,i,a) { return d3.interpolateString(a,"translate("+barXOffSet+",0)"); });

        //Update actual bars
        var textX = y1.rangeBand()/2;
        d3.selectAll(".baar").each(function (bar) {

            //Update Rectangle Position and Size
            d3.select(this).select("rect").attr("fill", seriesColorScale(bar.measurand)).transition()
                .attr("width", unitScales[bar.unit](bar.value))
                .attr("height", y1.rangeBand());

            //Update Bar Label
            d3.select(this).select("text")
                .text(""+Math.round(bar.value)+" ms").transition()
                .attr("y", textX)
                .attr("x", unitScales[bar.unit](bar.value)-5);
        });

        //Update Group Labels
        var groupLabelY = y1.rangeBand()/2;
        d3.selectAll(".barGroup").each(function (d) {
            d3.select(this).select(".barGroupLabel").text(d.label).attr("y",groupLabelY);
        })


    };
    
    var drawBarsBesideEachOther = function () {
        var y0 = d3.scale.ordinal()
            .rangeRoundBands([0, height], .1);

        var y1 = d3.scale.ordinal();

        var seriesColorScale = d3.scale.category20();

        var measurands = getAllMeasurands();


        y0.domain(groupings);
        y1.domain(measurands).rangeRoundBands([0,y0.rangeBand()]);

        unitScales = createUnitScales();

        enterBarGroups();

        allBarsGroup.selectAll(".barGroup").attr("transform", function (d) {return "translate(0,"+y0(d.grouping)+")"});

        appendMouseEvents();
        //Update Bar Container
        var bars = d3.selectAll(".baar");
        bars.transition()
            .attrTween("transform", function(d) { return d3.interpolateString("translate("+barXOffSet+",0)", "translate("+barXOffSet+","+y1(d.measurand)+")"); });

        //Update actual bars
        var textX = y1.rangeBand()/2;
        d3.selectAll(".baar").each(function (bar) {

            //Update Rectangle Position and Size
            var barWidth = unitScales[bar.unit](bar.value);
            var rect = d3.select(this).select("rect");
            rect.attr("fill", seriesColorScale(bar.measurand)).transition()
                .attr("width", unitScales[bar.unit](bar.value))
                .attr("height", y1.rangeBand());

            //Update Bar Label
            var textLabel = d3.select(this).select("text");
            var text = ""+Math.round(bar.value)+" ms";
            var textTrans = textLabel.transition()
                .text(text)
                .attr("y", textX);
            if (!checkIfTextWouldFitRect(text,textLabel, barWidth)){
                textTrans
                .attr("visibility","hidden")
                .style("text-anchor","start")
            } else {
                textTrans.attr("x", unitScales[bar.unit](bar.value)-valueLabelOffset);
            }

        });

        //Update Group Labels
        d3.selectAll(".barGroup").each(function (d) {
            var groupLabelY = y1.rangeBand()*(d.bars.length)/2;
            d3.select(this).select(".barGroupLabel").text(d.label).attr("y",groupLabelY);
        })
    };

    var appendMouseEvents = function () {
        d3.selectAll(".baar")
            .on("mouseover", mouseOver())
            .on("mouseout", mouseOut());
    };

    var createUnitScales = function () {
        unitScales = {};
        $.each(units, function (unit,values) {
            var scale = d3.scale.linear().rangeRound([0, width-barXOffSet]);
            scale.domain([0,d3.max(values)]);
            unitScales[unit] = scale;
        });
        return unitScales;
    };

    var enterBarGroups = function () {
        var select1 = allBarsGroup.selectAll(".barGroup").data(transformedData);

        select1.enter().append("g").attr("class", "barGroup").append("text").attr("class", "barGroupLabel").attr("alignment-baseline","central");
        select1.exit().remove();
        select1.each(function (group) {
            var bars = d3.select(this).selectAll(".baar").data(group.bars);
            bars.enter().append("g").attr("class", "baar").each(function (d) {
                d3.select(this).append("rect");
                d3.select(this).append("text").style({
                    "font-weight": "bold",
                    "fill": "white",
                    "text-shadow": "0 0 3px #000000",
                    "text-anchor":"end",
                    "alignment-baseline":"central"
                })
            });
            bars.exit().remove();

        });
    };

    var mouseOver = function () {
        return function(hoverBar) {
            d3.selectAll(".baar").each(function (bar) {
                if(bar.measurand != hoverBar.measurand){
                    d3.select(this).select("rect").transition().duration(transitionDuration).style("opacity", 0.2);
                    d3.select(this).select("text").transition().duration(transitionDuration).style("opacity", 0);
                } else{
                    var rect = d3.select(this).select("rect");
                    var text = d3.select(this).select("text");
                    var rectSize = parseInt(rect.attr("width"));
                    var textSize = text.node().getBBox().width;

                    if(rectSize<textSize){
                        var position = unitScales[bar.unit](bar.value) + valueLabelOffset;
                        text.transition().duration(transitionDuration).attr("x", position).attr("visibility","visible").style("opacity", 1);
                    } else {
                        text.transition().duration(transitionDuration).style("opacity", 1)
                    }
                    rect.transition().duration(transitionDuration).style("opacity", 1);
                }
            });
        }
    };

    var mouseOut = function () {
        return function () {
            d3.selectAll(".baar").each(function (bar) {
                var text = d3.select(this).select("text");
                var rect = d3.select(this).select("rect");
                var rectSize = parseInt(rect.attr("width"));
                var textSize = text.node().getBBox().width;
                if(rectSize<textSize){
                    text.transition().duration(transitionDuration).attr("x", 0).attr("visibility","hidden");
                } else{
                    text.transition().duration(transitionDuration).style("opacity", 1);
                }
                rect.transition().duration(transitionDuration).style("opacity", 1);
            });
        };
    };


    var checkIfTextWouldFitRect = function (text, textLabel, barWidth) {
        var clone = $("<text>"+text+"</text>");
        clone.text(text);
        var width = measureComponent(clone, function (d) {
            return d.width()
        });
        return width<barWidth
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
                return "translate(" + parseInt(barXOffSet+margin.left) + ")";
            });

        //update

        headerText
            .transition()
            .duration(transitionDuration)
            .attr("transform", function (d) {
                return "translate(" + parseInt(barXOffSet+margin.left) + ")";
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
