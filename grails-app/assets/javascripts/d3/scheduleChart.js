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
 */
function createScheduleChart(data, id) {

    // pick div and set width
    var div = d3.select("#" + id);
    var divWidth = parseInt(div.style("width"), 10);

    // height for one job
    var jobHeight = 50;
    var jobPadding = 5;

    // format start and end date
    var startDate = new Date(data.startDate);
    var endDate = new Date(data.endDate);
    var startDateString = "" + startDate.getDate() + "." + startDate.getMonth() + "." + startDate.getFullYear() + ", "
        + twoDigitString(startDate.getHours()) + ":" + twoDigitString(startDate.getMinutes()) + ":" + twoDigitString(startDate.getSeconds());
    var endDateString = "" + endDate.getDate() + "." + endDate.getMonth() + "." + endDate.getFullYear() + ", "
        + twoDigitString(endDate.getHours()) + ":" + twoDigitString(endDate.getMinutes()) + ":" + twoDigitString(endDate.getSeconds());

    div.append("h5")
        .text(startDateString + " - " + endDateString);

    // Get Domain for the location
    var jobCountLocation = data.jobs.length;
    var minDate = getMinDate(data);
    var maxDate = getMaxDate(data);
    var jobNames = getJobNames(data);

    // Define margins, width and height
    var margin = {top: 20, right: 50, bottom: 20, left: 200},
        width = divWidth - margin.left - margin.right,
        height = jobCountLocation * jobHeight;

    // Scale for x-Axis (Time)
    var xScale = d3.time.scale()
        .domain([minDate, maxDate])
        .nice(d3.time.hour)
        .range([0, width]);
    // Scale for y-axis (Ordinal)
    var yScale = d3.scale.ordinal()
        .domain(d3.range(jobNames.length))
        .rangeBands([0, height]);

    // Define zoom behavior
    var zoom = d3.behavior.zoom()
        .x(xScale)
        .scaleExtent([0, Infinity])
        .on("zoom", zoomed);

    // Color scale for locations
    var locColor = d3.scale.category20b();

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

    var yOffset = 0;
    //iterate over jobs
    for (var j = 0; j < data.jobs.length; j++) {
        var job = data.jobs[j];

        var jobContainer = drawingPlane.append("g")
            .attr("name", job.name);

        var jobExe = jobContainer.selectAll(".jobExe")
            .data(job.executionDates)
            .enter()
            .append("g")
            .attr("transform", "translate(0," + yOffset + ")");

        // Append rect for each execution date
        jobExe.append("rect")
            .attr("duration", job.durationInMinutes)
            .attr("y", jobPadding)
            .attr("x", function (d) {
                return xScale(new Date(d));
            })
            .attr("width", function (d) {
                var startDate = new Date(d);
                var endDate = new Date(startDate.getTime() + (job.durationInMinutes * 60 * 1000));
                return xScale(endDate) - xScale(startDate);
            })
            .attr("height", jobHeight - 2 * jobPadding)
            .attr("class", "jobRect")
            .style("fill", locColor(job.name));

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
    locContainer.append("g")
        .attr("class", "y axis")
        .call(d3.svg.axis().scale(yScale).orient("left"));
    locContainer.selectAll(".y.axis .tick").each(function(d) {
        d3.select(this).select("text")
            .text(jobNames[d].name);
    });
    locContainer.selectAll(".y.axis .tick").each(function(d) {
        var node = d3.select(this)
            .append("text")
            .attr("x", -9)
            .attr("y", 15)
            .attr("dy",".32em")
            .style("text-anchor", "end")
            .text(jobNames[d].description);
    });
    locContainer.append("rect")
        .attr("height", height)
        .attr("width", 1)
        .attr("x", width);


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


    // Function called on zoom
    function zoomed() {
        locContainer.select(".x.axis").call(xAxis);
        locContainer.selectAll(".x.axis.top").call(xAxisTop);
        locContainer.select(".xAxisGrid").call(xAxisGrid);
        locContainer.selectAll(".jobRect")
            .attr("x", function (d) {
                return xScale(new Date(d));
            })
            .attr("width", function (d) {
                var startDate = new Date(d);
                var endDate = new Date(startDate.getTime() + (d3.select(this).attr("duration") * 60 * 1000));
                return xScale(endDate) - xScale(startDate);
            });
    }

    // function updating aid line position on mouse move
    function mousemove() {
        var mouseXValue = xScale.invert(d3.mouse(this)[0]);
        locContainer.select(".verticalLine")
            .attr("transform", "translate(" + xScale(mouseXValue) + ",0)");
    }


    // Resets the zoom and pan
    function reset() {
        zoom.translate([0, 0]).scale(1);
        zoomed();
    }
}

/**
 * Returns the earliest date found in all job execution dates
 * @param location the location
 * @returns the earliest date
 */
function getMinDate(location) {
    var locMinDates = [];

    for (var i = 0; i < location.jobs.length; i++) {
        locMinDates.push(d3.min(location.jobs[i].executionDates, function (x) {
            return new Date(x);
        }));
    }

    return d3.min(locMinDates);
}
/**
 * Returns the latest date found in all job execution dates
 * @param location the location
 * @returns the latest date
 */
function getMaxDate(location) {
    var locMinDates = [];

    for (var i = 0; i < location.jobs.length; i++) {
        locMinDates.push(d3.max(location.jobs[i].executionDates, function (x) {
            return new Date(x);
        }));
    }

    return d3.max(locMinDates);
}

/**
 * Returns the job names
 * @param locations the location to iterate over
 * @returns {Array} job names
 */
function getJobNames(location) {
    var jobNames = [];
    for (var j = 0; j < location.jobs.length; j++) {
        jobNames.push({name: location.jobs[j].name, description: location.jobs[j].description});
    }
    return jobNames;
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