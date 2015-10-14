// This class provides functions which can be called from a Container.
// e.g. mySVGContainer.call(designRect)

function designBrowser(selection, differentColors) {
    if (differentColors == true) {
        // Defines Colors
        var color = d3.scale.category20c();
    }
    else {
        var color = function () {
            return "#AAA"
        };
    }

    this
        .attr("text", function (d) {
            return d.children ? "" : d.name + ": " + d.weight;
        })
        .call(position);


    // Browsertop-Height
    var browserTopHeight = 25;

    // only leafs should have this design
    var leafs = d3.selectAll(".leaf");
    // x = 0 and y = 0 is the top left corner of the svg element
    leafs.append("rect")
        .attr("class", "nodeRect")
        .attr("x", 0)
        .attr("y", 0)
        .attr("width", function () {
            return d3.select(this.parentNode).attr("width");
        })
        .attr("height", function () {
            return d3.select(this.parentNode).attr("height");
        })
        .style("fill", function (d) {
            return color(d.name);
        });
    leafs.append("rect")
        .attr("class", "browserTop")
        .attr("x", 0)
        .attr("y", 0)
        .attr("width", function () {
            return d3.select(this.parentNode).attr("width");
        })
        .attr("height",browserTopHeight);
    leafs.append("circle")
        .attr("class", "closeCircle")
        .attr("r", (browserTopHeight / 4))
        .attr("cx", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("x") + parent.attr("width") / 22;
        })
        .attr("cy", browserTopHeight/2);
    leafs.append("circle")
        .attr("class", "minCircle")
        .attr("r", (browserTopHeight / 4))
        .attr("cx", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("x") + parent.attr("width") / 22 + 2.5 * d3.select(this).attr("r");
        })
        .attr("cy", browserTopHeight/2);
    leafs.append("circle")
        .attr("class", "maxCircle")
        .attr("r", (browserTopHeight / 4))
        .attr("cx", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("x") + parent.attr("width") / 22 + 5 * d3.select(this).attr("r");
        })
        .attr("cy", browserTopHeight/2);
    leafs.append("rect")
        .attr("class", "browserSearch")
        .attr("x", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("x") + parent.attr("width") / 15 + 7 * (browserTopHeight / 4);
        })
        .attr("y", function () {
            var parent = d3.select(this.parentNode);
            return browserTopHeight/5;
        })
        .attr("width", function () {
            return d3.select(this.parentNode).attr("width") - d3.select(this).attr("x") - 7;
        })
        .attr("height", function () {
            var parent = d3.select(this.parentNode);
            return browserTopHeight - 2*d3.select(this).attr("y");
        });

    leafs.append("text")
        .attr("class", "browserText")
        .attr("text-anchor", "middle")
        .attr("x", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("x") + parent.attr("width") / 2;
        })
        .attr("y", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("y") + parent.attr("height") / 2 + browserTopHeight/2;
        })
        .attr("dy", ".35em")
        .text(function () {
            return d3.select(this.parentNode).attr("text");
        })
        .each(getFontSize)
        .style("font-size", function (d) {
            return d.scaleFont + "px";
        });

    function getFontSize(d) {
        var bbox = this.getBBox(),
            cbbox = this.parentNode.getBBox(),
            scale = Math.min((cbbox.width - browserTopHeight) / bbox.width, (cbbox.height -browserTopHeight ) / bbox.height);
        d.scaleFont = scale;
    }
}

function designRect() {
    // Defines Colors
    var color = d3.scale.category20c();

    this
        .style("background", function (d) {
            return color(d.name);
        })
        .attr("text", function (d) {
            return d.children ? "" : d.name;
        })
        .call(position);

    var leafs = d3.selectAll(".leaf");

    leafs.append("text")
        .attr("class", "browserText")
        .attr("text-anchor", "left")
        .attr("x", function () {
            var parent = d3.select(this.parentNode);
            return 10 + parent.attr("x");
        })
        .attr("y", function () {
            var parent = d3.select(this.parentNode);
            return parent.attr("y") +  (parent.attr("height") -15 );
        })
        .attr("dy", ".35em")
        .text(function () {
            return d3.select(this.parentNode).attr("text");
        });
}

function position() {
    this.style("left", function (d) {
        return d.x + "px";
    })
        .style("top", function (d) {
            return d.y + "px";
        })
        .style("width", function (d) {
            return Math.max(0, d.dx - 1) + "px";
        })
        .style("height", function (d) {
            return Math.max(0, d.dy - 1) + "px";
        })
        .attr("width", function (d) {
            return Math.max(0, d.dx - 1);
        })
        .attr("height", function (d) {
            return Math.max(0, d.dy - 1);
        });
}