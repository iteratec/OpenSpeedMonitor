//= require d3/d3.v3.js
/**
 * JS Class for Creating ScheduleCharts with de.iteratec.osm.d3Data.ScheduleChartData as JSON objects
 * Class uses d3.v3.js
 *
 *
 * @author Marcus Meier
 */

/**
 * Creates a zoomable ScheduleChart
 * ScheduleChart fits into container with given id
 * Its prerequisite that the container has a defined width
 *
 * @param data de.iteratec.osm.d3Data.ScheduleChartData as JSON
 * @param id a unique id for the container (div) to draw in
 * @param formId a unique id for the form belongs to the chart
 */
function createScheduleChart(rawdata, id, formId) {

    var data = rawdata;

    // pick div and set width
    var div = d3.select("#" + id);
    var divWidth = parseInt(div.style("width"), 10);

    // height for one job
    var jobHeight = 40;
    var jobPadding = 3;

    // start value
    var selectedHour = 6;
    var showIntersections = false;

    // format start and end date
    var startDate = new Date(data.startDate);
    var endDate = new Date(startDate);
    endDate.setHours(startDate.getHours() + selectedHour);

    var startDateString = getDateString(startDate);
    var endDateString = getDateString(endDate);

    div.append("h5")
        .attr("class", "intervalHeadline")
        .text(startDateString + " - " + endDateString);

    // Get Domain for the location
    var jobCount = data.jobs.length;
    var jobNamesAndDescriptions = getJobNames(data);
    var jobNamesTrimmed = trimJobNames(jobNamesAndDescriptions);

    // Define margins, width and height
    var margin = {top: 20, right: 50, bottom: 20, left: 200},
        width = divWidth - margin.left - margin.right,
        height = jobCount * jobHeight;

    // Scale for x-Axis (Time)
    var xScale = d3.time.scale()
        .nice(d3.time.hour)
        .range([0, width]);
    xScale.domain([startDate, endDate]);
    // Scale for y-axis (Ordinal)
    var yScale = d3.scale.ordinal()
        .domain(d3.range(jobNamesAndDescriptions.length))
        .rangeBands([0, height]);

    // Define zoom behavior
    var zoom = d3.behavior.zoom()
        .x(xScale)
        .scaleExtent([0, Infinity])
        .on("zoom", performScaling);

    // Color scale for locations
    var locColor = d3.scale.category20c();


    // svg container
    var locContainer = div.append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    // create drawing plane
    var drawingPlane = locContainer.append("svg")
        .attr("width", width)
        .attr("height", height);

    // append rect showing selected interval
    drawingPlane.append("rect")
        .attr("width", width)
        .attr("height", height)
        .style("fill", "lightgrey")
        .style("opacity", "0.4");
    drawingPlane.append("rect")
        .attr("class", "intervalRect")
        .attr("height", height)
        .style("fill", "white");

    // append job container
    var yOffset = 0;
    for (var i = 0; i < data.jobs.length; i++) {
        drawingPlane.append("g")
            .attr("class", "jobContainer" + i)
            .attr("transform", "translate(0," + yOffset + ")");
        yOffset += jobHeight;
    }

    // Append Axis
    var xAxis = d3.svg.axis()
        .scale(xScale)
        .orient("bottom");
    var xAxisTop = d3.svg.axis()
        .scale(xScale)
        .orient("top");
    var xAxisGrid = d3.svg.axis()
        .scale(xScale)
        .orient("bottom")
        .tickSize(-height);
    locContainer.append("g")
        .attr("class", "x axis")
        .call(xAxis)
        .attr("transform", "translate(0," + height + ")");
    locContainer.append("g")
        .attr("class", "x axis top")
        .call(xAxisTop);
    locContainer.append("g")
        .attr("class", "xAxisGrid")
        .call(xAxisGrid)
        .attr("transform", "translate(0," + height + ")");
    var yAxisContainer = locContainer.append("g")
        .attr("class", "y axis")
        .call(d3.svg.axis().scale(yScale).orient("left"));
    locContainer.selectAll(".y.axis .tick").each(function (d) {
        d3.select(this).select("text")
            .text(jobNamesTrimmed[d]);
    });
    locContainer.selectAll(".y.axis .tick").each(function (d) {
        var node = d3.select(this)
            .append("text")
            .attr("x", -9)
            .attr("y", 15)
            .attr("dy", ".32em")
            .style("text-anchor", "end")
            .text(jobNamesAndDescriptions[d].description);
    });
    // Vertical border on the right-hand side
    locContainer.append("rect")
        .attr("height", height)
        .attr("width", 1)
        .attr("x", width);

    // Tooltip on mouse event on y axis ticks
    locContainer.selectAll(".y.axis .tick")
        .style("pointer-events", "all")
        .on("mousemove", function (d) {
            var xPosition = d3.event.pageX + 10;
            var yPosition = d3.event.pageY + 10;

            d3.select("#tooltip")
                .style("left", xPosition + "px")
                .style("top", yPosition + "px");
            d3.select("#tooltip #heading")
                .text(jobNamesAndDescriptions[d].name);
            d3.select("#tooltip #info")
                .text(jobNamesAndDescriptions[d].description);
            d3.select("#tooltip").classed("hidden", false);
        })
        .on("mouseout", function () {
            d3.select("#tooltip").classed("hidden", true);
        });


    // Append Reset button
    var buttonContainer = locContainer.append("g")
        .attr("transform", "translate(" + width + "," + height + ")")
        .on("click", function (d) {
            reset()
        });
    buttonContainer.append("rect")
        .attr("class", "resetButton")
        .attr("width", margin.right)
        .attr("height", margin.bottom);

    buttonContainer.append("text")
        .attr("class", "resetButtonText")
        .text("Reset")
        .attr("x", margin.right / 2)
        .attr("y", margin.bottom / 2)
        .attr("dy", ".35em");

    // List discounted Jobs
    if (data.discountedJobs.length > 0) {
        div.append("h5")
            .text(data.discountedJobsLabel);
        for (var k = 0; k < data.discountedJobs.length; k++) {
            div.append("text")
                .html(data.discountedJobs[k] + "<br />");
        }
    }

    // Append vertical aid line at mouse position
    var verticalLine = locContainer.append("line")
        .attr("class", "verticalLine")
        .attr("y1", 0)
        .attr("y2", height);

    // Append area for spotting mouse events
    locContainer.append("rect")
        .attr("class", "mouseEventArea")
        .attr("width", width)
        .attr("height", height)
        .style("fill", "none")
        .style("pointer-events", "all")
        .on("mouseover", function () {
            verticalLine.style("display", null);
        })
        .on("mouseout", function () {
            verticalLine.style("display", "none");
        })
        .on("mousemove", mousemove)
        .call(zoom);
    // Add listener to radio buttons
    d3.selectAll("#" + formId).selectAll("input").on("change", change);


    // function updating aid line position on mouse move
    function mousemove() {
        var mouseXValue = xScale.invert(d3.mouse(this)[0]);
        locContainer.select(".verticalLine")
            .attr("transform", "translate(" + xScale(mouseXValue) + ",0)");
    }

    // updates the x scales of the objects
    function performScaling() {
        locContainer.select(".x.axis").call(xAxis);
        locContainer.selectAll(".x.axis.top").call(xAxisTop);
        locContainer.select(".xAxisGrid").call(xAxisGrid);
        locContainer.selectAll(".jobRect")
            .attr("x", function (d) {
                return xScale(new Date(d));
            })
            .attr("width", function (d) {
                var startDate = new Date(d);
                var endDate = new Date(startDate);
                endDate.setSeconds(startDate.getSeconds() + d3.select(this).attr("duration"))
                return xScale(endDate) - xScale(startDate);
            });
        drawingPlane.select(".intervalRect")
            .attr("x", xScale(startDate))
            .attr("width", xScale(endDate) - xScale(startDate));
        drawingPlane.selectAll(".intersectionRect")
            .attr("x", function (d) {
                return xScale(d.start);
            })
            .attr("width", function (d) {
                return xScale(d.end) - xScale(d.start);
            });

    }

    // Resets the zoom and pan
    function reset() {
        zoom.translate([0, 0]).scale(1);
        performScaling();
    }


    // handles checked radio button change
    function change() {
        if (this.value === "on") {
            showIntersections = true;
            drawIntersectionRects()
            performScaling()
        } else if (this.value === "off") {
            showIntersections = false;
            drawingPlane.selectAll(".intersectionRect").remove()
            performScaling()
        } else {
            selectedHour = parseInt(this.value);
            endDate = new Date(startDate);
            endDate.setHours(startDate.getHours() + selectedHour);
            updateGraph()
        }
    }


    // updates graph data
    function updateGraph() {
        xScale.domain([startDate, endDate]);
        zoom.x(xScale);

        drawingPlane.select(".intervalRect")
            .attr("x", xScale(startDate))
            .attr("width", xScale(endDate) - xScale(startDate));

        div.select(".intervalHeadline")
            .text(startDateString + " - " + getDateString(endDate));

        //iterate over jobs
        for (var j = 0; j < data.jobs.length; j++) {
            var job = data.jobs[j];
            var usedExecutionDates = getExecutionDates(job, startDate, selectedHour);

            // select job container
            var jobContainer = drawingPlane.select(".jobContainer" + j);

            var jobExe = jobContainer.selectAll(".jobExe").data(usedExecutionDates);
            jobExe.enter()
                .append("g")
                .attr("class", "jobExe")
                .append("rect")
                .attr("duration", job.durationInSeconds)
                .attr("y", jobPadding)
                .attr("height", jobHeight - 2 * jobPadding)
                .attr("class", "jobRect")
                .style("fill", locColor(job.name));

            jobExe.exit().remove();

        }

        // Update intersection rects
        if(showIntersections == true) {
            drawIntersectionRects();
        }

        // scale everything
        performScaling();
    }

    // Draws red rectangles for intersections
    function drawIntersectionRects() {
        var intersections = calculateIntersections(data, startDate, selectedHour, data.agentCount - 1);

        var redScale = d3.scale.linear()
            .domain([data.agentCount, intersections.maxIntersections])
            .range([230, 255]);
        var opacityScale = d3.scale.linear()
            .domain([data.agentCount, intersections.maxIntersections])
            .range([0.2, 0.7]);

        var intersectionContainer = drawingPlane.selectAll(".intersectionRect")
            .data(intersections.intersections);
        intersectionContainer.enter()
            .append("rect")
            .attr("class", "intersectionRect")
            .attr("height", height)
            .style("fill", function (d) {
                return d3.rgb(redScale(d.intersectionCount), 0, 0);
            })
            .style("opacity", function (d) {
                return opacityScale(d.intersectionCount)
            });
        intersectionContainer.exit().remove();
    }

    // First time drawing chart
    updateGraph();
}

