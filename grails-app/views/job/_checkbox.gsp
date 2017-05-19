<div class="form-group ${hasErrors(bean: job, field: booleanAttribute, 'error')}">
	<label for="chkbox-${booleanAttribute}" class="col-md-3 control-label">
		<g:message code="job.${booleanAttribute}.label" default="${booleanAttribute}" />
	</label>
	<div class="col-md-9">
		<div class="checkbox">
			<g:checkBox name="${booleanAttribute}" value="${job?."$booleanAttribute"}" id="chkbox-${booleanAttribute}"/>
		</div>
	</div>
</div>