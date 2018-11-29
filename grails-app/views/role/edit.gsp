<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="layoutOsm" />
        <g:set var="entityName" value="${message(code: 'role.label', default: 'Role')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="edit-role" class="content scaffold-edit" role="main">
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${role}">
            <ul class="errors" role="alert">
                <g:eachError bean="${role}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><div class="alert alert-danger"><g:message error="${error}"/></div></li>
                </g:eachError>
            </ul>
            </g:hasErrors>
            <g:form resource="${role}" method="PUT" class="form-horizontal">
                <ul class="nav nav-tabs">
                    <li class="active"><a data-toggle="pill" href="#roleMenu">Role</a></li>
                    <li><a data-toggle="tab" href="#userMenu">User</a></li>
                </ul>
                <div class="tab-content">
                    <div id="roleMenu" class="tab-pane fade in active">
                        <g:hiddenField name="version" value="${role?.version}" />
                        <fieldset class="form">
                            <f:all bean="role"/>
                        </fieldset>
                    </div>
                    <div id="userMenu" class="tab-pane fade">
                        <g:if test='${users.empty}'><g:message code='spring.security.ui.role_no_users'/></g:if>
                        <div class="form-group">
                            <g:each var='u' in='${users}'>
                                <g:link class="control-label" controller='user' action='edit' id='${u.id}'>${uiPropertiesStrategy.getProperty(u, 'username')}</g:link><br/>
                            </g:each>
                        </div>

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



