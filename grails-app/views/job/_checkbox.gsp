<div class="form-group ${hasErrors(bean: job, field: booleanAttribute, 'error')}">
	<label for="chkbox-active" class="col-md-2 control-label">
		<g:message code="job.${booleanAttribute}.label" default="${booleanAttribute}" />
	</label>
	<div class="col-md-10">
		<div class="checkbox">
			<g:checkBox name="${booleanAttribute}" id="${booleanAttribute}" value="${job?."$booleanAttribute"}" id="chkbox-active"/>
		</div>
	</div>
</div>