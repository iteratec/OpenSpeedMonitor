<%@ page import="de.iteratec.osm.measurement.script.Script"%>
<asset:javascript src="bower_components/clipboard/dist/clipboard.min.js"/>
<div class="row form-group ${hasErrors(bean: job, field: 'script', 'error')}">
	<label class="span3 text-right" for="script"> <g:message code="job.selectedScript.label" default="script" /> <span class="required-indicator">*</span>
	</label>	
	<div class="span9">
		<g:select class="form-control chosen" name="script.id" id="script" from="${Script.list()}"
			value="${job?.script?.id}" optionValue="label" optionKey="id" onchange="updateScriptEditHref('${ createLink(controller: 'script', action: 'edit', absolute: true) }', \$(this).val());" />
		</div>
</div>

<div class="row form-group">
	<label class="span3 text-right"><g:message code="job.placeholders.label" default="script" /></label>
	<div class="span9">
		<div id="placeholderCandidates" 
			data-noneUsedInScript-message="${ message(code: 'job.placeholders.usedInScript.none') }"></div>
	</div>
</div>
<p style="margin: 0; padding-top: 1em;">
    <g:message code="job.script.preview.label" />
    <a href="" target="_blank" id="editScriptLink">
        <i class="fa fa-edit" rel="tooltip" title="${ message(code: 'job.script.edit') }"></i>
    </a>:
</p>
<g:render template="/script/codemirror" model="${['code': job?.script?.navigationScript, 'measuredEvents': null, 'autoload': false, 'readOnly': true]}" />
<g:if test="${mode == 'edit'}">
<button class="btn btn-default" type="button" id="copyToClipboard"><g:message code="job.script.copyToClipboard" default="Copy to Clipboard"/></button>
</g:if>