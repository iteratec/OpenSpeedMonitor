<g:if test="${flash.message}">
	<div class="message" role="status">${flash.message}</div>
</g:if>
<g:hasErrors bean="${job}">
	<div class="alert alert-error">
		<g:renderErrors bean="${job}" as="list" />
	</div>
</g:hasErrors>