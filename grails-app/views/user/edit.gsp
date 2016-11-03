<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="kickstart" />
        <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="edit-user" class="content scaffold-edit" role="main">
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
            <g:form resource="${user}" method="PUT" class="form-horizontal">
                <ul class="nav nav-tabs">
                    <li class="active"><a data-toggle="pill" href="#userMenu">User</a></li>
                    <li><a data-toggle="tab" href="#roleMenu">Role</a></li>
                </ul>
                <div class="tab-content">
                    <div id="userMenu" class="tab-pane fade in active">
                        <g:hiddenField name="version" value="${user?.version}" />
                        <fieldset class="form">
                            <f:field bean="user" property="username"/>
                            <div class="control-group fieldcontain required">
                                <lable for="password" class="control-label">Password
                                <span class="required-indicator">*</span>
                                </lable>
                                <div class="controls">
                                    <input type="password" name="password" value="*****" id="password" data-cip-id="password">
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
                    <div id="roleMenu" class="tab-pane fade">
                        <g:each var='entry' in='${roleMap}'>
                            <g:set var='roleName' value='${uiPropertiesStrategy.getProperty(entry.key, 'authority')}'/>
                            <div class="form-group">
                                <g:link class="control-label" controller='role' action='edit' id='${entry.key.id}'>${roleName}</g:link>
                                <div>
                                    <bs:checkBox name="${roleName}" value="${entry.value}" />
                                </div>
                            </div>

                        </g:each>

                    </div>

                </div>

                <div>
                    <g:actionSubmit class="btn btn-primary" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
                    <g:render template="/_common/modals/deleteSymbolLink"/>
                    <button class="btn btn-default" type="reset"><g:message code="default.button.reset.label" default="Reset" /></button>
                </div>
            </g:form>
        </div>
    </body>
</html>



