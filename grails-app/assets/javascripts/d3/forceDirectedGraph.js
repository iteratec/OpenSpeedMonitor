//= require node_modules/d3/d3.min.js

/**
 * Create Force-Directed Graphs
 * @param rawWidth width of the graph
 * @param rawHeight height of the graph
 * @param nodeList List of string representation of all nodes
 * @param weightList list of numbers of weight for each node
 * @param elemPerChain list of int, count of Elements for each chain, thus
 *        sum(elemPerChain) has to be nodeList.length
 * @param id the id for the chart in case there are many chart on one side
 */
function createForceDirectedGraph(rawWidth, rawHeight, nodeList, weightList, elemPerChain, id) {

    // Defines the margins and the size of the diagram
    var margin = {top: 50, right: 30, bottom: 50, left: 30},
        width = rawWidth - margin.left - margin.right,
        height = rawHeight - margin.top - margin.bottom;

    var selector = "[id=" + id + "]";
    var chart = d3.select(selector)
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var numberOfChains = elemPerChain.length;

    var chainDistance = height / numberOfChains;
    var yOffset = -(Math.floor(numberOfChains / 2) * chainDistance);
    var index = 0;
    for (var i = 0; i < numberOfChains; i++) {
        var chainNodes = [];
        var chainWeigths = [];
        for (var j = 0; j < elemPerChain[i]; j++) {
            chainNodes.push(nodeList[index]);
            chainWeigths.push(weightList[index]);
            index++;
        }
        makeChain(chart, width, height, chainNodes, chainWeigths, yOffset);
        yOffset = yOffset + chainDistance;
    }
}

/**
 * Shows centered loading text
 * @param chart the container for the text
 * @param width the width of container
 * @param height the height of container
 */
var showLoadScreen = function (chart, width, height) {
    chart.append("text")
        .attr("class", "load")
        .attr("x", width / 2)
        .attr("y", height / 2)
        .attr("text-anchor", "middle")
        .text("Calculating forces...");
};
/**
 * Removes all loading texts from given container
 * @param chart the container contains the laoding text
 */
var hideLoadScreen = function (chart) {
    chart.selectAll(".load").remove();
};

/**
 * Creates Nodes with Attributes(incl. start point) and links from (node i) to (node i+1)
 * Lines up nodes from left to right
 * @param nodeList a List Nodenames
 * @param weightList a list of weights for the nodes. The weight with index i is mapped to the node with index i
 * @param width the width of the diagram (for positioning)
 * @param height the height of the diagram (for positioning)
 * @returns {{nodes: Array, links: Array}}
 */
function createNodesAndLinks(nodeList, weightList, width, height) {
    var nodesTmp = [];
    var linksTmp = [];
    var tmp;
    for (var i = 0; i < nodeList.length; i++) {
        nodesTmp.push({
            x: (i + 1) * width / (nodeList.length + 1),
            y: height / 2,
            name: nodeList[i],
            weight: weightList[i]
        });
        if (i < nodeList.length - 1) { // Last node has no link
            linksTmp.push({source: i, target: (i + 1)});
        }

    }
    return {nodes: nodesTmp, links: linksTmp};
}

/**
 * Makes one chain from given nodes
 * @param chart the chart where the chain has to be placed
 * @param width diagram widht
 * @param height diagram height
 * @param nodeList list of names for the nodes
 * @param weightList  a list of weights for the nodes. The weight with index i is mapped to the node with index i
 * @param yOffset offset for this chain. If offset is zero the chain will be drawn centered
 * @param numberOfChains the count of chains in this chart (for sizing elements)
 */
