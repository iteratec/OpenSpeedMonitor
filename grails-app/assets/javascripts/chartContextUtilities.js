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

/*
 * Contains functionality used to display a context menu in the chart in eventResultDashboard/showAll
 */

//= require bower_components/jQuery-contextMenu/dist/jquery.contextMenu.min.js


// context menu on all dots of all graphs
$(function () {
    $.contextMenu({
        selector: '.chart-context-menu',

        events: {
            show: function () {
                // don't loose the dot on which the context menu was called

                // get the current dot, clone it and mark the clone as visible while context menu is active
                var activeDot = $('.dot.active')[0];
                var dotOfContextMenu = $(activeDot).clone();
                $(dotOfContextMenu).attr('id', 'visibleWhileActiveContextMenu');

                // create the new dot and copy the styles of the active dot
                // get the distance to the left side
                var detailElement = $('.detail')[0];
                var left = $(detailElement).css("left");
                $(dotOfContextMenu).css("left", left);

                // display the new saved dot in the chart
                var chart = $('#rickshaw_chart');
                $(chart).append(dotOfContextMenu);
            },
            hide: function () {
                // loose the dot on which the context menu was called when it will be hidden
                $('#visibleWhileActiveContextMenu')[0].remove();
            }
        },

        callback: function (key) {
            // rickshawGraphBuilder is in the global namespace and
            // nearestPoint is set in the update method of Rickshaw.Graph.HoverDetail
            var nearestPoint = rickshawGraphBuilder.graph.nearestPoint;

            // default action to be executed; open wpt of the dot/measurement in the corresponding view
            window.open(buildWptUrl(key, nearestPoint));
        },

        items: {
            "summary": {
                name: chartContextMenuI18N.summary,
                icon: "fa-file-text-o"
            },
            "waterfallView": {
                name: chartContextMenuI18N.waterfall,
                icon: "fa-bars"
            },
            "performanceReview": {
                name: chartContextMenuI18N.performanceReview,
                icon: "fa-check"
            },
            "contentBreakdown": {
                name: chartContextMenuI18N.contentBreakdown,
                icon: "fa-pie-chart"
            },
            "domains": {
                name: chartContextMenuI18N.domains,
                icon: "fa-list"
            },
            "screenshot": {
                name: chartContextMenuI18N.screenshot,
                icon: "fa-picture-o"
            },
            "filmstrip": {
                name: chartContextMenuI18N.filmstrip,
                icon: "fa-film"
            },
            "compare": {
                name: chartContextMenuI18N.compareFilmstrips,
                icon: "fa-columns",
                // show only if at least one point is already selected and
                // also only if comparison is not only with itself
                visible: function () {
                    var nearestDotId = rickshawGraphBuilder.graph.nearestPoint.value.wptResultInfo.testId;

                    if (rickshawGraphBuilder.graph.selectedPoints.length == 1 &&
                        rickshawGraphBuilder.graph.selectedPoints[0].value.wptResultInfo.testId == nearestDotId) {
                        return false;
                    }
                    return rickshawGraphBuilder.graph.selectedPoints.length > 0;
                },
                callback: function (itemKey) {
                    // if current point is not selected yet, do it and then open the comparison view
                    if (isNotSelected(rickshawGraphBuilder.graph.nearestPoint)) {
                        selectPoint(rickshawGraphBuilder.graph.nearestPoint);
                    }
                    window.open(buildWptUrl(itemKey, rickshawGraphBuilder.graph.nearestPoint));
                }
            },
            separator: "-----",
            "selectPoint": {
                name: chartContextMenuI18N.selectPoint,
                icon: "fa-dot-circle-o",
                // show only if point is not selected yet
                visible: function () {
                    return isNotSelected(rickshawGraphBuilder.graph.nearestPoint);
                },
                callback: function () {
                    selectPoint(rickshawGraphBuilder.graph.nearestPoint);
                }
            },
            "deselectPoint": {
                name: chartContextMenuI18N.deselectPoint,
                icon: "fa-trash-o",
                // show only if point is already selected
                visible: function () {
                    return !isNotSelected(rickshawGraphBuilder.graph.nearestPoint);
                },
                callback: function () {
                    deselectPoint(rickshawGraphBuilder.graph.nearestPoint);
                }
            }
        }
    });
});


