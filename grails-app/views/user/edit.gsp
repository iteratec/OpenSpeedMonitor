<%@ page import="de.iteratec.osm.security.Role" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="layoutOsm"/>
    <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<div id="edit-user" class="content scaffold-edit" role="main">
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${user}">
        <ul class="errors" role="alert">
            <g:eachError bean="${user}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><div
                        class="alert alert-danger"><g:message error="${error}"/></div></li>
            </g:eachError>
        </ul>
    </g:hasErrors>
    <g:form resource="${user}" method="PUT" class="form-horizontal">
        <ul class="nav nav-tabs section">
            <li class="active"><a data-toggle="pill" href="#userMenu">User</a></li>
            <g:if test="${sec.loggedInUserInfo(field: 'id') as long != user.id as long}">
                <sec:ifAllGranted roles='ROLE_SUPER_ADMIN'>
                    <li><a data-toggle="tab" href="#roleMenu">Role</a></li>
                </sec:ifAllGranted>
            </g:if>
        </ul>

        <div class="tab-content">
            <div id="userMenu" class="tab-pane fade in active">
                <g:hiddenField name="version" value="${user?.version}"/>
                <fieldset class="form-horizontal">
                    <f:field bean="user" property="username"/>
                    <div class="form-group fieldcontain required">
                        <label for="password" class="control-label col-md-3">Password
                            <span class="required-indicator">*</span>
                        </label>

                        <div class="controls col-md-6">
                            <input type="password" name="password" value="*****" id="password" data-cip-id="password"
                                   class="form-control"/>
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
            <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
                <div id="roleMenu" class="tab-pane fade form-horizontal">
                    <g:each var='entry' in='${roleMap}'>
                        <g:set var='roleName' value='${uiPropertiesStrategy.getProperty(entry.key, 'authority')}'/>
                        <div class="form-group">
                            <label class="col-md-3">
                                <g:link class="control-label" controller='role' action='edit'
                                        id='${entry.key.id}'>${roleName}</g:link>
                            </label>

                            <div class="col-md-6">
                                <bs:checkBox name="${roleName}" value="${entry.value}"/>
                            </div>
                        </div>

                    </g:each>

                </div>
            </sec:ifAllGranted>
        </div>

        <div>
            <g:actionSubmit class="btn btn-primary" action="update"
                            value="${message(code: 'default.button.update.label', default: 'Update')}"/>
            <g:render template="/_common/modals/deleteSymbolLink"/>
            <button class="btn btn-default" type="reset"><g:message code="default.button.reset.label"
                                                                    default="Reset"/></button>
        </div>
    </g:form>
</div>
</body>
</html>