function makeChain(chart, width, height, nodeList, weightList, yOffset) {
    var container = chart.append("g")
        .attr("class", "container")
        .attr("width", "100%")
        .attr("height", "100%");

    // Creates nodes with attributes and links between nodes
    var nodesAndLinks = createNodesAndLinks(nodeList, weightList, width, height);
    var nodes = nodesAndLinks.nodes;
    var links = nodesAndLinks.links;

    // Scales the weighs to  forces
    var scaleForce = d3.scale.linear()
        .domain([d3.min(weightList), d3.max(weightList)])
        .range([0, 50]);

    // Defines the force
    var force = d3.layout.force()
        .size([width, height])
        .nodes(nodes)
        .links(links)
        .gravity(0)
        .charge(function (d) {
            return (scaleForce(d.weight));
        });

    // The link distance
    force.linkDistance(width / (nodeList.length + 1))
        .linkStrength(.10);

    // Puts Data in the svg container
    // important: First do the links, so nodes are on top of them
    var link = container.selectAll('.link')
        .data(links)
        .enter().append('line')
        .attr('class', 'link');

    // The size of one node before it's scaled with its weight
    var nodeSize = width / ((nodeList.length + 1) * 5);
    // scales the weigths for the visual dimension
    var weightScale = d3.scale.linear()
        .domain([d3.min(weightList), d3.max(weightList)])
        .range([0.0001, 2 * nodeSize]); // Avoids dividing by zero

    // a g-Element so the nodes can be selected
    var node = container.selectAll('.node')
        .data(nodes)
        .enter()
        .append("g")
        .attr("class", "nodeG");

    var nodeGElements = container.selectAll(".nodeG");

    // Textcontainer for mouse over
    nodeGElements.append("text")
        .attr("class", "mouseText")
        .attr("y", 10)
        .attr("x", 20)
        .attr("dy", ".35em")
        .style("text-anchor", "middle")
        .text("");

    // Creates SVG-Container for each node
    var nodeSVG = nodeGElements.append('svg')
        .attr('class', 'nodeSVG')
        .attr("width", function (d) {
            return (nodeSize + weightScale(d.weight)) * 2;
        })
        .attr("height", function (d) {
            return (nodeSize + weightScale(d.weight)) * 2;
        })
        .attr("text", function (d) {
            return d.name;
        })
        .attr("transform", function (d) {
            var abc = -(nodeSize + weightScale(d.weight));
            return "translate(" + abc + "," + abc + ")";
        })
        .on("mouseover", function (d) {
            d3.select(this.parentNode).select(".mouseText").text(d.name)
        })
        .on("mouseout", function (d) {
            d3.select(this.parentNode).select(".mouseText").text("")
        });
    //.call(force.drag);

    drawOneNode(nodeSVG);



    // advantageous if force.on('end'..)
    //force.on('start', function () {
    //    showLoadScreen(container, width, height);
    //});


    // Defines function which is called when calculations are finished or processing (ticks)
    // possible: on('end'...), on('tick'...).
    // 'End' leads to longer loading times but less rendering. Dragging using 'end' is possible but unattractive
    force.on('tick', function () {
        //hideLoadScreen(container);

        // Updates the position of the nodes as setting the center point of the circle to the calculated position
        nodeSVG.attr('x', function (d) {
            return d.x;
        })
            .attr('y', function (d) {
                return d.y + yOffset;
            });


        // Updates the link positions
        link.attr('x1', function (d) {
            return d.source.x;
        })
            .attr('y1', function (d) {
                return d.source.y + yOffset;
            })
            .attr('x2', function (d) {
                return d.target.x;
            })
            .attr('y2', function (d) {
                return d.target.y + yOffset;
            });

    });

    // starts the calculation
    force.start();
}

/**
 * Defines how one node will look like (In this case it looks like a browser)
 * @param nodeSVG the container where in the node is drawn
 * @param nodeSize the size of the nodes
 */
var drawOneNode = function (nodeSVG) {
    nodeSVG.append("rect")
        .attr("class", "nodeRect")
        .attr("rx", 0)
        .attr("ry", 0)
        .attr("x", function () {
            return d3.select(this.parentNode).attr("x");
        })
        .attr("y", function () {
            return d3.select(this.parentNode).attr("x");
        })
        .attr("width", function () {
            return d3.select(this.parentNode).attr("width");
        })
        .attr("height", function () {
            return d3.select(this.parentNode).attr("height");
        });

    nodeSVG.append("rect")
        .attr("class", "browserTop")
        .attr("rx", 0)
        .attr("ry", 0)
        .attr("x", function () {
            return d3.select(this.parentNode).attr("x");
        })
        .attr("y", function () {
            return d3.select(this.parentNode).attr("y");
        })
        .attr("width", function () {
            return d3.select(this.parentNode).attr("width");
        })
        .attr("height", function () {
            return d3.select(this.parentNode).attr("height") / 8;
        });
    nodeSVG.append("circle")
        .attr("class", "closeCircle")
        .attr("cx", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("x") + parent.attr("width") / 15;
        })
        .attr("cy", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("y") + (parent.attr("height") / 8) / 2;
        })
        .attr("r", function () {
            return d3.select(this.parentNode).attr("height") / 10 / 5;
        });
    nodeSVG.append("circle")
        .attr("class", "minCircle")
        .attr("cx", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("x") + parent.attr("width") / 9;
        })
        .attr("cy", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("y") + (parent.attr("height") / 8) / 2;
        })
        .attr("r", function () {
            return d3.select(this.parentNode).attr("height") / 10 / 5;
        });
    nodeSVG.append("circle")
        .attr("class", "maxCircle")
        .attr("cx", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("x") + parent.attr("width") / 6.5;
        })
        .attr("cy", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("y") + (parent.attr("height") / 8) / 2;
        })
        .attr("r", function () {
            return d3.select(this.parentNode).attr("height") / 10 / 5;
        });
    nodeSVG.append("rect")
        .attr("class", "browserSearch")
        .attr("rx", 0)
        .attr("ry", 0)
        .attr("x", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("x") + parent.attr("width") / 5;
        })
        .attr("y", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("y") + parent.attr("height") / 40;
        })
        .attr("width", function () {
            return d3.select(this.parentNode).attr("width") - d3.select(this.parentNode).attr("width") / 4;
        })
        .attr("height", function () {
            return d3.select(this.parentNode).attr("height") / 12;
        });

    nodeSVG.append("text")
        .attr("class", "browserText")
        .attr("text-anchor", "middle")
        .attr("x", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("x") + parent.attr("width") / 2;
        })
        .attr("y", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("y") + parent.attr("height") / 2;
        })
        .attr("dy", ".35em")
        .text(function () {
            return d3.select(this.parentNode).attr("text");
        })
        .style("fill", "#A9A9A9")
        .style("font-size", "1px")
        .each(getFontSize)
        .style("font-size", function (d) {
            return d.scaleFont + "px";
        });

    function getFontSize(d) {
        var bbox = this.getBBox(),
            cbbox = this.parentNode.getBBox(),
            scale = Math.min(cbbox.width / bbox.width, cbbox.height / bbox.height);
        d.scaleFont = scale;
    }
};