// context menu on the rickshaw chart
$(function () {
    $.contextMenu({
        selector: '#rickshaw_chart',

        items: {
            "comparePoints": {
                name: chartContextMenuI18N.compareFilmstrips,
                icon: "fa-columns",
                // show only if at least one point is already saved
                visible: function () {
                    return rickshawGraphBuilder.graph.selectedPoints.length > 1;
                },
                callback: function () {
                    window.open(buildWptUrl());
                }
            },
            "deselectAllPoints": {
                name: chartContextMenuI18N.deselectAllPoints,
                icon: "fa-trash-o",
                // show only if at least one point is already saved
                visible: function () {
                    return rickshawGraphBuilder.graph.selectedPoints.length > 0;
                },
                callback: function () {
                    deselectAllPoints();
                }
            }
        }
    });
});


// select/deselect points on the graph with meta-key+click
$(function () {
    $('#rickshaw_main').on('click', '.chart-context-menu', function (event) {
        if (event.metaKey || event.ctrlKey) {
            var nearestPoint = rickshawGraphBuilder.graph.nearestPoint;

            if (isNotSelected(nearestPoint)) {
                selectPoint(nearestPoint);
            } else {
                deselectPoint(nearestPoint)
            }

            event.preventDefault();
            return false;
        }
    });
});


function buildWptUrl(wptView = null, nearestPoint = null) {
    var url = null;
    if (!nearestPoint || !wptView) {
        // build url for the comparison view launched from the chart context menu
        url = rickshawGraphBuilder.graph.selectedPoints[0].value.wptResultInfo.wptServerBaseurl.toString() +
            "video/compare.php?tests=" + comparingPartOfFilmstripsURL();
    } else {
        // build url for the comparison view launched from a aspecific point

        // get the wpt infos of the corresponding data point
        var wptServerBaseurl = nearestPoint.value.wptResultInfo.wptServerBaseurl.toString();
        var testId = nearestPoint.value.wptResultInfo.testId.toString();
        var numberOfWptRun = nearestPoint.value.wptResultInfo.numberOfWptRun.toString();
        var cachedView = nearestPoint.value.wptResultInfo.cachedView;
        var cached = null;
        var oneBaseStepIndexInJourney = "";
        if (nearestPoint.value.wptResultInfo.oneBaseStepIndexInJourney) {
            oneBaseStepIndexInJourney = nearestPoint.value.wptResultInfo.oneBaseStepIndexInJourney.toString();
        }

        // build the url
        if (wptView == "compare") {
            url = wptServerBaseurl + "video/compare.php?tests=" + comparingPartOfFilmstripsURL();
        } else if (wptView == "filmstrip") {
            cached = cachedView ? "1" : "0";

            url = wptServerBaseurl + "video/compare.php?tests=" +
                testId +
                "-r:" + numberOfWptRun +
                "-c:" + cached +
                "-s:" + oneBaseStepIndexInJourney;
        } else {
            cached = cachedView ? "cached/" : "";

            url = wptServerBaseurl + "result/" + testId + "/" + cached;

            if (wptView == "summary") url += "#run";

            url += numberOfWptRun;

            switch (wptView) {
                case "summary":
                    url += "_step";
                    break;
                case "waterfallView":
                    url += "/details/#waterfall_view_step";
                    break;
                case "performanceReview":
                    url += "/performance_optimization/#review_step";
                    break;
                case "contentBreakdown":
                    url += "/breakdown/#breakdown_fv_step";
                    break;
                case "domains":
                    url += "/domains/#breakdown_fv_step";
                    break;
                case "screenshot":
                    url += "/screen_shot/#step_";
                    break;
                default:
                    alert("URL could not be configured. Please try again.");
                    return;
            }

            url += oneBaseStepIndexInJourney;
        }
    }

    return url;
}


