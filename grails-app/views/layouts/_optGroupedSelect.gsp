<select ${multiple} id="${id}" class="${cssClass}" style="${style}" name="${name}">
	<g:each in="${dataMap}" var="data">
		<optgroup label="${message(code: 'de.iteratec.isr.measurand.group.'+data.key)}">
			<g:each in="${data.value}" var="value">
				<g:if test="${selectedValues.contains(value)}">
					<option value="${value}" selected="true"><g:message code="de.iteratec.isr.measurand.${value}" default="${value}" /></option>
				</g:if>
				<g:else>
					<option value="${value}" ><g:message code="de.iteratec.isr.measurand.${value}" default="${value}" /></option>
				</g:else>
			</g:each>
		</optgroup>
	</g:each>
</select>