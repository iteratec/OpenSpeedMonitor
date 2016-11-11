<%@page defaultCodec="none" %>
<%--
A UI component to select date and time. _dateTimePicker.js works with this component, but needs to be initialized
explicitly. See selectIntervalTimeframeCard.js as an example.
--%>
<div id="${id}">
    <div class="input-group bootstrap-timepicker time-control">
        <span class="input-group-addon">
            <g:checkBox name="${manualTimeName}" checked="${manualTimeValue}"/>
        </span>
        <input type="text" class="form-control"
               value="${(time=='00:00'||time=='0:00')?'00:001':time}" disabled/>
        <span class="input-group-addon"><i class="fa fa-clock-o"></i></span>
    </div>
    <div class="date-control">
        <input class="form-control" type="text" id="${dateControlId}"
               data-date-format="${dateFormat}" data-date-week-start="${weekStart}"
               placeholder="start date" value="${date}" />
    </div>
    <input type="hidden" name="${dateName}" class="date-hidden" value="${date}" />
    <input type="hidden" name="${timeName}" class="time-hidden" value="${time}" />
</div>