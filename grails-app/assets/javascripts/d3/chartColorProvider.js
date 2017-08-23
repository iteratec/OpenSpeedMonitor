//= require /bower_components/d3/d3.min.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.ChartColorProvider = function() {
    var measurandGroupColorCombination = null;

    var trafficColors = [
        "#5cb85c",
        "#f0ad4e",
        "#d9534f"
    ];

    var init = function () {
        var loadingTimeColors = [
                "#1660A7",
                "#558BBF",
                "#95b6d7",
                "#d4e2ef"
            ],
            countOfRequestColors = [
                "#E41A1C",
                "#eb5859",
                "#f29697",
                "#fad5d5"
            ],
            sizeOfRequestColors = [
                "#F18F01",
                "#f4ad46",
                "#f8cc8b",
                "#fcead0"
            ],
            csiColors = [
                "#59B87A",
                "#86cb9e",
                "#b3dec2",
                "#e0f2e6"
            ],
            speedIndexColors = loadingTimeColors;

        measurandGroupColorCombination = {
            "ms": loadingTimeColors,
            "s": loadingTimeColors,
            "#": countOfRequestColors,
            "KB": sizeOfRequestColors,
            "MB": sizeOfRequestColors,
            "%": csiColors,
            "": speedIndexColors
        }
    };

    var getColorscaleForMeasurandGroup = function (measurandUnit, skipFirst) {
        var colors = measurandGroupColorCombination[measurandUnit].slice(skipFirst ? 1 : 0)
        var colorscale = d3.scale.ordinal()
                           .domain(createDomain(colors.length))
                           .range(colors);

        return colorscale;
    };

    var getColorscaleForTrafficlight = function () {
        var colorscale = d3.scale.ordinal()
                           .domain(["good", "ok", "bad"])
                           .range(trafficColors);

        return colorscale;
    };

    var createDomain = function (arrayLength) {
        var array = [];
        for (var i = 0; i < arrayLength; i++) {
            array.push(i);
        }
        return array;
    };

    init();

    return {
        getColorscaleForMeasurandGroup: getColorscaleForMeasurandGroup,
        getColorscaleForTrafficlight: getColorscaleForTrafficlight
    }
};

