/**
 * Appends a clock to the given node
 * @param node the parent node where the clock should be appended
 * @param hour the hour (int) the clock should show
 * @param size the size of the clock
 */
var appendClock = function (node, hour, size) {

    var width = size;             // Width of clock
    var height = size;             // Height of clock

    var cx = width / 2;          // Center x
    var cy = height / 2;          // Center y
    var margin = 3;
    var r = width / 2 - margin;  // Radius of clock face

    // clock container
    var container = node.append("svg")
        .attr("class", "clock")
        .attr("width", width)
        .attr("height", height)
        .attr("x", -(size / 2))
        .attr("y", 5);

    // clock background
    container.append("circle")
        .attr("class", "clockBorder")
        .attr("cx", cx)
        .attr("cy", cy)
        .attr("r", r);

    var data = [hour];
    // Create hands
    container.selectAll("line.hand")
        .data(data)
        .enter()
        .append("line")
        .attr("class", "hour hand")
        .attr("x1", cx)
        .attr("y1", cy + Math.round(0.10 * r))
        .attr("x2", cx)
        .attr("y2", Math.round(0.4 * r))
        .attr("transform", rotationTransform);


    function rotationTransform(d) {
        var angle = (d % 12) * 30; // 30 degrees per hour
        return "rotate(" + angle + "," + cx + "," + cy + ")"
    }

};

/**
 * Append a text visualization of the given hour (00:00) to the node
 * @param node the parent node where the text should be appended
 * @param hour the hour (int) for the text
 * @param size the size of the text box
 */
var appendDisplayText = function (node, hour,size) {
    var width = size;             // Width of container
    var height = size;             // Height of container
    var margin = 3;

    if(hour < 10) {
        var hourString = "0" + hour;
    }
    else {
        var hourString = "" + hour;
    }

    // text container
    var container = node.append("svg")
        .attr("class", "clock")
        .attr("width", width)
        .attr("height", height)
        .attr("x", -(size / 2))
        .attr("y", size+10);

    container.append("text")
        .attr("x", size/2)
        .attr("dy", ".9em")
        .style("text-anchor", "middle")
        .text(hourString);
};