/**
 * Returns the job names and their description
 * @param locations the location to iterate over
 * @returns {object} with form {name: String, description: String}
 */
function getJobNames(location) {
    var jobNames = [];
    for (var j = 0; j < location.jobs.length; j++) {
        jobNames.push({name: location.jobs[j].name, description: location.jobs[j].description});
    }
    return jobNames;
}
/**
 * Cuts equal beginnings of the job names and returns a new list as result
 * If jobNames.length <= 1 then result == jobNames
 * @param jobNames job names to be trimmed
 * @return {Array} the trimmed job Names
 */
function trimJobNames(jobNames) {
    var result = [];
    for (var j = 0; j < jobNames.length; j++) {
        result.push(jobNames[j].name.slice(0))
    }
    if(jobNames.length <= 1) {
        return result;
    }
    var change = true;
    while (change) {
        var letter = result[0].charAt(0);
        if (result.every(function (elem) {
                return beginsWith(elem, letter)
            })) {
            for (var i = 0; i < result.length; i++) {
                result[i] = result[i].substring(1);
            }
        } else {
            change = false;
        }
    }
    return result;
}

/**
 * Checks if a word begins with a given letter
 * @param word the word to check
 * @param letter the letter to check
 * @returns {boolean} true if word.charAt(0) == letter
 */
