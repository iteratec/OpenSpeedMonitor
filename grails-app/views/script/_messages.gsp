<g:if test="${flash.message}">
	<div class="message" role="status">${flash.message}</div>
</g:if>
<g:hasErrors bean="${script}">
	<div class="alert alert-error">
		<g:renderErrors bean="${script}" as="list" />
	</div>
</g:hasErrors>