<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup"%>
<%@ page import="de.iteratec.osm.measurement.schedule.ConnectivityProfile" %>


<g:render template="checkbox" model="${['booleanAttribute': 'active', 'job': job]}" />

<div class="form-group ${hasErrors(bean: job, field: 'firstViewOnly', 'error')}">
	<div class="row">
		<label for="chkbox-first-view" class="span3 text-right">
			<g:message code="job.firstView.label" default="first view" />
		</label>
		<div class="span8">
			<input type="checkbox" class="form-control" id="chkbox-first-view" checked disabled>
		</div>
	</div>
	<div class="row">
		<label for="chkbox-repeated-view" class="span3 text-right">
			<g:message code="job.repeatedView.label" default="repeated view" />
		</label>
		<div class="span8">
			<g:checkBox class="form-control" name="repeatedView" value="${ job ? !job.firstViewOnly : false }" id="chkbox-repeated-view"/>
		</div>
	</div>
</div>

<g:render template="checkbox" model="${['booleanAttribute': 'persistNonMedianResults', 'job': job]}" />

<div class="row form-group ${hasErrors(bean: job, field: 'description', 'error')} required">
	<label for="description" class="span3 text-right">
		<g:message code="job.description.label" default="description" />
	</label>
	<div class="span8">
		<textarea class="form-control width_31em" name="description" id="description" rows="3">${job?.description?.trim()}</textarea>
	</div>
</div>

<div class="row">
	<label for="tags" class="span3 text-right">
		<g:message code="job.tags.label" default="tags" />
	</label>
	<div class="span8">
		<ul name="tags" style="margin-left:0px;" id="tags" class="width_31em">
		      <g:each in="${job?.tags}">
		        <li>${it}</li>
		      </g:each>
		</ul>
	</div>
</div>


<div class="row form-group ${hasErrors(bean: job, field: 'maxDownloadTimeInMinutes', 'error')}">
	<label class="span3 text-right" for="maxDownloadTimeInMinutesValue">
		<abbr title="${message(code: 'job.maxDownloadTimeInMinutes.description', args: [defaultMaxDownloadTimeInMinutes])}" data-placement="bottom" rel="tooltip">
			<g:message code="job.maxDownloadTimeInMinutes.label" default="maxDownloadTimeInMinutes" />
		</abbr>
		<span class="required-indicator">*</span>
	</label> 
	<div class="span8">
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
<div class="row form-group ${hasErrors(bean: job, field: 'executionSchedule', 'error')}">
	<label class="span3 text-right" for="executionScheduleShown">
		<g:message code="job.executionSchedule.label" default="executionSchedule" />
	</label>
	<div class="span8">

        <input type="text" id="execution-schedule-shown" class="form-control"
               value="${job.executionSchedule ? job.executionSchedule.substring(job.executionSchedule.indexOf(' ')+1, job.executionSchedule.size()) : ''}"
               onchange="updateExecScheduleInformations('0 ' + this.value, '${createLink(action: 'nextExecution', absolute: true)}')">
        <input type="text" id="execution-schedule" name="executionSchedule" value="${job?.executionSchedule}" style="display: none">
        <i class="icon-question-sign icon-large clickable-icon" onclick="toggleCronInstructions()"></i>
        <br><span id="cronhelp-readable-expression"></span>
		<br><span id="cronhelp-next-execution"></span>
        <br>
        <span id="cron-instructions" style="display: none;">
            <g:render template="cronInstructions"></g:render>
        </span>
	</div>
</div>



<div class="row form-group ${hasErrors(bean: job, field: 'jobGroup', 'error')} required">
	<label class="span3 text-right" for="jobGroup">
		<g:message code="job.jobGroup.label" default="jobGroup" />
		<span class="required-indicator">*</span>
	</label>
	<div class="span8">
		<g:select id="jobgroup" class="form-control chosen" name="jobGroup.id" from="${JobGroup.list()}"
			value="${job?.jobGroup?.id}" optionValue="name" optionKey="id" />
	</div>
</div>

<div class="row form-group">
	<label class="span3 text-right" for="connectivityProfile"> <g:message
			code="connectivityProfile.label" default="connectivityProfile" />
			 <span class="required-indicator">*</span>
	</label>
	<div class="span8">
		<g:select id="connectivityProfile" class="form-control chosen" name="connectivityProfile.id" from="${ ConnectivityProfile.list() }"
			optionKey="id" noSelection="${['null':'Native: No synthetic traffic shaping applied']}"
			value="${job?.connectivityProfile?.id}" />
	</div>
</div>

<div id="connectivityProfileDetails">
	<input type="hidden" id="customConnectivityProfile" name="customConnectivityProfile" value="${job?.customConnectivityProfile}" />
	<g:each var="attribute" in="['bandwidthDown', 'bandwidthUp', 'latency', 'packetLoss']">
		<div class="row form-group ${hasErrors(bean: job, field: attribute, 'error')}">
			<label class="span3 text-right" for="${attribute}">
				<g:message code="connectivityProfile.${attribute}.label"
					default="${attribute}" />
			</label>
			<div class="span8">
				<g:textField class="form-control" name="${attribute}"
					value="${job?."$attribute"}" />
			</div>
		</div>	
	</g:each>
</div>

<div class="row form-group ${hasErrors(bean: job, field: 'runs', 'error')} required">
	<label class="span3 text-right" for="runs"> <g:message
			code="job.runs.label" default="runs" /> <span
		class="required-indicator">*</span>
	</label>
	<div class="span8">
		<g:textField class="form-control" name="runs" value="${job?.runs ?: 1}" />
	</div>
</div>