describe("GroupedBarchart Creation", function () {
    var id = "GroupedBarchartID";
    var dataWithOneGroupAndOneMeasurand = {
        "filterRules": {
            "Job1": ["Page1 | Desktop"],
            "Job2": ["Page1 | Desktop"],
            "Job3": ["Page1 | Desktop"],
            "Job4": ["Page1 | Desktop"]
        },
        "groupingLabel": "Page / JobGroup",
        "i18nMap": {
            "measurand": "Messgröße",
            "jobGroup": "Job Gruppe",
            "page": "Seite",
            "comparativeImprovement": "Verbesserung",
            "comparativeDeterioration": "Verschlechterung"
        },
        "series": [{
            "data": [{
                "grouping": "Page1 | Desktop",
                "measurand": "doc complete (Bis zum onload-Event)",
                "originalMeasurandName": "docCompleteTimeInMillisecsUncached",
                "value": 554.823,
                "valueComparative": null
            }], "dimensionalUnit": "ms", "stacked": null, "yAxisLabel": "Ladezeit [ms]"
        }]
    };

    var dataWithTwoGroupsAndOneMeasurand = {
        //Filter rules should be distinct, to test them
        "filterRules": {
            "Job1": ["Page1 | Desktop"],
            "Job2": ["Page1 | Smartphone"]
        },
        "groupingLabel": "Page / JobGroup",
        "i18nMap": {
            "measurand": "Messgröße",
            "jobGroup": "Job Gruppe",
            "page": "Seite",
            "comparativeImprovement": "Verbesserung",
            "comparativeDeterioration": "Verschlechterung"
        },
        "series": [{
            "data": [{
                "grouping": "Page1 | Desktop",
                "measurand": "doc complete (Bis zum onload-Event)",
                "originalMeasurandName": "docCompleteTimeInMillisecsUncached",
                "value": 1863.3596,
                "valueComparative": null
            }, {
                "grouping": "Page1 | Smartphone",
                "measurand": "doc complete (Bis zum onload-Event)",
                "originalMeasurandName": "docCompleteTimeInMillisecsUncached",
                "value": 5398.5369,
                "valueComparative": null
            }], "dimensionalUnit": "ms", "stacked": null, "yAxisLabel": "Ladezeit [ms]"
        }]
    };

    var dataWithTwoGroupsAndTwoMeasurands = {
        "filterRules": {
            "Job1": ["Page1 | Desktop", "Page1 | Smartphone"],
            "Job2": ["Page1 | Desktop", "Page1 | Smartphone"]
        },
        "groupingLabel": "Page / JobGroup",
        "i18nMap": {
            "measurand": "Measurand",
            "jobGroup": "Job Group",
            "page": "Page",
            "comparativeImprovement": "Improvement",
            "comparativeDeterioration": "Deterioration"
        },
        "series": [{
            "data": [{
                "grouping": "Page1 | Desktop",
                "measurand": "doc complete time",
                "originalMeasurandName": "docCompleteTimeInMillisecsUncached",
                "value": 35135.1235,
                "valueComparative": null
            }, {
                "grouping": "Page1 | Smartphone",
                "measurand": "doc complete time",
                "originalMeasurandName": "docCompleteTimeInMillisecsUncached",
                "value": 4521.12588,
                "valueComparative": null
            }], "dimensionalUnit": "ms", "stacked": null, "yAxisLabel": "Loading Time [ms]"
        }, {
            "data": [{
                "grouping": "Page1 | Desktop",
                "measurand": "bytes until doc complete",
                "originalMeasurandName": "docCompleteIncomingBytesUncached",
                "value": 0.5248963215878622,
                "valueComparative": null
            }, {
                "grouping": "Page1 | Smartphone",
                "measurand": "bytes until doc complete",
                "originalMeasurandName": "docCompleteIncomingBytesUncached",
                "value": 0.4852145636512224,
                "valueComparative": null
            }], "dimensionalUnit": "MB", "stacked": null, "yAxisLabel": "Size [kb]"
        }]
    };

    function flushAllD3Transitions() {
        //source: http://stackoverflow.com/a/22552228
        var now = Date.now;
        Date.now = function () {
            return Infinity;
        };
        d3.timer.flush();
        Date.now = now;
    }

    function drawChartWithTwoGroupsAndOnePage() {
        drawChart(dataWithTwoGroupsAndOneMeasurand);
    }

    function drawChartWithOneGroupeAndOnePage() {
        drawChart(dataWithOneGroupAndOneMeasurand);
    }

    function drawChart(data) {
        var chart = OpenSpeedMonitor.ChartModules.PageAggregation(id);
        chart.drawChart(data);
        flushAllD3Transitions();
    }

    function createCopyOfDataWithDifferentUnit(unit) {
        var copy = $.extend(true, {}, dataWithOneGroupAndOneMeasurand); //make a copy of the object
        copy["series"][0]["dimensionalUnit"] = unit;
        return copy;
    }

    function createRequiredDomElements(){
        var body = $(document.body);
        body.append($('<div id=' + id + '></div>>'));
        body.append($('<label class="btn btn-sm btn-default" id="inFrontButton"><input type="radio" name="mode">In Front</label>'));
        var filterGroup = $('<div id="filter-dropdown-group" class="btn-group">')
        filterGroup.append($('<button id="filter-dropdown" type="button" class="btn btn-default btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">Filtern <span class="caret"></span></button>'));
        filterGroup.append($(' <ul class="dropdown-menu pull-right"><li id="customer-journey-header" class="dropdown-header">Customer Journey </li></ul>'));
        body.append(filterGroup);
    }

    beforeEach(function () {
        createRequiredDomElements();
     });

    afterEach(function () {
        $("#" + id).remove();
    });

    it("should be exactly one bar in the chart, when we provide just data for one", function () {
        drawChartWithOneGroupeAndOnePage();
        expect($(".bar").length).toBe(1);
    });

    it("should be the right value within the bar", function () {
        drawChartWithOneGroupeAndOnePage();
        expect($(".bar").text()).toContain("" + parseFloat(554.823).toFixed(2))
    });

    it("should be the right unit for times on the bar", function () {
        drawChartWithOneGroupeAndOnePage();
        expect($(".bar").text()).toContain("ms")
    });

    it("should be the right unit for request size on the bar", function () {
        var singleGroupWithMB = createCopyOfDataWithDifferentUnit("MB");
        drawChart(singleGroupWithMB);
        expect($(".bar").text()).toContain("MB")
    });

    it("should be the right unit for request amount on the bar", function () {
        var singleGroupWithAmount = createCopyOfDataWithDifferentUnit("#");
        drawChart(singleGroupWithAmount);
        expect($(".bar").text()).toContain("#")
    });

    it("should be the right unit for percentages on the bar", function () {
        var singleGroupWithPercent = createCopyOfDataWithDifferentUnit("%");
        drawChart(singleGroupWithPercent);
        expect($(".bar").text()).toContain("%")
    });

    it("should be exactly two bars in the chart, when we provide data of two", function () {
        drawChartWithTwoGroupsAndOnePage();
        expect($(".bar").length).toBe(2);
    });

    it("should exist a legend entry for every unit", function () {
        drawChart(dataWithTwoGroupsAndTwoMeasurands);
        expect($(".d3chart-legend-entry").length).toBe(2)
    });

    it("should be a text for every measurand within the legend", function () {
        drawChart(dataWithTwoGroupsAndTwoMeasurands);
        var wholeText = $(".d3chart-legend-entry").text();
        expect(wholeText).toContain("doc complete time");
        expect(wholeText).toContain("bytes until doc complete");
    });

    it("should exist a group for every page group combination", function () {
       drawChart(dataWithTwoGroupsAndTwoMeasurands);
       expect($('.barGroup').length).toBe(2);
    });

    it("The inFront button should be disabled if we got multiple measurandgroups", function(){
        drawChart(dataWithTwoGroupsAndTwoMeasurands);
        expect($('#inFrontButton').attr("class")).toContain('disabled');
    });

    it("should be a reversed order of bars, after we clicked one of the sortiung buttons", function () {
        drawChartWithTwoGroupsAndOnePage();
        $('#all-bars-desc').click();
        flushAllD3Transitions();
        var firstDesc = $('.bar')[0];
        $('#all-bars-asc').click();
        var lastAsc = $('.bar')[0];
        flushAllD3Transitions();
        expect(firstDesc).toBe(lastAsc);
    });

    it("should be only one bar left, if we filter one of the two", function () {
       drawChartWithTwoGroupsAndOnePage();
       var lengthBefore = $('.bar').length;
       $('.filterRule')[0].click();
       flushAllD3Transitions();
       var lengthAfter = $('.bar').length;
       expect(lengthBefore-1).toBe(lengthAfter)
    });


});
