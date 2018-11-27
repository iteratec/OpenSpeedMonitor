<!DOCTYPE html>
<html>
    <head>
        <g:set var="entityName" value="${message(code: 'graphiteEventSourcePath.label', default: 'GraphiteEventSourcePath')}" scope="request"/>
        <meta name="layout" content="layoutOsm" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="create-graphiteEventSourcePath" class="content scaffold-create" role="main">
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${graphiteEventSourcePath}">
            <ul class="errors" role="alert">
                <g:eachError bean="${graphiteEventSourcePath}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><div class="alert alert-danger"><g:message error="${error}"/></div></li>
                </g:eachError>
            </ul>
            </g:hasErrors>
            <g:form action="save" class="form-horizontal">
                <fieldset class="form-horizontal">
                    <g:render template="form" />
                </fieldset>
                <div>
                    <g:submitButton name="create" class="btn btn-primary" value="${message(code: 'default.button.create.label', default: 'Create')}" />
                    <button class="btn btn-default" type="reset"><g:message code="default.button.reset.label" default="Reset" /></button>
                </div>
            </g:form>
        </div>
    </body>
</html>
