/*
 * OpenSpeedMonitor (OSM)
 * Copyright 2014 iteratec GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

/**
 * Uses rickshaw to initialize, update and render the components of the graph.
 */
function RickshawGraphBuilder(args) {
    var self = this;

    this.divId;
    this.htmlProvider;
    this.graph;
    this.xAxis;
    this.yAxes = [];
    this.slider;
    this.legend;
    this.dataLabelsActivated = false;
    this.dataLabelsHaveBeenAdded = false;

    this.initialize = function (args) {
        if ((args.hasOwnProperty("dataLabelsActivated")) && (args.dataLabelsActivated == true)) { //display dataLabels
            this.dataLabelsActivated = true;
        }

        self.autoResize = args.width < 0 || args.width == "auto";
        self.divId = args.divId;
        args.series = self.composeSeries(args.data);

        self.htmlProvider = new HtmlProvider(args);
        args.htmlProvider = self.htmlProvider;

        self.initializeGraph(args);
        args.graph = self.graph;

        self.xAxis = new XAxis(args);
        self.initializeYAxes(args);
        self.initializeHoverDetail();
        self.initializeLegend();
        if (args.hasOwnProperty("annotations")) { //display annotations
            self.initializeAnnotator(args.annotations);
        }
        self.initializeSlider();
        self.graph.onUpdate(self.update);
        self.graph.render();
        self.updateTitle(args.title);
        self.addDataLabels();

        new ChartAdjuster(self, args);
        new ChartExporter(args);
        self.updateBorders({
            measurandGroupName: this.measurandGroup,
            bottom: args.bottom,
            top: args.top
        });
        self.initializeEventListeners();
    };

    this.addDataLabels = function () {
        if (($('#to-enable-label').is(':checked')) && (!this.dataLabelsHaveBeenAdded)) {
            if ($(".pointMarker").length < 1) {
                //activate pointMarker
                $("#to-enable-marker").trigger('click');
            }
            var rememberLastRowColor = "";
            var actualIndex = 0;
            $(".pointMarker").each(function (index) {
                var percentage = 0;
                var currentMarkerColor = rgb2hex($(this).css("border-top-color"));
                if (currentMarkerColor != rememberLastRowColor) {
                    rememberLastRowColor = currentMarkerColor;
                    actualIndex = 0;
                }
                self.graph.series.forEach(function (series) {
                    if (currentMarkerColor === series.color) {
                        //get args.series.ROW.data.INDEX.y * 100 rounded
                        if (!/undef/i.test(typeof series.data[actualIndex])) {
//              percentage =  Math.round((series.data[index].y)*100)/100;
                            percentage = parseFloat(series.data[actualIndex].y).toFixed(2);
                        }
                        //end loop
                        return false;
                    }
                });
                if (percentage > 0) {
                    var totalHeight = $(this).parent().height();
                    var distanceTop = $(this).css("top").replace(/[^-\d\.]/g, '');
                    //calculate data, round to 2 digits
                    //        var percentage = Math.round((100 - ((distanceTop * 100) / totalHeight))*100)/100;
                    //display data
                    $(this).parent().append("<div class='dataLabel' style='top:" + (parseInt($(this).css('top'), 10) - 5) + "px;left:" + (parseInt($(this).css('left'), 10) - 9) + "px;height:100px;width:100px;font-size: 13pt;font-weight: bold;color: #b3b3b3;cursor: default;fill: #b3b3b3;'>" + percentage + "</div>");
                }
                actualIndex++;
            });
            this.dataLabelsHaveBeenAdded = true;
        }
    };

    this.updateDataLabels = function () {
        //remove labels
        $(".dataLabel").each(function (index) {
            $(this).remove();
        });
        //re-add labels
        this.dataLabelsHaveBeenAdded = false;
        self.addDataLabels();
    };

    this.update = function () {
        self.xAxis.updateXAxis();
        self.removeGrid();
        self.updateYAxes();
        self.updateDataLabels();
    };

    this.updateBorders = function (args) {
        self.graph.updateBorders(args);
    };

    this.updateSize = function (args) {

        if (args.height == "auto") {
            args.height = self.htmlProvider.HEIGHT_OF_CHART;
        } else {
            args.height = parseInt(args.height);
        }

        if (args.width == "auto" || args.width == -1) {
            args.width = $(window).width() - 145;
        } else {
            args.width = parseInt(args.width);
        }

        $("#rickshaw_main").width(args.width);
        var widthOfChartSvg = $(self.graph.element).width();

        // set height of html components
        $("#rickshaw_chart_title").width(args.width);
        $(".rickshaw_y-axis_left").height(args.height);
        $(".rickshaw_y-axis_right").height(args.height);
        $("#rickshaw_y-axes_right").height(args.height);
        $("#rickshaw_chart").height(args.height);
        $("#rickshaw_addons").width(args.width - 70);
        $("#rickshaw_addons ul").width(args.width - 70);
        $("#rickshaw_timeline").width(args.width - 60);
        $("#rickshaw_slider").width(args.width - 70);
        $("#rickshaw_range_slider_preview_container").width(args.width - 70);
        $("#rickshaw_range_slider_preview").width(args.width - 70);
        $("#rickshaw_x-axis").width(args.width - 60);
        $(".x_axis_d3").attr("width", args.width);

        self.graph.configure({
            width: widthOfChartSvg,
            height: args.height
        });
        self.graph.update();
        self.graph.render();
    };

    this.updateTitle = function (title) {
        $("#rickshaw_chart_title").html(title);
    };

    this.updateAliases = function () {
        $(".label").each(function () {
            var originName = $(this).attr("data-origin-name");
            var graphNameAlias = $("#graphAlias_" + makeValidSelector(originName));
            if (graphNameAlias != undefined && graphNameAlias.length > 0) {
                var alias = graphNameAlias.find("#alias").val();
                $(this).html(alias);
            } else {
                $(this).html(originName);
            }
        });
        self.calculateLegendColumnWidth();
    };

    this.calculateLegendColumnWidth = function () {
        var newMaxWidth = -1;
        $(".line").each(function () {
            var swatchWidth = $(this).find(".swatch").width();
            var width = $(this).find(".label").width() + swatchWidth + 12;
            if(width>newMaxWidth) newMaxWidth = width;
        });
        $(".rickshaw_legend > ul").css({"column-width":  newMaxWidth+"px"});
    };

    this.updateYAxes = function () {
        self.yAxes.forEach(function (axis) {
            // update label text
            var containerOfYAxis = $(axis.element).parent();
            var containerOfLabel = containerOfYAxis.children("div");
            var measurandGroup = self.graph.measurandGroupsManager
                .getMeasurandGroup(axis.measurandGroup);
            if (measurandGroup.label) {
                containerOfLabel.html(measurandGroup.label);
            }

            // update label position
            var RESULTING_LEFT_VALUE = 0;
            if (containerOfLabel.attr("class") == "rickshaw_y-axis_left_label") {
                RESULTING_LEFT_VALUE = 25;
            } else {
                RESULTING_LEFT_VALUE = 40;
            }
            var width = containerOfLabel.width();
            var left = RESULTING_LEFT_VALUE - Math.ceil(width / 2);
            containerOfLabel.css("left", left)

            // update opacity
            var activeMeasurandGroups = self.graph.getActiveMeasurandGroups()
            var axisContainer = $(axis.element).parent();
            if ($.inArray(axis.measurandGroup, activeMeasurandGroups) == -1) {
                axisContainer.addClass("disabledYAxis");
            } else {
                axisContainer.removeClass("disabledYAxis");
            }
        });
    };

    this.updateColorsOfSeries = function (args) {
        /*
         * args ist ein assoziatives Array der Form args[series.name] = Farbe
         * Farbe ist ein String: Raute + 6 Hex Werte (z.B. "#000000" für Schwarz)
         */
        self.graph.series.forEach(function (series) {
            var newColor = args[series.name];
            if (newColor) {
                series.color = newColor;

                // update legend
                $(".label").each(function () {
                    if ($(this).attr("data-origin-name") == series.name || $(this).val() == series.name) {
                        $(this).closest(".line").find(".swatch").css("background-color", newColor);
                    }
                });

            }
        });

        self.graph.render();
        // TODO Slider.Preview zeigt dennoch die alten Farben an
        self.initializeSlider();
    };

    this.updateDrawPointMarkers = function (drawPointMarkers) {
        if (drawPointMarkers) {
            self.graph.drawPointMarkers = drawPointMarkers
            self.graph.render();
        } else {
            $("#rickshaw_chart > .pointMarker").remove();
        }
    };

    this.updateDrawPointLabels = function (drawPointLabels) {
        $(".dataLabel").each(function (index) {
            $(this).remove();
        });
        this.dataLabelsHaveBeenAdded = false;
        if (drawPointLabels) {
            self.addDataLabels();
        }
    };

    this.initializeGraph = function (args) {
        self.graph = new Rickshaw.Graph({
            element: document.getElementById("rickshaw_chart"),
            width: $("#rickshaw_chart").width(),
            height: $("#rickshaw_chart").height(),
            renderer: 'line',
            interpolation: 'linear',
            series: args.series,
            NUMBER_OF_YAXIS_TICKS: args.NUMBER_OF_YAXIS_TICKS,
            drawPointMarkers: args.drawPointMarkers,
            drawPointLabels: args.drawPointLabels
        });
    };

    this.initializeHoverDetail = function () {
        var xFormatter = function (x) {
            return new Date(x * 1000).toLocaleString();
        };
        var hoverDetail = new Rickshaw.Graph.HoverDetail({
            xFormatter: xFormatter,
            graph: self.graph
        });
        hoverDetail.formatter = function (activePoint, pointsAtSameTimepoint, formattedX, testingAgent) {
            // create html content for the hover detail table
            var pointData = "";
            pointsAtSameTimepoint.forEach(function (point) {
                // get the correct scale depending on the yAxis
                var scale = $.grep(self.yAxes, function (e) {
                    return e.measurandGroup == point.series.measurandGroup;
                })[0];
                // update names if they are customized
                var name = point.name;
                var aliasDiv = $(makeValidSelector("#graphAlias_" + name));
                if (aliasDiv.length >= 1) {
                    name = aliasDiv.find("#alias").val();
                }
                // highlight the value of the active point
                var optionalHighlighting = "<tr>";
                if (point == activePoint) {
                    optionalHighlighting = "<tr class=\"highlighted_detail_value\">";
                }
                // insert values in the table
                pointData += optionalHighlighting + "<td>" + name + ": " + "</td>" +
                    "<td>" + "<i class=\"fas fa-circle\" style=\"color:" + point.series.color + "\"></i> " +
                    scale.scale.invert(point.value.y).toFixed(2) + "</td>" + "</tr>";
            });


            return "<table border=\"0\" class=\"chart-tiptext\">" +
                "<tr>" + "<td>Timestamp: </td>" + "<td>" + formattedX + "</td>" + "</tr>" +
                pointData +
                "<tr>" + "<td>Test agent: </td>" + "<td>" + testingAgent + "</td>" + "</tr>" +
                "</table>";
        };
    };

    this.initializeAnnotator = function (args) {
        var annotator = new Rickshaw.Graph.Annotate({
            graph: self.graph,
            element: document.getElementById('rickshaw_timeline')
        });
        if (args) {
            for (index = 0; index < args.length; ++index) {
                annotator.add(args[index].x, args[index].text);
            }
            annotator.update();
        }
    };

    this.initializeLegend = function () {
        self.legend = new Rickshaw.Graph.Legend({
            graph: self.graph,
            element: document.getElementById('rickshaw_legend')
        });

        new Rickshaw.Graph.Behavior.Series.Toggle({
            graph: self.graph,
            legend: self.legend
        });
    };

    this.initializeSlider = function () {
        self.slider = new Rickshaw.Graph.RangeSlider.Preview({
            graph: self.graph,
            element: document.querySelector("#rickshaw_slider")
        });
    };

    this.initializeYAxes = function (args) {
        var measurandGroups = args.graph.measurandGroupsManager.measurandGroups;

        for (var i = 0; i < measurandGroups.length; i++) {

            var id_prefix, orientation;
            if (i == 0) {
                orientation = "left";
            } else {
                orientation = "right";
            }

            var scale = d3.scale.linear().domain([0, 1]);
            var axis = new Rickshaw.Graph.Axis.Y.Scaled({
                element: document.getElementById("rickshaw_yAxis_" + i),
                graph: args.graph,
                orientation: orientation,
                scale: scale,
                tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
                measurandGroup: measurandGroups[i].name,
                tickValues: measurandGroups[i].computeTickValues()
            });
            self.yAxes.push(axis);
        }
    };

    this.removeGrid = function () {
        // delete x-axis-grid
        $("g.x_grid_d3").remove();

        // delete x-axis border
        $("#rickshaw_x-axis > svg > g > path").remove();

        // delete left y-axis borders
        $(".rickshaw_graph.y_axis > g > path").remove();

        // delete all y-axis grids except one
        var grids = $("#rickshaw_graphic_svg > .y_grid");
        var numberOfGrids = grids.length;

        grids.each(function (index) {
            if (index < numberOfGrids - 1) {
                if (typeof this.remove == 'function') {
                    this.remove();
                }
            }
        });
    };

    this.composeSeries = function (data) {
        var measurandGroups = [];
        var series = [];
        var scale = d3.scale.linear().domain([0, 1]);
        var palette = new Rickshaw.Color.Palette({
            scheme: 'iteratec'
        });
        //Data has to be ordered to ensure that the colors are not random
        var dataTupleList = [];
        for (var key in data) dataTupleList.push([key, data[key]]);
        dataTupleList.sort(function (a, b) {
            a = a[1].name;
            b = b[1].name;

            return a < b ? -1 : (a > b ? 1 : 0);
        });

        for (var i = 0; i < dataTupleList.length; i++) {
            var entry = {
                color: palette.color(),
                data: dataTupleList[i][1].data,
                name: dataTupleList[i][1].name,
                scale: scale,
                measurandGroup: dataTupleList[i][1].measurandGroup,
                label: dataTupleList[i][1].yAxisLabel
            };
            series.push(entry);

            // count measurand groups
            if ($.inArray(dataTupleList[i][1].measurandGroup, measurandGroups) == -1) {
                measurandGroups.push(dataTupleList[i][1].measurandGroup);
            }
        }
        series.numberOfMeasurandGroups = measurandGroups.length;
        return series;
    };

    this.initializeEventListeners = function () {
        $(window).on('resize', function () {
            if (!self.autoResize) {
                return;
            }
            self.updateSize({
                width: 'auto',
                height: 'auto'
            });
        });
    };

    this.initialize(args);
    self.updateSize(args);
}

