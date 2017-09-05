<!DOCTYPE html>
<html>
    <head>
        <g:set var="entityName" value="${message(code: 'threshold.label', default: 'Threshold')}" scope="request" />
        <meta name="layout" content="kickstart" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="edit-threshold" class="content scaffold-edit" role="main">
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${threshold}">
            <ul class="errors" role="alert">
                <g:eachError bean="${threshold}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><div class="alert alert-danger"><g:message error="${error}"/></div></li>
                </g:eachError>
            </ul>
            </g:hasErrors>
            <g:form resource="${threshold}" method="PUT" class="form-horizontal">
                <g:hiddenField name="version" value="${threshold?.version}" />
                <fieldset class="form-horizontal">
                    <f:all bean="threshold"/>
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
