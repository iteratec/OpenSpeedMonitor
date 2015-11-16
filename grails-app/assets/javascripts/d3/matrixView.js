/**
 * Creates a matrix view with colored data
 * @param data de.iteratec.osm.d3data.MatrixViewData-Object as JSON
 * @param chartDivIdentifier the id of the div where to draw the chart
 *                          the div has to have a defined size.
 */
function createMatrixView(data, chartDivIdentifier) {

    //pick div and set width
    var div = d3.select("#" + chartDivIdentifier);
    var divWidth = parseInt(div.style("width"), 10);

    // Define margins, width and height
    var margin = {top: 100, right: 100, bottom: 50, left: 100},
        width = divWidth - margin.right,
        tileSize = width / data.columnNames.length,
        tilePadding = 3,
        height = data.rowNames.length * tileSize;

    // Define color scale
    var colorCalibration = ["#F4C2C2","#F75D59","#FF2400","#E42217","#DC381F","#A40000", "#701C1C"];
    var colorScale = d3.scale.linear()
        .domain(d3.range(0, 1, 1.0 / (colorCalibration.length - 1)))
        .range(colorCalibration);
    var scaleForColorScale = d3.scale.linear()
        .domain([data.weightMin, data.weightMax])
        .range([0,1]);

    // does tooltip at mouse position
    var mousemove = function () {
        var xPosition = d3.event.pageX + 10;
        var yPosition = d3.event.pageY + 10;

        d3.select("#tooltipMatrixView")
            .style("left", xPosition + "px")
            .style("top", yPosition + "px");
        d3.select("#tooltipMatrixView #columnName")
            .text(data.columnLabel + ": " + d3.select(this).attr("columnName"));
        d3.select("#tooltipMatrixView #rowName")
            .text(data.rowLabel + ": " + d3.select(this).attr("rowName"));
        d3.select("#tooltipMatrixView #matrixWeight")
            .text(data.weightLabel + ": " + d3.select(this).attr("matrixWeight"));

        d3.select("#tooltipMatrixView").classed("hidden", false);
    };

    var mouseout = function () {
        d3.select("#tooltipMatrixView").classed("hidden", true);
    };

    // Create SVG Container
    var svg = div.append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    // scales
    var xScale = d3.scale.ordinal()
        .domain(data.columnNames)
        .rangeBands([0, width]);
    var yScale = d3.scale.ordinal()
        .domain(data.rowNames)
        .rangeBands([0, height]);

    //Defines axis
    var xAxis = d3.svg.axis()
        .scale(xScale)
        .orient("top");
    var yAxis = d3.svg.axis()
        .scale(yScale)
        .orient("left");

    // create squares for each possible entry
    for(var i = 0; i < data.columnNames.length; i++) {
        var currentHorizontalName = data.columnNames[i];

        for(var j = 0; j < data.rowNames.length; j++){
            var currentrowName = data.rowNames[j];

            svg.append("g")
                .attr("name", "" + makeValidString(currentHorizontalName + currentrowName))
                .append("rect")
                .attr("x", xScale(currentHorizontalName) + tilePadding)
                .attr("y", yScale(currentrowName) + tilePadding)
                .attr("width", tileSize - 2*tilePadding)
                .attr("height", tileSize - 2*tilePadding)
                .style("fill", "#F2F2F2");
        }

    }

    // Color tiles using weights from data
    for(var e = 0; e < data.entries.length; e++) {
        var entry = data.entries[e];

        svg.select("[name=" + makeValidString(entry.columnName + entry.rowName) + "]")
            .attr("matrixWeight", entry.weight)
            .attr("columnName", entry.columnName)
            .attr("rowName", entry.rowName)
            .on("mousemove", mousemove)
            .on("mouseout", mouseout)
            .select("rect")
            .style("fill", colorScale(scaleForColorScale(entry.weight)))
    }

    // draw axis
    svg.append("g")
        .attr("class", "xAxisMatrix")
        .call(xAxis)
        .selectAll("text")
        .attr("dx", ".5em")
        .attr("dy", "1em")
        .style("text-anchor", "start")
        .attr("transform", "rotate(-90)" );
    svg.append("g")
        .attr("class", "yAxisMatrix")
        .call(yAxis);

    // Append axis labels
    var labelContainer = svg.append("g")
        .attr("class", "matrixViewAxisLabel");
    labelContainer.append("text")
        .attr("text-anchor", "end")
        .attr("dy", "-3em")
        .text(data.columnLabel);
    labelContainer.append("text")
        .attr("dx", -margin.left)
        .text(data.rowLabel);

    // Append key
    var keyContainer = svg.append("g")
        .attr("transform", "translate(0," + height + ")");

    for(var k = 1; k <= colorCalibration.length; k++) {
        keyContainer.append("rect")
            .attr("x", k* 20)
            .attr("y", 10)
            .attr("width", 15)
            .attr("height", 15)
            .style("fill", colorCalibration[k]);
    }

    keyContainer.append("text")
        .attr("y", 40)
        .text(data.colorBrightLabel);
    keyContainer.append("text")
        .attr("x", (colorCalibration.length - 1) * 20)
        .attr("y", 40)
        .text(data.colorDarkLabel);
}

/**
 * Deletes special characters and white spaces
 * @param input
 * @returns {*|string}
 */
function makeValidString(input) {
    var result = input.trim();

    result = result.replace(" ", "");
    result = result.replace(/\.|\_|\#|\,/g, "");
    return result;
}
