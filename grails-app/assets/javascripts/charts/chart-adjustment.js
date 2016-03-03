/* 
 * OpenSpeedMonitor (OSM)
 * Copyright 2014 iteratec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Initialization and event-handlers for chart-adjustment-accordion.
 */
$(document).ready(function () {
    var chartAdjustmentIsOnPage = ($('#collapseAdjustment').length > 0);
    if (chartAdjustmentIsOnPage) {
        var adjuster
        var exporter
        if (CHARTLIB.toUpperCase() == "HIGHCHARTS") {
            adjuster = new HighchartAdjuster();
            exporter = new HighchartExporter();
        } else if (CHARTLIB.toUpperCase() == "RICKSHAW") {
            // done in rickshawChartCreation.js
        } else {
            $('#collapseAdjustment').parent().parent().parent().remove();
        }
    }
});

function HighchartAdjuster() {
    this.initialize = function () {
        var multipleYAxis = false;
        if ($('#dia-y-axis-min').length > 0 && $('#dia-y-axis-max').length > 0) {
            multipleYAxis = true;
        }

        //initialize
        $('#dia-width').val(chart.chartWidth);
        $('#dia-height').val(chart.chartHeight);
        if (multipleYAxis) {
            if ($('#dia-y-axis-min').val().length == 0) $('#dia-y-axis-min').val(chart.yAxis[0].dataMin);
            if ($('#dia-y-axis-max').val().length == 0) $('#dia-y-axis-max').val(chart.yAxis[0].dataMax);
        }
        $("#collapseAdjustment").collapse('hide');
        //register events
        $('#to-enable-marker').bind('change', function () {
            var toEnableMarkers = $(this).is(':checked');
            var countAllDatapoints = 0;
            jQuery.each(
                chart.series,
                function (i, series) {
                    countAllDatapoints += series.data.length;
                });
            if (toEnableMarkers && countAllDatapoints > 10000) {
                window.alert("Too many datapoints to show and label them!");
                $(this).prop('checked', false);
            } else {
                jQuery.each(
                    chart.series,
                    function (i, series) {
                        series.update(
                            {
                                marker: {enabled: toEnableMarkers}
                            }
                        )
                    }
                );
            }
        });
        $('#to-enable-label').bind('change', function () {
            var toEnableLabels = $(this).is(':checked');
            var countAllDatapoints = 0;
            jQuery.each(
                chart.series,
                function (i, series) {
                    countAllDatapoints += series.data.length;
                });
            if (toEnableLabels && countAllDatapoints > 10000) {
                window.alert("Too many datapoints to show and label them!");
                $(this).prop('checked', false);
            } else {
                jQuery.each(
                    chart.series,
                    function (i, series) {
                        series.update(
                            {
                                dataLabels: {enabled: toEnableLabels}
                            }
                        )
                    }
                );
            }
        });
        $('#dia-change-chartsize').bind('click', function () {
            var diaWidth = $('#dia-width').val();
            var diaHeight = $('#dia-height').val();
            var maxWidth = 5000;
            var minWidth = 540;
            var maxHeight = 3000;
            if ($.isNumeric(diaWidth) && $.isNumeric(diaHeight)
                && parseInt(diaWidth) > 0 && parseInt(diaWidth) <= maxWidth && parseInt(diaWidth) >= minWidth
                && parseInt(diaHeight) > 0 && parseInt(diaHeight) <= maxHeight) {
                var diaWidth = $('#dia-width').val();
                var diaHeight = $('#dia-height').val();
                chart.setSize(diaWidth, diaHeight);
                chart.options.exporting.sourceWidth = diaWidth;
                chart.options.exporting.sourceHeight = diaHeight;
            } else {
                window.alert("Width and height of diagram has to be numeric values. Maximum is 5.000 x 3.000 pixels, minimum width is 540 pixels.");
            }
        });


        if (multipleYAxis) {
            $('#dia-change-yaxis').bind('click', function () {
                var diaYAxisMin = $('#dia-y-axis-min').val();
                var diaYAxisMax = $('#dia-y-axis-max').val();
                if ($.isNumeric(diaYAxisMin) && $.isNumeric(diaYAxisMax)
                    && parseInt(diaYAxisMax) > parseInt(diaYAxisMin)) {
                    chart.yAxis[0].setExtremes(diaYAxisMin, diaYAxisMax);
                } else {
                    window.alert("Minimum and maximum of Y-Axis has to be numeric values and maximum must be greater than minimum!");
                }
            });
        }
        $('#dia-title').bind('input', function () {
            chart.setTitle({text: $(this).val()});
        });

    };
    this.initialize();
}

