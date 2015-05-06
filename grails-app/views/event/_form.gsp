<%@ page import="de.iteratec.osm.report.chart.Event" %>

      <div class="control-group fieldcontain ${hasErrors(bean: eventInstance, field: 'eventDate', 'error')} required">
        <label for="eventDate" class="control-label"><g:message code="event.date.label" default="Date" /><span class="required-indicator">*</span></label>
        <div class="controls">
          <bs:datePicker name="eventDate" required="" precision="day"  value="${eventInstance?.eventDate}"  />
        </div>
      </div>
      
      <div class="control-group fieldcontain ${hasErrors(bean: eventInstance, field: 'eventTime', 'error')} required">
        <label for="eventTime" class="control-label"><g:message code="event.time.label" default="Uhrzeit" /><span class="required-indicator">*</span></label>
        <div class="controls">        
          <div class="input-append bootstrap-timepicker">
            <input id="eventTimeTimepicker" type="text" class="input-small content-box" value="${(eventInstance?.eventTime=='00:00'||eventInstance?.eventTime=='0:00')?'00:001':eventInstance?.eventTime}">
            <span class="add-on"><i class="icon-time"></i></span>
          </div>
        </div>
            <input type="hidden" name="eventTime" id="eventTime" value="${eventInstance?.eventTime}">
      </div>      

			<div class="control-group fieldcontain ${hasErrors(bean: eventInstance, field: 'shortName', 'error')} ">
				<label for="shortName" class="control-label"><g:message code="event.shortName.label" default="Short Name" /></label>
				<div class="controls">
					<g:textArea name="shortName" cols="40" rows="5" maxlength="255" value="${eventInstance?.shortName}"/>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: eventInstance, field: 'htmlDescription', 'error')} ">
				<label for="htmlDescription" class="control-label"><g:message code="event.htmlDescription.label" default="Html Description" /></label>
				<div class="controls">
					<g:textArea name="htmlDescription" cols="40" rows="5" maxlength="255" value="${eventInstance?.htmlDescription}"/>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: eventInstance, field: 'globallyVisible', 'error')} ">
				<label for="globallyVisible" class="control-label"><g:message code="event.globallyVisible.label" default="Globally Visible" /></label>
				<div class="controls">
					<bs:checkBox name="globallyVisible" value="${eventInstance?.globallyVisible}" />
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: eventInstance, field: 'jobGroup', 'error')} ">
				<label for="jobGroup" class="control-label"><g:message code="event.jobGroup.label" default="Job Group" /></label>
				<div class="controls">
					<g:select name="jobGroup" from="${de.iteratec.osm.measurement.schedule.JobGroup.list()}" multiple="multiple" optionKey="id" size="5" value="${eventInstance?.jobGroup*.id}" class="many-to-many"/>
				</div>
			</div>