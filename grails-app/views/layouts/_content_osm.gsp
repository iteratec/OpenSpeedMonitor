<div id="Content" class="container-fluid">
	<!-- print system messages (infos, warnings, etc) - not validation errors -->
	<g:if test="${flash.message && !layout_noflashmessage}">
		<div class="alert alert-info">${flash.message}</div>
	</g:if>

	<!-- Show page's content -->
	<g:layoutBody />
	<g:pageProperty name="page.body" />
</div>