function beginsWith(word, letter) {
    return word.charAt(0) == letter;
}
/**
 * Returns a String consists of minimum two Digits
 * @param number the Number to convert
 * @returns {string} "0[Number]" if Number less than 10, "[number]" otherwise
 */
function twoDigitString(number) {
    if (number < 10) {
        return "0" + number;
    }
    else {
        return "" + number;
    }
}

/**
 * Gets the execution dates for a job in the next X hours
 * @param job the job to get the dates for
 * @param selectedHour the interval
 * @returns {Array} an Array of execution dates. An empty array if none is found
 */
function getExecutionDates(job, startdate, selectedHour) {
    var result = [];
    var endOfInterval = new Date(startdate);
    endOfInterval.setHours(startdate.getHours() + selectedHour);
    var allExecutionDates = job.executionDates;

    var counter = 0;
    var loopDate = new Date(allExecutionDates[counter]);
    while ((loopDate <= endOfInterval) && !(typeof allExecutionDates[counter] === undefined)) {
        result.push(allExecutionDates[counter]);
        counter++;
        loopDate = new Date(allExecutionDates[counter]);
    }

    return result;
}

/**
 * Creates a string representing a given date [DD.MM.YYYY, HH:MM:SS]
 * @param date the date
 * @returns {string} the string representation
 */