function XAxis(args) {
    var self = this;
    this.graph;
    this.rickshawXAxis;
    this.NUMBER_OF_TICKS;

    this.initialize = function (args) {
        self.graph = args.graph;

        self.rickshawXAxis = new Rickshaw.Graph.Axis.X({
            graph: args.graph,
            orientation: 'bottom',
            element: document.getElementById('rickshaw_x-axis')
        });
    };

    this.updateXAxis = function () {
        self.setNumberOfTicks();
        self.setTickValueLabels();
        self.rickshawXAxis.render();
        self.formatXAxisLabels();
    };

    this.setNumberOfTicks = function () {
        var width = self.graph.width;
        self.NUMBER_OF_TICKS = Math.floor(width / 80);
    };

    this.setTickValueLabels = function () {
        var DAYS = [];
        var MONTHS = [];
        if (document.documentElement.lang == "de") {
            DAYS = ["Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag",
                "Samstag"];
            MONTHS = ["Januar", "Februar", "März", "April", "Mai", "Juni",
                "Juli", "August", "September", "Oktober", "November",
                "Dezember"];
        } else {
            DAYS = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
                "Saturday"];
            MONTHS = ["January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November",
                "December"];
        }
        var xValuesRange = self.graph.renderer.domain().x;
        var minDate = new Date(xValuesRange[0] * 1000);
        var maxDate = new Date(xValuesRange[1] * 1000);
        var timeDiff = Math.abs(maxDate.getTime() - minDate.getTime());
        var diffMinutes = Math.ceil(timeDiff / (1000 * 60));
        var diffHours = Math.ceil(timeDiff / (1000 * 60 * 60));
        var diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24));
        var diffMonths = Math.ceil(timeDiff / (1000 * 3600 * 24 * 30));
        var diffYears = Math.ceil(timeDiff / (1000 * 3600 * 24 * 30 * 12));

        var format, tickValues = [];

        var regexS = "[\\?&]selectedInterval=([^&#]*)";
        var regex = new RegExp(regexS);
        var resultsSelectedInterval = regex.exec(window.location.href);

        regexS = "[\\?&]aggrGroup=([^&#]*)";
        regex = new RegExp(regexS);
        var resultsAggrGroup = regex.exec(window.location.href);

        // measuredEvent // hourly
        // daily_page daily_shop // daily
        // page shop // weekly

        if (resultsAggrGroup != null) {
            switch (resultsAggrGroup[1]) {
                case "measuredEvent":
                    resultsSelectedInterval = [];
                    resultsSelectedInterval[1] = 60;
                    break;
                case "daily_page":
                case "daily_shop":
                    resultsSelectedInterval = [];
                    resultsSelectedInterval[1] = 1440;
                    break;
                case "page":
                case "shop":
                    resultsSelectedInterval = [];
                    resultsSelectedInterval[1] = 10080;
            }
        }

        if (window.location.href.indexOf("wideScreenDiagramMontage") > -1) {
            resultsSelectedInterval = [];
            resultsSelectedInterval[1] = 10080;
        }

        if (( resultsSelectedInterval != null ) && (resultsSelectedInterval[1] != -1)) {
//    aggregate weekly (if applicable)
            if (resultsSelectedInterval[1] == 10080) {//weekly
                var dayName = "Friday";
                regexS = "[\\?&]dayForLabel=([^&#]*)";
                regex = new RegExp(regexS);
                var resultsDayForLabel = regex.exec(window.location.href);
                if ((resultsDayForLabel != null) && (DAYS.indexOf(resultsDayForLabel[1]) > -1)) {
                    dayName = resultsDayForLabel[1];
                }
                tickValues = self.getDaysInRange(minDate, maxDate);
                var index = tickValues.length;
                while (index--) {
                    var date = new Date(tickValues[index] * 1000);
                    var dayNameToCheck = DAYS[date.getDay()];
                    if (dayNameToCheck != dayName) { // purge days not meeting requirement
                        tickValues.splice(index, 1);
                    }
                }
                // re-add tickValues if list is empty
                if (tickValues.length == 0) {
                    tickValues = self.getDaysInRange(minDate, maxDate);
                }
                format = function (n) {
                    var date = new Date(n * 1000);
                    var dayName = DAYS[date.getDay()];
                    var dateLabel = self.getDateISO(date);
                    return dayName + "_nl_" + dateLabel;
                }
            }
//    aggregate daily (if applicable) or if aggregate is weekly and tickValues empty
            if ((resultsSelectedInterval[1] == 1440) || ((resultsSelectedInterval[1] == 10080) && (tickValues.length == 0))) {// daily
                tickValues = self.getDaysInRange(minDate, maxDate);
                format = function (n) {
                    var date = new Date(n * 1000);
                    var dayName = DAYS[date.getDay()];
                    var dateLabel = self.getDateISO(date);
                    return dayName + "_nl_" + dateLabel;
                }
            }
//    aggregate hourly (if applicable) or if aggregate is daily/weekly and tickValues empty
            if ((resultsSelectedInterval[1] == 60) || (tickValues.length == 0)) {
                tickValues = self.getHoursInRange(minDate, maxDate);
                format = function (n) {
                    var date = new Date(n * 1000);
                    var time = self.getTimeString(date);
                    var dateLabel = self.getDateISO(date);
                    return time + "_nl_" + dateLabel;
                }
            }
            if (tickValues.length == 0) {
                tickValues = self.getMinutesInRange(minDate, maxDate);
                format = function (n) {
                    var date = new Date(n * 1000);
                    var time = self.getTimeString(date);
                    var dateLabel = self.getDateISO(date);
                    return time + "_nl_" + dateLabel;
                }
            }
            if (tickValues.length == 0) {
                tickValues = self.getDefaultTickValues(minDate, maxDate);
                format = function (n) {
                    var date = new Date(n * 1000);
                    var time = self.getTimeString(date);
                    var dateLabel = self.getDateISO(date);
                    return time + "_nl_" + dateLabel;
                }
            }
        } else {
            if (diffYears >= self.NUMBER_OF_TICKS) {
                tickValues = self.getYearsInRange(minDate, maxDate);
                format = function (n) {
                    var date = new Date(n * 1000);
                    var year = date.getFullYear();
                    var dateLabel = self.getDateISO(date);
                    return year + "_nl_" + dateLabel;
                }
            } else if (diffMonths >= self.NUMBER_OF_TICKS) {
                tickValues = self.getMonthsInRange(minDate, maxDate);
                format = function (n) {
                    var date = new Date(n * 1000);
                    var monthName = MONTHS[date.getMonth()];
                    var dateLabel = self.getDateISO(date);
                    return monthName + "_nl_" + dateLabel;
                }
            } else if (diffDays >= self.NUMBER_OF_TICKS) {
                tickValues = self.getDaysInRange(minDate, maxDate);
                format = function (n) {
                    var date = new Date(n * 1000);
                    var dayName = DAYS[date.getDay()];
                    var dateLabel = self.getDateISO(date);
                    return dayName + "_nl_" + dateLabel;
                }
            } else if (diffHours >= self.NUMBER_OF_TICKS) {
                tickValues = self.getHoursInRange(minDate, maxDate);
                format = function (n) {
                    var date = new Date(n * 1000);
                    var time = self.getTimeString(date);
                    var dateLabel = self.getDateISO(date);
                    return time + "_nl_" + dateLabel;
                }
            } else if (diffMinutes >= self.NUMBER_OF_TICKS) {
                tickValues = self.getMinutesInRange(minDate, maxDate);
                format = function (n) {
                    var date = new Date(n * 1000);
                    var time = self.getTimeString(date);
                    var dateLabel = self.getDateISO(date);
                    return time + "_nl_" + dateLabel;
                }
            } else {
                tickValues = self.getDefaultTickValues(minDate, maxDate);
                format = function (n) {
                    var date = new Date(n * 1000);
                    var time = self.getTimeString(date);
                    var dateLabel = self.getDateISO(date);
                    return time + "_nl_" + dateLabel;
                }
            }
        }

        self.rickshawXAxis.tickFormat = format;

        // set number of ticks
        var tickValuesResult = [];
        var step = Math.ceil(tickValues.length / self.NUMBER_OF_TICKS);
        var index = 0;
        while (index <= tickValues.length) {
            if (tickValues[index]) {
                tickValuesResult.push(tickValues[index]);
            }
            index += step;
        }

        self.rickshawXAxis.tickValues = tickValuesResult;
    };

    this.getYearsInRange = function (minDate, maxDate) {
        var years = [];
        var date = new Date(minDate.getTime());
        date.setMilliseconds(0);
        date.setSeconds(0);
        date.setMinutes(0);
        date.setHours(0);
        date.setDate(1);
        date.setMonth(0);

        while (date < maxDate) {
            // increase date by one year
            date = new Date(date.getTime());
            date.setYear(date.getFullYear() + 1);

            if (date < maxDate) {
                years.push(date.getTime() / 1000);
            }
        }
        return years;
    };

    this.getMonthsInRange = function (minDate, maxDate) {
        var months = [];
        var date = new Date(minDate.getTime());
        date.setMilliseconds(0);
        date.setSeconds(0);
        date.setMinutes(0);
        date.setHours(0);
        date.setDate(1);

        while (date < maxDate) {
            date = new Date(date.getTime());
            self.increaseMonth(date);
            if (date < maxDate) {
                months.push(date.getTime() / 1000);
            }
        }
        return months;
    };

    this.getDaysInRange = function (minDate, maxDate) {
        var days = [];
        var date = new Date(minDate.getTime());
        var maxDateAsTime = new Date(maxDate.getTime());
        date.setMilliseconds(0);
        date.setSeconds(0);
        date.setMinutes(0);
        date.setHours(0);

        while (date < maxDateAsTime) {
            date = new Date(date.getTime());
            self.increaseDay(date);
            if (date < maxDateAsTime) {
                days.push(date.getTime() / 1000);
            }
        }
        return days;
    };

    this.getHoursInRange = function (minDate, maxDate) {
        var hours = [];
        var date = new Date(minDate.getTime());
        date.setMilliseconds(0);
        date.setSeconds(0);
        date.setMinutes(0);

        while (date < maxDate) {
            date = new Date(date.getTime());
            self.increaseHour(date);
            if (date < maxDate) {
                hours.push(date.getTime() / 1000);
            }
        }
        return hours;
    };

    this.getMinutesInRange = function (minDate, maxDate) {
        var minutes = [];
        var date = new Date(minDate.getTime());
        date.setMilliseconds(0);
        date.setSeconds(0);

        while (date < maxDate) {
            date = new Date(date.getTime());
            self.increaseMinute(date);
            if (date < maxDate) {
                minutes.push(date.getTime() / 1000);
            }
        }
        return minutes;
    };

    this.getDefaultTickValues = function (minDate, maxDate) {
        var dif = maxDate.getTime() - minDate.getTime();
        var tickValue = Math.floor(dif / self.NUMBER_OF_TICKS);
        var tickValues = [];

        for (var i = 0; i < self.NUMBER_OF_TICKS; i++) {
            var tick = (minDate.getTime()) + (i * tickValue);
            tickValues.push(tick / 1000);
        }
        return tickValues;
    };

    this.formatXAxisLabels = function () {
        // Converts the strings "_nl_" into new lines
        d3.select("#rickshaw_x-axis > svg").selectAll("g g text").each(
            function (d) {
                var el = d3.select(this);
                var words = el.text().split('_nl_');
                el.text('');

                for (var i = 0; i < words.length; i++) {
                    var tspan = el.append('svg:tspan').text(words[i]);
                    if (i > 0)
                        tspan.attr('x', 0).attr('dy', '15');
                }
            });
    };

    this.getDateISO = function (date) {
        return date.getFullYear() + "-" + (date.getMonth() + 1) + "-"
            + date.getDate();
    };

    this.getTimeString = function (date) {
        var result = date.getHours();
        if (date.getMinutes() < 10) {
            result = result + ":0" + date.getMinutes();
        } else if (date.getMinutes() == 0) {
            result = result + ":00";
        } else {
            result = result + ":" + date.getMinutes();
        }
        return result;
    };

    this.increaseMonth = function (date) {
        if (date.getMonth() == 11) {
            date.setYear(date.getFullYear() + 1);
            date.setMonth(0);
        } else {
            date.setMonth(date.getMonth() + 1);
        }
    };

    this.increaseDay = function (date) {
        var month = date.getMonth() + 1;
        var increase = function (date, daysOfMonth) {
            if (date.getDate() == daysOfMonth) {
                date.setDate(1);
                self.increaseMonth(date);
            } else {
                date.setDate(date.getDate() + 1);
            }
        };

        if (month == 2) {
            if (date.getFullYear() % 4 == 0) {
                increase(date, 29);
            } else {
                increase(date, 28);
            }
        } else if ($.inArray(month, [1, 3, 5, 7, 8, 10, 12]) >= 0) {
            increase(date, 31);
        } else {
            increase(date, 30);
        }
    };

    this.increaseHour = function (date) {
        if (date.getHours() == 23) {
            date.setHours(0);
            self.increaseDay(date);
        } else {
            date.setHours(date.getHours() + 1);
        }
    };

    this.increaseMinute = function (date) {
        if (date.getMinutes() == 59) {
            date.setMinutes(0);
            self.increaseHour(date);
        } else {
            date.setMinutes(date.getMinutes() + 1);
        }
    };

    this.initialize(args);
}

