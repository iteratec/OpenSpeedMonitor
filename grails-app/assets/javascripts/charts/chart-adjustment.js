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