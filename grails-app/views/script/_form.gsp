<div class="fieldcontain ${hasErrors(bean: script, field: 'label', 'error')} form-group">
	<label for="label" class="control-label col-md-2">
		<g:message code="script.label.label" default="Name"/>
		<span class="required-indicator">*</span>
	</label>
	<div class="col-md-10">
		<g:textField name="label" value="${script?.label}" class="form-control" required=""/>
	</div>
</div>
<div class="fieldcontain ${hasErrors(bean: script, field: 'description', 'error')} form-group">
	<label for="description" class="control-label col-md-2">
		<g:message code="script.description.label" default="Beschreibung" />
	</label>
	<div class="col-md-10">
		<textarea class="form-control" name="description" id="description" rows="3">${script?.description}</textarea>
	</div>
</div>
<div class="fieldcontain ${hasErrors(bean: script, field: 'navigationScript', 'error')}">
	<div class="text-right"><a href="https://sites.google.com/a/webpagetest.org/docs/using-webpagetest/scripting" target="_blank">
		<g:message code="de.iteratec.osm.measurement.script.wpt-dsl.link.text" default="Documentation WebPagetest DSL" /></a></div>
	<g:render template="codemirror" model="${['code': script?.navigationScript, 'measuredEvents': measuredEvents, 'autoload': true]}" />
	<p style="margin-top: 1em;"><g:message code="script.autoComplete.label" default="Press Ctrl + Space to get a list of keywords or valid event names, respectively." /></p>
</div>


