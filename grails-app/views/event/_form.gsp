<%@ page import="de.iteratec.osm.report.chart.Event" %>

<div class="form-group fieldcontain ${hasErrors(bean: eventInstance, field: 'eventDate', 'error')} required">
    <label for="eventDate" class="control-label col-md-3"><g:message code="event.date.label" default="Date"/><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <bs:datePicker name="eventDate" required="" precision="day" value="${eventInstance?.eventDate}" class="form-control" />
    </div>
</div>

<div class="form-group fieldcontain required">
    <label class="control-label col-md-3"><g:message code="event.time.label" default="Uhrzeit"/><span
            class="required-indicator">*</span></label>


    <div class="col-md-6">
        <div class="input-group" >
            <input id="hourTimepicker" name="time" type="text" class="form-control"
                   value="<g:if test="${(eventInstance?.eventDate?.getHours() as String)}">${
                       (eventInstance?.eventDate?.getHours() as String).padLeft(2, "0") + ":" + (eventInstance?.eventDate?.minutes as String).padLeft(2, "0")
                   }</g:if><g:else>00:00</g:else>">
            <span class="input-group-addon"><i class="fas fa-clock"></i></span>
        </div>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: eventInstance, field: 'shortName', 'error')} ">
    <label for="shortName" class="control-label col-md-3"><g:message code="event.shortName.label" default="Short Name"/></label>

    <div class="col-md-6">
        <g:textArea name="shortName" cols="40" rows="5" maxlength="255" value="${eventInstance?.shortName}" class="form-control"/>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: eventInstance, field: 'description', 'error')} ">
    <label for="description" class="control-label col-md-3"><g:message code="event.description.label"
                                                              default="Html Description"/></label>

    <div class="col-md-6">
        <g:textArea name="description" cols="40" rows="5" maxlength="255" class="form-control"
                    value="${eventInstance?.description}"/>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: eventInstance, field: 'globallyVisible', 'error')} ">
    <label for="globallyVisible" class="control-label col-md-3"><g:message code="event.globallyVisible.label"
                                                                  default="Globally Visible"/></label>

    <div class="col-md-6">
        <bs:checkBox name="globallyVisible" value="${eventInstance?.globallyVisible}" class="form-control"/>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: eventInstance, field: 'jobGroups', 'error')} ">
    <label for="jobGroups" class="control-label col-md-3"><g:message code="event.jobGroup.label" default="Job Group"/></label>

    <div class="col-md-6">
        <g:select name="jobGroups" from="${de.iteratec.osm.measurement.schedule.JobGroup.list()}" multiple="multiple"
                  optionKey="id" size="5" value="${eventInstance?.jobGroups*.id}" class="many-to-many form-control"/>
    </div>
</div>
