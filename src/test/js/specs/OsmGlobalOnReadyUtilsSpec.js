describe("OpenSpeedMonitor.onReadyUtils()", function () {

    var osmOnReadyUtils;
    beforeEach(function () {
        osmOnReadyUtils = OpenSpeedMonitor.stringUtils;
    });

    it("function stringToBoolean(string) provides true for string 'yes'", function () {
        expect(osmOnReadyUtils.stringToBoolean("yes")).toBe(true);
    });
    it("function stringToBoolean(string) provides true for string 'true'", function () {
        expect(osmOnReadyUtils.stringToBoolean("yes")).toBe(true);
    });
    it("function stringToBoolean(string) provides true for string '1'", function () {
        expect(osmOnReadyUtils.stringToBoolean("yes")).toBe(true);
    });
    it("function stringToBoolean(string) provides true for string 'on'", function () {
        expect(osmOnReadyUtils.stringToBoolean("on")).toBe(true);
    });
    it("function stringToBoolean(string) provides false for string 'no'", function () {
        expect(osmOnReadyUtils.stringToBoolean("no")).toBe(false);
    });
    it("function stringToBoolean(string) provides false for string 'false'", function () {
        expect(osmOnReadyUtils.stringToBoolean("false")).toBe(false);
    });
    it("function stringToBoolean(string) provides false for string '0'", function () {
        expect(osmOnReadyUtils.stringToBoolean("0")).toBe(false);
    });
    it("function stringToBoolean(string) provides false for string 'off'", function () {
        expect(osmOnReadyUtils.stringToBoolean("off")).toBe(false);
    });

});
