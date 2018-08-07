//= require /node_modules/bootstrap-timepicker/js/bootstrap-timepicker.js
//= require_self

$(function () {
    var timeInput = $('#hourTimepicker');
    timeInput.timepicker({showMeridian: false});
});