function YValueFormatter() {
    var self = this;

    this.getFormatterForSpecificMeasurandGroup = function (measurandGroup) {
        var result;
        if (measurandGroup.name == "LOAD_TIMES") {
            result = self.getFormatterForLoadTimes(measurandGroup);
        } else if (measurandGroup.name == "REQUEST_COUNTS") {
            result = self.getFormatterForRequestCounts();
        } else if (measurandGroup.name == "REQUEST_SIZES") {
            result = self.getFormatterForRequestSizes(measurandGroup);
        } else if (measurandGroup.name == "PERCENTAGES") {
            result = self.getFormatterForPercentages();
        } else if (measurandGroup.name == "UNDEFINED") {
            result = self.getDefaultFormatter();
        } else {
            result = self.getDefaultFormatter();
        }

        return result;
    };

    this.getFormatterForLoadTimes = function (measurandGroup) {
        var result = {};
        var dif = measurandGroup.currentScale.tickMax
            - measurandGroup.currentScale.tickMin;
        dif = Math.abs(dif / 1000);
        if (dif >= measurandGroup.NUMBER_OF_YAXIS_TICKS) {
            result.forAxis = function (y) {
                return y / 1000;
            };
            result.forAxis.unit = "[s]";
            result.forHoverDetail = function (y) {
                return parseFloat(y / 1000).toFixed(3);
            };
        } else {
            result.forAxis = function (y) {
                return y;
            };
            result.forAxis.unit = "[ms]";
            result.forHoverDetail = function (y) {
                return parseFloat(y).toFixed(0);
            };
        }
        return result;
    };

    this.getFormatterForRequestSizes = function (measurandGroup) {
        var result = {};
        var dif = measurandGroup.currentScale.tickMax
            - measurandGroup.currentScale.tickMin;
        dif = Math.abs(dif);

        if (dif >= measurandGroup.NUMBER_OF_YAXIS_TICKS) {
            result.forAxis = function (y) {
                return y;
            };
            result.forAxis.unit = "[kb]";
            result.forHoverDetail = function (y) {
                return parseFloat(y).toFixed(0);
            }
        } else {
            result.forAxis = function (y) {
                return y;
            };
            result.forAxis.unit = "[b]";
            result.forHoverDetail = function (y) {
                return parseFloat(y).toFixed(0);
            };
        }
        return result;
    };

    this.getFormatterForRequestCounts = function () {
        var result = {};
        result.forAxis = function (y) {
            return y;
        };
        result.forAxis.unit = "[c]";
        result.forHoverDetail = function (y) {
            return parseFloat(y).toFixed(0);
        };
        return result;
    };

    this.getFormatterForPercentages = function () {
        var result = {};
        result.forAxis = function (y) {
            return y;
        };
        result.forAxis.unit = "[%]";
        result.forHoverDetail = function (y) {
            return parseFloat(y).toFixed(2);
        };
        return result;
    };

    this.getDefaultFormatter = function () {
        var result = {};

        result.forAxis = function (y) {
            return y;
        };
        result.forAxis.unit = "";
        result.forHoverDetail = function (y) {
            return parseFloat(y).toFixed(0);
        };

        return result;
    }
}

