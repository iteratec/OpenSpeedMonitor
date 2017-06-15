<div class="form-group">
	<label class="col-md-4 control-label" for="${booleanAttribute}">
		<abbr title="${description}"
			  data-placement="bottom" rel="tooltip">
			${label}
		</abbr>
	</label>
	<div class="col-md-8">
		<div class="checkbox">
			<g:checkBox name="${booleanAttribute}" value="${job?."$booleanAttribute"}" id="chkbox-${booleanAttribute}"/>
		</div>
	</div>
</div>