<g:each var="variable" in="${variables}">
	<div class="placeholder-row" id="${variable.name}">
		<label for="variables.${variable.name}">&#x24;{${variable.name}}</label>
		<g:if test="${ variable.editable }">
			<input id="variables.${variable.name}" type="text" name="variables.${variable.name}" value="${variable.value}" class="form-control"/>
		</g:if>
		<g:else>
			<input id="variables.${variable.name}" type="hidden" name="variables.${variable.name}" value="${variable.value}" />
			${variable.value}
			<a onclick="$('#${variable.name}').remove();">
				<i class="fa fa-times" rel="tooltip" title="Delete" style="cursor: pointer; text-decoration: none;"></i>
			</a>
		</g:else>
	</div>
</g:each>
<g:if test="${ variables.size() == 0 }">
	<g:message code="job.placeholders.usedInScript.none" args="${[script.label]}" />
</g:if>