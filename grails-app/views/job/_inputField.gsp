<div id="${attribute}" class="form-group ${hasErrors(bean: job, field: attribute, 'error')}">
	<label for="inputField-${attribute}" class="col-md-3 control-label">
		<g:message code="job.${attribute}.label" default="${attribute}" />
	</label>
		<div class="col-md-9">
			<g:textField class="form-control" name="${attribute}" value="${job?."$attribute"}" id="inputField-${attribute}"/>
		</div>
</div>