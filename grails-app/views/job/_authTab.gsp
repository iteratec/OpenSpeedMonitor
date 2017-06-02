<div class="row fieldcontain">
    <label class="col-md-4 control-label">
        <g:message code="script.authentication.label" default="Basic Authentication" />
    </label>
    <div class="col-md-8">
        <div class="checkbox">
            <g:checkBox id="provideAuthenticateInformation" name="provideAuthenticateInformation"
                        value="${job?.provideAuthenticateInformation}" />
        </div>
    </div>
</div>

<div style="margin-top: 15px" class="row form-group fieldcontain">
    <label for="authUsername" class="col-md-4 control-label" for="authUsername">
        <g:message code="script.authUser.label" default="User" />
    </label>
    <div class="col-md-4">
        <g:textField disabled style="max-width: 200px;" class="form-control" name="authUsername" id="authUsername" value="${job?.authUsername}" autocomplete="off"/>
    </div>
</div>

<div class="row form-group fieldcontain">
    <label for="authPassword" class="col-md-4 control-label">
        <g:message code="script.authPassword.label" default="Password" />
    </label>
    <div class="col-md-4">
        <g:textField style="max-width: 200px;" class="form-control" name="authPassword" id="authPassword" value="${job?.authPassword}" type="password"/>
    </div>
</div>
