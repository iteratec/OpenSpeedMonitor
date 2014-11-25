<g:each var="booleanAttribute" in="['captureVideo', 'web10', 'noscript', 'clearcerts', 'ignoreSSL', 'standards', 'tcpdump', 'bodies', 'continuousVideo', 'keepua']">
	<g:render template="checkbox" model="${['booleanAttribute': booleanAttribute, 'job': job]}" />
</g:each>

<r:script>
	$(document).ready(function() {
		$('#provideAuthenticateInformation').click(function() {
			$('.authInfo').toggle($(this).prop('checked'));
		});
		$('.authInfo').toggle($('#provideAuthenticateInformation').prop('checked'));
	});
</r:script>

<div class="row fieldcontain ${hasErrors(bean: job, field: 'provideAuthenticateInformation', 'error')}">
	<label class="span3 text-right">
		<g:message code="script.authentication.label" default="Authenfizierung" />
	</label>
	<div class="span9">
		<g:checkBox id="provideAuthenticateInformation" name="provideAuthenticateInformation" value="${job?.provideAuthenticateInformation}" />
	</div>
</div>


<div class="row form-group authInfo fieldcontain ${hasErrors(bean: job, field: 'authUsername', 'error')}">
	<label for="authUsername" class="span3 text-right" for="authUsername">
		<g:message code="script.authUser.label" default="Benutzer" />
	</label>
	<div class="span9">
		<g:textField name="authUsername" id="authUsername" value="${job?.authUsername}" />
	</div>
</div>
<div class="row form-group authInfo fieldcontain ${hasErrors(bean: job, field: 'authPassword', 'error')}">
	<label for="authPassword" class="span3 text-right">
		<g:message code="script.authPassword.label" default="Passwort" />
	</label>
	<div class="span9">
		<g:passwordField name="authPassword" id="authPassword" value="${job?.authPassword}" />
	</div>
</div>