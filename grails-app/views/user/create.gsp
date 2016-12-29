<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="kickstart" />
        <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="create-user" class="content scaffold-create" role="main">
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${user}">
            <ul class="errors" role="alert">
                <g:eachError bean="${user}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><div class="alert alert-danger"><g:message error="${error}"/></div></li>
                </g:eachError>
            </ul>
            </g:hasErrors>
            <g:form action="save" class="form-horizontal">
                <ul class="nav nav-tabs section">
                    <li class="active"><a data-toggle="pill" href="#userMenu">User</a></li>
                    <li><a data-toggle="tab" href="#roleMenu">Role</a></li>
                </ul>
                <div class="tab-content">
                    <div id="userMenu" class="tab-pane fade in active">
                        <g:hiddenField name="version" value="${user?.version}" />
                        <fieldset class="form-horizontal">
                            <f:field bean="user" property="username"/>
                            <div class="form-group fieldcontain required">
                                <label for="password" class="control-label col-md-3">Password
                                    <span class="required-indicator">*</span>
                                </label>
                                <div class="controls col-md-6">
                                    <input type="password" name="password" value="" id="password" data-cip-id="password" class="form-control" />
                                </div>
                            </div>
                            <f:with bean="user">
                                <f:field property="email"/>
                                <f:field property="enabled"/>
                                <f:field property="accountExpired"/>
                                <f:field property="accountLocked"/>
                                <f:field property="passwordExpired"/>
                            </f:with>
                        </fieldset>
                    </div>
                    <div id="roleMenu" class="tab-pane fade form-horizontal">
                        <g:each var='entry' in='${authorityList}'>
                            <g:set var='roleName' value='${uiPropertiesStrategy.getProperty(entry, 'authority')}'/>
                            <div class="form-group">
                                <label class="col-md-3">
                                    <g:link class="control-label" controller='role' action='edit' id='${entry.id}'>${roleName}</g:link>
                                </label>
                                <div class="col-md-6">
                                    <bs:checkBox name="${roleName}" />
                                </div>
                            </div>

                        </g:each>

                    </div>

                </div>
                <div>
                    <g:submitButton name="create" class="btn btn-primary" value="${message(code: 'default.button.create.label', default: 'Create')}" />
                    <button class="btn btn-default" type="reset"><g:message code="default.button.reset.label" default="Reset" /></button>
                </div>
            </g:form>
        </div>
    </body>
</html>
