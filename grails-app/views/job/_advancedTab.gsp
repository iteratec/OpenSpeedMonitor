<div class="form-group ${hasErrors(bean: job, field: 'firstViewOnly', 'error')}">
    <label for="chkbox-first-view" class="col-md-2 control-label">
        <g:message code="job.firstView.label" default="first view"/>
    </label>

    <div class="col-md-10">
        <div class="checkbox">
            <input type="checkbox" id="chkbox-first-view" checked disabled>
        </div>
    </div>
</div>

<div class="form-group ${hasErrors(bean: job, field: 'firstViewOnly', 'error')}">
    <label for="chkbox-repeated-view" class="col-md-2 control-label">
        <g:message code="job.repeatedView.label" default="repeated view"/>
    </label>

    <div class="col-md-10">
        <div class="checkbox">
            <g:checkBox name="repeatedView" value="${job && !job.firstViewOnly ? true : false}" id="chkbox-repeated-view"/>
        </div>
    </div>
</div>

<g:render template="checkbox" model="${['booleanAttribute': 'persistNonMedianResults', 'job': job]}"/>

<g:each var="stringAttribute"  in="['option_block',
                                    'option_uastring',
                                    'option_medianMetric',
                                    'option_custom',
                                    'option_tester',
                                    'option_affinity',
                                    'option_appendua',
                                    'option_type',
                                    'option_customHeaders']">
    <g:render template="inputField" model="${['attribute': stringAttribute, 'job': job]}" />
</g:each>

<g:each var="booleanAttribute" in="['captureVideo',
                                    'clearcerts',
                                    'ignoreSSL',
                                    'standards',
                                    'tcpdump',
                                    'bodies',
                                    'continuousVideo',
                                    'keepua']">
	<g:render template="checkbox" model="${['booleanAttribute': booleanAttribute, 'job': job]}" />
</g:each>

<g:render template="checkbox" model="${['booleanAttribute': 'option_customDimensions', 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'option_width', 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'option_height', 'job': job]}" />
<g:render template="checkbox" model="${['booleanAttribute': 'option_customBrowserDimensions', 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'option_browser_width', 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'option_browser_height', 'job': job]}" />

<div class="row fieldcontain ${hasErrors(bean: job, field: 'provideAuthenticateInformation', 'error')}">
	<label class="col-md-2 control-label">
		<g:message code="script.authentication.label" default="Authenfizierung" />
	</label>
	<div class="col-md-10">
		<div class="checkbox">
			<g:checkBox id="provideAuthenticateInformation" name="provideAuthenticateInformation"
						value="${job?.provideAuthenticateInformation}" />
		</div>
	</div>
</div>


<p></p>
<div class="row form-group authInfo fieldcontain ${hasErrors(bean: job, field: 'authUsername', 'error')}">
	<label for="authUsername" class="col-md-2 control-label" for="authUsername">
		<g:message code="script.authUser.label" default="Benutzer" />
	</label>
	<div class="col-md-10">
		<g:textField class="form-control" name="authUsername" id="authUsername" value="${job?.authUsername}" autocomplete="off"/>
	</div>
</div>
<div class="row form-group authInfo fieldcontain ${hasErrors(bean: job, field: 'authPassword', 'error')}">
	<label for="authPassword" class="col-md-2 control-label">
		<g:message code="script.authPassword.label" default="Passwort" />
	</label>
	<div class="col-md-2">
		<g:textField class="form-control" name="authPassword" id="authPassword" value="${job?.authPassword}" />
	</div>
</div>