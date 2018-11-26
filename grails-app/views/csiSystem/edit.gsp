<%@ page import="de.iteratec.osm.csi.CsiSystem" %>
<!doctype html>
<html>

<head>
    <g:set var="entityName" value="${message(code: 'csiSystem.label', default: 'CsiSystem')}" scope="request"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="layoutOsm"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<g:render template="/_menu/submenubarWithoutDelete"/>
<section id="edit-csiSystem" class="first">

    <div class="alert alert-danger" id="errorDiv" style="display: none" > </div>
    <g:if test="${flash.error}">
        <div class="alert alert-danger" style="display: block">${flash.error}</div>
    </g:if>
    <g:else>
        <g:hasErrors bean="${csiSystem}">
            <div class="alert alert-danger">
                <g:renderErrors bean="${csiSystem}" as="list"/>
            </div>
        </g:hasErrors>
    </g:else>

    <g:form resource="${csiSystem}" method="put" class="form-horizontal">
        <g:hiddenField name="id" value="${csiSystem?.id}"/>
        <g:hiddenField name="version" value="${csiSystem?.version}"/>
        <fieldset class="form">
            <g:render template="form"/>
        </fieldset>

        <div>
            <g:actionSubmit class="btn btn-primary" action="update"
                            value="${message(code: 'default.button.update.label', default: 'Update')}"
                            onclick="return validateInput()"/>
            <g:render template="/_common/modals/deleteSymbolLink" model="[controllerLink:createLink([controller: 'csiSystem', action: 'createDeleteConfirmationText'])]"/>
            <button class="btn btn-default" type="reset"><g:message code="default.button.reset.label" default="Reset"/></button>
        </div>
    </g:form>

</section>

<g:render template='jobGroupWeight' model="['jobGroupWeight': null, 'i': '_clone', 'hidden': true]"/>
</body>

</html>
