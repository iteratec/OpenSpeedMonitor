//= require /bower_components/d3/d3.min.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.ChartColorProvider = function() {
    var measurandGroupColorCombination = null;

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
            speedIndexColors = [
                "#1660A7",
                "#558BBF",
                "#95b6d7",
                "#d4e2ef"
            ];

        measurandGroupColorCombination = {
            "ms": loadingTimeColors,
            "#": countOfRequestColors,
            "kb": sizeOfRequestColors,
            "%": csiColors,
            "": speedIndexColors
        }
    };

    var getColorscaleForMeasurandGroup = function (measurandUnit) {
        var colors = measurandGroupColorCombination[measurandUnit];
        var colorscale = d3.scale.ordinal()
                           .range(colors);

        return colorscale;
    };

    init();

    return {
        getColorscaleForMeasurandGroup: getColorscaleForMeasurandGroup
    }
};

