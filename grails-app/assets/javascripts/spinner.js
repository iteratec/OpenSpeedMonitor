/*
 * OpenSpeedMonitor (OSM)
 * Copyright 2016 iteratec GmbH
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

//= require bower_components/spin.js/spin.js
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

/**
 * This class creates a spinner which can be hooked into dom elements in the following manner and sizes:
 * - Spinner():                 Default spinner (large): Blocking the sites content
 * - Spinner(element):          Default spinner (large): Hooked into 'element' and blocking its content
 * - Spinner(element, 'small'): Small spinner; Hooked into 'element' indicating loading in the top right corner
 *
 * * * This spinner sets the position of the spinnerTargteElement to 'position: relative' for correct placement.
 * * * After the spinner stopped the position gets reset to the previous state.
 *
 */

OpenSpeedMonitor.Spinner = function(spinnerTargetElement, size) {
    spinnerTargetElement = $(spinnerTargetElement).length ? $(spinnerTargetElement) : $('body');

    var spinner = null;
    var spinnerContainer = null;

    var spinnerOptions = null;
    var defaultSpinnerOptions = {
          lines: 13             // The number of lines to draw
        , length: 28            // The length of each line
        , width: 10             // The line thickness
        , radius: 35            // The radius of the inner circle
        , scale: 1              // Scales overall size of the spinner
        , corners: 0.5          // Corner roundness (0..1)
        , color: '#333333'      // #rgb or #rrggbb or array of colors
        , opacity: 0.25         // Opacity of the lines
        , rotate: 0             // The rotation offset
        , direction: 1          // 1: clockwise, -1: counterclockwise
        , speed: 1              // Rounds per second
        , trail: 60             // Afterglow percentage
        , fps: 20               // Frames per second when using setTimeout() as a fallback for CSS
        , zIndex: 2e9           // The z-index (defaults to 2000000000)
        , className: 'spinner'  // The CSS class to assign to the spinner
        , top: '50%'            // Top position relative to parent
        , left: '50%'           // Left position relative to parent
        , shadow: false         // Whether to render a shadow
        , hwaccel: false        // Whether to use hardware acceleration
        , position: 'absolute'  // Element positioning
    };

    var smallSpinnerOptions = {
          lines: 11             // The number of lines to draw
        , length: 0             // The length of each line
        , width: 4              // The line thickness
        , radius: 6             // The radius of the inner circle
        , scale: 0.05           // Scales overall size of the spinner
        , corners: 1            // Corner roundness (0..1)
        , color: '#333333'      // #rgb or #rrggbb or array of colors
        , opacity: 0            // Opacity of the lines
        , rotate: 0             // The rotation offset
        , direction: 1          // 1: clockwise, -1: counterclockwise
        , speed: 1              // Rounds per second
        , trail: 93             // Afterglow percentage
        , fps: 20               // Frames per second when using setTimeout() as a fallback for CSS
        , zIndex: 2e9           // The z-index (defaults to 2000000000)
        , className: 'spinner'  // The CSS class to assign to the spinner
        , top: '50%'            // Top position relative to parent
        , left: '50%'           // Left position relative to parent
        , shadow: false         // Whether to render a shadow
        , hwaccel: false        // Whether to use hardware acceleration
        , position: 'absolute'  // Element positioning
    };

    if (typeof size == 'undefined') {
        spinnerOptions = defaultSpinnerOptions;
    } else {
        spinnerOptions = smallSpinnerOptions;
    }

    var init = function () {
        // the default spinner target element is the body
        if (spinnerTargetElement.selector == 'body') {
            defaultSpinnerOptions['position'] = 'fixed';
        }

        // create the spinner container with the corresponding styling
        spinnerContainer = $('<div/>');
        if (size == 'small') {
            spinnerContainer.addClass('spinnerSmallContainer');
        } else {
            spinnerContainer.addClass('spinnerDefaultContainer');
        }

        // create the spinner
        spinner = new Spinner(spinnerOptions);

        // create the background
        var spinnerBackground = $('<div/>');
        spinnerBackground.addClass('spinnerBackground');
        spinnerContainer.append(spinnerBackground);

        // add spinner DOM elements
        spinnerTargetElement.prepend(spinnerContainer);
    };

    var start = function () {
        // for correct positioning the spinnerTargetElement must have 'position: relative'
        spinnerTargetElement.css('position', 'relative');

        // spin inserts the spinner div as first element of its target
        // spin needs the actual node, not a jquery object
        spinner.spin(spinnerContainer[0]);

        // show the spinner container
        spinnerContainer.css('display', 'block');
    };

    var stop = function () {
        spinner.stop();
        spinnerContainer.empty();

        // hide the spinner container
        spinnerContainer.css('display', 'none');

        // reset the spinnerTargetElement
        spinnerTargetElement.css('position', '');
    };

    init();

    return {
        spinner: spinner,
        start: start,
        stop: stop
    }
};