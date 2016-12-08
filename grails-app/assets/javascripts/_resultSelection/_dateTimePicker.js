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

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.DateTimePicker = function (dateTimePickerElement, autoTime) {
  dateTimePickerElement = $(dateTimePickerElement);
  autoTime = autoTime || "00:00";

  var dateInput = dateTimePickerElement.find(".date-control input[type='text']");
  var timeInput = dateTimePickerElement.find(".time-control input[type='text']");
  var timeManualCheckbox = dateTimePickerElement.find(".time-control input[type='checkbox']");
  var dateHiddenValue = dateTimePickerElement.find("input.date-hidden");
  var timeHiddenValue = dateTimePickerElement.find("input.time-hidden");
  var defaultDatePickerOptions = {endDate: "+0d"};

  var init = function () {
    dateInput.datepicker(defaultDatePickerOptions);
    timeInput.timepicker({showMeridian: false});

    registerEvents();
  };

  var registerEvents = function () {
    dateInput.datepicker().on('changeDate', function (ev) {
      if (!ev.date) { // invalid date set
        return;
      }
      dateHiddenValue.val(formatDateInternal(ev.date));
      dateInput.datepicker("hide");
      if (!timeManualCheckbox.is(':checked')) {
        setTime(autoTime);
      }
      triggerChangeEvent();
    });
    timeManualCheckbox.on('change', function (ev) {
      var manualSelection = timeManualCheckbox.is(':checked');
      timeInput.attr("disabled", !manualSelection);
      triggerChangeEvent();
    });
    timeInput.on('changeTime.timepicker', function (ev) {
      timeHiddenValue.val(ev.time.value);
      triggerChangeEvent();
    });
  };

  var setManualTime = function (isManualTime) {
    timeManualCheckbox.prop('checked', isManualTime);
    timeInput.attr("disabled", !isManualTime);
  };

  var setTime = function (time) {
    var pattern = new RegExp("([0-9]|[01][0-9]|2[0-3]):([0-5][0-9])");
    if (!pattern.test(time)) {
      console.log("Invalid time to set. Expected in format hh:mm");
      time = autoTime;
    }

    timeInput.val(time);
    timeHiddenValue.val(time);
    var timePickerValue = time;

    // workaround for the bootstrap timepicker
    if (timePickerValue == '00:00' || timePickerValue == '0:00') {
      timePickerValue = "00:001";
    }

    timeInput.timepicker('setTime', timePickerValue);
  };

  var setDate = function (date) {
    var dateObject = parseDateInternal(date);
    dateHiddenValue.val(date);
    dateInput.datepicker("setDate", dateObject);
  };

  var parseDateInternal = function (dateString) {
    var pattern = new RegExp("([0-2][0-9]|3[01]).(0[1-9]|1[0-2]).[0-9]{4}");
    if (!pattern.test(dateString)) {
      console.log("Invalid date to set. Expected in format dd.mm.yyyy");
      return new Date();
    }
    var parts = dateString.split(".");
    return new Date(parts[2], parts[1] - 1, parts[0]);
  };

  var formatDateInternal = function (date) {
    return twoDigitString(date.getDate()) + "." + twoDigitString(date.getMonth() + 1) + "." + date.getFullYear();
  };

  var twoDigitString = function (number) {
    return ("00" + number).substr(-2, 2);
  };

  var triggerChangeEvent = function () {
    dateTimePickerElement.trigger("changeDateTime", getValues());
  };

  var setValuesByDate = function (date) {
    if (!(date instanceof Date)) {
      console.log("Invalid date object to set datepicker from.");
      return;
    }
    setDate(formatDateInternal(date));
    setTime(twoDigitString(date.getHours()) + ":" + twoDigitString(date.getMinutes()));
  };

  var getValues = function () {
    return {
      date: dateHiddenValue.val(),
      manualTime: timeManualCheckbox.is(':checked'),
      time: timeHiddenValue.val()
    };
  };

  var getValuesAsDate = function () {
    var values = getValues();
    var date = parseDateInternal(values.date);
    if (values.manualTime) {
      var hoursMinutes = values.time.split(":");
      date.setHours(hoursMinutes[0]);
      date.setMinutes(hoursMinutes[1]);
    }
    return date;
  };

  var setValues = function (newValues) {
    if (newValues == undefined || newValues == null) {
      return;
    }
    if (newValues.date) {
      setDate(newValues.date);
    }
    if (newValues.manualTime !== undefined) {
      var isManual = typeof newValues.manualTime == "string" ?
        OpenSpeedMonitor.stringUtils().stringToBoolean(newValues.manualTime) : newValues.manualTime;
      setManualTime(isManual);
    }
    if (newValues.time) {
      setTime(newValues.time);
    }
  };

  var setStartDate = function (startDate) {
    var dateObject = null;
    if (startDate instanceof Date) {
      dateObject = startDate;
    } else if (startDate) {
      dateObject = parseDateInternal(startDate);
    }
    dateInput.datepicker("setStartDate", dateObject);
  };

  init();
  return {
    getValues: getValues,
    getValuesAsDate: getValuesAsDate,
    setValues: setValues,
    setValuesByDate: setValuesByDate,
    setStartDate: setStartDate
  };
};
