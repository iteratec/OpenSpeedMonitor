<!DOCTYPE html>
<html>
    <head>
        <g:set var="entityName" value="${message(code: 'graphiteServer.label', default: 'GraphiteServer')}" scope="request"/>
        <meta name="layout" content="kickstart" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="edit-graphiteServer" class="content scaffold-edit" role="main">
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${graphiteServer}">
            <ul class="errors" role="alert">
                <g:eachError bean="${graphiteServer}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><div class="alert alert-danger"><g:message error="${error}"/></div></li>
                </g:eachError>
            </ul>
            </g:hasErrors>
            <g:form resource="${graphiteServer}" method="PUT" class="form-horizontal">
                <g:hiddenField name="version" value="${graphiteServer?.version}" />
                <fieldset class="form-horizontal">
                    <g:render template="form" />
                </fieldset>
                <div>
                    <g:actionSubmit class="btn btn-primary" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
                    <g:render template="/_common/modals/deleteSymbolLink"/>
                    <button class="btn btn-default" type="reset"><g:message code="default.button.reset.label" default="Reset" /></button>
                </div>
            </g:form>
        </div>
    </body>
</html>
