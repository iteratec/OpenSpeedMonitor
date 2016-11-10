//= require bower_components/d3/d3.min.js
//= require d3/treeMapDesigner.js

/**
 * Creates a treemap and a list of data having weight = 0
 * If there is a div with id="treemapSpan" and another one with id="zeroWeightSpan"
 *  the diagram will use these divs and fit right into them.
 *
 * @param altWidth the width of the diagram if no suitable div is found (div id='zeroWeightSpan' and div id='treemapSpan')
 * @param altHeight the height of the diagram if no suitable div is found
 * @param data de.iteratec.osm.d3data.TreemapData-Object as JSON
 * @param design one of 'browser', 'rect'
 * @param id id of the div to draw the diagram in
 */
function createTreemap(altWidth, altHeight, data, design, id) {
    var rawWidth, rawHeight;

    // filter data with 0 weight
    // TODO remove filteredData from data (only necessary for less calculation)
    var filteredData = [];
    for (var i = 0; i < data.children.length; i++) {
        var testNode = data.children[i];
        if (testNode.weight == 0) {
            filteredData.push(testNode);
        }
    }

    // set sizes if divs are found
    if (!d3.select('#treemapSpan').empty() && !d3.select('#zeroWeightSpan').empty()) {
        // var treemapSpanWidth = parseInt(d3.select('#treemapSpan').style('width'), 10);
        var treemapSpanWidth = $(window).innerWidth() * 0.5;
        var zeroWeigthSpanWidth = parseInt(d3.select('#zeroWeightSpan').style('width'), 10);
        rawWidth = treemapSpanWidth;
        rawHeight = 2 / 3 * rawWidth;
    } else {
        rawWidth = altWidth;
        rawHeight = altHeight;
    }

    // sorts filteredData alphabetically
    filteredData.sort(function (a, b) {
        return a.name > b.name;
    });

    var selector = "[id=" + id + "]";
    var container = d3.select(selector);
    // Defines the margins and the size of the treemap
    var margin = {top: 10, right: 0, bottom: 10, left: 0},
        width = rawWidth - margin.left - margin.right,
        height = rawHeight - margin.top - margin.bottom;

    $('#treemapSpan')
        .css("display", "flex")

    // Set attributes of the treemap container
    container
        .style("width", width + margin.left + margin.right + "px")
        .style("height", height + margin.top + margin.bottom + "px")


    // does tooltip at mouse position
    var mousemove = function (d) {
        var xPosition = d3.event.pageX + 10;
        var yPosition = d3.event.pageY + 10;

        var treemapSpanTop = $('#treemapSpan').offset().top;
        var treemapSpanLeft = $('#treemapSpan').offset().left;

        var tooltip = d3.select("#tooltipTreemap");
        tooltip
            .style("left", xPosition - treemapSpanLeft + "px")
            .style("top", yPosition - treemapSpanTop + "px")
            .style("z-index", 3);
        d3.select("#tooltipTreemap #heading")
            .text(data.dataName + ": " + d.name);
        d3.select("#tooltipTreemap #info")
            .text(data.weightName + ": " + d.weight);
        d3.select("#tooltipTreemap").classed("hidden", false);
    };

    var mouseout = function () {
        d3.select("#tooltipTreemap").classed("hidden", true);
    };

    var treemap = d3.layout.treemap()
        .size([width, height])
        .value(function (d) {
            return d.weight;
        }); // Defines which attribute is pivotal for the size

    var div = container.append("div")
        .style("position", "relative")
        .style("width", (width + margin.left + margin.right) + "px")
        .style("height", (height + margin.top + margin.bottom) + "px")
        .style("left", margin.left + "px")
        .style("top", margin.top + "px");


    var node = div.datum(data).selectAll(".node")
        .data(treemap.nodes)
        .enter().append("svg")
        .attr("class", function (d) {
            if (d.children) {
                return "node inner"
            } else {
                return "node leaf"
            }
        });
    d3.selectAll(".leaf")
        .on("mousemove", mousemove)
        .on("mouseout", mouseout);


    // decides which design is used
    if (design.toLowerCase() == "browser") {
        node.call(designBrowser, true);
    }
    else {
        node.call(designRect);
    }

    addFilterBox();

    function addFilterBox() {
        var filterBox = d3.select('#zeroWeightSpan');
        filterBox.style("padding-left", "20px");

        filterBox.append("text")
            .html("<strong>" + data.zeroWeightLabel + ": </strong>");

        var listGroup = filterBox.append("ul")
            .attr("class", "list-group");
        for (var i = 0; i < filteredData.length; i++) {
            listGroup.append("li")
                .attr("class", "list-group-item")   // Should look nicer with Bootstrap3
                .html(filteredData[i].name);
        }
    }

}