function HtmlProvider(args) {
    var self = this;
    this.HEIGHT_OF_CHART;
    this.numberOfMeasurandGroups;

    this.initialize = function (args) {
        self.HEIGHT_OF_CHART = args.height;
        self.numberOfMeasurandGroups = args.series.numberOfMeasurandGroups;

        self._generateLeftYAxis();
        self._generateRightYAxes();
        self._setHeightOfChartContainer();
        self._setWidthOfHtmlComponents();
    };

    this._generateLeftYAxis = function () {
        var height = self.HEIGHT_OF_CHART;
        $("#rickshaw_yAxis_0").height(height).append(
            "<div class=\"rickshaw_y-axis_left_label\"> </div>");
    };

    this._generateRightYAxes = function () {
        var height = self.HEIGHT_OF_CHART;
        $("#rickshaw_y-axes_right").height(height);

        for (var i = 1; i < self.numberOfMeasurandGroups; i++) {
            var id = "rickshaw_yAxis_" + i;

            // y-axis
            $('<div>').attr({
                "id": id
            }).height(height).addClass("rickshaw_y-axis_right").appendTo(
                $("#rickshaw_y-axes_right"));

            // label
            $('<div>').addClass("rickshaw_y-axis_right_label").html("")
                .appendTo($("#" + id));
        }
    };

    this._setHeightOfChartContainer = function () {
        $("#rickshaw_chart").css({
            "height": self.HEIGHT_OF_CHART + "px"
        });
    };

    this._setWidthOfHtmlComponents = function () {
        var WIDTH_OF_SINGLE_YAXIS = $(".rickshaw_y-axis_left").width();

        var totalAvailableWidth = $("#rickshaw_main").width();
        var widthOfRightYAxis = WIDTH_OF_SINGLE_YAXIS;
        var widthOfLeftYAxis = WIDTH_OF_SINGLE_YAXIS;

        var numberOfRightYAxes = self.numberOfMeasurandGroups - 1;
        var totalWidthOfRightYAxes = numberOfRightYAxes * widthOfRightYAxis;
        var widthOfGraph = totalAvailableWidth - widthOfLeftYAxis
            - totalWidthOfRightYAxes;

        // container which contains all right y-axes
        $("#rickshaw_y-axes_right").css({
            "width": totalWidthOfRightYAxes + "px"
        });
        // y-axis on the right side
        $(".rickshaw_y-axis_right").css({
            "width": widthOfRightYAxis + "px"
        });

        // container which contains the chart svg
        $("#rickshaw_chart").css({
            "margin-left": widthOfLeftYAxis,
            "margin-right": totalWidthOfRightYAxes
        });

        // x-axis
        $("#rickshaw_x-axis").css({
            "margin-left": widthOfLeftYAxis,
        });

        // container which contains x-axis, slider and legend
        $("#rickshaw_addons").css({
            "margin-left": widthOfLeftYAxis + "px",
            "width": eval(parseInt(widthOfGraph) - 10) + "px"
        });
        $("#rickshaw_timeline").css({
            "margin-left": widthOfLeftYAxis + "px",
            "width": widthOfGraph + "px"
        });

        // container which contains the x-axis svg
        $("#rickshaw_x-axis").css({
            "width": widthOfGraph + "px"
        });

        // place the slider below the chart
        $("#rickshaw_slider").css({
            "width": eval(parseInt(widthOfGraph) + 10) + "px"
        });
    };
    this.initialize(args);
}

