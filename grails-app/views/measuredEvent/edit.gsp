<!DOCTYPE html>
<html>
<head>
    <g:set var="entityName" value="${message(code: 'measuredEvent.label', default: 'MeasuredEvent')}" scope="request"/>
    <meta name="layout" content="layoutOsm"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<g:render template="/_menu/submenubarWithoutDelete"/>
<div id="edit-measuredEvent" class="content scaffold-edit" role="main">
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${measuredEvent}">
        <ul class="errors" role="alert">
            <g:eachError bean="${measuredEvent}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><div
                        class="alert alert-danger"><g:message error="${error}"/></div></li>
            </g:eachError>
        </ul>
    </g:hasErrors>
    <g:form resource="${measuredEvent}" method="PUT" class="form-horizontal">
        <g:hiddenField name="version" value="${measuredEvent?.version}"/>
        <fieldset class="form">
            <f:all bean="measuredEvent"/>
        </fieldset>

        <div class="form-actions">
            <g:actionSubmit class="btn btn-primary" action="update"
                            value="${message(code: 'default.button.update.label', default: 'Update')}"/>
            <button class="btn" type="reset"><g:message code="default.button.reset.label" default="Reset"/></button>
        </div>
    </g:form>
</div>
</body>
</html>