function getDateString(date) {
    return "" + date.getDate() + "." + date.getMonth() + "." + date.getFullYear() + ", "
        + twoDigitString(date.getHours()) + ":" + twoDigitString(date.getMinutes()) + ":" + twoDigitString(date.getSeconds());
}

/**
 * Calculates the intersections of dates
 * @param data the incoming data
 * @param startDate the start date
 * @param selectedHours the length of the interval
 * @returns {Object} with form: {maxIntersections: int, intersections: Array}  intersections has objects having form: {start: Date, end: Date, intersectionCount: Int}
 */
function calculateIntersections(data, startDate, selectedHours, toleratedIntersectionCount) {
    var result = {maxIntersections: 0, intersections: []};
    var endDate = new Date(startDate);
    endDate.setHours(startDate.getHours() + selectedHours);
    var allExecutionDates = data.allExecutionDates.slice();
    var allEndDates = data.allEndDates.slice();
    allExecutionDates.sort();
    allEndDates.sort();


    // Gets and removes the first element from array
    var selectedDate = new Date(allExecutionDates.shift());
    var nextExeDate = new Date(allExecutionDates.shift());
    var nextEndDate = new Date(allEndDates.shift());
    var intersections = 0;
    var maxIntersections = 0;

    while (!allExecutionDates.length == 0 && !allEndDates.length == 0 && (selectedDate <= endDate)) {
        var interval;

        if (nextExeDate <= nextEndDate) {
            interval = {start: selectedDate, end: nextExeDate, intersectionCount: intersections};
            selectedDate = nextExeDate;
            nextExeDate = new Date(allExecutionDates.shift());
            intersections++;
            if (intersections > maxIntersections) {
                maxIntersections = intersections;
            }
        }
        else {
            interval = {start: selectedDate, end: nextEndDate, intersectionCount: intersections};
            selectedDate = nextEndDate;
            nextEndDate = new Date(allEndDates.shift());
            intersections--;
        }

        if (interval.intersectionCount > toleratedIntersectionCount && interval.start < interval.end) {
            result.intersections.push(interval);
        }
    }

    result.maxIntersections = maxIntersections;
    return result;
}