describe("GroupedBarchart Creation", function () {
    var id = "GroupedBarchartID";
    var dataWithOneGroupAndOneMeasurand = {
        "filterRules": {
            "OTTO_ADS-kleiderschrank": ["ADS | develop_Desktop"],
            "OTTO_Multistep_Desktop": ["ADS | develop_Desktop"],
            "OTTO_Multistep_Desktop_navigates": ["ADS | develop_Desktop"],
            "Scale_Loadtest_multistep": ["ADS | develop_Desktop"]
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
                "grouping": "ADS | develop_Desktop",
                "measurand": "doc complete (Bis zum onload-Event)",
                "originalMeasurandName": "docCompleteTimeInMillisecsUncached",
                "value": 722.7778,
                "valueComparative": null
            }], "dimensionalUnit": "ms", "stacked": null, "yAxisLabel": "Ladezeit [ms]"
        }]
    };

    var dataWithTwoGroupsAndOneMeasurand = {
        "filterRules": {
            "OTTO_ADS und HP": ["ADS_entry | develop_Desktop", "ADS_entry | develop_Smartphone"],
            "OTTO_ADS und HP_UserAgentSetzbar": ["ADS_entry | develop_Desktop", "ADS_entry | develop_Smartphone"]
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
                "grouping": "ADS_entry | develop_Desktop",
                "measurand": "doc complete (Bis zum onload-Event)",
                "originalMeasurandName": "docCompleteTimeInMillisecsUncached",
                "value": 2217.4543,
                "valueComparative": null
            }, {
                "grouping": "ADS_entry | develop_Smartphone",
                "measurand": "doc complete (Bis zum onload-Event)",
                "originalMeasurandName": "docCompleteTimeInMillisecsUncached",
                "value": 4695.7555,
                "valueComparative": null
            }], "dimensionalUnit": "ms", "stacked": null, "yAxisLabel": "Ladezeit [ms]"
        }]
    };

    var dataWithTwoGroupsAndTwoMeasurands = {
        "filterRules": {
            "OTTO_ADS und HP": ["ADS_entry | develop_Desktop", "ADS_entry | develop_Smartphone"],
            "OTTO_ADS und HP_UserAgentSetzbar": ["ADS_entry | develop_Desktop", "ADS_entry | develop_Smartphone"]
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
                "grouping": "ADS_entry | develop_Desktop",
                "measurand": "doc complete time",
                "originalMeasurandName": "docCompleteTimeInMillisecsUncached",
                "value": 2217.4543,
                "valueComparative": null
            }, {
                "grouping": "ADS_entry | develop_Smartphone",
                "measurand": "doc complete time",
                "originalMeasurandName": "docCompleteTimeInMillisecsUncached",
                "value": 4695.7555,
                "valueComparative": null
            }], "dimensionalUnit": "ms", "stacked": null, "yAxisLabel": "Loading Time [ms]"
        }, {
            "data": [{
                "grouping": "ADS_entry | develop_Desktop",
                "measurand": "bytes until doc complete",
                "originalMeasurandName": "docCompleteIncomingBytesUncached",
                "value": 0.5337830400466919,
                "valueComparative": null
            }, {
                "grouping": "ADS_entry | develop_Smartphone",
                "measurand": "bytes until doc complete",
                "originalMeasurandName": "docCompleteIncomingBytesUncached",
                "value": 0.5223064155578613,
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
        var chart = OpenSpeedMonitor.ChartModules.PageAggregationHorizontal(id);
        chart.drawChart(data);
        flushAllD3Transitions();
    }

    function createCopyOfDataWithDifferentUnit(unit) {
        var copy = $.extend(true, {}, dataWithOneGroupAndOneMeasurand); //make a copy of the object
        copy["series"][0]["dimensionalUnit"] = unit;
        return copy;
    }

    beforeEach(function () {
        $(document.body).append($('<div id=' + id + '></div>>'));
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
        expect($(".bar").text()).toContain("" + parseFloat(722.5).toFixed(0))
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
    })
});