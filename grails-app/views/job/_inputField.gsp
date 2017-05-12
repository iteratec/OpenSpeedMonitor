<div class="form-group ${hasErrors(bean: job, field: atrribute, 'error')}">
	<label for="inputField-${atrribute}" class="col-md-2 control-label">
		<g:message code="job.${atrribute}.label" default="${atrribute}" />
	</label>
		<div class="col-md-8">
			<g:textField class="form-control" name="${atrribute}" value="${job?."$atrribute"}" id="inputField-${atrribute}"/>
		</div>
</div>