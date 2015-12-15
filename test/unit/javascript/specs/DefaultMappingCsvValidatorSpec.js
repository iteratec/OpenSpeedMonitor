describe("validate csv", function () {
    POSTLOADED = {};

    beforeEach(function () {
        POSTLOADED.defaultMappingFormatError = "DefaultMappingFormatError";
        POSTLOADED.loadTimeIntegerError = "LoadTimeIntegerError";
        POSTLOADED.customerFrustrationDoubleError = "CustomerFrustrationDoubleError";
        POSTLOADED.defaultMappingNotAllvaluesError = "DefaultMappingNotAllValuesError";
        POSTLOADED.customerSatisfactionNotInPercentError = "CustomerSatisfactionNotInPercentError";
        POSTLOADED.percentagesBetween0And1Error = "PercentagesBetween0And1Eroor";
    });

    it("correct csv file return no error message", function () {
        // create csv string
        var csv = "name;loadTimeInMilliSecs;customerSatisfactionInPercent\n";
        for (var i = 0; i <= 20000; i += 50) {
            csv += "Default1;" + i + ";50.0\n";
        }

        var errorMessages = validate(csv);
        expect(errorMessages.length).toBe(0);
    });

    it("percentages in ragen of 0 and 1 should resolve in error", function () {
        // create csv string
        var csv = "name;loadTimeInMilliSecs;customerSatisfactionInPercent\n";
        for (var i = 0; i <= 20000; i += 50) {
            // returns number between 0 and 1
            csv += "Default1;" + i + ";" + 0.5 + "\n";
        }

        var errorMessages = validate(csv);
        expect(errorMessages.length).toBe(1);
        expect(errorMessages[0]).toBe(POSTLOADED.percentagesBetween0And1Error);
    });

    it("customer satisfaction not in range of 0 and 100 should resolve in error", function () {
        // create csv string
        var csv = "name;loadTimeInMilliSecs;customerSatisfactionInPercent\n";
        for (var i = 0; i <= 20000; i += 50) {
            // returns number between 0 and 1
            csv += "Default1;" + i + ";" + 150 + "\n";
        }

        var errorMessages = validate(csv);
        expect(errorMessages.length).toBe(1);
        expect(errorMessages[0]).toBe(POSTLOADED.customerSatisfactionNotInPercentError);
    });

    it("negative customer satisfaction should resolve in error", function () {
        // create csv string
        var csv = "name;loadTimeInMilliSecs;customerSatisfactionInPercent\n";
        for (var i = 0; i <= 20000; i += 50) {
            // returns number between 0 and 1
            csv += "Default1;" + i + ";" + -10 + "\n";
        }

        var errorMessages = validate(csv);
        expect(errorMessages.length).toBe(3);
        expect(errorMessages.indexOf(POSTLOADED.customerSatisfactionNotInPercentError) >= 0).toBeTruthy();
        expect(errorMessages.indexOf(POSTLOADED.percentagesBetween0And1Error) >= 0).toBeTruthy();
        expect(errorMessages.indexOf(POSTLOADED.customerFrustrationDoubleError) >= 0).toBeTruthy();
    });

    it("customer satisfaction not as double should resolve in error", function () {
        // create csv string
        var csv = "name;loadTimeInMilliSecs;customerSatisfactionInPercent\n";
        for (var i = 0; i <= 20000; i += 50) {
            // returns number between 0 and 1
            csv += "Default1;" + i + ";" + "10.5.5" + "\n";
        }

        var errorMessages = validate(csv);
        expect(errorMessages.length).toBe(1);
        expect(errorMessages[0]).toBe(POSTLOADED.customerFrustrationDoubleError);
    });

    it("load time not as integer should resolve in error", function () {
        // create csv string
        var csv = "name;loadTimeInMilliSecs;customerSatisfactionInPercent\n";
        for (var i = 0; i <= 20000; i += 50) {
            // returns number between 0 and 1
            csv += "Default1;" + i + "a;" + "10.5" + "\n";
        }

        var errorMessages = validate(csv);
        expect(errorMessages.length).toBe(1);
        expect(errorMessages[0]).toBe(POSTLOADED.loadTimeIntegerError);
    });

    it("csv does not contain values between 0 and 20000 in 50ms steps resolve in error", function () {
        // create csv string
        var csv = "name;loadTimeInMilliSecs;customerSatisfactionInPercent\n";
        for (var i = 0; i <= 200; i += 50) {
            csv += "Default1;" + i + ";50.0\n";
        }

        var errorMessages = validate(csv);
        expect(errorMessages.length).toBe(1);
        expect(errorMessages[0]).toBe(POSTLOADED.defaultMappingNotAllvaluesError);
    });
});

describe("searchForNamesInCsv", function() {
    var correctCsv = "";

    beforeEach(function() {
        correctCsv = "name;loadTimeInMilliSecs;customerSatisfactionInPercent\n";
        for (var i = 0; i <= 20000; i += 50) {
            correctCsv += "Default1;" + i + ";50.0\n";
        }
    });

    it("name already in csv", function() {
        var names = ["Default1", "Default2"];
        var existingNames = searchForNamesInCsv(names, correctCsv);
        expect(existingNames.length).toBe(1);
        expect(existingNames[0]).toBe("Default1");
    });

    it("name not in csv", function() {
        var names = ["Default2"];
        var existingNames = searchForNamesInCsv(names, correctCsv);
        expect(existingNames.length).toBe(0);
    });
});