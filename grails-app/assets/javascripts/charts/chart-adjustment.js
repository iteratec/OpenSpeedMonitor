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

function addAlias() {
    var clone = $("#graphAlias_clone").clone();
    var graphNameSelect = clone.find("#graphName");
    var selectedValue = graphNameSelect.val();
    var htmlId = 'graphAlias_' + selectedValue;

    clone.attr('id', htmlId);

    $("#graphAliasChildlist").append(clone);
    clone.show();
    graphNameSelect.change(function () {
        var selectedValue = $(this).val();
        $(this).closest(".graphAlias-div").attr('id', 'graphAlias_' + selectedValue);
        $("#graphAliasChildlist").trigger("graphAliasChildsChanged");
        initColorPicker($(this).parent().parent().siblings().find(".colorpicker-component"), selectedValue);
    });
    clone.find("#alias").on('input', function () {
        $("#graphAliasChildlist").trigger("graphAliasChildsChanged");
    });
    clone.find("#removeButton").on('click', function () {
        $(this).closest(".graphAlias-div").remove();
        $("#graphAliasChildlist").trigger("graphAliasChildsChanged");
    });
    var colorPickerComponent = clone.find(".colorpicker-component");
    initColorPicker(colorPickerComponent, selectedValue);
    $(colorPickerComponent).colorpicker().on('changeColor', function (e) {
        var currentColor = e.color.toString();
        var name = $(this).closest(".graphAlias-div").find("#graphName").val();
        var argument = {};
        argument[name] = currentColor;
        $("#graphAliasChildlist").trigger("graphAliasColorChanged", argument);
    });
}

function initColorPicker(colorPickerComponent, selectedValue) {
    $(colorPickerComponent).colorpicker({component: '.colorpicker-target'});

    var selectedSeries = window.rickshawGraphBuilder.graph.series.find(function(el) { return el.name == selectedValue });
    var selectedSeriesColor = '#FFFFFF';

    if (selectedSeries) {
        selectedSeriesColor = selectedSeries.color;
    }

    $(colorPickerComponent).colorpicker('setValue', selectedSeriesColor);
}

function initGraphNameAliases(graphNameAliases) {
    var filteredGraphNameAliases = filterAliases(graphNameAliases);
    if (filteredGraphNameAliases.length == 0)
        return;
    var keys = Object.keys(filteredGraphNameAliases);
    if (keys.length > 0) {
        for (var i = 0; i < keys.length; i++) {
            addAlias();
        }
        var counter = 0;
        $(".graphAlias-div").each(function () {
            var id = $(this).attr("id");
            if (id != "graphAlias_clone") {
                var name = keys[counter];
                var alias = filteredGraphNameAliases[name];
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
    var filteredGraphColors = filterAliases(graphColors);
    if (filteredGraphColors.length == 0)
        return;
    var keys = Object.keys(filteredGraphColors);
    for (var i = 0; i < keys.length; i++) {
        var name = keys[i];
        var color = filteredGraphColors[name];
        var container = $("#graphAlias_" + makeValidSelector(name));
        if (container) {
            container.find("#color").val(color);
            var argument = {};
            argument[name] = color;
            $("#graphAliasChildlist").trigger("graphAliasColorChanged", argument);
        }
    }
}

/**
 * removes graphNameAliases for which no graph exists
 */
function filterAliases(graphNameAliases) {
    var result = {};

    for (var key in graphNameAliases) {
        if ($('*[data-origin-name=\"' + key + '\"]').length > 0) {
            result[key] = graphNameAliases[key];
        }
    }

    return result;
}