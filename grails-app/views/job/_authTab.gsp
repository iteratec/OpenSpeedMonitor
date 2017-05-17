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
<div class="row form-group fieldcontain ${hasErrors(bean: job, field: 'authUsername', 'error')}">
    <label for="authUsername" class="col-md-2 control-label" for="authUsername">
        <g:message code="script.authUser.label" default="Benutzer" />
    </label>
    <div class="col-md-10">
        <g:textField class="form-control" name="authUsername" id="authUsername" value="${job?.authUsername}" autocomplete="off"/>
    </div>
</div>
<div class="row form-group fieldcontain ${hasErrors(bean: job, field: 'authPassword', 'error')}">
    <label for="authPassword" class="col-md-2 control-label">
        <g:message code="script.authPassword.label" default="Passwort" />
    </label>
    <div class="col-md-2">
        <g:textField class="form-control" name="authPassword" id="authPassword" value="${job?.authPassword}" />
    </div>
</div>