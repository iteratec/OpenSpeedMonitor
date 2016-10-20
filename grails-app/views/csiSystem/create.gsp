<%@ page import="de.iteratec.osm.csi.CsiSystem" %>
<!doctype html>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="kickstart"/>
    <g:set var="entityName" value="${message(code: 'csiSystem.label', default: 'CsiSystem')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>

<section id="create-csiSystem" class="first">

    <div class="alert alert-danger" id="errorDiv" style="display: none"></div>
    <g:if test="${flash.error}">
        <div class="alert alert-danger" style="display: block">${flash.error}</div>
    </g:if>
    <g:else>
        <g:hasErrors bean="${csiSystem}">
            <div class="alert alert-danger" style="display: none">
                <g:renderErrors bean="${csiSystem}" as="list"/>
            </div>
        </g:hasErrors>
    </g:else>

    <g:form action="save" class="form-horizontal">
        <fieldset class="form">
            <g:render template="form"/>
        </fieldset>

        <div class="form-actions">
            <g:submitButton name="create" class="btn btn-primary"
                            value="${message(code: 'default.button.create.label', default: 'Create')}"
                            onclick="return validateInput()"/>
            <button class="btn btn-default" type="reset"><g:message code="default.button.reset.label" default="Reset"/></button>
        </div>
    </g:form>

</section>

<g:render template='jobGroupWeight' model="['jobGroupWeight': null, 'i': '_clone', 'hidden': true]"/>

</body>

</html>