function comparingPartOfFilmstripsURL() {
    var urlPart = "";
    rickshawGraphBuilder.graph.selectedPoints.forEach(function (point) {
        var testId = point.value.wptResultInfo.testId.toString();
        var numberOfWptRun = point.value.wptResultInfo.numberOfWptRun.toString();
        var cached = point.value.wptResultInfo.cachedView ? "1" : "0";
        var oneBaseStepIndexInJourney = point.value.wptResultInfo.oneBaseStepIndexInJourney.toString();

        urlPart += testId +
            "-r:" + numberOfWptRun +
            "-c:" + cached +
            "-s:" + oneBaseStepIndexInJourney + ",";
    });

    // return this part of the url, but delete the last unnecessary comma
    return urlPart.slice(0, -1);
}


function selectPoint(nearestPoint) {
    // selection is only possible if same server was used
    if (rickshawGraphBuilder.graph.selectedPoints.length > 0) {
        var server = rickshawGraphBuilder.graph.selectedPoints[0].value.wptResultInfo.wptServerBaseurl;

        if (server != nearestPoint.value.wptResultInfo.wptServerBaseurl) {
            $("#ContextMenuErrorModal").modal();
            return;
        }
    }

    // create and save the html id for the dot in the 'rickshawGraphBuilder.graph.selectedPoints' datastructure
    var html_id = nearestPoint.html_id = "selected_dot_" + (rickshawGraphBuilder.graph.selectedPoints.length + 1).toString();
    // add the point to the datastructure
    rickshawGraphBuilder.graph.selectedPoints.push(nearestPoint);

    // create the html:
    // get the current dot and mark it as saved
    var activeDot = $('.dot.active')[0];
    $(activeDot).addClass('saved');
    // set an id for the ability to delete it individually
    $(activeDot).attr('id', html_id);

    // create the new dot and copy the styles of the active dot but without the active context menu class property
    var newSavedDot = $(activeDot).clone();
    newSavedDot.removeClass('context-menu-active');
    // get the distance to the left side
    var detailElement = $('.detail')[0];
    var left = $(detailElement).css("left");
    $(newSavedDot).css("left", left);

    // display the new saved dot in the chart
    var chart = $('#rickshaw_chart');
    $(chart).append(newSavedDot);
}


function isNotSelected(point) {
    var isNotSaved = true;
    rickshawGraphBuilder.graph.selectedPoints.forEach(function (savedPoint) {
        if (point.formattedXValue == savedPoint.formattedXValue &&
            point.formattedYValue == savedPoint.formattedYValue &&
            point.name == savedPoint.name) {
            isNotSaved = false;
        }
    });

    return isNotSaved;
}


function deselectPoint(point) {
    var itemToRemove = null;
    // get the current item in the datastructure
    rickshawGraphBuilder.graph.selectedPoints.forEach(function (savedPoint) {
        if (point.formattedXValue == savedPoint.formattedXValue &&
            point.formattedYValue == savedPoint.formattedYValue &&
            point.name == savedPoint.name) {
            itemToRemove = savedPoint;
        }
    });
    // delete the element from the datastructure
    rickshawGraphBuilder.graph.selectedPoints.splice(itemToRemove, 1);

    // remove the dot from the DOM
    $('#' + itemToRemove.html_id)[0].remove();
}


function deselectAllPoints() {
    // reset the point datastructure
    rickshawGraphBuilder.graph.selectedPoints = [];
    // delete the DOM elements
    var savedDots = document.getElementsByClassName('saved');
    while (savedDots[0]) {
        savedDots[0].parentNode.removeChild(savedDots[0]);
    }
}