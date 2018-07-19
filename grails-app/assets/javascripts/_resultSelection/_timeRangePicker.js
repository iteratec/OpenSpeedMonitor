/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*     http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

"use strict";

//= require /node_modules/air-datepicker/dist/js/datepicker.min.js
//= require /node_modules/air-datepicker/dist/js/i18n/datepicker.en.js
//= require /node_modules/moment/min/moment.min.js
//= require_self

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.timeRangePicker = function (timeRangePickerElement) {
    var containerElement = $(timeRangePickerElement);
    var pickerFrom = null;
    var pickerTo = null;
    var userInputFromElement = containerElement.find(".timerange-userinput-from");
    var userInputToElement = containerElement.find(".timerange-userinput-to");
    var valueFromElement = containerElement.find(".timerange-value-from");
    var valueToElement = containerElement.find(".timerange-value-to");
    var dateTo = null;
    var dateFrom = null;
    var datePicker = $.fn.datepicker.Constructor;
    var eventsEnabled = false;
    var defaultDateFormat = "dd.mm.yyyy";
    var wasManualChange = false;
    var datePickerOptions = {
        language: "en",
        timepicker: true,
        timeFormat: "hh:ii",
        minutesStep: 1,
        offset: 5,
        autoClose: false,
        toggleSelected: false
    };

    var init = function() {
        initRangeFrom();
        initRangeTo();

        registerEvents();
        eventsEnabled = true;
    };

    var initRangeFrom = function () {
        dateFrom = convertToDateOrDefault(valueFromElement.val(), new Date());
        pickerFrom = userInputFromElement.datepicker($.extend({}, datePickerOptions, {
            startDate: dateFrom,
            maxDate: new Date(),
            dateFormat: userInputFromElement.data("date-format") || defaultDateFormat,
            onSelect: function (formatted, date) {
                dateFrom = date;
                valueFromElement.val(date.toISOString());
                pickerTo.update("minDate", date);
                if (!pickerFrom.timepickerIsActive && !wasManualChange) {
                    pickerFrom.hide();
                    userInputToElement.trigger('focus');
                }
                wasManualChange = false;
                triggerRangeChanged();
            }
        })).data("datepicker");
        pickerFrom.$datepicker.on('mouseenter', '.datepicker--cell', function () {
            pickerFrom.views[pickerFrom.currentView]._update();
        });
        pickerFrom.update("onRenderCell", pickerFromOnRenderCell);
        userInputFromElement.on('click', function () {
            pickerTo.hide();
        });
    };

    var initRangeTo = function () {
        dateTo = convertToDateOrDefault(valueToElement.val(), new Date());
        var maxDate = new Date();
        maxDate.setHours(23, 59, 59, 999);
        pickerTo = userInputToElement.datepicker($.extend({}, datePickerOptions, {
            startDate: dateTo,
            maxDate: maxDate,
            minDate: dateFrom,
            dateFormat: userInputFromElement.data("date-format") || defaultDateFormat,
            onSelect: function (formatted, date) {
                dateTo = date;
                valueToElement.val(date.toISOString());
                pickerFrom.update("maxDate", date);
                if (!pickerTo.timepickerIsActive && !wasManualChange) {
                    pickerTo.hide();
                }
                wasManualChange = false;
                triggerRangeChanged();
            }
        })).data("datepicker");
        pickerTo.$datepicker.on('mouseenter', '.datepicker--cell', function () {
            pickerTo.views[pickerTo.currentView]._update();
        });
        pickerTo.update("onRenderCell", pickerToOnRenderCell);
        userInputToElement.on('click', function () {
            pickerFrom.hide();
        });
    };

    var registerEvents = function () {
        userInputFromElement.on("change keyup", function () { onManualChange(pickerFrom, $(this).val()); });
        userInputFromElement.on("blur", function () { onBlur(pickerFrom, dateFrom)});
        userInputToElement.on("change keyup", function () { onManualChange(pickerTo, $(this).val()); });
        userInputToElement.on("blur", function () { onBlur(pickerTo, dateTo)});
    };

    var onBlur = function (pickerInstance, resetDate) {
        if (pickerInstance.$el.hasClass("has-error")) {
            wasManualChange = true;
            pickerInstance.selectDate(resetDate);
            pickerInstance.$el.removeClass("has-error");
        }
    };

    var onManualChange = function (pickerInstance, value) {
        var selected = moment(value, getMomentDateTimeFormat(pickerInstance), true);
        var valid = !isNaN(selected.valueOf());
        pickerInstance.$el.toggleClass("has-error", !valid);
        if (valid) {
            wasManualChange = true;
            pickerInstance.selectDate(new Date(selected.valueOf()));
        }
    };

    var getMomentDateTimeFormat = function (pickerInstance) {
        var format = pickerInstance.opts.dateFormat + pickerInstance.opts.dateTimeSeparator + pickerInstance.opts.timeFormat;
        return format.replace(/y/g, "Y").replace(/m/g, "M").replace(/d/g, "D")
                     .replace(/h/g, "H").replace(/i/g, "m").replace(/@/g, "S");
    };

    var convertToDateOrDefault = function (isoDate, defaultValue) {
        if (Object.prototype.toString.call(isoDate) === '[object Date]' && !isNaN(isoDate.valueOf())) {
            return isoDate;
        }
        if (!isoDate) {
            return defaultValue;
        }
        var parsedDate = new Date(isoDate);
        return !isNaN(parsedDate.valueOf()) ? parsedDate : defaultValue;
    };

    var cellRenderCommonClassNames = function (date, focused) {
        var classes = datePicker.isSame(dateFrom, date) ? ' -range-from- -selected-' : '';
        classes += datePicker.isSame(dateTo, date) ? ' -range-to- -selected-' : '';
        classes += (!focused && datePicker.bigger(dateFrom, date) && datePicker.less(dateTo, date)) ? ' -in-range-' : '';
        return classes;
    };

    var pickerToOnRenderCell = function (date) {
        var focused = pickerTo.focused;
        var classes = cellRenderCommonClassNames(date, focused);

        if (focused) {
            var isInRange = datePicker.bigger(dateFrom, date) && datePicker.less(focused, date) && datePicker.less(pickerTo.maxDate, date);
            var isRangeEnd = datePicker.bigger(dateFrom, date) && datePicker.isSame(focused, date);
            classes += isInRange ? " -in-range-" : "";
            classes += isRangeEnd ? " -range-to-" : "";
        }

        return {
            html: '',
            classes: classes,
            disabled: false
        };
    };

    var pickerFromOnRenderCell = function (date) {
        var focused = pickerFrom.focused;
        var classes = cellRenderCommonClassNames(date, focused);

        if (focused) {
            var isInRange = datePicker.less(dateTo, date) && datePicker.bigger(focused, date) && datePicker.bigger(pickerFrom.minDate, date);
            var isRangeStart = datePicker.less(dateTo, date) && datePicker.isSame(focused, date);
            classes += isInRange ? " -in-range-" : "";
            classes += isRangeStart ? " -range-from-" : "";
        }

        return {
            html: '',
            classes: classes,
            disabled: false
        };
    };

    var triggerRangeChanged = function () {
        if (eventsEnabled) {
            containerElement.trigger("rangeChanged", getRange());
        }
    };

    var getStart = function () {
        return new Date(dateFrom.valueOf());
    };

    var getEnd = function () {
        return new Date(dateTo.valueOf());
    };

    var getRange = function () {
        return [getStart(), getEnd()];
    };

    var setRange = function (from, to) {
        var eventsWereEnabled = enableEvents(false);
        pickerFrom.selectDate(convertToDateOrDefault(from, dateFrom));
        pickerTo.selectDate(convertToDateOrDefault(to, dateTo));
        enableEvents(eventsWereEnabled);
    };

    var enableEvents = function(enable) {
        var wereEnabled = eventsEnabled;
        eventsEnabled = enable;
        return wereEnabled;
    };

    init();
    return {
        getStart: getStart,
        getEnd: getEnd,
        getRange: getRange,
        setRange: setRange
    };
};
