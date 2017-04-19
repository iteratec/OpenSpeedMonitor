/**
 * Created by jwi on 19/04/17.
 */

describe("ChartColorProvider function getColorscaleForMeasurandGroup", function () {

    beforeEach( function () {
        // nothing to do here
    });

    it("provides the colors of the loading time measurand group", function () {
       var measurandUnit = "ms";
       var expectedColors = [
           "#1660A7",
           "#558BBF",
           "#95b6d7",
           "#d4e2ef"
           ];

       var chartColorProvider = OpenSpeedMonitor.ChartColorProvider();
       var colorscale = chartColorProvider.getColorscaleForMeasurandGroup(measurandUnit);

       expect(colorscale(0)).toEqual(expectedColors[0]);
       expect(colorscale(1)).toEqual(expectedColors[1]);
       expect(colorscale(2)).toEqual(expectedColors[2]);
       expect(colorscale(3)).toEqual(expectedColors[3]);
    });
});