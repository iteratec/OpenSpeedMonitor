/**
 * Creates a bar chart
 *
 * Data to inject:
 * altWidth : diagram width in px in case no suitable div is found (div id='barChartSpan')
 * altHeight : diagram height in px in case no suitable div is found (div id='barChartSpan')
 * data : de.iteratec.osm.d3data.BarChartData-Object as JSON
 * img : one of 'clocks', 'none'
 * id : the id of the barChart on this page (unique)
 *
 */
//= require node_modules/d3/d3.min.js
//= require d3/clocks.js

function createBarChart(altWidth, altHeight, data, img, id) {
    var rawWidth, rawHeight;

    if (!d3.select('#barChartSpan').empty()) {
        var barChartSpanWidth = $(window).innerWidth() * 0.5;
        rawWidth = barChartSpanWidth;
        rawHeight = 2 / 3 * rawWidth;
    } else {
        rawWidth = altWidth;
        rawHeight = altHeight;
    }

    var xValues = [];
    var yValues = [];

    // get Data from Json Object
    var bars = data.bars;
    for (var i = 0; i < bars.length; i++) {
        xValues.push(bars[i].name);
        yValues.push(bars[i].weight);
    }

// Defines the margins and the size of the diagram
    var margin = {top: 20, right: 30, bottom: 80, left: 50},
        width = rawWidth - margin.left - margin.right,
        height = rawHeight - margin.top - margin.bottom;

// creates chart with defined height and width
    var selector = "[id=" + id + "]";
    var chart = d3.select(selector)
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

// barwidth = width of diagram / data count (if there would be no space between them)
    var barWidth = width / bars.length;
    var barSpace = barWidth / 5; // scales spaces between bars


// scales data to fit into diagram
    var y = d3.scale.linear()
        .range([height, 0])
        .domain([0, Math.ceil(d3.max(yValues))]); // math.ceil rounds number upward
    var x = d3.scale.ordinal()
        .domain(xValues)
        .rangeBands([0, width]);


// defines bars and declares enter function (new data)
// does mouseOver effect
    var bar = chart.selectAll("g")
        .data(yValues)
        .enter().append("g")
        .attr("transform", function (d, i) {
            return "translate(" + (i * barWidth + barSpace / 2 ) + ", 0)";
        })
        .on("mouseover", function (d) {
            d3.select(this).select("text").text(d)
        })
        .on("mouseout", function (d) {
            d3.select(this).select("text").text("")
        });

// calculates position and size of one rect
    bar.append("rect")
        .attr("class", "barRect")
        .attr("y", function (d) {
            return y(d);
        })
        .attr("height", function (d) {
            return height - y(d);
        })
        .attr("width", barWidth - barSpace)

// Textcontainer for mouse over bar
    bar.append("text")
        .attr("y", function (d) {
            return y(d) - 10;
        })
        .attr("x", barWidth / 2)
        .attr("dy", ".35em")
        .style("text-anchor", "middle")
        .text("");

//Defines X axis
    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom");

// Adds axis and moves it down
    var xAxisGroup = chart.append("g")
        .attr("class", "xAxis")
        .attr("transform", "translate(0," + (height ) + ")")
        .call(xAxis);

//Defines Y Axis
    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left");

    var yAxisGroup = chart.append("g")
        .attr("class", "yAxis")
        .call(yAxis);

// Labels for Axis
    chart.append("text")
        .attr("class", "axisLabel")
        .attr("transform", "rotate(-90)")
        .attr("y", 0 - margin.left)
        .attr("x", 0 - (height / 2))
        .attr("dy", "1em")
        .text(data.yLabel);

    chart.append("text")
        .attr("class", "axisLabel")
        .attr("toSelect", "x")
        .attr("y", height + margin.bottom)
        .attr("dy", "-0.55em")
        .attr("x", width)
        .text(data.xLabel);


    // Create clock icons if img=='clocks'
    if (img.toLowerCase() == "clocks") {
        // Images instead of text
        chart.select(".xAxis").selectAll("text").remove();
        chart.select(".xAxis").selectAll("line").remove();
        chart.selectAll(".xAxis .tick").each(function (d) {
            var node = d3.select(this);
            appendClock(node, d, barWidth - 5);
            appendDisplayText(node, d, barWidth - 2);
        });
        // Move xAxis label down
        chart.select("[toSelect=x")
            .attr("y", height + margin.bottom - 5);
    }

}