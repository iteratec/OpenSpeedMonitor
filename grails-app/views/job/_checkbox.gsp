<div class="form-group ${hasErrors(bean: job, field: booleanAttribute, 'error')}">
	<label for="chkbox-${booleanAttribute}" class="col-md-2 control-label">
		<g:message code="job.${booleanAttribute}.label" default="${booleanAttribute}" />
	</label>
	<div class="col-md-10">
		<div class="checkbox">
			<g:checkBox id="${booleanAttribute}" name="${booleanAttribute}" value="${job?."$booleanAttribute"}" id="chkbox-${booleanAttribute}"/>
		</div>
	</div>
</div>