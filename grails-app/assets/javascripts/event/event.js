//= require /date-time-picker/bootstrap-timepicker.min.js
//= require_self

$(document).ready(function() {
    var timeInput = $('#hourTimepicker');
    timeInput.timepicker({showMeridian: false});
});