function ChartAdjuster(graphBuilder, args) {
    var self = this;

    this.initialize = function (graphBuilder, args) {
        self.graphBuilder = graphBuilder;
        self.registerEventHandlers();
        self.createYAxisAdjuster(args);
        self.addFunctionalityShowDataMarker();
        self.addFunctionalityShowDataLabels();
    };

    this.registerEventHandlers = function () {
        $('#adjustChartApply').on('click', function () {
            self.graphBuilder.updateTitle($('#dia-title').val());
            var success = self.updateAllYAxis();
            if (self.updateSize() && success) {
                $('#adjustChartModal').modal('hide');
            }
        });
        var aliasChildList = $("#graphAliasChildlist");
        aliasChildList.on("graphAliasChildsChanged", self.graphBuilder.updateAliases);
        aliasChildList.on("graphAliasColorChanged", function (event, nameAndColor) {
            self.graphBuilder.updateColorsOfSeries(nameAndColor);
        });
    };

    this.updateSize = function () {
        var diaWidth = $('#dia-width').val();
        var diaHeight = $('#dia-height').val();
        var maxWidth = 5000;
        var minWidth = 540;
        var maxHeight = 3000;
        var widthNumeric = $.isNumeric(diaWidth) &&
            parseInt(diaWidth) <= maxWidth &&
            parseInt(diaWidth) >= minWidth;
        var autoWidth = diaWidth < 0 || diaWidth == "auto";
        var heightNumeric = $.isNumeric(diaHeight) && parseInt(diaHeight) <= maxHeight;
        if ((widthNumeric || autoWidth) && (heightNumeric || diaHeight == "auto")) {
            self.graphBuilder.autoResize = autoWidth;
            self.graphBuilder.updateSize({
                width: diaWidth,
                height: diaHeight
            });
            return true
        } else {
            window
                .alert("Width and height of diagram must be numeric values. Maximum is 5.000 x 3.000 pixels, minimum width is 540 pixels.");
            return false
        }
    };

    this.createYAxisAdjuster = function (args) {
        var measurandGroups = args.graph.measurandGroupsManager.measurandGroups;
        var blankYAxisAdjuster = $("div.adjust_chart_y_axis");
        var parentContainer = $("#adjust_chart_y_axis_container");
        measurandGroups.forEach(function (mg) {
            var unit = mg.label.match(/\[.*]/);
            if (unit != null && unit[0] != null) unit = unit[0].replace("\[", "").replace("]", "");
            var yAxisAdjuster = blankYAxisAdjuster.clone();
            parentContainer.append(yAxisAdjuster);
            var yAxisAdjusterLabel = yAxisAdjuster.find("label");
            yAxisAdjusterLabel.html(yAxisAdjusterLabel.html() + mg.label.replace("[" + unit + "]", ""));

            yAxisAdjuster.find(".dia-y-axis-name").val(mg.name);

            var inputMin = yAxisAdjuster.find(".dia-y-axis-min");
            yAxisAdjuster.find(".minimumUnit").html(unit);
            yAxisAdjuster.find(".maximumUnit").html(unit);

            var inputMax = yAxisAdjuster.find(".dia-y-axis-max");
            inputMin.val("0");
            inputMax.val("auto");
        });
        blankYAxisAdjuster.remove();
    };

    this.updateAllYAxis = function () {
        var success = true;
        $("div.adjust_chart_y_axis").each(function (i, el) {
            success = self.updateOneYAxis(el) && success
        });
        return success;
    };

    this.updateOneYAxis = function (container) {
        container = $(container);
        var diaYAxisMin = container.find('.dia-y-axis-min').val();
        var diaYAxisMax = container.find('.dia-y-axis-max').val();

        var valid = true;
        if (diaYAxisMin != "auto" && !($.isNumeric(diaYAxisMin))
            && diaYAxisMax != "auto" && !($.isNumeric(diaYAxisMax))) {
            valid = false;
        }
        if (valid) {
            if (diaYAxisMax != "auto" && diaYAxisMin != "auto") {
                if (!(parseFloat(diaYAxisMax) > parseFloat(diaYAxisMin))) {
                    valid = false;
                }
            }
        }
        if (valid) {
            self.graphBuilder.updateBorders({
                measurandGroupName: container.find('.dia-y-axis-name').val(),
                bottom: diaYAxisMin,
                top: diaYAxisMax
            });
        } else {
            window.alert("Minimum and maximum of Y-Axis has to be \"auto\" or numeric values and maximum must be greater than minimum!");
        }
        return valid;
    };

    this.addFunctionalityShowDataMarker = function () {
        $('#to-enable-marker').on('change', function () {
            var toEnableMarkers = $(this).is(':checked');
            self.graphBuilder.updateDrawPointMarkers(toEnableMarkers);
        });
    };

    this.addFunctionalityShowDataLabels = function () {
        $('#to-enable-label').on('change', function () {
            var toEnableLabels = $(this).is(':checked');
            if (toEnableLabels && $(rickshawGraphBuilder.graph.series[0].data).length > 10000) {
                window.alert("Too many datapoints to show and label them!");
                $(this).prop('checked', false);
            } else {
                self.graphBuilder.updateDrawPointLabels(toEnableLabels);
            }
        });
    };

    this.initialize(graphBuilder, args);
}


