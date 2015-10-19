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


    // create Headlines
    div.append("h4")
        .text(data.name);
    div.append("h5")
        .text(startDateString + " - " + endDateString);

    // get locations
    var locations = data.locations;

    // Overall Domain
    var minDate = getMinDate(locations);
    var maxDate = getMaxDate(locations);
    var jobCount = getJobCount(locations);
    var jobNames = getJobNames(locations);
    var locationNames = getLocationNames(locations);
    var yRangeLocations = getYRangeLocations(locations, jobHeight);

    // Define margins, width and height
    var margin = {top: 20, right: 100, bottom: 20, left: 150},
        width = divWidth - margin.left - margin.right,
        height = jobCount * jobHeight;


    // Scale for x-Axis (Time)
    var xScale = d3.time.scale()
        .domain([minDate, maxDate])
        .nice(d3.time.hour)
        .range([0, width]);
    // Scale for y-axis (Ordinal)
    var yScale = d3.scale.ordinal()
        .domain(jobNames)
        .rangeBands([0, height]);
    // Scale for right hand side y-Axis (Location ordinal scale)
    var yScaleRight = d3.scale.ordinal()
        .domain(locationNames)
        .range(yRangeLocations);

    // Define zoom behavior
    var zoom = d3.behavior.zoom()
        .x(xScale)
        .scaleExtent([0, Infinity])
        .on("zoom", zoomed);

    // Color scale for locations
    var locColor = d3.scale.category20b();


    // Create SVG Container
    var svg = div.append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .call(zoom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    // Create drawing plane
    var drawingPlane = svg.append("svg")
        .attr("width", width)
        .attr("height", height);

    // Iterate over locations
    var yOffset = 0;
    for (var i = 0; i < locations.length; i++) {
        var loc = locations[i];
        var locContainer = drawingPlane.append("g")
            .attr("locName", loc.name);

        // iterate over jobs
        for (var j = 0; j < loc.jobs.length; j++) {
            var job = loc.jobs[j];

            var jobContainer = locContainer.append("g")
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

        // make Seperator after Location
        svg.append("rect")
            .attr("class", "seperator")
            .attr("height", 1)
            .attr("width", width + margin.right)
            .attr("y", yOffset);
    }

    // Resets the zoom and pan
    function reset() {
        zoom.translate([0, 0]).scale(1);
        zoomed();
    }


    // Append Axis
    var xAxis = d3.svg.axis()
        .scale(xScale)
        .orient("bottom");
    var xAxisGrid = d3.svg.axis()
        .scale(xScale)
        .orient("bottom")
        .tickSize(-height);
    svg.append("g")
        .attr("class", "x axis")
        .call(xAxis)
        .attr("transform", "translate(0," + height + ")");
    svg.append("g")
        .attr("class", "xAxisGrid")
        .call(xAxisGrid)
        .attr("transform", "translate(0," + height + ")");
    svg.append("g")
        .attr("class", "y axis")
        .call(d3.svg.axis().scale(yScale).orient("left"));
    svg.append("g")
        .attr("transform", "translate(" + width + ",0)")
        .attr("class", "locationAxis")
        .call(d3.svg.axis().scale(yScaleRight).orient("right"));
    svg.append("rect")
        .attr("height", height)
        .attr("width", 1)
        .attr("x", width);


    // Append Reset button
    var buttonContainer = svg.append("g")
        .attr("transform", "translate(" + width + "," +  height + ")")
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



    // List discounted Locations
    if (data.discountedLocations.length > 0) {
        div.append("h5")
            .text(data.discountedLocationsLabel);
        for (var a = 0; a < data.discountedLocations.length; a++) {
            div.append("text")
                .html(data.discountedLocations[a] + "<br />");
        }
    }

    // List discounted Jobs
    if (data.discountedJobs.length > 0) {
        div.append("h5")
            .text(data.discountedJobsLabel);
        for (var k = 0; k < data.discountedJobs.length; k++) {
            div.append("text")
                .html(data.discountedJobs[i] + "<br />");
        }
    }

    // Function called on zoom
    function zoomed() {
        svg.select(".x.axis").call(xAxis);
        svg.select(".xAxisGrid").call(xAxisGrid);
        svg.selectAll(".jobRect")
            .attr("x", function (d) {
                return xScale(new Date(d));
            })
            .attr("width", function (d) {
                var startDate = new Date(d);
                var endDate = new Date(startDate.getTime() + (d3.select(this).attr("duration") * 60 * 1000));
                return xScale(endDate) - xScale(startDate);
            });
    }
}

/**
 * Returns the earliest date found in all job execution dates
 * @param locations the locations to iterate over
 * @returns the earliest date
 */
function getMinDate(locations) {
    var locMinDates = [];

    for (var i = 0; i < locations.length; i++) {
        var loc = locations[i];
        locMinDates.push(d3.min(loc.jobs, function (d) {
            return d3.min(d.executionDates, function (x) {
                return new Date(x);
            });
        }));
    }
    return d3.min(locMinDates);
}
/**
 * Returns the latest date found in all job execution dates
 * @param locations the locations to iterate over
 * @returns the latest date
 */
function getMaxDate(locations) {
    var locMaxDates = [];

    for (var i = 0; i < locations.length; i++) {
        var loc = locations[i];
        locMaxDates.push(d3.max(loc.jobs, function (d) {
            return d3.max(d.executionDates, function (x) {
                return new Date(x);
            });
        }));
    }
    return d3.min(locMaxDates);
}
/**
 * Return the overall count of jobs
 * @param locations the locations to iterate over
 * @returns {number} count of overall jobs
 */
function getJobCount(locations) {
    var jobCount = 0;

    for (var i = 0; i < locations.length; i++) {
        jobCount += locations[i].jobs.length;
    }
    return jobCount;
}
/**
 * Returns the job names
 * @param locations the locations to iterate over
 * @returns {Array} job names
 */
function getJobNames(locations) {
    var jobNames = [];
    for (var i = 0; i < locations.length; i++) {
        var loc = locations[i];

        for (var j = 0; j < loc.jobs.length; j++) {
            jobNames.push("" + loc.jobs[j].name);
        }

    }
    return jobNames;
}
/**
 * Returns the location Names
 * @param locations the locations to iterate over
 * @returns {Array} the location names
 */
function getLocationNames(locations) {
    var locationNames = [];

    for (var i = 0; i < locations.length; i++) {
        locationNames.push("" + locations[i].name);
    }

    return locationNames;
}
/**
 * Returns a List of Numbers which are centered in the location area
 * @param locations the locations to iterate over
 * @param jobHeight the Height of one job
 * @returns {Array} of Numbers
 */
function getYRangeLocations(locations, jobHeight) {
    var yRange = [];

    var offset = 0;

    for (var i = 0; i < locations.length; i++) {
        locationMid = (locations[i].jobs.length * jobHeight) / 2;

        offset += locationMid;

        yRange.push(offset);

        offset += locationMid;
    }

    return yRange;
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