<div class="row form-group ${hasErrors(bean: job, field: booleanAttribute, 'error')}">
	<label for="chkbox-active" class="span3 text-right">
		<g:message code="job.${booleanAttribute}.label" default="${booleanAttribute}" />
	</label>
	<div class="span8">
		<g:checkBox class="form-control" name="${booleanAttribute}" id="${booleanAttribute}" value="${job?."$booleanAttribute"}" id="chkbox-active"/>
	</div>
</div>