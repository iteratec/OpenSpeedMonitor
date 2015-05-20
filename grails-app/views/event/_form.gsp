<%@ page import="de.iteratec.osm.report.chart.Event" %>

<div class="control-group fieldcontain ${hasErrors(bean: eventInstance, field: 'eventDate', 'error')} required">
    <label for="eventDate" class="control-label"><g:message code="event.date.label" default="Date"/><span
            class="required-indicator">*</span></label>

    <div class="controls">
        <bs:datePicker name="eventDate" required="" precision="day" value="${eventInstance?.eventDate}"/>
    </div>
</div>

<div class="control-group fieldcontain  required">
    <label class="control-label"><g:message code="event.time.label" default="Uhrzeit"/><span
            class="required-indicator">*</span></label>


    <div class="controls">
        <div class="input-append bootstrap-timepicker" }>
            <input id="fromHourTimepicker" name="time" type="text" class="input-small content-box"
                   value="<g:if test="${(eventInstance?.eventDate?.getHours() as String) != null && (eventInstance?.eventDate?.getHours() as String).isEmpty()}">
                   ${(eventInstance?.eventDate?.getHours() as String).padLeft(2, "0") + ":" + (eventInstance?.eventDate?.minutes as String).padLeft(2, "0")}
                   </g:if>
                   <g:else>00:00</g:else>">
            <span class="add-on"><i class="icon-time"></i></span>
        </div>
    </div>
</div>

<div class="control-group fieldcontain ${hasErrors(bean: eventInstance, field: 'shortName', 'error')} ">
    <label for="shortName" class="control-label"><g:message code="event.shortName.label" default="Short Name"/></label>

    <div class="controls">
        <g:textArea name="shortName" cols="40" rows="5" maxlength="255" value="${eventInstance?.shortName}"/>
    </div>
</div>

<div class="control-group fieldcontain ${hasErrors(bean: eventInstance, field: 'description', 'error')} ">
    <label for="description" class="control-label"><g:message code="event.description.label"
                                                                  default="Html Description"/></label>

    <div class="controls">
        <g:textArea name="description" cols="40" rows="5" maxlength="255"
                    value="${eventInstance?.description}"/>
    </div>
</div>

<div class="control-group fieldcontain ${hasErrors(bean: eventInstance, field: 'globallyVisible', 'error')} ">
    <label for="globallyVisible" class="control-label"><g:message code="event.globallyVisible.label"
                                                                  default="Globally Visible"/></label>

    <div class="controls">
        <bs:checkBox name="globallyVisible" value="${eventInstance?.globallyVisible}"/>
    </div>
</div>

<div class="control-group fieldcontain ${hasErrors(bean: eventInstance, field: 'jobGroups', 'error')} ">
    <label for="jobGroups" class="control-label"><g:message code="event.jobGroup.label" default="Job Group"/></label>

    <div class="controls">
        <g:select name="jobGroups" from="${de.iteratec.osm.measurement.schedule.JobGroup.list()}" multiple="multiple"
                  optionKey="id" size="5" value="${eventInstance?.jobGroups*.id}" class="many-to-many"/>
    </div>
</div>