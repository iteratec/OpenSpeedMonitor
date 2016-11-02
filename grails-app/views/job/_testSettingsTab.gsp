<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup"%>
<%@ page import="de.iteratec.osm.measurement.schedule.ConnectivityProfile" %>


<g:render template="checkbox" model="${['booleanAttribute': 'active', 'job': job]}" />

<div class="form-group ${hasErrors(bean: job, field: 'firstViewOnly', 'error')}">
	<label for="chkbox-first-view" class="col-md-2 control-label">
		<g:message code="job.firstView.label" default="first view" />
	</label>
	<div class="col-md-10">
		<div class="checkbox">
			<input type="checkbox" id="chkbox-first-view" checked disabled>
		</div>
	</div>
</div>
<div class="form-group ${hasErrors(bean: job, field: 'firstViewOnly', 'error')}">
	<label for="chkbox-repeated-view" class="col-md-2 control-label">
		<g:message code="job.repeatedView.label" default="repeated view" />
	</label>
	<div class="col-md-10">
		<div class="checkbox">
			<g:checkBox name="repeatedView" value="${ job ? !job.firstViewOnly : false }" id="chkbox-repeated-view"/>
		</div>
	</div>
</div>

<g:render template="checkbox" model="${['booleanAttribute': 'persistNonMedianResults', 'job': job]}" />

<div class="form-group ${hasErrors(bean: job, field: 'description', 'error')} required">
	<label for="description" class="col-md-2 control-label">
		<g:message code="job.description.label" default="description" />
	</label>
	<div class="col-md-10">
		<textarea class="form-control" name="description" id="description" rows="3">${job?.description?.trim()}</textarea>
	</div>
</div>

<div class="form-group">
	<label for="tags" class="col-md-2 control-label">
		<g:message code="job.tags.label" default="tags" />
	</label>
	<div class="col-md-10">
		<ul name="tags" id="tags">
		      <g:each in="${job?.tags}">
		        <li>${it}</li>
		      </g:each>
		</ul>
	</div>
</div>


<div class="form-group ${hasErrors(bean: job, field: 'maxDownloadTimeInMinutes', 'error')}">
	<label class="col-md-2 control-label" for="maxDownloadTimeInMinutesValue">
		<abbr title="${message(code: 'job.maxDownloadTimeInMinutes.description', args: [defaultMaxDownloadTimeInMinutes])}" data-placement="bottom" rel="tooltip">
			<g:message code="job.maxDownloadTimeInMinutes.label" default="maxDownloadTimeInMinutes" />
		</abbr>
		<span class="required-indicator">*</span>
	</label> 
	<div class="col-md-8">
		<span id="maxDownloadTimeInMinutes">
			<g:set var="isEditable" value="${job?.maxDownloadTimeInMinutes != defaultMaxDownloadTimeInMinutes}" />
			<input type="text" class="form-control ${ isEditable ? '' : 'non-editable' }"
				name="maxDownloadTimeInMinutes"
				value="${job?.maxDownloadTimeInMinutes}" id="maxDownloadTimeInMinutesValue" 
				placeholder="${defaultMaxDownloadTimeInMinutes}"
				${ isEditable ? '' : 'readonly' } />
			<g:message code="job.maxDownloadTimeInMinutes.label.unit" />
			
			<a href="#" style="${ isEditable ? 'display: none' : '' }">
				<g:message code="job.maxDownloadTimeInMinutes.change" default="Ändern" />
			</a> 
		</span>
	</div>
</div>
<div class="form-group ${hasErrors(bean: job, field: 'executionSchedule', 'error')}">
	<label class="col-md-2 control-label" for="executionScheduleShown">
		<g:message code="job.executionSchedule.label" default="executionSchedule" />
	</label>
	<div class="col-md-8">

        <input type="text" id="execution-schedule-shown" class="form-control"
               value="${job.executionSchedule ? job.executionSchedule.substring(job.executionSchedule.indexOf(' ')+1, job.executionSchedule.size()) : ''}"
               onchange="updateExecScheduleInformations('0 ' + this.value, '${createLink(action: 'nextExecution', absolute: true)}')">
        <input type="text" id="execution-schedule" name="executionSchedule" value="${job?.executionSchedule}" style="display: none">
        <i class="fa fa-question-circle fa-lg clickable-icon" onclick="toggleCronInstructions()"></i>
        <br><span id="cronhelp-readable-expression"></span>
		<br><span id="cronhelp-next-execution"></span>
        <br>
        <span id="cron-instructions" style="display: none;">
            <g:render template="cronInstructions"></g:render>
        </span>
	</div>
</div>



<div class="form-group ${hasErrors(bean: job, field: 'jobGroup', 'error')} required">
	<label class="col-md-2 control-label" for="jobGroup">
		<g:message code="job.jobGroup.label" default="jobGroup" />
		<span class="required-indicator">*</span>
	</label>
	<div class="col-md-8">
		<g:select id="jobgroup" class="form-control chosen" name="jobGroup.id" from="${JobGroup.list()}"
			value="${job?.jobGroup?.id}" optionValue="name" optionKey="id" />
	</div>
</div>

<div class="form-group ${hasErrors(bean: job, field: 'runs', 'error')} required">
	<label class="col-md-2 control-label" for="runs"> <g:message
			code="job.runs.label" default="runs" /> <span
		class="required-indicator">*</span>
	</label>
	<div class="col-md-8">
		<g:textField class="form-control" name="runs" value="${job?.runs ?: 1}" />
	</div>
</div>

<div class="form-group">
    <label class="col-md-2 control-label" for="connectivityProfile"> <g:message
            code="connectivityProfile.label" default="connectivityProfile" />
    <span class="required-indicator">*</span>
    </label>
    <div class="col-md-8">
        <g:select id="connectivityProfile" class="chosen"
				  data-placeholder="${g.message(code: 'web.gui.jquery.chosen.multiselect.placeholdermessage', 'default': 'Please chose an option')}"
                  name="connectivityProfile.id" from="${connectivites }" optionKey="id"
				  value="${job.connectivityProfile?job.connectivityProfile.id:null}" />
    </div>
</div>
<div id="connectivityProfileDetails">
    <input type="hidden" id="customConnectivityProfile" name="customConnectivityProfile" value="${job?.customConnectivityProfile}" />
    <input type="hidden" id="noTrafficShapingAtAll" name="noTrafficShapingAtAll" value="${job?.noTrafficShapingAtAll}" />
    <div class="form-group">
        <label class="col-md-2 control-label" for="customConnectivityName">
            <g:message code="job.customConnectivityProfile.label" default="Name of custom connectivity profile" />
        </label>
        <div class="col-md-8">
            <g:textField class="form-control" name="customConnectivityName" id="custom-connectivity-name"
                         value="${job?.customConnectivityName}" readonly="readonly" />
            <label class="checkbox-inline">
                <g:checkBox name="setCustomConnNameManually" id="setCustomConnNameManually"></g:checkBox>
                <g:message code="job.customConnectivityProfile.setnamemanually.label" default="Set name manually." />
            </label>
        </div>
    </div>
    <g:each var="attribute" in="['bandwidthDown', 'bandwidthUp', 'latency', 'packetLoss']">
        <div class="form-group ${hasErrors(bean: job, field: attribute, 'error')}">
            <label class="col-md-2 control-label" for="${attribute}">
                <g:message code="connectivityProfile.${attribute}.label"
                           default="${attribute}" />
            </label>
            <div class="col-md-8">
                <g:textField class="form-control" name="${attribute}" id="custom-${attribute}"
                             value="${job?."$attribute"}" />
            </div>
        </div>
    </g:each>
</div>