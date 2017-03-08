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
    headerLine;

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
    width = parseInt($("#" + chartIdentifier).width(), 10);

    svg = d3.select("#" + chartIdentifier).append("svg")
      .attr("class", "d3chart")
      .attr("width", width);

    headerLine = svg.append("g");
    allBarsGroup = svg.append("g");
    trafficLightBars = svg.append("g");

    window.onresize = function () {
      // calcChartWidth();
      // resize();
    };

  };

  var initChartData = function (barchartData) {

    barchartData.series.forEach(function (series) {
      var labelUtil = OpenSpeedMonitor.ChartModules.ChartLabelUtil(series.data);
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

    actualBarchartData.series.forEach(function (currentSeries) {
      var seriesData = currentSeries.data;
      var unitName = currentSeries.dimensionalUnit;
      drawSeries(seriesData, unitName);
    })

    drawTrafficLight();

    var headerData = [{headerText: commonLabelParts}];
    drawHeader(headerData);

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

    var setWidthOfBarByData = function (selection) {
      selection
        .attr("width", function (d) {
          return xScale(d.value)
        })
    };
    var defineXScale = function () {
      var paddingBetweenLabelAndBar = barPadding;
      xScale
        .domain([0, absoluteMaxValue])
        .range([0, width - outerMargin * 2 - getMaxLabelWidth() - paddingBetweenLabelAndBar]);
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
      .text(function (d) {
        return d.label;
      })
      .each(function (elem) {
        labelWidths.push({identifier: elem.label + elem.measurand, width: this.getBBox().width})
      });

    defineXScale();

    singleBarGroupsEnter.append("rect")
      .attr("transform", "translate(" + parseInt(getMaxLabelWidth() + barPadding) + ", 0)")
      .attr("height", barHeight)
      .attr("width", initialBarWidth)
      .attr("fill", function (d, i) {
        return colorPalette(0);
      })
      .transition()
      .duration(transitionDuration)
      .call(setWidthOfBarByData);

    singleBarGroupsEnter.append("text")
      .attr("class", "d3chart-value")
      .attr("y", barHeight / 2)
      .attr("dx", -valueMarginInBar + getMaxLabelWidth()) //margin right
      .attr("dy", ".35em") //vertical align middle
      .attr("text-anchor", "end")
      .text(function (d) {
        return (Math.round(d.value)) + " " + unitName;
      })
      .attr("x", function (d) {
        var width = this.getBBox().width;
        return Math.max(width + valueMarginInBar, xScale(d.value));
      });

    // update ////////////////////////////////////////////////////////////////////////////////////

    singleBarGroups
      .transition()
      .duration(transitionDuration)
      .attr("transform", function (d, index) {
        var yOffset = (topMargin + index * (barHeight + barPadding) + barPadding);
        return "translate(" + outerMargin + "," + yOffset + ")";
      });

    defineXScale();

  };

  var getMaxLabelWidth = function () {
    return d3.max(labelWidths, function(d) { return d.width; });
  }

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
      .duration(transitionDuration)
      .attr("width", function (d) {
        return xScale(d.upperBoundary - d.lowerBoundary);
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
      .attr("x", function (d) {
        return xScale(d.upperBoundary - d.lowerBoundary) / 2;
      })
      .attr("text-anchor", "mid");

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

  };

  initChart();

  return {
    drawChart: drawChart
  };

});
