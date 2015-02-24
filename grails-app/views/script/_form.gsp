<div class="fieldcontain ${hasErrors(bean: script, field: 'label', 'error')} required">
	<label for="label">
		<g:message code="script.label.label" default="Name"/>
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="label" value="${script?.label}" class="width_31em"/>
</div>
<div class="fieldcontain ${hasErrors(bean: script, field: 'description', 'error')}">
	<label for="description">
		<g:message code="script.description.label" default="Beschreibung" />
	</label>
    <textarea class="form-control width_31em" name="description" id="description" rows="3">${script?.description}</textarea>
</div>
<div class="fieldcontain ${hasErrors(bean: script, field: 'navigationScript', 'error')}">
	<label for="navigationScript">
		<g:message code="script.navigationScript.label" default="Code" />
	</label>
	<g:render template="codemirror" model="${['code': script?.navigationScript, 'measuredEvents': measuredEvents, 'autoload': true, 'readOnly': false]}" />	
	<p style="margin-top: 1em;"><g:message code="script.autoComplete.label" default="Drücken Sie Strg + Leertaste zum Vervollständigen von Event-Namen." /></p>	
</div>


