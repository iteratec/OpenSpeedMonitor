<div id="${attribute}" class="form-group ${hasErrors(bean: job, field: attribute, 'error')}">
	<label for="inputField-${attribute}" class="col-md-3 control-label">
		<g:message code="job.${attribute}.label" default="${attribute}" />
	</label>
		<div class="col-md-9">
			<textarea class="form-control" name="${attribute}" rows="3" id="inputField-${attribute}">${job?."$attribute"?.trim()}</textarea>
		</div>
</div>