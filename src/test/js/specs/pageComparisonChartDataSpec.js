describe("PageComparisonChartData data transformation", function () {
    var pageComparisonData = null;
    var width = 400;
    class PageComparisonSeriesBuilder {
        constructor() {
            this._series = {
                dimensionalUnit: 'ms',
                data: [
                    {
                        aggregationValue: 'avg',
                        grouping: "",
                        measurand: "",
                        value: 1
                    },
                    {
                        aggregationValue: 'avg',
                        grouping: "",
                        measurand: "",
                        value: 1
                    }
                ]
            }
        }
        makeDocComplete() {
            return this.measurand("DOC_COMPLETE_TIME");
        };
        makeSpeedIndex() {
            return this.measurand("SPEED_INDEX");
        };
        makeTTFB() {
            return this.measurand("FIRST_BYTE");
        }
        makeIncomingBytesFullyLoaded() {
            return this.measurand("FULLY_LOADED_INCOMING_BYTES");
        };
        makeRequestsDocComplete() {
            return this.measurand("DOC_COMPLETE_REQUESTS");
        }
        makeCustomerSatisfaction() {
            return this.measurand("CS_BY_WPT_DOC_COMPLETE");
        }
        measurand(measurand) {
            this._series.data[0].measurand = measurand;
            this._series.data[1].measurand = measurand;
            return this;
        }
        grouping(page,grouping) {
            this._series.data[page].grouping = grouping;
            return this;
        }
        groupingPage1(group) {
            return this.grouping(0,group)
        }
        groupingPage2(group) {
            return this.grouping(1,group)
        }
        valuePage1(val) {
            return this.value(0, val)
        }
        valuePage2(val) {
            return this.value(1, val)
        }
        value(page,value) {
            this._series.data[page].value = value;
            return this;
        }
        build() {
            return Object.assign({}, this._series)
        }
    }
    beforeEach(function () {
        $(document.body).append($("<svg width='" + width +"' />"));
        pageComparisonData = OpenSpeedMonitor.ChartModules.PageComparisonData(d3.select("svg"));
    });
    it("getDataForHeader should return a label with the equal job group", function () {
        pageComparisonData.setData({
            series: [
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group1 | Page1").valuePage1(300)
                    .groupingPage2("Group1 | Page2").valuePage2(600).build()
            ]
        });
        expect(pageComparisonData.getDataForHeader().text).toEqual("Group1 - Average");
    });
    it("getDataForHeader should return only aggregationtype as label if nothing is equal for all series", function () {
        pageComparisonData.setData({
            series: [
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group1 | Page1")
                    .valuePage1(300).groupingPage2("Group2 | Page2").valuePage2(600) .build()
            ]
        });
        expect(pageComparisonData.getDataForHeader().text).toEqual("Average");
    });
    it("getDataForBarScore should return the right maximum ", function () {
        var expectedMaximum = 9999;
        pageComparisonData.setData({
            series: [
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group1 | Page1").valuePage1(30)
                    .groupingPage2("Group1 | Page2").valuePage2(100).build(),
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group2 | Page3").valuePage1(expectedMaximum)
                    .groupingPage2("Group2 | Page4").valuePage2(600).build(),
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group3 | Page5").valuePage1(10)
                    .groupingPage2("Group3 | Page6").valuePage2(50).build()
            ]
        });
        expect(pageComparisonData.getDataForBarScore().max).toBe(expectedMaximum);
    });

    it("getDataForBarScore should return the right amount of bars", function () {
        pageComparisonData.setData({
            series: [
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group1 | Page1").valuePage1(30)
                    .groupingPage2("Group1 | Page2").valuePage2(100).build(),
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group2 | Page3").valuePage1(999)
                    .groupingPage2("Group2 | Page4").valuePage2(600).build(),
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group3 | Page5").valuePage1(10)
                    .groupingPage2("Group3 | Page6").valuePage2(50).build()
            ]
        });
        var barsOfFirstPage = pageComparisonData.getDataForBars(0);
        var barsOfSecondPage = pageComparisonData.getDataForBars(1);
        expect(barsOfFirstPage.values.length).toBe(3);
        expect(barsOfSecondPage.values.length).toBe(3);
    });
    it("getDataForBarScore should return the correct values", function () {
        var firstPage1 = 2;
        var firstPage2 = 3;
        var firstPage3 = 5;
        var secondPage1 = 6;
        var secondPage2 = 7;
        var secondPage3 = 8;
        pageComparisonData.setData({
            series: [
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group1 | Page1").valuePage1(firstPage1)
                    .groupingPage2("Group1 | Page2").valuePage2(secondPage1).build(),
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group2 | Page3").valuePage1(firstPage2)
                    .groupingPage2("Group2 | Page4").valuePage2(secondPage2).build(),
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group3 | Page5").valuePage1(firstPage3)
                    .groupingPage2("Group3 | Page6").valuePage2(secondPage3).build()
            ]
        });
        var firstPageValues = pageComparisonData.getDataForBars(0).values.map(function (page) {
            return page.value
        });
        var secondPageValues = pageComparisonData.getDataForBars(1).values.map(function (page) {
            return page.value
        });
        expect(firstPageValues).toEqual([firstPage1,firstPage2, firstPage3]);
        expect(secondPageValues).toEqual([secondPage1,secondPage2, secondPage3]);
    });
    it("getDataForBarScore should return the same maximum for first and second bar", function () {
        pageComparisonData.setData({
            series: [
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group1 | Page1").valuePage1(30)
                    .groupingPage2("Group1 | Page2").valuePage2(100).build(),
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group2 | Page3").valuePage1(999)
                    .groupingPage2("Group2 | Page4").valuePage2(600).build(),
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group3 | Page5").valuePage1(10)
                    .groupingPage2("Group3 | Page6").valuePage2(50).build()
            ]
        });
        var barsOfFirstPage = pageComparisonData.getDataForBars(0);
        var barsOfSecondPage = pageComparisonData.getDataForBars(1);
        expect(barsOfFirstPage.values.max).toEqual(barsOfSecondPage.values.max);
    });

    it("getDataForBarScore should return the right color for first and second pages", function () {
        pageComparisonData.setData({
            series: [
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group1 | Page1").valuePage1(30)
                    .groupingPage2("Group1 | Page2").valuePage2(100).build(),
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group2 | Page3").valuePage1(999)
                    .groupingPage2("Group2 | Page4").valuePage2(600).build(),
                new PageComparisonSeriesBuilder().makeDocComplete().groupingPage1("Group3 | Page5").valuePage1(10)
                    .groupingPage2("Group3 | Page6").valuePage2(50).build()
            ]
        });
        var barsOfFirstPage = pageComparisonData.getDataForBars(0);
        var barsOfSecondPage = pageComparisonData.getDataForBars(1);
        expect(barsOfFirstPage.values[0].color).toEqual("#1f77b4");
        expect(barsOfSecondPage.values[0].color).toEqual("#aec7e8");
        expect(barsOfFirstPage.values[1].color).toEqual("#ff7f0e");
        expect(barsOfSecondPage.values[1].color).toEqual("#ffbb78");
        expect(barsOfFirstPage.values[2].color).toEqual("#2ca02c");
        expect(barsOfSecondPage.values[2].color).toEqual("#98df8a");
    });
});