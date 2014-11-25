<div class="">
	<ul class="nav nav-tabs" data-role="listview" data-split-icon="gear"
		data-filter="true">
<!-- 
		<sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
			<g:each status="i" var="c"
				in="${grailsApplication.controllerClasses.sort { it.logicalPropertyName } }">
				<li
					class="controller${params.controller == c.logicalPropertyName ? " active" : ""}">
					<g:link controller="${c.logicalPropertyName}" action="index">
						<g:message code="${c.logicalPropertyName}.label"
							default="${c.logicalPropertyName.capitalize()}" />
					</g:link>
				</li>
			</g:each>
		</sec:ifAllGranted>
 -->		
		<sec:ifNotGranted roles="ROLE_SUPER_ADMIN">
			<li class="controller"><g:link controller="csiDashboard"
					action="showAll"
					title="${g.message([code:'csiDashboard.description'])}">
					<g:message code="csiDashboard.label" />
				</g:link></li>
			<li class="controller"><g:link controller="eventResultDashboard"
					action="showAll"
					title="${g.message([code:'eventResultDashboard.description'])}">
					<g:message code="eventResultDashboard.label" />
				</g:link></li>
				
			<%-- Controller buggy und daher ausgeblendet --%>
			<g:if test="${false}">
			<li class="controller"><g:link controller="eventResult"
					action="listResults"
					title="${g.message([code:'eventResult.description'])}">
					<g:message code="eventResult.label" />
				</g:link></li>
			</g:if>
		</sec:ifNotGranted>

	</ul>
</div>
