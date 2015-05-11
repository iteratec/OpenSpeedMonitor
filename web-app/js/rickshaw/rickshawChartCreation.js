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
  
  this.initialize = function(args) {
    
    if ((args.hasOwnProperty("dataLabelsActivated")) && (args.dataLabelsActivated == true)) { //display dataLabels
      this.dataLabelsActivated = true;
    }

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
    
    new ChartAdjuster(args);
    new ChartExporter(args);
  }

  this.addDataLabels = function() {
    if (($('#to-enable-label').is(':checked')) && (!this.dataLabelsHaveBeenAdded)) {
      if($(".pointMarker").length < 1) {
        //activate pointMarker
        $("#to-enable-marker").click();
      }
      $(".pointMarker").each(function( index ) {
        var percentage = 0;
        var currentMarkerColor = self.rgb2hex($( this ).css("border-top-color"));
        self.graph.series.forEach(function(series) {
          if(currentMarkerColor === series.color) {
            //get args.series.ROW.data.INDEX.y * 100 rounded
            if ( !/undef/i.test(typeof series.data[index])) {
//              percentage =  Math.round((series.data[index].y)*100)/100;
              percentage =  parseFloat(series.data[index].y).toFixed(2);
            }
            //end loop
            return false;
          }
        });
        if (percentage > 0) {
          var totalHeight = $( this ).parent().height();
          var distanceTop = $( this ).css("top").replace(/[^-\d\.]/g, '') ;
          //calculate data, round to 2 digits
  //        var percentage = Math.round((100 - ((distanceTop * 100) / totalHeight))*100)/100;
          //display data
          $( this ).parent().append( "<div class='dataLabel' style='top:"+(parseInt($(this).css('top'), 10)-5)+"px;left:"+(parseInt($(this).css('left'), 10)-9)+"px;height:100px;width:100px;font-size: 13pt;font-weight: bold;color: #b3b3b3;cursor: default;fill: #b3b3b3;'>"+percentage+"</div>" );
        }
      });
      this.dataLabelsHaveBeenAdded = true;
    }
  }
  
  this.rgb2hex = function (rgb){
    rgb = rgb.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);
    return "#" +
     ("0" + parseInt(rgb[1],10).toString(16)).slice(-2) +
     ("0" + parseInt(rgb[2],10).toString(16)).slice(-2) +
     ("0" + parseInt(rgb[3],10).toString(16)).slice(-2);
   }

  this.updateDataLabels = function() {
    //remove labels
    $(".dataLabel").each(function( index ) {
      $(this).remove();
    });
    //re-add labels
    this.dataLabelsHaveBeenAdded = false;
    self.addDataLabels();
  }

  this.update = function() {
    self.xAxis.updateXAxis();
    self.removeGrid();
    self.updateYAxes();
    self.updateDataLabels();
  }

  this.updateBorders = function(args) {
    self.graph.updateBorders(args);
  }

  this.updateSize = function(args) {

    if (args.height == "auto") {
      args.height = self.htmlProvider.HEIGHT_OF_CHART;
    }

    $("#rickshaw_main").width(args.width);
    var widthOfChartSvg = $(rickshawGraphBuilder.graph.element).width();

    // set height of html components
    $("#rickshaw_chart_title").width(args.width);
    $(".rickshaw_y-axis_left").height(args.height);
    $(".rickshaw_y-axis_right").height(args.height);
    $("#rickshaw_y-axes_right").height(args.height);
    $("#rickshaw_chart").height(args.height);
    $("#rickshaw_addons").width(args.width-60);
    $("#rickshaw_timeline").width(args.width-60);
    $("#rickshaw_slider").width(eval(parseInt(args.width) + 10));
    $("#rickshaw_range_slider_preview_container").width(eval(parseInt(args.width) + 10));
    $("#rickshaw_x-axis").width(args.width);
    $(".x_axis_d3").attr("width", args.width);
    $(".graph").width(eval(parseInt(args.width) + 25));

    self.graph.configure({
      width : widthOfChartSvg,
      height : args.height
    });
    self.graph.render();
  }

  this.updateTitle = function(title) {
    $("#rickshaw_chart_title").html(title);
  }

  this.updateYAxes = function() {
    self.yAxes.forEach(function(axis) {
      // update label text
      var containerOfYAxis = $(axis.element).parent();
      var containerOfLabel = containerOfYAxis.children("div");
      var measurandGroup = self.graph.measurandGroupsManager
          .getMeasurandGroup(axis.measurandGroup);
      if (measurandGroup.label) {
        var lastIndex = measurandGroup.label.lastIndexOf(" ")
        var str = measurandGroup.label.substring(0, lastIndex);
        var label = str + " "
            + measurandGroup.yValueFormatterForAxis.unit
        containerOfLabel.html(label);
      }
      
      // update label position
      var RESULTING_LEFT_VALUE = 0;
      if (containerOfLabel.attr("class") == "rickshaw_y-axis_left_label") {
        RESULTING_LEFT_VALUE = 25;
      } else {
        RESULTING_LEFT_VALUE = 40;
      }
      var width = containerOfLabel.width();
      var left = RESULTING_LEFT_VALUE - Math.ceil(width/2);
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
  }

  this.updateColorsOfSeries = function(args) {
    /*
     * TODO Funktion per GUI erreichbar einbauen:
     * args ist ein assoziatives Array der Form args[series.name] = Farbe
     * Farbe ist ein String: Raute + 6 Hex Werte (z.B. "#000000" für Schwarz)
     */
    self.graph.series.forEach(function(series) {
      if (args[series.name]) {
        series.color = args[series.name];
      }
    });
    self.graph.render();
    // TODO Slider.Preview zeigt dennoch die alten Farben an
    self.initializeSlider();
  }
  
  this.updateDrawPointMarkers = function(drawPointMarkers){
    if (drawPointMarkers) {
      self.graph.drawPointMarkers = drawPointMarkers
      self.graph.render();
    } else {
      $("#rickshaw_chart > .pointMarker").remove();
    }
  }
  
  this.updateDrawPointLabels = function(drawPointLabels){
    $(".dataLabel").each(function( index ) {
      $(this).remove();
    });
    this.dataLabelsHaveBeenAdded = false;
    if (drawPointLabels) {
      self.addDataLabels();
    }
  }

  this.initializeGraph = function(args) {
    self.graph = new Rickshaw.Graph({
      element : document.getElementById("rickshaw_chart"),
      width : $("#rickshaw_chart").width(),
      height : $("#rickshaw_chart").height(),
      renderer : 'line',
      interpolation : 'linear',
      series : args.series,
      NUMBER_OF_YAXIS_TICKS : args.NUMBER_OF_YAXIS_TICKS,
      drawPointMarkers : args.drawPointMarkers,
      drawPointLabels : args.drawPointLabels
    });
  }
  
  this.initializeHoverDetail = function() {
    var xFormatter = function(x) {
      return new Date(x * 1000).toLocaleString();
    }
    var hoverDetail = new Rickshaw.Graph.HoverDetail({
      xFormatter : xFormatter,
      graph : self.graph
    });
		hoverDetail.formatter = function(series, x, y, formattedX, formattedY, d, testingAgent) {
			return "<table border=\"0\" class=\"chart-tiptext\">" +
                "<tr>" + "<td>Timestamp: </td>" + "<td>" + formattedX + "</td>" + "</tr>" +
                "<tr>" + "<td>Label: </td>" + "<td>" + series.name + "</td>" + "</tr>" +
                "<tr>" + "<td>Value: </td>" + "<td>" + formattedY + "</td>" + "</tr>" +
                "<tr>" + "<td>Test agent: </td>" + "<td>" + testingAgent + "</td>" + "</tr>" +
                "</table>";
    };
  }

  this.initializeAnnotator = function(args) {
    var annotator = new Rickshaw.Graph.Annotate( {
      graph: self.graph,
      element: document.getElementById('rickshaw_timeline')
    } );
    if (args) {
      for (index = 0; index < args.length; ++index) {
        annotator.add(args[index].x, args[index].text);
      }
      annotator.update();
    }
  }
  
  this.initializeLegend = function() {
    self.legend = new Rickshaw.Graph.Legend({
      graph : self.graph,
      element : document.getElementById('rickshaw_legend')
    });

    new Rickshaw.Graph.Behavior.Series.Toggle({
      graph : self.graph,
      legend : self.legend
    });
  }
  
  this.initializeSlider = function() {
    self.slider = new Rickshaw.Graph.RangeSlider.Preview({
      graph : self.graph,
      element : document.querySelector("#rickshaw_slider")
    });
  }

  this.initializeYAxes = function(args) {
    var measurandGroups = args.graph.measurandGroupsManager.measurandGroups;
    
    for (var i = 0; i < measurandGroups.length; i++) {

      var id_prefix, orientation;
      if (i == 0) {
        orientation = "left";
      } else {
        orientation = "right";
      }

      var scale = d3.scale.linear().domain([ 0, 1 ]);
      var axis = new Rickshaw.Graph.Axis.Y.Scaled({
        element : document.getElementById("rickshaw_yAxis_" + i),
        graph : args.graph,
        orientation : orientation,
        scale : scale,
        tickFormat : Rickshaw.Fixtures.Number.formatKMBT,
        measurandGroup : measurandGroups[i].name,
        tickValues : measurandGroups[i].computeTickValues()
      });
      self.yAxes.push(axis);
    }
  }

  this.removeGrid = function() {
    // delete x-axis-grid
    $("g.x_grid_d3").remove();

    // delete x-axis border
    $("#rickshaw_x-axis > svg > g > path").remove();

    // delete left y-axis borders
    $(".rickshaw_graph.y_axis > g > path").remove();

    // delete all y-axis grids except one
    var grids = $("#rickshaw_graphic_svg > .y_grid");
    var numberOfGrids = grids.length;

    grids.each(function(index) {
      if (index < numberOfGrids - 1) {
        if (typeof this.remove == 'function') {
          this.remove();
        }
      }
    });
  }

  this.composeSeries = function(data) {
    var measurandGroups = []
    var series = [];
    var scale = d3.scale.linear().domain([ 0, 1 ]);
    var palette = new Rickshaw.Color.Palette({
      scheme : 'iteratec'
    });

    data.forEach(function(eachData) {
      var entry = {
        color : palette.color(),
        data : eachData.data,
        name : eachData.name,
        scale : scale,
        measurandGroup : eachData.measurandGroup,
        label : eachData.yAxisLabel
      };
      series.push(entry);

      // count measurand groups
      if ($.inArray(eachData.measurandGroup, measurandGroups) == -1) {
        measurandGroups.push(eachData.measurandGroup);
      }
    });
    series.numberOfMeasurandGroups = measurandGroups.length;
    return series;
  }

  this.initialize(args);
}

function XAxis(args) {
  var self = this;
  this.graph;
  this.rickshawXAxis;
  this.NUMBER_OF_TICKS;

  this.initialize = function(args) {
    self.graph = args.graph;

    self.rickshawXAxis = new Rickshaw.Graph.Axis.X({
      graph : args.graph,
      orientation : 'bottom',
      element : document.getElementById('rickshaw_x-axis')
    });
  }

  this.updateXAxis = function() {
    self.setNumberOfTicks();
    self.setTickValueLabels();
    self.rickshawXAxis.render();
    self.formatXAxisLabels();
  }

  this.setNumberOfTicks = function() {
    var width = self.graph.width;
    self.NUMBER_OF_TICKS = Math.floor(width / 80);
  }

  this.setTickValueLabels = function() {
    var DAYS = [ "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
        "Saturday", "Sunday " ];
    var MONTHS = [ "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November",
        "December" ];
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

    if (diffYears >= self.NUMBER_OF_TICKS) {
      tickValues = self.getYearsInRange(minDate, maxDate);
      format = function(n) {
        var date = new Date(n * 1000);
        var year = date.getFullYear();
        var dateLabel = self.getDateISO(date);
        return year + "_nl_" + dateLabel;
      }
    } else if (diffMonths >= self.NUMBER_OF_TICKS) {
      tickValues = self.getMonthsInRange(minDate, maxDate);
      format = function(n) {
        var date = new Date(n * 1000);
        var monthName = MONTHS[date.getMonth()];
        var dateLabel = self.getDateISO(date);
        return monthName + "_nl_" + dateLabel;
      }
    } else if (diffDays >= self.NUMBER_OF_TICKS) {
      tickValues = self.getDaysInRange(minDate, maxDate);
      format = function(n) {
        var date = new Date(n * 1000);
        var dayName = DAYS[date.getDay()];
        var dateLabel = self.getDateISO(date);
        return dayName + "_nl_" + dateLabel;
      }
    } else if (diffHours >= self.NUMBER_OF_TICKS) {
      tickValues = self.getHoursInRange(minDate, maxDate);
      format = function(n) {
        var date = new Date(n * 1000);
        var time = self.getTimeString(date);
        var dateLabel = self.getDateISO(date);
        return time + "_nl_" + dateLabel;
      }
    } else if (diffMinutes >= self.NUMBER_OF_TICKS) {
      tickValues = self.getMinutesInRange(minDate, maxDate);
      format = function(n) {
        var date = new Date(n * 1000);
        var time = self.getTimeString(date);
        var dateLabel = self.getDateISO(date);
        return time + "_nl_" + dateLabel;
      }
    } else {
      tickValues = self.getDefaultTickValues(minDate, maxDate);
      format = function(n) {
        var date = new Date(n * 1000);
        var time = self.getTimeString(date);
        var dateLabel = self.getDateISO(date);
        return time + "_nl_" + dateLabel;
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
  }

  this.getYearsInRange = function(minDate, maxDate) {
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
  }

  this.getMonthsInRange = function(minDate, maxDate) {
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
  }

  this.getDaysInRange = function(minDate, maxDate) {
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
  }

  this.getHoursInRange = function(minDate, maxDate) {
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
  }

  this.getMinutesInRange = function(minDate, maxDate) {
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
  }

  this.getDefaultTickValues = function(minDate, maxDate) {
    var dif = maxDate.getTime() - minDate.getTime();
    var tickValue = Math.floor(dif / self.NUMBER_OF_TICKS);
    var tickValues = [];

    for (var i = 0; i < self.NUMBER_OF_TICKS; i++) {
      var tick = (minDate.getTime()) + (i * tickValue);
      tickValues.push(tick / 1000);
    }
    return tickValues;
  }

  this.formatXAxisLabels = function() {
    // Converts the strings "_nl_" into new lines
    d3.select("#rickshaw_x-axis > svg").selectAll("g g text").each(
        function(d) {
          var el = d3.select(this);
          var words = el.text().split('_nl_');
          el.text('');

          for (var i = 0; i < words.length; i++) {
            var tspan = el.append('svg:tspan').text(words[i]);
            if (i > 0)
              tspan.attr('x', 0).attr('dy', '15');
          }
        });
  }

  this.getDateISO = function(date) {
    return date.getFullYear() + "-" + (date.getMonth() + 1) + "-"
        + date.getDate();
  }

  this.getTimeString = function(date) {
    var result = date.getHours();
    if (date.getMinutes() < 10) {
      result = result + ":0" + date.getMinutes();
    } else if (date.getMinutes() == 0) {
      result = result + ":00";
    } else {
      result = result + ":" + date.getMinutes();
    }
    return result;
  }

  this.increaseMonth = function(date) {
    if (date.getMonth() == 11) {
      date.setYear(date.getFullYear() + 1);
      date.setMonth(0);
    } else {
      date.setMonth(date.getMonth() + 1);
    }
  }

  this.increaseDay = function(date) {
    var month = date.getMonth() + 1;
    var increase = function(date, daysOfMonth) {
      if (date.getDate() == daysOfMonth) {
        date.setDate(1);
        self.increaseMonth(date);
      } else {
        date.setDate(date.getDate() + 1);
      }
    }

    if (month == 2) {
      if (date.getFullYear() % 4 == 0) {
        increase(date, 27);
      } else {
        increase(date, 28);
      }
    } else if ($.inArray(month, [ 1, 3, 5, 7, 8, 10, 12 ]) >= 0) {
      increase(date, 31);
    } else {
      increase(date, 30);
    }
  }

  this.increaseHour = function(date) {
    if (date.getHours() == 23) {
      date.setHours(0);
      self.increaseDay(date);
    } else {
      date.setHours(date.getHours() + 1);
    }
  }

  this.increaseMinute = function(date) {
    if (date.getMinutes() == 59) {
      date.setMinutes(0);
      self.increaseHour(date);
    } else {
      date.setMinutes(date.getMinutes() + 1);
    }
  }

  this.initialize(args);
}

function YValueFormatter() {
  var self = this;

  this.getFormatterForSpecificMeasurandGroup = function(measurandGroup) {
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
  }

  this.getFormatterForLoadTimes = function(measurandGroup) {
    var result = {};
    var dif = measurandGroup.currentScale.tickMax
        - measurandGroup.currentScale.tickMin;
    dif = Math.abs(dif);

    if (dif >= measurandGroup.NUMBER_OF_YAXIS_TICKS) {
      result.forAxis = function(y) {
        return y;
      };
      result.forAxis.unit = "[s]";
      result.forHoverDetail = function(y) {
        return parseFloat(y).toFixed(3);
      };
    } else {
      result.forAxis = function(y) {
        return y * 1000;
      };
      result.forAxis.unit = "[ms]";
      result.forHoverDetail = function(y) {
        return parseFloat(y * 1000).toFixed(0);
      };
    }
    return result;
  }

  this.getFormatterForRequestSizes = function(measurandGroup) {
    var result = {};
    var dif = measurandGroup.currentScale.tickMax
        - measurandGroup.currentScale.tickMin;
    dif = Math.abs(dif);

    if (dif >= measurandGroup.NUMBER_OF_YAXIS_TICKS) {
      result.forAxis = function(y) {
        return y;
      }
      result.forAxis.unit = "[kb]";
      result.forHoverDetail = function(y) {
        return parseFloat(y).toFixed(0);
      }
    } else {
      result.forAxis = function(y) {
        return y * 1024;
      };
      result.forAxis.unit = "[b]";
      result.forHoverDetail = function(y) {
        return parseFloat(y * 1024).toFixed(0);
      };
    }
    return result;
  }
  
  this.getFormatterForRequestCounts = function() {
    var result = {};
    result.forAxis = function(y) {
        return y;
      };
      result.forAxis.unit = "[c]";
      result.forHoverDetail = function(y) {
        return parseFloat(y).toFixed(0);
      }
    return result;
  }
      
  this.getFormatterForPercentages = function() {
    var result = {};
    result.forAxis = function(y) {
        return y;
      };
      result.forAxis.unit = "[%]";
      result.forHoverDetail = function(y) {
        return parseFloat(y).toFixed(2);
      }
    return result;
}

  this.getDefaultFormatter = function() {
    var result = {};
    
    result.forAxis = function(y) {
      return y;
    }
    result.forAxis.unit = "";
    result.forHoverDetail = function(y) {
      return parseFloat(y).toFixed(0);
    }
    
    return result;
  }
}

function HtmlProvider(args) {
  var self = this;
  this.HEIGHT_OF_CHART;
  this.numberOfMeasurandGroups;

  this.initialize = function(args) {
    self.HEIGHT_OF_CHART = args.heightOfChart;
    self.numberOfMeasurandGroups = args.series.numberOfMeasurandGroups;

    self._generateLeftYAxis();
    self._generateRightYAxes();
    self._setHeightOfChartContainer();
    self._setWidthOfHtmlComponents();
  }

  this._generateLeftYAxis = function() {
    var height = self.HEIGHT_OF_CHART;
    $("#rickshaw_yAxis_0").height(height).append(
        "<div class=\"rickshaw_y-axis_left_label\"> </div>");
  }

  this._generateRightYAxes = function() {
    var height = self.HEIGHT_OF_CHART;
    $("#rickshaw_y-axes_right").height(height);

    for (var i = 1; i < self.numberOfMeasurandGroups; i++) {
      var id = "rickshaw_yAxis_" + i;

      // y-axis
      $('<div>').attr({
        "id" : id
      }).height(height).addClass("rickshaw_y-axis_right").appendTo(
          $("#rickshaw_y-axes_right"));

      // label
      $('<div>').addClass("rickshaw_y-axis_right_label").html("")
          .appendTo($("#" + id));
    }
  }

  this._setHeightOfChartContainer = function() {
    $("#rickshaw_chart").css({
      "height" : self.HEIGHT_OF_CHART + "px"
    });
  }

  this._setWidthOfHtmlComponents = function() {
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
      "width" : totalWidthOfRightYAxes + "px"
    });
    // y-axis on the right side
    $(".rickshaw_y-axis_right").css({
      "width" : widthOfRightYAxis + "px"
    });

    // container which contains the chart svg
    $("#rickshaw_chart").css({
      "margin-left" : widthOfLeftYAxis,
      "margin-right" : totalWidthOfRightYAxes
    });

    // x-axis
    $("#rickshaw_x-axis").css({
      "margin-left" : widthOfLeftYAxis,
    });

    // container which contains x-axis, slider and legend
    $("#rickshaw_addons").css({
      "margin-left" : widthOfLeftYAxis + "px",
      "width" : widthOfGraph + "px"
    });
    $("#rickshaw_timeline").css({
      "margin-left" : widthOfLeftYAxis + "px",
      "width" : widthOfGraph + "px"
    });

    // container which contains the x-axis svg
    $("#rickshaw_x-axis").css({
      "width" : widthOfGraph + "px"
    });

    // place the slider below the chart
    $("#rickshaw_slider").css({
      "width" : eval(parseInt(widthOfGraph) + 10) + "px"
    });
  }
  this.initialize(args);
}

function ChartAdjuster(args) {
  var self = this;
  
  this.initialize = function(args) {
    $('#dia-width').val(args.graph.width);
    $('#dia-height').val(args.graph.height);
    $(".collapse").collapse('hide');
    
    self.addFunctionalityAdjustingChartSize();
    self.addFunctionalityCustomizeTitle();
    self.createYAxisAdjuster(args);
    self.replaceDataMarkerCheckbox();
    self.replaceDataLabelsCheckbox();
    self.addFunctionalityShowDataMarker();
    self.addFunctionalityShowDataLabels();
  }
  
  this.addFunctionalityAdjustingChartSize = function() {
    $('#dia-change-chartsize')
    .bind(
        'click',
        function() {
          var diaWidth = $('#dia-width').val()
          var diaHeight = $('#dia-height').val()
          var maxWidth = 5000
          var minWidth = 540
          var maxHeight = 3000
          if ($.isNumeric(diaWidth) && $.isNumeric(diaHeight)
              && parseInt(diaWidth) > 0
              && parseInt(diaWidth) <= maxWidth
              && parseInt(diaWidth) >= minWidth
              && parseInt(diaHeight) > 0
              && parseInt(diaHeight) <= maxHeight) {
            rickshawGraphBuilder.updateSize({
              width : $('#dia-width').val(),
              height : $('#dia-height').val()
            });
            if (parseInt(diaWidth) < 1070) {
              $("#rickshaw_legend > ul").css({
                "-moz-column-count" : 1 + "",
                "-webkit-column-count" : 1 + "",
                "column-count" : 1 + ""
              });
            } else {
              $("#rickshaw_legend > ul").css({
                "-moz-column-count" : 2 + "",
                "-webkit-column-count" : 2 + "",
                "column-count" : 2 + ""
              });
            }
            //center legend
//            var leftDistance = Math.floor(((diaWidth - $("#rickshaw_legend").outerWidth()) / 2) - 60);
//            document.getElementById('rickshaw_legend').style.cssText = "margin-left:" + leftDistance + "px !important";
//            $(".rickshaw_legend").css({
//              "margin-left" : "" + leftDistance + "px"
//            });
          } else {
            window
                .alert("Width and height of diagram must be numeric values. Maximum is 5.000 x 3.000 pixels, minimum width is 540 pixels.");
          }
        });

  }
  
  this.addFunctionalityCustomizeTitle = function() {
    $('#dia-title').bind('input', function() {
      rickshawGraphBuilder.updateTitle($(this).val());
    });
  }
  
  this.createYAxisAdjuster = function(args) {
    var measurandGroups = args.graph.measurandGroupsManager.measurandGroups;
    var parentContainer= $("#collapseAdjustment > .accordion-inner > .span11");
    var blankYAxisAdjuster = $("[id=adjust_chart_y_axis]");
    measurandGroups.forEach(function(mg) {
      var yAxisAdjuster = blankYAxisAdjuster.clone();
      parentContainer.append(yAxisAdjuster);
      var button = yAxisAdjuster.find("button");
      var yAxisAdjusterLabel = yAxisAdjuster.find(".span2.text-right");
      yAxisAdjusterLabel.html(yAxisAdjusterLabel.html() + ": " + mg.label);
      button[0].measurandGroup = mg.name;
      
      var inputMin = button.parent().find("#dia-y-axis-min");
      var inputMax = button.parent().find("#dia-y-axis-max");
      // TODO: werte dynamisch ermitteln
      inputMin.val(0);
      inputMax.val("auto");
      
      self.addFunctionalityAdjustYAxis(button);

    });
    blankYAxisAdjuster.remove();
  }
  
  this.addFunctionalityAdjustYAxis = function(button) {
    button
    .bind(
        'click',
        function() {
          var diaYAxisMin = $(this).parent().find(
              '#dia-y-axis-min').val();
          var diaYAxisMax = $(this).parent().find(
              '#dia-y-axis-max').val();

          var valide = true;
          if (diaYAxisMin != "auto"
              && !($.isNumeric(diaYAxisMin))
              && diaYAxisMax != "auto"
              && !($.isNumeric(diaYAxisMax))) {
            valide = false;
          }
          if (valide) {
            if (diaYAxisMax != "auto"
                && diaYAxisMin != "auto") {
              if (!(parseFloat(diaYAxisMax) > parseFloat(diaYAxisMin))) {
                valide = false;
              }
            }
          }
          if (valide) {
            rickshawGraphBuilder
                .updateBorders({
                  measurandGroupName : this.measurandGroup,
                  bottom : diaYAxisMin,
                  top : diaYAxisMax
                });
          } else {
            window
                .alert("Minimum and maximum of Y-Axis has to be \"auto\" or numeric values and maximum must be greater than minimum!");
          }
        });
  }

  this.replaceDataMarkerCheckbox = function() {
    var checkbox = $("#to-enable-marker").parent().parent();
    var parentContainer = checkbox.parent();
    checkbox.detach();
    parentContainer.append(checkbox);
  }
  
  this.replaceDataLabelsCheckbox = function() {
    var checkbox = $("#to-enable-label").parent().parent();
    var parentContainer = checkbox.parent();
    checkbox.detach();
    parentContainer.append(checkbox);
  }

  this.addFunctionalityShowDataMarker = function() {
    $('#to-enable-marker').bind('change', function(){
      var toEnableMarkers = $(this).is(':checked');
      rickshawGraphBuilder.updateDrawPointMarkers(toEnableMarkers);
    });
  }
  
  this.addFunctionalityShowDataLabels = function() {    
    $('#to-enable-label').bind('change', function(){
      var toEnableLabels = $(this).is(':checked');
      if (toEnableLabels && $(rickshawGraphBuilder.graph.series[0].data).length > 10000) {
        window.alert("Too many datapoints to show and label them!");
        $(this).prop('checked', false);
      } else {
        rickshawGraphBuilder.updateDrawPointLabels(toEnableLabels);
      }
    });
  }
  
  this.initialize(args);
}

function ChartExporter(args) {
  var self = this;
  
  this.initialize = function(args) {
    d3.select("#dia-save-chart-as-png").on("click", function(){

      deferrerCollection = new Array();
      
      if(window.location.href.indexOf("csiDashboard/showDefault") > -1) {
        //resize
        deferrerCollection.push($.Deferred());
        var previousWidth=parseFloat($('#rickshaw_chart_title').css('width'));
        var previousHeight=parseFloat($('#rickshaw_yAxis_0').css('height'));
        self.resizeGraphTo(1418, 572, deferrerCollection[deferrerCollection.length - 1]);
      }
      
    self.assignAllRelevantCssToStyleAttributes();
      
      var yAxisCount = 0;
      $('.y_axis').each(function() {
        var newCanvasId = 'canvas_y_axis_' + yAxisCount.toString() + '';
        deferrerCollection.push($.Deferred());
        self.renderSvgElementOnNewCanvasWithDelay($( this ), newCanvasId, deferrerCollection[deferrerCollection.length - 1]);
        yAxisCount++;
      });

      var titleContent = $('#rickshaw_chart_title').html().trim();
      if (titleContent != "") {
        deferrerCollection.push($.Deferred());
        self.renderDomElementOnNewCanvasWithDelay(document.querySelector("#rickshaw_chart_title"), 'canvas_chart_title', deferrerCollection[deferrerCollection.length - 1]);
      }
  
      deferrerCollection.push($.Deferred());
      self.renderDomElementOnNewCanvasWithDelay(document.querySelector("#rickshaw_legend"), 'canvas_legend', deferrerCollection[deferrerCollection.length - 1]);
      
      deferrerCollection.push($.Deferred());
      self.renderSvgElementOnNewCanvasWithDelay($('#rickshaw_graphic_svg'), 'canvas_graphic_svg', deferrerCollection[deferrerCollection.length - 1]);
      
      self.modifyStylesForRendering();
      deferrerCollection.push($.Deferred());
      self.renderSvgElementOnNewCanvasWithDelay($('.x_axis_d3'), 'canvas_x_axis_d3', deferrerCollection[deferrerCollection.length - 1]);

      var dataLabelCount = 0;
      $('.dataLabel').each(function() {
        deferrerCollection.push($.Deferred());
        var newCanvasId = 'canvas_dataLabel_' + dataLabelCount.toString() + '';
        self.renderDomElementOnNewCanvasWithDelay($( this ), newCanvasId, deferrerCollection[deferrerCollection.length - 1]);
        dataLabelCount++;
      });
      
      var rightLabelCount = 0;
      $('.rickshaw_y-axis_right_label').each(function() {
        deferrerCollection.push($.Deferred());
        var newCanvasId = 'canvas_y-axis_right_label_' + rightLabelCount.toString() + '';
        self.renderDomElementOnNewCanvasWithDelay($( this ), newCanvasId, deferrerCollection[deferrerCollection.length - 1]);
        rightLabelCount++;
      });
      
      deferrerCollection.push($.Deferred());
      self.renderDomElementOnNewCanvasWithDelay(document.querySelector(".rickshaw_y-axis_left_label"), 'canvas_y-axis_left_label', deferrerCollection[deferrerCollection.length - 1]);

      $.when.apply($, deferrerCollection).then(function(){
        //merge all canvases into one
        var retVal = prepareNewBlankCanvas(".graph", 131);
        var canvas = retVal.canvas;
        var ctx = retVal.ctx;
        
        //get top left of .graph
        bodyRect = document.body.getBoundingClientRect();
        graphRect = d3.select(".graph").node().getBoundingClientRect();
        graphOffsetTop = graphRect.top - bodyRect.top;
        graphOffsetLeft = graphRect.left - bodyRect.left;
  
        self.mergeCanvases("#rickshaw_graphic_svg", "#canvas_graphic_svg", ctx, bodyRect, graphOffsetTop, graphOffsetLeft);
        self.mergeCanvases(".x_axis_d3", "#canvas_x_axis_d3", ctx, bodyRect, graphOffsetTop, graphOffsetLeft);
        self.mergeCanvases("#rickshaw_legend", "#canvas_legend", ctx, bodyRect, (graphOffsetTop+131), graphOffsetLeft);
        
        var yAxisCount = 0;
        $('.y_axis').each(function() {
          var newCanvasId = '#canvas_y_axis_' + yAxisCount.toString() + '';
          self.mergeCanvasesFromSourceObject($( this ), newCanvasId, ctx, bodyRect, graphOffsetTop, graphOffsetLeft);
          yAxisCount++;
        });
        
        var titleNode = document.getElementById("canvas_chart_title");
        var canvasExist = titleNode != null;
        if(canvasExist) {
          self.mergeCanvases("#rickshaw_chart_title", "#canvas_chart_title", ctx, bodyRect, graphOffsetTop, graphOffsetLeft);
        }
  
        self.modifyStylesAfterRendering();
        self.mergeLabelCanvases($(".rickshaw_y-axis_left_label"), "#canvas_y-axis_left_label", ctx, bodyRect, graphOffsetTop, graphOffsetLeft);

        var dataLabelCount = 0;
        $('.dataLabel').each(function() {
          var newCanvasId = '#canvas_dataLabel_' + dataLabelCount.toString() + '';
          self.mergeCanvasesFromSourceObject($( this ), newCanvasId, ctx, bodyRect, graphOffsetTop, graphOffsetLeft);
          dataLabelCount++;
        });
        
        var rightLabelCount = 0;
        $('.rickshaw_y-axis_right_label').each(function() {
          var newCanvasId = '#canvas_y-axis_right_label_' + rightLabelCount.toString() + '';
          self.mergeLabelCanvases($( this ), newCanvasId, ctx, bodyRect, graphOffsetTop, graphOffsetLeft);
          rightLabelCount++;
        });
        
        //convert to image
        try {
          downloadCanvas(canvas, "png");
          if(window.location.href.indexOf("csiDashboard/showDefault") > -1) {

            deferrerCollection.push($.Deferred());
            self.resizeGraphTo(previousWidth, previousHeight, deferrerCollection[deferrerCollection.length - 1]);
          }
        } 
        catch(err) {} // handle IE        
      });
    });
  }  
  
  this.assignAllRelevantCssToStyleAttributes = function() {
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
    d3.selectAll(".rickshaw_graph .y_ticks text, .rickshaw_graph .x_ticks_d3 text").style({
     "opacity": "0.5",
     "font-size": "9px",
     "pointer-events": "none",
     "color": "#333333",
     "font-family": "Helvetica,Arial,sans-serif",
     "line-height": "20px",
    });
  }

  this.modifyStylesForRendering = function() {
    d3.selectAll("#rickshaw_x-axis").style({
      "margin-left": "0px",
      });
    d3.selectAll(".x_axis_d3").style({
      "left": "0px",
      });
    d3.selectAll(".rickshaw_y-axis_left_label, .rickshaw_y-axis_right_label").style({
     "-moz-transform": "none", /* Firefox 3.6 Firefox 4 */
     "-webkit-transform": "none", /* Safari */
     "-o-transform": "none", /* Opera */
     "-ms-transform": "none", /* IE9 */
     "transform": "none", /* W3C */
    });
  }

  this.modifyStylesAfterRendering = function() {
    d3.selectAll(".rickshaw_y-axis_left_label, .rickshaw_y-axis_right_label").style({
      "-moz-transform": "rotate(-90deg)", /* Firefox 3.6 Firefox 4 */
      "-webkit-transform": "rotate(-90deg)", /* Safari */
      "-o-transform": "rotate(-90deg)", /* Opera */
      "-ms-transform": "rotate(-90deg)", /* IE9 */
      "transform": "rotate(-90deg)", /* W3C */
     });
  }

  this.renderDomElementOnNewCanvasWithDelay = function(domElement, newCanvasId, deferrer) {
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
  }


  this.resizeGraphTo = function(width, height, deferrer) {
    rickshawGraphBuilder.updateSize({
      width : width,
      height : height
    });

    if (parseInt(width) < 1070) {
      $("#rickshaw_legend > ul").css({
        "-moz-column-count" : 1 + "",
        "-webkit-column-count" : 1 + "",
        "column-count" : 1 + ""
      });
    } else {
      $("#rickshaw_legend > ul").css({
        "-moz-column-count" : 2 + "",
        "-webkit-column-count" : 2 + "",
        "column-count" : 2 + ""
      });
    }
    deferrer.resolve();
  }
  
  this.renderSvgElementOnNewCanvasWithDelay = function(svgElement, newCanvasId, deferrer) {
    var html2 = svgElement.clone().wrapAll("<div/>").parent().html();
    html2 = html2.replace(/<svg (.*?)>/, '<svg xmlns="http://www.w3.org/2000/svg" $1>');
    html2 = html2.replace(/top: -[\d]+\.?[\d]*px/, 'top: 0');
    
    if(!!navigator.userAgent.match(/Trident/)){
      html2 = html2.replace(/ xmlns=\"http:\/\/www.w3.org\/2000\/svg\"/, '');
    }
    var imgsrc = 'data:image/svg+xml;base64,'+ btoa(html2);
    var img = '<img src="'+imgsrc+'">';
    
    var canvas = document.createElement('canvas');
    canvas.setAttribute('id', newCanvasId);
    canvas.setAttribute('style', "display:none");
    canvas.width = 3000;
    canvas.height = 5000;
    document.body.appendChild(canvas);
    
    var ctx2 = canvas.getContext("2d");
    
    var image2 = new Image;
    image2.src = imgsrc;
    image2.onload = function() {
      ctx2.drawImage(image2, 0, 0);
      deferrer.resolve();
    }
  }

  this.mergeCanvases = function(originalElementId, sourceCanvasId, targetContext, bodyRect, graphOffsetTop, graphOffsetLeft) {
    curElemRect = document.querySelector(originalElementId).getBoundingClientRect();
    curElemOffsetTop = curElemRect.top - bodyRect.top;
    curElemOffsetLeft = curElemRect.left - bodyRect.left;
    distanceTop = curElemOffsetTop - graphOffsetTop;
    distanceLeft = curElemOffsetLeft - graphOffsetLeft;
    useMe = document.querySelector(sourceCanvasId);
    targetContext.drawImage(useMe,distanceLeft,distanceTop);
    removeObjectFromDom(sourceCanvasId);
  }

  this.mergeCanvasesFromSourceObject = function(originalElement, sourceCanvasId, targetContext, bodyRect, graphOffsetTop, graphOffsetLeft) {
    curElemRect = originalElement.offset();
    curElemOffsetTop = curElemRect.top - bodyRect.top;
    curElemOffsetLeft = curElemRect.left - bodyRect.left;
    distanceTop = curElemOffsetTop - graphOffsetTop;
    distanceLeft = curElemOffsetLeft - graphOffsetLeft;
    useMe = document.querySelector(sourceCanvasId);
    targetContext.drawImage(useMe,distanceLeft,distanceTop);
    removeObjectFromDom(sourceCanvasId);
  }

  this.mergeLabelCanvases = function(originalElement, sourceCanvasId, ctx, bodyRect, graphOffsetTop, graphOffsetLeft) {
    ctx.save();
    curElemRect = originalElement.offset();
    curElemOffsetTop = curElemRect.top - bodyRect.top;
    curElemOffsetLeft = curElemRect.left - bodyRect.left;
    distanceTop = curElemOffsetTop - graphOffsetTop;
    distanceLeft = curElemOffsetLeft - graphOffsetLeft;
    ctx.translate(distanceLeft, distanceTop);
    ctx.translate(Math.floor(originalElement.height() / 2), Math.floor(originalElement.width() / 2));
    ctx.rotate(-90*Math.PI/180);
    useMe = document.querySelector(sourceCanvasId);
    ctx.drawImage(useMe, (((originalElement.width()) / 2) * -1), (((originalElement.height()) / 2) * -1));
    removeObjectFromDom(sourceCanvasId);
    ctx.restore();
  }
  
  this.initialize(args);
}