function rgb2hex(rgb) {
    rgb = rgb.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);
    return "#" +
        ("0" + parseInt(rgb[1], 10).toString(16)).slice(-2) +
        ("0" + parseInt(rgb[2], 10).toString(16)).slice(-2) +
        ("0" + parseInt(rgb[3], 10).toString(16)).slice(-2);
}


function ChartExporter(args) {
    var self = this;

    this.initialize = function (args) {
        d3.select("#dia-save-chart-as-png").on("click", function () {

            window.scrollTo(0, 0);
            document.documentElement.style.overflow = 'hidden';  // firefox, chrome
            document.body.scroll = "no"; // ie only

            var opts = {
                lines: 15, // The number of lines to draw
                length: 20, // The length of each line
                width: 10, // The line thickness
                radius: 30, // The radius of the inner circle
                corners: 1, // Corner roundness (0..1)
                rotate: 0, // The rotation offset
                direction: 1, // 1: clockwise, -1: counterclockwise
                color: '#000', // #rgb or #rrggbb or array of colors
                speed: 1, // Rounds per second
                trail: 60, // Afterglow percentage
                shadow: true, // Whether to render a shadow
                hwaccel: false, // Whether to use hardware acceleration
                className: 'spinner', // The CSS class to assign to the spinner
                zIndex: 2e9, // The z-index (defaults to 2000000000)
                top: 'auto', // Top position relative to parent in px
                left: '50%' // Left position relative to parent in px
            };

            var origGraphElement = document.getElementsByClassName('graph')[0];
            var graphParent = origGraphElement.parentNode;
            origGraphElement.setAttribute('id', "originalGraph");

            // diagramm kopieren
            var cln = origGraphElement.cloneNode(true);
            var spinner = new Spinner(opts).spin(cln);
            cln.setAttribute('id', "workingCopy");
            cln.className = "graphDuplicate";

            graphParent.insertBefore(cln, graphParent.childNodes[0]);

            self.assignAllRelevantCssToStyleAttributes();

            origGraphElement.style.position = ('absolute');
            origGraphElement.style.marginTop = ('1500px');
            // namen in der kopie eindeutig machen
            self.renameChildNodeIds(cln);
            deferrerCollection = new Array();

            if (window.location.href.indexOf("wideScreenDiagramMontage") > -1) {
                //resize
                deferrerCollection.push($.Deferred());
                var rightOffset = 25;
                var previousWidth = parseFloat($('#rickshaw_chart_title').css('width')) - rightOffset;
                var previousHeight = parseFloat($('#rickshaw_yAxis_0').css('height'));
                self.resizeGraphTo(1393, 467, deferrerCollection[deferrerCollection.length - 1]);
                //reapply dataLabels

            }
            self.assignAllRelevantCssToStyleAttributes();

            var yAxisCount = 0;
            $('#originalGraph .y_axis').each(function () {
                var newCanvasId = 'canvas_y_axis_' + yAxisCount.toString() + '';
                deferrerCollection.push($.Deferred());
                self.renderSvgElementOnNewCanvasWithDelay($(this), newCanvasId, deferrerCollection[deferrerCollection.length - 1]);
                yAxisCount++;
            });

            var pointMarkerCount = 0;
            $('#originalGraph .pointMarker').each(function () {
                var newCanvasId = 'canvas_pointMarker_' + pointMarkerCount.toString() + '';
                deferrerCollection.push($.Deferred());
                self.renderDomElementOnNewCanvasWithDelay($(this), newCanvasId, deferrerCollection[deferrerCollection.length - 1]);
                pointMarkerCount++;
            });

            if (window.location.href.indexOf("wideScreenDiagramMontage") < 0) {
                var titleContent = $('#rickshaw_chart_title').html().trim();
                if (titleContent != "") {
                    deferrerCollection.push($.Deferred());
                    self.renderDomElementOnNewCanvasWithDelay(document.querySelector("#rickshaw_chart_title"), 'canvas_chart_title', deferrerCollection[deferrerCollection.length - 1]);
                }
            }

            deferrerCollection.push($.Deferred());
            self.renderDomElementOnNewCanvasWithDelay(document.querySelector("#rickshaw_legend"), 'canvas_legend', deferrerCollection[deferrerCollection.length - 1]);

            deferrerCollection.push($.Deferred());
            self.renderSvgElementOnNewCanvasWithDelay($('#originalGraph #rickshaw_graphic_svg:first'), 'canvas_graphic_svg', deferrerCollection[deferrerCollection.length - 1]);

            self.modifyStylesForRendering();
            deferrerCollection.push($.Deferred());
            self.renderSvgElementOnNewCanvasWithDelay($('#originalGraph .x_axis_d3'), 'canvas_x_axis_d3', deferrerCollection[deferrerCollection.length - 1]);

            var dataLabelCount = 0;
            $('#originalGraph .dataLabel').each(function () {
                deferrerCollection.push($.Deferred());
                var newCanvasId = 'canvas_dataLabel_' + dataLabelCount.toString() + '';
                self.renderDomElementOnNewCanvasWithDelay($(this), newCanvasId, deferrerCollection[deferrerCollection.length - 1]);
                dataLabelCount++;
            });

            var rightLabelCount = 0;
            $('#originalGraph .rickshaw_y-axis_right_label').each(function () {
                deferrerCollection.push($.Deferred());
                var newCanvasId = 'canvas_y-axis_right_label_' + rightLabelCount.toString() + '';
                self.renderDomElementOnNewCanvasWithDelay($(this), newCanvasId, deferrerCollection[deferrerCollection.length - 1]);
                rightLabelCount++;
            });

            deferrerCollection.push($.Deferred());
            self.renderDomElementOnNewCanvasWithDelay($("#originalGraph .rickshaw_y-axis_left_label"), 'canvas_y-axis_left_label', deferrerCollection[deferrerCollection.length - 1]);

            $.when.apply($, deferrerCollection).then(function () {
                //merge all canvases into one
                var reduceHeightBy = 112; // slider isn't included in export, thus height is lower
                var moveOffsetUpwardsBy = 0;
                if (window.location.href.indexOf("wideScreenDiagramMontage") > -1) {
                    reduceHeightBy = 182; // for this diagramm, title isn't included in export, thus height is lower
                    moveOffsetUpwardsBy = 65; // for this diagramm, title isn't included in export, thus all elements are closer to the top
                }

                var retVal = prepareNewBlankCanvas(".graph", reduceHeightBy);
                var canvas = retVal.canvas;
                var ctx = retVal.ctx;

                //get top left of .graph
                bodyRect = document.body.getBoundingClientRect();
                graphRect = d3.select(".graph").node().getBoundingClientRect();
                graphOffsetTop = graphRect.top - bodyRect.top;
                graphOffsetLeft = graphRect.left - bodyRect.left;

                self.mergeCanvases("#rickshaw_graphic_svg", "#canvas_graphic_svg", ctx, bodyRect, (graphOffsetTop + moveOffsetUpwardsBy), graphOffsetLeft);
                self.mergeCanvases(".x_axis_d3", "#canvas_x_axis_d3", ctx, bodyRect, (graphOffsetTop + moveOffsetUpwardsBy), graphOffsetLeft);
                self.mergeCanvases("#rickshaw_legend", "#canvas_legend", ctx, bodyRect, (graphOffsetTop + reduceHeightBy), graphOffsetLeft);

                var yAxisCount = 0;
                $('#originalGraph .y_axis').each(function () {
                    var newCanvasId = '#canvas_y_axis_' + yAxisCount.toString() + '';
                    self.mergeCanvasesFromSourceObject($(this), newCanvasId, ctx, bodyRect, (graphOffsetTop + moveOffsetUpwardsBy), graphOffsetLeft);
                    yAxisCount++;
                });

                var pointMarkerCount = 0;

                marklineLabel = "";

                var marklineLabel = $("#originalGraph span.label:contains('Ziel-Kundenzufriedenheit')");
                if (!(marklineLabel.length)) {
                    marklineLabel = $("#originalGraph span.label:contains('Target-CSI')");
                }
                var marklineColor = "";
                if (marklineLabel.length) {
                    marklineColor = marklineLabel.prev().css("background-color");
                }

                $('#originalGraph .pointMarker').each(function () {
                    var newCanvasId = '#canvas_pointMarker_' + pointMarkerCount.toString() + '';
                    if ((marklineColor != "") && (marklineColor == $(this).css("background-color"))) {
                        removeObjectFromDom(newCanvasId);
                    } else {
                        self.mergeCanvasesFromSourceObject($(this), newCanvasId, ctx, bodyRect, (graphOffsetTop + moveOffsetUpwardsBy), graphOffsetLeft);
                    }
                    pointMarkerCount++;
                });

                var titleNode = document.getElementById("canvas_chart_title");
                var canvasExist = titleNode != null;
                if (canvasExist) {
                    self.mergeCanvases("#rickshaw_chart_title", "#canvas_chart_title", ctx, bodyRect, graphOffsetTop, graphOffsetLeft);
                }

                self.modifyStylesAfterRendering();
                self.mergeLabelCanvases($("#originalGraph .rickshaw_y-axis_left_label"), "#canvas_y-axis_left_label", ctx, bodyRect, (graphOffsetTop + moveOffsetUpwardsBy), graphOffsetLeft);

                var dataLabelCount = 0;
                $('#originalGraph .dataLabel').each(function () {
                    var newCanvasId = '#canvas_dataLabel_' + dataLabelCount.toString() + '';
                    self.mergeCanvasesFromSourceObject($(this), newCanvasId, ctx, bodyRect, (graphOffsetTop + moveOffsetUpwardsBy), graphOffsetLeft);
                    dataLabelCount++;
                });

                var rightLabelCount = 0;
                $('#originalGraph .rickshaw_y-axis_right_label').each(function () {
                    var newCanvasId = '#canvas_y-axis_right_label_' + rightLabelCount.toString() + '';
                    self.mergeLabelCanvases($(this), newCanvasId, ctx, bodyRect, (graphOffsetTop + moveOffsetUpwardsBy), graphOffsetLeft);
                    rightLabelCount++;
                });
                //convert to image
                try {
                    downloadCanvas(canvas, "png");
                    if (window.location.href.indexOf("wideScreenDiagramMontage") > -1) {
                        deferrerCollection.push($.Deferred());
                        self.resizeGraphTo(previousWidth, previousHeight, deferrerCollection[deferrerCollection.length - 1]);
                    }
                    origGraphElement.style.position = ('initial');
                    origGraphElement.style.marginTop = ('0px');
                    removeObjectFromDom('#workingCopy');
                    removeObjectFromDom('#canvas_everything_merged');
                    spinner.stop();
                    document.documentElement.style.overflow = 'auto';  // firefox, chrome
                    document.body.scroll = "yes"; // ie only
                }
                catch (err) {
                    if (window.location.href.indexOf("wideScreenDiagramMontage") > -1) {
                        deferrerCollection.push($.Deferred());
                        self.resizeGraphTo(previousWidth, previousHeight, deferrerCollection[deferrerCollection.length - 1]);
                    }
                    origGraphElement.setAttribute('style', "position:initial;top:0px;left:0px;");
                    removeObjectFromDom('#workingCopy');
                    removeObjectFromDom('#canvas_everything_merged');
                    spinner.stop();
                    document.documentElement.style.overflow = 'auto';  // firefox, chrome
                    document.body.scroll = "yes"; // ie only
                } // handle IE
            });
        });
    };

    this.renameChildNodeIds = function (node) {
        for (var i = 0; i < node.childNodes.length; i++) {
            var child = node.childNodes[i];
            self.renameChildNodeIds(child);
            if ((typeof child.id !== 'undefined') && (child.id !== "")) {
                child.id = "workingCopy_" + child.id;
            }
        }
    };

    this.assignAllRelevantCssToStyleAttributes = function () {
        d3.selectAll("#rickshaw_y-axes_right").style({
            "float": "right",
        });
        d3.selectAll(".rickshaw_y-axis_right").style({
            "display": "inline-block",
            "position": "relative",
        });
        d3.selectAll(".domain").style({
            "fill": "none",
        });
        d3.selectAll(".rickshaw_graph").style({
            "position": "relative",
        });
        d3.selectAll(".rickshaw_graph svg").style({
            "display": "block",
            "overflow": "hidden",
        });
        d3.selectAll(".rickshaw_graph .y_axis").style({
            "fill": "none",
        });
        d3.selectAll(".rickshaw_graph .y_ticks .tick line, .rickshaw_graph .x_ticks_d3 .tick").style({
            "stroke": "rgba(0, 0, 0, 0.16)",
            "stroke-width": "2px",
            "shape-rendering": "crisp-edges",
            "pointer-events": "none",
        });
        d3.selectAll(".rickshaw_graph .y_grid .tick, .rickshaw_graph .x_grid_d3 .tick").style({
            "z-index": "-1",
            "stroke": "rgba(0, 0, 0, 0.20)",
            "stroke-width": "1px",
            "stroke-dasharray": "1 1",
        });
        d3.selectAll('.rickshaw_graph .y_grid .tick[data-y-value="0"]').style({
            "stroke-dasharray": "1 0",
        });
        d3.selectAll(".rickshaw_graph .y_grid path, .rickshaw_graph .x_grid_d3 path").style({
            "fill": "none",
            "stroke": "none",
        });
        d3.selectAll(".rickshaw_graph .y_ticks text,  .rickshaw_graph .x_ticks_d3 text").style({
            "opacity": "0.5",
            "font-size": "9px",
            "pointer-events": "none",
            "color": "#333333",
            "font-family": "Helvetica,Arial,sans-serif",
            "line-height": "20px",
        });
    };

    this.modifyStylesForRendering = function () {
        d3.select("#originalGraph").selectAll("#rickshaw_x-axis").style({
            "margin-left": "0px",
        });
        d3.select("#originalGraph").selectAll(".x_axis_d3").style({
            "left": "0px",
        });
        d3.select("#originalGraph").selectAll(".rickshaw_y-axis_left_label, .rickshaw_y-axis_right_label").style({
            "-moz-transform": "none", /* Firefox 3.6 Firefox 4 */
            "-webkit-transform": "none", /* Safari */
            "-o-transform": "none", /* Opera */
            "-ms-transform": "none", /* IE9 */
            "transform": "none", /* W3C */
        });
    };

    this.modifyStylesAfterRendering = function () {
        d3.select("#originalGraph").selectAll(".rickshaw_y-axis_left_label, .rickshaw_y-axis_right_label").style({
            "-moz-transform": "rotate(-90deg)", /* Firefox 3.6 Firefox 4 */
            "-webkit-transform": "rotate(-90deg)", /* Safari */
            "-o-transform": "rotate(-90deg)", /* Opera */
            "-ms-transform": "rotate(-90deg)", /* IE9 */
            "transform": "rotate(-90deg)", /* W3C */
        });
    };

    this.renderDomElementOnNewCanvasWithDelay = function (domElement, newCanvasId, deferrer) {
        html2canvas(domElement, {
            onrendered: function (canvas) {
                canvas.setAttribute('id', newCanvasId);
                canvas.setAttribute('style', "display:none");
                document.body.appendChild(canvas);
                deferrer.resolve();
            },
            width: 3000,
            height: 5000
        });
    };


    this.resizeGraphTo = function (width, height, deferrer) {
        rickshawGraphBuilder.updateSize({
            width: width,
            height: height
        });

        if (parseInt(width) < 1070) {
            $("#rickshaw_legend > ul").css({
                "-moz-column-count": 1 + "",
                "-webkit-column-count": 1 + "",
                "column-count": 1 + ""
            });
        } else {
            $("#rickshaw_legend > ul").css({
                "-moz-column-count": 2 + "",
                "-webkit-column-count": 2 + "",
                "column-count": 2 + ""
            });
        }
        deferrer.resolve();
    };

    this.renderSvgElementOnNewCanvasWithDelay = function (svgElement, newCanvasId, deferrer) {
        var html2 = svgElement.clone().wrapAll("<div/>").parent().html();
        html2 = html2.replace(/<svg (.*?)>/, '<svg xmlns="http://www.w3.org/2000/svg" $1>');
        html2 = html2.replace(/top: -[\d]+\.?[\d]*px/, 'top: 0');

        if (!!navigator.userAgent.match(/Trident/)) {
            html2 = html2.replace(/ xmlns=\"http:\/\/www.w3.org\/2000\/svg\"/, '');
        }
        var imgsrc = 'data:image/svg+xml;base64,' + btoa(html2);
        var img = '<img src="' + imgsrc + '">';

        var canvas = document.createElement('canvas');
        canvas.setAttribute('id', newCanvasId);
//    canvas.setAttribute('style', "display:none");
        canvas.width = 3000;
        canvas.height = 5000;
        document.body.appendChild(canvas);

        var ctx2 = canvas.getContext("2d");

        var image2 = new Image;
        image2.src = imgsrc;
        image2.onload = function () {
            ctx2.drawImage(image2, 0, 0);
            deferrer.resolve();
        }
    }

    this.mergeCanvases = function (originalElementId, sourceCanvasId, targetContext, bodyRect, graphOffsetTop, graphOffsetLeft) {
        curElemRect = document.querySelector("#originalGraph " + originalElementId).getBoundingClientRect();
        curElemOffsetTop = curElemRect.top - bodyRect.top;
        curElemOffsetLeft = curElemRect.left - bodyRect.left;
        distanceTop = curElemOffsetTop - graphOffsetTop;
        distanceLeft = curElemOffsetLeft - graphOffsetLeft;
        useMe = document.querySelector(sourceCanvasId);
        targetContext.drawImage(useMe, distanceLeft, distanceTop);
        removeObjectFromDom(sourceCanvasId);
    }

    this.mergeCanvasesFromSourceObject = function (originalElement, sourceCanvasId, targetContext, bodyRect, graphOffsetTop, graphOffsetLeft) {
        curElemRect = originalElement.offset();
        curElemOffsetTop = curElemRect.top - bodyRect.top;
        curElemOffsetLeft = curElemRect.left - bodyRect.left;
        distanceTop = curElemOffsetTop - graphOffsetTop;
        distanceLeft = curElemOffsetLeft - graphOffsetLeft;
        useMe = document.querySelector(sourceCanvasId);
        targetContext.drawImage(useMe, distanceLeft, distanceTop);
        removeObjectFromDom(sourceCanvasId);
    };

    this.mergeLabelCanvases = function (originalElement, sourceCanvasId, ctx, bodyRect, graphOffsetTop, graphOffsetLeft) {
        ctx.save();
        curElemRect = originalElement.offset();
        curElemOffsetTop = curElemRect.top - bodyRect.top;
        curElemOffsetLeft = curElemRect.left - bodyRect.left;
        distanceTop = curElemOffsetTop - graphOffsetTop;
        distanceLeft = curElemOffsetLeft - graphOffsetLeft;
        ctx.translate(distanceLeft, distanceTop);
        ctx.translate(Math.floor(originalElement.height() / 2), Math.floor(originalElement.width() / 2));
        ctx.rotate(-90 * Math.PI / 180);
        useMe = document.querySelector(sourceCanvasId);
        ctx.drawImage(useMe, (((originalElement.width()) / 2) * -1), (((originalElement.height()) / 2) * -1));
        removeObjectFromDom(sourceCanvasId);
        ctx.restore();
    };

    this.initialize(args);
}

// escapes special characters
//
function makeValidSelector(identifier) {
    return identifier.replace(/(\^|\!|\"|\$|\&|\(|\)|\{|\}|\?|\\|\`|\=|\¸|\,|\*|\+|\~|\'|\#|\-|\_|\.|\:|\,|\;|\<|\>|\/|:|\.|\[|\]|,|\||\ |\%)/g, "\\$1");

}
