<%@ page import="de.iteratec.osm.measurement.script.Script" %>
<asset:javascript src="bower_components/clipboard/dist/clipboard.min.js"/>
<div class="form-group ${hasErrors(bean: job, field: 'script', 'error')}">
    <label class="col-md-2 control-label" for="script"><g:message code="job.selectedScript.label"
                                                                  default="Selected Script"/> <span
            class="required-indicator">*</span>
    </label>

    <div class="col-md-4">
        <g:select class="form-control chosen" name="script.id" id="script" from="${[[id: "", label: ""]] + Script.list()}"
                  value="${job?.script?.id || ""}" optionValue="label" optionKey="id"
                  data-placeholder="${message(code: 'job.script.choose', default: 'Choose a script')}"
                  onchange="updateScriptEditHref('${createLink(controller: 'script', action: 'edit')}', \$(this).val());"/>
    </div>
</div>

<div class="form-group">
    <label class="col-md-2 control-label"><g:message code="job.placeholders.label" default="Variables"/></label>

    <div class="col-md-9">
        <div id="placeholderCandidates"
             data-noneUsedInScript-message="${message(code: 'job.placeholders.usedInScript.none', default: 'No variables are used in script {0}')}"></div>
    </div>
</div>

<g:render template="/script/codemirror"
          model="${['code': job?.script?.navigationScript, 'measuredEvents': null, 'autoload': false, 'readOnly': true]}"/>

<button class="btn btn-default" id="script_button_copyToClipboard"  type="button" id="copyToClipboard">
    <g:message code="job.script.copyToClipboard" default="Copy To Clipboard"/>
</button>
<a href="" target="_blank" id="editScriptLink">
    <button id="script_button_edit"  class="btn btn-default" type="button" id="edit">
        <g:message code="job.script.edit" default="Edit script"/>
    </button>
</a>