function HighchartExporter(args) {
    var self = this;

    this.initialize = function (args) {

        //convert and download highcharts
        d3.select("#dia-save-chart-as-png").on("click", function () {
            var retVal = prepareNewBlankCanvas("svg");
            var canvas = retVal.canvas;

            var html = d3.select("svg").node().parentNode.innerHTML;
            html = html.replace(/<tspan x=\".*?\">/gi, '');
            html = html.replace(/<\/tspan>/gi, '');
            canvg(canvas, html);

            //convert to image
            try {
                //checking if image data gathered from canvas is not a blank image, otherwise restarting Exporter function, presuming that svg image meanwhile has completed rendering
                var dataConverted = canvas.toDataURL("image/png");
                if (dataConverted.indexOf("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA") > -1) {
                    removeObjectFromDom("#canvas_everything_merged");
                    HighchartExporter(args);
                } else {
                    downloadCanvas(canvas, "png");
                    removeObjectFromDom("#canvas_everything_merged");
                }
            }
            catch (err) {
            } // handle IE
        });

    };

    this.initialize(args);
}

function addAlias() {
    var clone = $("#graphAlias_clone").clone();
    var selectedValue = clone.find("#graphName").val();
    var htmlId = 'graphAlias_' + selectedValue;

    clone.attr('id', htmlId);

    $("#graphAliasChildlist").append(clone);
    clone.show();

    clone.find("#graphName").change(function () {
        var selectedValue = $(this).val();
        $(this).closest(".graphAlias-div").attr('id', 'graphAlias_' + selectedValue);
        $("#graphAliasChildlist").trigger("graphAliasChildsChanged");
    });
    clone.find("#alias").on('input', function () {
        $("#graphAliasChildlist").trigger("graphAliasChildsChanged");
    });
    clone.find("#removeButton").on('click', function () {
        $(this).closest(".graphAlias-div").remove();
        $("#graphAliasChildlist").trigger("graphAliasChildsChanged");
    });
    clone.find("#color").change(function () {
        var currentColor = $(this).val();
        $(this).css('background-color', currentColor);
        var name = $(this).closest(".graphAlias-div").find("#graphName").val();
        var argument = {};
        argument[name] = currentColor;
        $("#graphAliasChildlist").trigger("graphAliasColorChanged", argument);
    });

    // initial coloring
    clone.find("#color").css('background-color', clone.find("#color").val());
}

function initGraphNameAliases(graphNameAliases) {
    var keys = Object.keys(graphNameAliases);
    if (keys.length > 0) {
        for (var i = 0; i < keys.length; i++) {
            addAlias();
        }
        var counter = 0;
        $(".graphAlias-div").each(function () {
            var id = $(this).attr("id");
            if (id != "graphAlias_clone") {
                var name = keys[counter];
                var alias = graphNameAliases[name];
                $(this).find("#alias").val(alias);
                $(this).find("#graphName option[value='" + name + "']").attr('selected', true);
                $(this).attr('id', 'graphAlias_' + name);
                counter++;
            }
        });

        $("#graphAliasChildlist").trigger("graphAliasChildsChanged");
    }
}

function initGraphColors(graphColors) {
    var keys = Object.keys(graphColors);
    for (var i = 0; i < keys.length; i++) {
        var name = keys[i];
        var color = graphColors[name];
        var container = $(makeValidSelector("#graphAlias_" + name));
        if (container) {
            container.find("#color").val(color);
            container.find("#color").css("background-color", color);
            var argument = {};
            argument[name] = color;
            $("#graphAliasChildlist").trigger("graphAliasColorChanged", argument);
        }
    }
}