describe("PageAggregationChartData data transformation", function () {
    var pageAggregationData = null;
    var width = 1000;

    class SeriesBuilder {
        constructor() {
            this._hasComparativeData = false;
            this._series = {
                aggregationValue: "avg",
                measurandGroup: "UNKNOWN",
                unit: "?",
                measurand: "DOC_COMPLETE_TIME",
                measurandLabel: "DOC_COMPLETE_TIME_label",
                jobGroup: "TestJobGroup",
                page: "TestPage",
                value: 1.0,
                valueComparative: null
            };
        }
        makeDocComplete() {
            return this.makeLoadTime().measurand("DOC_COMPLETE_TIME");
        };
        makeSpeedIndex() {
            return this.makeLoadTime().measurand("SPEED_INDEX");
        };
        makeTTFB() {
            return this.makeLoadTime().measurand("FIRST_BYTE");
        }
        makeIncomingBytesFullyLoaded() {
            return this.makeRequestSize().measurand("FULLY_LOADED_INCOMING_BYTES");
        };
        makeRequestsDocComplete() {
            return this.makeRequestCounts().measurand("DOC_COMPLETE_REQUESTS");
        }
        makeCustomerSatisfaction() {
            return this.makePercentages().measurand("CS_BY_WPT_DOC_COMPLETE");
        }
        makeRequestSize() {
            this._series.measurandGroup = "REQUEST_SIZES";
            this._series.unit = "MB";
            return this;
        };
        makeRequestCounts() {
            this._series.measurandGroup = "REQUEST_COUNTS";
            this._series.unit = "#";
            return this;
        };
        makePercentages() {
            this._series.measurandGroup = "PERCENTAGES";
            this._series.unit = "%";
            return this;
        };
        makeLoadTime() {
            this._series.measurandGroup = "LOAD_TIMES";
            this._series.unit = "ms";
            return this;
        };
        measurand(measurand) {
            this._series.measurand = measurand;
            this._series.measurandLabel = measurand + "_label";
            return this;
        };
        jobGroup(jobGroup) {
            this._series.jobGroup = jobGroup;
            return this;
        };
        page(page) {
            this._series.page = page;
            return this;
        };
        value(value) {
            this._series.value = value;
            return this;
        };
        valueComparative(valueComparative) {
            this._series.valueComparative = valueComparative;
            return this;
        };
        build() {
            return Object.assign({}, this._series);
        };
    }

    beforeEach(function () {
        $(document.body).append($("<svg width='" + width +"' />"));
        pageAggregationData = OpenSpeedMonitor.ChartModules.PageAggregationData(d3.select("svg"));
    });

    it("getDataForHeader should return a label with job group and page for one if equal for all series", function () {
        pageAggregationData.setData({
            series: [
                new SeriesBuilder().makeDocComplete().jobGroup("TestGroup").page("TestPage").build(),
                new SeriesBuilder().makeSpeedIndex().jobGroup("TestGroup").page("TestPage").build()
            ]
        });
        expect(pageAggregationData.getDataForHeader().text).toEqual("TestGroup, TestPage - Average");
    });

    it("getDataForHeader should return a label with job group if jobGroup is equal for all series", function () {
        pageAggregationData.setData({
            series: [
                new SeriesBuilder().makeDocComplete().jobGroup("TestGroup").page("TestPage").build(),
                new SeriesBuilder().makeSpeedIndex().jobGroup("TestGroup").page("TestPage").build(),
                new SeriesBuilder().makeDocComplete().jobGroup("TestGroup").page("TestPage2").build()
            ]
        });
        expect(pageAggregationData.getDataForHeader().text).toEqual("TestGroup - Average");
    });

    it("getDataForHeader should return a label with page if page is equal for all series", function () {
        pageAggregationData.setData({
            series: [
                new SeriesBuilder().makeDocComplete().jobGroup("TestGroup1").page("TestPage").build(),
                new SeriesBuilder().makeDocComplete().jobGroup("TestGroup2").page("TestPage").build()
            ]
        });
        expect(pageAggregationData.getDataForHeader().text).toEqual("TestPage - Average");
    });

    it("hasStackedBars simply returns the internal boolean value", function () {
        expect(pageAggregationData.hasStackedBars()).toBe(true);
        pageAggregationData.setData({ stackBars: false });
        expect(pageAggregationData.hasStackedBars()).toBe(false);
        pageAggregationData.setData({});
        expect(pageAggregationData.hasStackedBars()).toBe(false);
        pageAggregationData.setData({ stackBars: true });
        expect(pageAggregationData.hasStackedBars()).toBe(true);
    });

    it("can determine if we have load times", function () {
        var incomingBytes = new SeriesBuilder().makeIncomingBytesFullyLoaded().build();
        var customerSatisfaction = new SeriesBuilder().makeCustomerSatisfaction().build();
        var requestCounts = new SeriesBuilder().makeRequestsDocComplete().build();
        var speedIndex = new SeriesBuilder().makeSpeedIndex().build();

        pageAggregationData.setData({
            series: [incomingBytes, customerSatisfaction, requestCounts]
        });
        expect(pageAggregationData.hasLoadTimes()).toBe(false);

        pageAggregationData.setData({
            series: [incomingBytes, customerSatisfaction, requestCounts, speedIndex]
        });
        expect(pageAggregationData.hasLoadTimes()).toBe(true);
    });

    it("can return all measurands", function () {
        pageAggregationData.setData({ series: [
            new SeriesBuilder().makeIncomingBytesFullyLoaded().build(),
            new SeriesBuilder().makeCustomerSatisfaction().build(),
            new SeriesBuilder().makeRequestsDocComplete().build(),
            new SeriesBuilder().makeSpeedIndex().build(),
            new SeriesBuilder().makeSpeedIndex().jobGroup("group2").build() // twice to check for deduplication
        ]});
        var expectedMeasurands = [ 'CS_BY_WPT_DOC_COMPLETE', 'DOC_COMPLETE_REQUESTS', 'FULLY_LOADED_INCOMING_BYTES', 'SPEED_INDEX' ];
        expect(pageAggregationData.getAllMeasurands().sort()).toEqual(expectedMeasurands.sort());
    });

    it("getAllMeasurands contains deterioration and improvement if comparative values are given", function () {
        pageAggregationData.setData(
            { hasComparativeData : true, series: [
            new SeriesBuilder().makeSpeedIndex().page("page1").value(800).valueComparative(2000).build(),
            new SeriesBuilder().makeSpeedIndex().page("page2").value(900).valueComparative(500).build()
        ]});
        var expectedMeasurands = [ 'SPEED_INDEX', 'SPEED_INDEX_improvement', 'SPEED_INDEX_deterioration'];
        expect(pageAggregationData.getAllMeasurands().sort()).toEqual(expectedMeasurands.sort());
    });

    it("getDataForBarScore determines correct min and max load_time values, starting from 0", function () {
        pageAggregationData.setData({ series: [
            new SeriesBuilder().makeIncomingBytesFullyLoaded().value(5000).build(),
            new SeriesBuilder().makeTTFB().value(2000).build(),
            new SeriesBuilder().makeSpeedIndex().value(1500).build()
        ]});
        expect(pageAggregationData.getDataForBarScore().min).toBe(0);
        expect(pageAggregationData.getDataForBarScore().max).toBe(2000);
    });

    it("getDataForBarScore determines correct min and max load_time values, starting from negative min", function () {
        pageAggregationData.setData({ series: [
            new SeriesBuilder().makeTTFB().value(2000).build(),
            new SeriesBuilder().makeSpeedIndex().value(-10).build()
        ]});
        expect(pageAggregationData.getDataForBarScore().min).toBe(-10);
        expect(pageAggregationData.getDataForBarScore().max).toBe(2000);
    });

    it("getDataForBarScore determines correct min and max load_time values, ending at 0", function () {
        pageAggregationData.setData({ series: [
            new SeriesBuilder().makeIncomingBytesFullyLoaded().value(-5000).build(),
            new SeriesBuilder().makeTTFB().value(-2000).build(),
            new SeriesBuilder().makeSpeedIndex().value(-10).build()
        ]});
        expect(pageAggregationData.getDataForBarScore().min).toBe(-2000);
        expect(pageAggregationData.getDataForBarScore().max).toBe(0);
    });

    it("sortByMeasurandOrder sorts a list of measurands by a fixed order, unknowns being last", function() {
        var sorted = pageAggregationData.sortByMeasurandOrder([
            'CS_BY_WPT_DOC_COMPLETE',
            'DOC_COMPLETE_REQUESTS',
            'FULLY_LOADED_INCOMING_BYTES',
            'foo_bar',
            'FIRST_BYTE',
            'VISUALLY_COMPLETE'
        ]);
        expect(sorted).toEqual([
            'CS_BY_WPT_DOC_COMPLETE',
            'VISUALLY_COMPLETE',
            'FIRST_BYTE',
            'FULLY_LOADED_INCOMING_BYTES',
            'DOC_COMPLETE_REQUESTS',
            'foo_bar'
        ]);
    });

    it("getDataForLegend returns an object containing the sorted entries with measurand labels", function () {
        pageAggregationData.setData({ series: [
            new SeriesBuilder().makeIncomingBytesFullyLoaded().page("page1").build(),
            new SeriesBuilder().makeTTFB().page("page1").build(),
            new SeriesBuilder().makeTTFB().page("page2").build(),
            new SeriesBuilder().makeSpeedIndex().page("page1").build(),
            new SeriesBuilder().makeSpeedIndex().page("page2").build()
        ]});
        var legendData = pageAggregationData.getDataForLegend();
        expect(legendData.entries.length).toBe(3);
        expect(legendData.entries[0].id).toBe("SPEED_INDEX");
        expect(legendData.entries[0].label).toBe("SPEED_INDEX_label");
        expect(legendData.entries[0].color).toBeDefined();
        expect(legendData.entries[1].id).toBe("FIRST_BYTE");
        expect(legendData.entries[1].label).toBe("FIRST_BYTE_label");
        expect(legendData.entries[1].color).toBeDefined();
        expect(legendData.entries[2].id).toBe("FULLY_LOADED_INCOMING_BYTES");
        expect(legendData.entries[2].label).toBe("FULLY_LOADED_INCOMING_BYTES_label");
        expect(legendData.entries[2].color).toBeDefined();
    });

    it("getDataForLegend contains deterioration and improvement if defined", function () {
        pageAggregationData.setData({ series: [
            new SeriesBuilder().makeTTFB().page("page1").value(1000).valueComparative(2000).build(),
            new SeriesBuilder().makeTTFB().page("page2").value(1000).valueComparative(500).build()
        ], i18nMap: {
            "comparativeImprovement": "improvementLabel",
            "comparativeDeterioration": "deteriorationLabel"
        }});
        var colorScale = OpenSpeedMonitor.ChartColorProvider().getColorscaleForTrafficlight();
        var legendData = pageAggregationData.getDataForLegend();
        expect(legendData.entries.length).toBe(3);
        expect(legendData.entries[0].id).toBe("FIRST_BYTE");
        expect(legendData.entries[0].label).toBe("FIRST_BYTE_label");
        expect(legendData.entries[0].color).toBeDefined();
        expect(legendData.entries[1].id).toBe("FIRST_BYTE_improvement");
        expect(legendData.entries[1].label).toBe("improvementLabel");
        expect(legendData.entries[1].color).toEqual(colorScale("good"));
        expect(legendData.entries[2].id).toBe("FIRST_BYTE_deterioration");
        expect(legendData.entries[2].label).toBe("deteriorationLabel");
        expect(legendData.entries[2].color).toEqual(colorScale("bad"));
    });

    it("getDataForLegend contains only deterioration if values are only higher", function () {
        pageAggregationData.setData({ series: [
            new SeriesBuilder().makeTTFB().page("page2").value(1000).valueComparative(500).build()
        ], i18nMap: {
            "comparativeDeterioration": "deteriorationLabel"
        }});
        var colorScale = OpenSpeedMonitor.ChartColorProvider().getColorscaleForTrafficlight();
        var legendData = pageAggregationData.getDataForLegend();
        expect(legendData.entries.length).toBe(2);
        expect(legendData.entries[0].id).toBe("FIRST_BYTE");
        expect(legendData.entries[0].label).toBe("FIRST_BYTE_label");
        expect(legendData.entries[0].color).toBeDefined();
        expect(legendData.entries[1].id).toBe("FIRST_BYTE_deterioration");
        expect(legendData.entries[1].label).toBe("deteriorationLabel");
        expect(legendData.entries[1].color).toEqual(colorScale("bad"));
    });

    it("getDataForLegend higher value in cs is improvement", function () {
        pageAggregationData.setData({ series: [
            new SeriesBuilder().makeCustomerSatisfaction().page("page1").value(50).valueComparative(10).build()
        ], i18nMap: {
            "comparativeImprovement": "improvementLabel"
        }});
        var colorScale = OpenSpeedMonitor.ChartColorProvider().getColorscaleForTrafficlight();
        var legendData = pageAggregationData.getDataForLegend();
        expect(legendData.entries.length).toBe(2);
        expect(legendData.entries[0].id).toBe("CS_BY_WPT_DOC_COMPLETE");
        expect(legendData.entries[0].label).toBe("CS_BY_WPT_DOC_COMPLETE_label");
        expect(legendData.entries[0].color).toBeDefined();
        expect(legendData.entries[1].id).toBe("CS_BY_WPT_DOC_COMPLETE_improvement");
        expect(legendData.entries[1].label).toBe("improvementLabel");
        expect(legendData.entries[1].color).toEqual(colorScale("good"));
    });

    it("getDataForSideLabels has empty texts for same pages and same job groups", function() {
        pageAggregationData.setData({
            series: [
                new SeriesBuilder().makeDocComplete().jobGroup("TestGroup").page("TestPage").build(),
                new SeriesBuilder().makeSpeedIndex().jobGroup("TestGroup").page("TestPage").build()
            ]
        });
        expect(pageAggregationData.getDataForSideLabels().labels).toEqual([""]);
    });

    it("getDataForSideLabels contains pages names for same job groups", function() {
        pageAggregationData.setData({
            series: [
                new SeriesBuilder().makeDocComplete().jobGroup("TestGroup").page("page1").build(),
                new SeriesBuilder().makeDocComplete().jobGroup("TestGroup").page("page2").build()
            ]
        });
        expect(pageAggregationData.getDataForSideLabels().labels).toEqual(["page1", "page2"]);
    });

    it("getDataForSideLabels contains job group names for same pages", function() {
        pageAggregationData.setData({
            series: [
                new SeriesBuilder().makeDocComplete().jobGroup("group1").page("page").build(),
                new SeriesBuilder().makeDocComplete().jobGroup("group2").page("page").build()
            ]
        });
        expect(pageAggregationData.getDataForSideLabels().labels).toEqual(["group1", "group2"]);
    });

    it("getDataForSideLabels contains job group and page names for different values", function() {
        pageAggregationData.setData({
            series: [
                new SeriesBuilder().makeDocComplete().jobGroup("group1").page("page1").build(),
                new SeriesBuilder().makeDocComplete().jobGroup("group2").page("page2").build()
            ]
        });
        expect(pageAggregationData.getDataForSideLabels().labels).toEqual(["page1, group1", "page2, group2"]);
    });

    it("getDataForBars returns data for the selected measurand, including value for missing series", function () {
        var page1DocComplete = new SeriesBuilder().makeDocComplete().page("page1").value(2000).build();
        var page1TTFB = new SeriesBuilder().makeTTFB().page("page1").value(5000).build();
        var page1Requests = new SeriesBuilder().makeRequestsDocComplete().page("page1").value(10).build();
        var page2DocComplete = new SeriesBuilder().makeDocComplete().page("page2").value(1000).build();
        var page2Requests = new SeriesBuilder().makeRequestsDocComplete().page("page2").value(9).build();
        pageAggregationData.setData({
            series: [ page1DocComplete, page2DocComplete, page1TTFB, page1Requests, page2Requests]
        });
        var docCompleteData = pageAggregationData.getDataForBars("DOC_COMPLETE_TIME");
        var ttfbData = pageAggregationData.getDataForBars("FIRST_BYTE");
        var requestsData = pageAggregationData.getDataForBars("DOC_COMPLETE_REQUESTS");

        expect(docCompleteData.max).toBe(5000);
        expect(docCompleteData.min).toBe(0);
        expect(docCompleteData.values).toEqual([page1DocComplete, page2DocComplete]);

        expect(requestsData.max).toBe(10);
        expect(requestsData.min).toBe(0);
        expect(requestsData.values).toEqual([page1Requests, page2Requests]);

        expect(ttfbData.max).toBe(5000);
        expect(ttfbData.min).toBe(0);
        expect(ttfbData.values).toEqual([page1TTFB, { page: 'page2', jobGroup: 'TestJobGroup', id: 'page2;TestJobGroup', value: null }]);
    });

    it("getDataForBars contains data for improvement and deterioration if defined", function () {
        pageAggregationData.setData({ series: [
            new SeriesBuilder().makeTTFB().page("page1").value(1000).valueComparative(2500).build(),
            new SeriesBuilder().makeTTFB().page("page2").value(1200).valueComparative(500).build()
        ]});
        var ttfbData = pageAggregationData.getDataForBars("FIRST_BYTE");
        var ttfbImprovementData = pageAggregationData.getDataForBars("FIRST_BYTE_improvement");
        var ttfbDeteriorationData = pageAggregationData.getDataForBars("FIRST_BYTE_deterioration");
        expect(ttfbData.min).toBe(-1500);
        expect(ttfbData.max).toBe(1200);
        expect(ttfbData.values[0].value).toBe(1200);
        expect(ttfbData.values[1].value).toBe(1000);

        expect(ttfbImprovementData.min).toBe(-1500);
        expect(ttfbImprovementData.max).toBe(1200);
        expect(ttfbImprovementData.values[0].value).toBeNull();
        expect(ttfbImprovementData.values[1].value).toBe(-1500);

        expect(ttfbDeteriorationData.min).toBe(-1500);
        expect(ttfbDeteriorationData.max).toBe(1200);
        expect(ttfbDeteriorationData.values[0].value).toBe(700);
        expect(ttfbDeteriorationData.values[1].value).toBeNull();
    });

    it("data is sorted ascending by highest order measurand", function() {
        pageAggregationData.setData({
            series: [
                new SeriesBuilder().makeDocComplete().page("page1").value(2000).build(),
                new SeriesBuilder().makeTTFB().page("page1").value(5000).build(),
                new SeriesBuilder().makeRequestsDocComplete().page("page1").value(9000).build(),
                new SeriesBuilder().makeDocComplete().page("page2").value(1000).build(),
                new SeriesBuilder().makeTTFB().page("page1").value(7000).build(),
                new SeriesBuilder().makeRequestsDocComplete().page("page2").value(9900).build()
            ],
            selectedFilter: "asc"
        });
        expect(pageAggregationData.getDataForSideLabels().labels).toEqual(["page2", "page1"]);
        expect(pageAggregationData.getDataForBars("FIRST_BYTE").values.map(v => v.page)).toEqual(["page2", "page1"]);
        expect(pageAggregationData.getDataForBars("DOC_COMPLETE_TIME").values.map(v => v.page)).toEqual(["page2", "page1"]);
        expect(pageAggregationData.getDataForBars("DOC_COMPLETE_REQUESTS").values.map(v => v.page)).toEqual(["page2", "page1"]);
    });

    it("data is sorted descending by highest order measurand", function() {
        pageAggregationData.setData({
            series: [
                new SeriesBuilder().makeDocComplete().page("page1").value(2000).build(),
                new SeriesBuilder().makeTTFB().page("page1").value(5000).build(),
                new SeriesBuilder().makeRequestsDocComplete().page("page1").value(9000).build(),
                new SeriesBuilder().makeDocComplete().page("page2").value(1000).build(),
                new SeriesBuilder().makeTTFB().page("page1").value(7000).build(),
                new SeriesBuilder().makeRequestsDocComplete().page("page2").value(9900).build()
            ],
            selectedFilter: "desc"
        });
        expect(pageAggregationData.getDataForSideLabels().labels).toEqual(["page1", "page2"]);
        expect(pageAggregationData.getDataForBars("FIRST_BYTE").values.map(v => v.page)).toEqual(["page1", "page2"]);
        expect(pageAggregationData.getDataForBars("DOC_COMPLETE_TIME").values.map(v => v.page)).toEqual(["page1", "page2"]);
        expect(pageAggregationData.getDataForBars("DOC_COMPLETE_REQUESTS").values.map(v => v.page)).toEqual(["page1", "page2"]);
    });

    it("data is can be filtered and sorted be predefined filterRule; adding missing series", function() {
        pageAggregationData.setData({
            series: [
                new SeriesBuilder().makeDocComplete().page("page1").jobGroup("group").value(10).build(),
                new SeriesBuilder().makeSpeedIndex().page("page1").jobGroup("group").value(20).build(),
                new SeriesBuilder().makeSpeedIndex().page("page2").jobGroup("group").value(200).build(),
                new SeriesBuilder().makeDocComplete().page("page3").jobGroup("group").value(1000).build(),
                new SeriesBuilder().makeSpeedIndex().page("page3").jobGroup("group").value(2000).build(),
                new SeriesBuilder().makeDocComplete().page("page4").jobGroup("group").value(10000).build(),
                new SeriesBuilder().makeSpeedIndex().page("page4").jobGroup("group").value(20000).build(),
                new SeriesBuilder().makeDocComplete().page("page4").jobGroup("different").value(15000).build(),
                new SeriesBuilder().makeSpeedIndex().page("page4").jobGroup("different").value(25000).build()
            ],
            filterRules: {
                "customFilter": [
                    {page: "page3", jobGroup: "group"},
                    {page: "page2", jobGroup: "group"},
                    {page: "page4", jobGroup: "different"}
                ]
            },
            selectedFilter: "customFilter"
        });

        expect(pageAggregationData.getDataForSideLabels().labels).toEqual(["page3, group", "page2, group", "page4, different"]);

        var docCompleteValues = pageAggregationData.getDataForBars("DOC_COMPLETE_TIME").values.map(v => [v.page, v.jobGroup, v.value]);
        expect(docCompleteValues).toEqual([["page3", "group", 1000], ["page2", "group", null], ["page4", "different", 15000]]);

        var speedIndexDataValues = pageAggregationData.getDataForBars("SPEED_INDEX").values.map(v => [v.page, v.jobGroup, v.value]);
        expect(speedIndexDataValues).toEqual([["page3", "group", 2000], ["page2", "group", 200], ["page4", "different", 25000]]);
    });
});
