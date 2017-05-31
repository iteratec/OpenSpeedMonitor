<div class="form-group">
	<label class="col-md-4 control-label" for="${booleanAttribute}">
		<abbr title="${message(code: "job.${booleanAttribute}.description")}"
			  data-placement="bottom" rel="tooltip">
			<g:message code="job.${booleanAttribute}.label" default="${booleanAttribute}"/>
		</abbr>
	</label>
	<div class="col-md-8">
		<div class="checkbox">
			<g:checkBox name="${booleanAttribute}" value="${job?."$booleanAttribute"}" id="chkbox-${booleanAttribute}"/>
		</div>
	</div>
</div>