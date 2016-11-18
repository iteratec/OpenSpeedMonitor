/**
 * Creates a multi line chart in given div
 * @param data de.itertec.osm.d3Data.MultiLineChart as JSON
 * @param chartDivIdentifier a unique identifier for the chart div
 * @param enableHover
 * @param customColorScale
 */
function createMultiLineGraph(
    data,
    chartDivIdentifier,
    enableHover,
    customColorScale,
    legendEntryClickCallback
) {
    var graphObject = {};
    //Values

    //Since we get our data unified and without ids, we create a map where each name get's an id.
    //If we do visual changes we can rely on this ids to get the right line.
    graphObject.idMap = {};
    //Functions
    data.lines.forEach(function (el,i,a) {
        graphObject.idMap[el.name] = i;
    });
    graphObject.identifier = chartDivIdentifier;
    graphObject.selectedName = "";
    graphObject.selectedId = "";
    graphObject.selectedLine = [[]];

    if(customColorScale == null){
        graphObject.colorScale = d3.scale.category20c();
    } else{
        graphObject.colorScale = customColorScale;
    }

    graphObject.getSelectedName = function(){return graphObject.selectedLine.datum().name};
    graphObject.getSelectedId = function(){return graphObject.idMap[graphObject.getSelectedName()]};
    graphObject.getColorForName = function(name){return graphObject.colorScale(name)};
    graphObject.clearGraph = function(){
            d3.select("#"+graphObject.identifier).html("");
    };

    graphObject.legendEntryClickCallback = legendEntryClickCallback;

    createGraph();

    function createGraph(){
        //pick div and set width
        var div = d3.select("#" + graphObject.identifier);
        var divWidth = $(window).innerWidth() * 0.5;

        // Get Lines from data
        var lines = [];
        for (var i = 0; i < data.lines.length; i++) {
            lines.push(data.lines[i]);
        }

        // Define margins, width and height
        var margin = {top: 20, right: 50, bottom: 50, left: 50},
            width = divWidth,
            height = 1 / 2 * width;

        // Create SVG Container
        var svg = div.append("svg")
            .attr("id", "csiMappingModalDialogSVG")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        // X and Y Scale
        var xScale = d3.scale.linear()
            .range([0, width]);
        var yScale = d3.scale.linear()
            .range([height, 0]);

        // Define axis
        var xAxis = d3.svg.axis()
            .scale(xScale)
            .ticks(Math.round(width / 50))
            .orient("bottom");
        var yAxis = d3.svg.axis()
            .scale(yScale)
            .ticks(Math.round(height / 50))
            .orient("left");

        // line connecting the data points
        var line = d3.svg.line()
            .interpolate("basis")
            .x(function (d) {
                return xScale(d.xValue)
            })
            .y(function (d) {
                return yScale(d.yValue)
            });


        // define domains
        var xMin = d3.min(lines, function (l) {
            return d3.min(l.xPoints)
        });
        var xMax = d3.max(lines, function (l) {
            return d3.max(l.xPoints)
        });
        var yMax = d3.max(lines, function (l) {
            return d3.max(l.yPoints)
        });
        xScale.domain([xMin, xMax]);
        yScale.domain([-2, yMax]);

        // draw axis
        svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height + ")")
            .call(xAxis);
        svg.append("g")
            .attr("class", "y axis")
            .call(yAxis);

        // Map data to names so each name gets its own line
        graphObject.colorScale.domain(lines, function (l) {
            return l.name
        });
        var oneLineData = lines.map(function (line) {
            return {
                name: line.name,
                id:graphObject.idMap[line.name],
                values: line.xPoints.map(function (d, i) {
                    return {xValue: line.xPoints[i], yValue: line.yPoints[i]}
                })
            };
        });

        // Define data and look of a line
        var oneLine = svg.selectAll(".oneLine")
            .data(oneLineData)
            .enter()
            .append("g")
            .attr("id", function (d){
                return "line_"+d.id
            })
            .attr("chosen", false)
            .attr("class", "oneLine");
        oneLine.append("path")
            .attr("class", "line")
            .attr("d", function (d) {
                return line(d.values);
            })
            .style("stroke", function (d) {
                return graphObject.colorScale(d.name)
            });


        // diagramKey
        var diagramKeySVG = div.append("svg")
            .attr("class", "diagramKeySvg")
            .attr("x", margin.left)
            .attr("y", height + margin.bottom)
            .attr("width", width)
            .attr("height", lines.length * 14);
        var diagramKey = diagramKeySVG.selectAll(".diagramKey")
            .data(lines)
            .enter()
            .append("g")
            .attr("name",function(d){return "diagramKey_"+ graphObject.idMap[d.name]})
            .attr("id",function(d){return "diagramKey_"+ graphObject.idMap[d.name]})
            .attr("class", "diagramKey");

        diagramKey.append("rect")
            .attr("x", 5)
            .attr("y", function (d, i) {
                return (i) * 12
            })
            .attr("height", 10)
            .attr("width", 10)
            .style("fill", function (d) {
                return graphObject.colorScale(d.name);
            });
        diagramKey.append("text")
            .attr("x", 20)
            .attr("y", function (d, i) {
                return (i) * 12
            })
            .attr("dy", "0.75em")
            .text(function (d) {
                return d.name;
            });

        var maxWidth = 400;
        //TODO firefox can't handle getBBox, we should calculate a good width
        //diagramKey.each(function(d){
        //    var node = d3.select(this).node();
        //    var currentWidth=node.getBBox();//.width;
        //    if(currentWidth>maxWidth) maxWidth=currentWidth;
        //    var currentHeight= node.getBBox();  //.height;
        //    if(currentHeight>maxHeight) maxHeight=currentHeight;
        //});
        var rects = diagramKey.append("rect")
            .attr("width", maxWidth+5)
            .attr("height", 12)
            .style("opacity", 0)
            .attr("y", function (d, i) {
                return (i) * 12
            });
        if(enableHover){
            rects.style("cursor", "pointer")
                 .on("mouseenter", function(d){handleDiagramKeyMouseEnter(d)})
                 .on("mouseleave", function(){handleDiagramKeyMouseLeave()})
                 .on("click", function(d){handleDiagramKeyClick(d.name)});
        }


        // Labels for Axis
        svg.append("text")
            .attr("class", "axisLabel")
            .attr("transform", "rotate(-90)")
            .attr("y", 0 - margin.left)
            .attr("x", 0 - (height / 2))
            .attr("dy", "1em")
            .style("text-anchor", "middle")
            .text(data.yLabel);
        svg.append("text")
            .attr("class", "axisLabel")
            .attr("toSelect", "x")
            .attr("y", height + margin.bottom)
            .attr("dy", "-0.35em")
            .attr("x", width)
            .text(data.xLabel);


        // --- Things for making tooltip and mouse event ---
        // append the rectangle to capture mouse
        bisectXIndex = d3.bisector(function(d) { return d; }).left;
        var focus = svg.append("g")
            .style("display", "none");
        svg.append("rect")
            .attr("width", width)
            .attr("height", height)
            .style("fill", "none")
            .style("pointer-events", "all")
            .on("mouseover", function() { focus.style("display", null); })
            .on("mouseout", function() { focus.style("display", "none"); })
            .on("mousemove", mousemove);
        // append the circle at the intersection
        focus.append("circle")
            .attr("class", "y")
            .style("fill", "none")
            .style("stroke", "blue")
            .attr("r", 2);
        // append the vertical line at mouse position
        focus.append("line")
            .attr("class", "verticalLine")
            .attr("y1", 0)
            .attr("y2", height);
        // append the horizontal line at mouse position
        focus.append("line")
            .attr("class", "horizontalLine")
            .attr("x1", -width)
            .attr("x2", width);

        // Text describes data at mouse position
        var tooltipTextContainer = focus.append("g")
            .attr("class", "tooltipTextContainer");
        tooltipTextContainer.append("rect")
            .attr("x", -50)
            .attr("width", 100)
            .attr("height", 20)
            .style("fill", "white");
        tooltipTextContainer.append("text")
            .style("text-anchor", "middle")
            .attr("y", 10)
            .attr("dy", ".35em");
        var xTextContainer = focus.append("g")
            .attr("class", "xTextContainer");
        xTextContainer.append("rect")
            .attr("x", -50)
            .attr("width", 100)
            .attr("height", 20)
            .style("fill", "white");
        xTextContainer.append("text")
            .style("text-anchor", "middle")
            .attr("y", 10)
            .attr("dy", ".35em");

        // function updating text and positions on mouse event
        function mousemove() {
            var mouseXValue = xScale.invert(d3.mouse(this)[0]);
            var mouseYValue = yScale.invert(d3.mouse(this)[1]);
            var closestLineIndex = 0;
            var closestDistance = yMax;
            var closestPointIndex;
            // find closest line to mouse
            for(var i = 0; i < lines.length; i++) {
                var lineToCheck = lines[i];

                var index = bisectXIndex(lineToCheck.xPoints, mouseXValue, 1);

                var distance = Math.abs(mouseYValue - lineToCheck.yPoints[index]);

                if(distance < closestDistance) {
                    closestPointIndex = index;
                    closestLineIndex = i;
                    closestDistance = distance;
                }
            }

            // Update Positions and text
            var yPoint = yScale(lines[closestLineIndex].yPoints[closestPointIndex]);
            focus.select("circle.y")
                .attr("transform", "translate(" + xScale(mouseXValue) + "," + yPoint + ")");
            focus.select(".verticalLine")
                .attr("transform", "translate(" + xScale(mouseXValue) + ",0)");
            focus.select(".horizontalLine")
                .attr("transform", "translate(" + xScale(mouseXValue) + "," + yPoint + ")");

            var toolTipYPos = d3.min([(yPoint + 20), (height - 30)]);
            focus.select(".tooltipTextContainer")
                .attr("transform", "translate(" + xScale(mouseXValue) + "," + toolTipYPos + ")");
            focus.select(".xTextContainer")
                .attr("transform", "translate(" + (xScale(mouseXValue) + 20) + "," + 0 + ")");
            tooltipTextContainer.select("text")
                .text(lines[closestLineIndex].name + ": " + lines[closestLineIndex].yPoints[closestPointIndex]);
            xTextContainer.select("text")
                .text(lines[closestLineIndex].xPoints[closestPointIndex]);
        }

        function handleDiagramKeyMouseEnter(d){
            var hover = makeSingleTextBlack(graphObject.idMap[d.name]);
            var chosen = graphObject.selectedLine;
            if(chosen[0][0] == null){
                highlightLine(d.name);
                hover.style("font-weight", "bold");
            }

        }

        function handleDiagramKeyMouseLeave(){
            var chosen = graphObject.selectedLine;
            selectAllDiagramKeysTexts().style("font-weight", "normal");
            if(chosen[0][0] == null){
                highlightLine(null);
                makeTextBlack();
            } else{
                highlightLine(chosen.datum().name);
                makeTextGrey();
                makeSingleTextBlack(graphObject.idMap[chosen.datum().name]).style("font-weight", "bold");
            }
        }

        function handleDiagramKeyClick(name){
            var chosen = d3.select("#"+graphObject.identifier).select("#line_"+ graphObject.idMap[name]);
            var bool = (chosen.attr("chosen") === "true");
            d3.select("#"+graphObject.identifier).selectAll(".oneLine").attr("chosen", false);
            if(bool){
                graphObject.selectedLine.attr("chosen", false);
                graphObject.selectedLine = [[]];
                makeTextBlack();
                highlightLine(null);
                if(graphObject.legendEntryClickCallback != undefined){
                    graphObject.legendEntryClickCallback(null)
                }
                graphObject.selectedName = "";
                graphObject.selectedId = "";
            } else{
                graphObject.selectedLine = chosen;
                graphObject.selectedLine.attr("chosen", true);
                makeTextGrey();
                makeSingleTextBlack(graphObject.getSelectedId()).style("font-weight", "bold");
                highlightLine(name);
                if(graphObject.legendEntryClickCallback != undefined){
                    graphObject.legendEntryClickCallback(name)
                }
                graphObject.selectedName = name;
                graphObject.selectedId = graphObject.idMap[name];
            }
        }

        /**
         * Change the color of a specific diagram key to black. The changed object will be returned.
         * @param name
         * @returns {*}
         */
        function makeSingleTextBlack(name){
            return d3.select("#"+graphObject.identifier).select("[name=diagramKey_"+name+"]").select("text").attr("fill", "black");
        }

        /**
         * Change the color of every diagram key within the given chartDivIdentifier to a specified grey
         */
        function makeTextGrey(){
            colorAllDiagramKeys("#DBDBDB");
        }
        /**
         * Change the color of every diagram key within the given chartDivIdentifier to black
         */
        function makeTextBlack(){
            colorAllDiagramKeys("black");
        }
        /**
         * Change the color of every diagram key within the given chartDivIdentifier to a specified color
         * @param color
         */
        function colorAllDiagramKeys(color){
            selectAllDiagramKeysTexts().attr("fill",color);
        }
        /**
         * Selects all texts from all diagram key whichs belongs to the given chartDivIdentifier
         * @returns {*}
         */
        function selectAllDiagramKeysTexts(){
            return d3.select("#"+graphObject.identifier).selectAll(".diagramKey").selectAll("text");
        }

        /**
         * Fades all non selected lines to grey. If this name is not defined, this method will just return.
         * If the name is null or empty this method will give every path it's origin color.
         * @param name
         */
        function highlightLine(name){
            var chosenOne =  d3.select("#"+graphObject.identifier).select("#line_"+graphObject.idMap[name]);
            //fade out all lines or set them to their origin color
            var allLines = d3.select("#"+graphObject.identifier).selectAll(".oneLine").select(".line");
            var colorFunction;
            if(name == "" || name == null || name == "null"){
                colorFunction = function(d){return graphObject.colorScale(d.name)};
            } else{
                colorFunction = function() {return "#DBDBDB"};
            }
            allLines.transition().duration(100).style("stroke", colorFunction).style("stroke-width",2);

            if(chosenOne[0][0] != null){
                //re-append line so it won't be hidden by other lines
                chosenOne.node().parentNode.appendChild(chosenOne.node());
                //If there is a transition running, this will stop it for
                //our chosen one and start a transition to it's origin color
                chosenOne.select(".line").transition().duration(100).style("stroke",graphObject.colorScale(name)).style("stroke-width",5);
            }
        }
    }

    return graphObject;
}

