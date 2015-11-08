<%@ page import="org.springframework.web.util.HtmlUtils" %>
<label for="navigationScript">
    <g:message code="script.navigationScript.label" default="Code" />
</label>
<textarea name="navigationScript" id="navigationScript">${code}</textarea>
<span id="setEventName-warning-clone" class="setEventName-warning-icon" style="display: none;" rel="tooltip" data-html="true"></span>
<p>
    <input type="checkbox" id="lineBreakToggle" checked />
    <label for="lineBreakToggle" style="display: inline">
        <g:message code="script.wrapLines.label" />
    </label>
</p>
<g:if test="${autoload}">
	<p id="usedVariables" 
		data-instructions="${HtmlUtils.htmlEscape(message(code: 'script.placeholdersInstructions.label'))}"
		data-usedvars="${HtmlUtils.htmlEscape(message(code: 'codemirror.usedVariables.label'))}">
    </p>
</g:if>	
