<!-- 
This menu is used to show function that can be triggered on the content (an object or list of objects).
-->

<%-- Only show the "Pills" navigation menu if a controller exists (but not for home) --%>
<g:if test="${	params.controller != null
			&&	params.controller != ''
			&&	params.controller != 'home'
			&&	params.controller != 'script'
			&&	params.controller != 'job'
			&&  params.controller != 'connectivityProfile'
			&&  params.controller != 'osmConfiguration'
}">
	<ul id="Menu" class="nav nav-pills">

		<g:set var="entityName" value="${message(code: params.controller+'.label', default: params.controller.substring(0,1).toUpperCase() + params.controller.substring(1).toLowerCase())}" />

		<li class="${ params.action == "index" ? 'active' : '' }">
			<g:link action="index"><i class="fas fa-th-list"></i> <g:message code="default.list.label"
																			 args="[entityName]"/></g:link>
		</li>
		<li class="${ params.action == "create" ? 'active' : '' }">
			<g:link action="create"><i class="fas fa-plus"></i> <g:message code="default.new.label"
																		   args="[entityName]"/></g:link>
		</li>

		<g:if test="${ params.action == 'show' || params.action == 'edit' || params.action == 'update' }">
			<!-- the item is an object (not a list) -->
			<li class="${ params.action == "edit" ? 'active' : '' }">
				<g:link action="edit" id="${params.id}"><i class="fas fa-pencil-alt"></i> <g:message code="default.edit.label"  args="[entityName]"/></g:link>
			</li>
			<li>
				<g:render template="/_common/modals/deleteTextLink"/>
			</li>
		</g:if>
		
	</ul>
</g:if>
