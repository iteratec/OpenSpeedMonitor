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
}">
	<ul id="Menu" class="nav nav-pills">

		<g:render template="/_menu/submenubarNormalButtons"/>
		
	</ul>
</g:if>