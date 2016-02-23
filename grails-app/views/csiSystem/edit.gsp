<%@ page import="de.iteratec.osm.csi.CsiSystem" %>
<!doctype html>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="kickstart_osm"/>
    <g:set var="entityName" value="${message(code: 'csiSystem.label', default: 'CsiSystem')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<g:render template="/_menu/submenubarWithoutDelete"/>
<section id="edit-csiSystem" class="first">

    <div class="alert alert-error" id="errorDiv" style="display: none" > </div>
    <g:if test="${flash.error}">
        <div class="alert alert-error" style="display: block">${flash.error}</div>
    </g:if>
    <g:else>
        <g:hasErrors bean="${csiSystemInstance}">
            <div class="alert alert-error">
                <g:renderErrors bean="${csiSystemInstance}" as="list"/>
            </div>
        </g:hasErrors>
    </g:else>

    <g:form method="post" class="form-horizontal">
        <g:hiddenField name="id" value="${csiSystemInstance?.id}"/>
        <g:hiddenField name="version" value="${csiSystemInstance?.version}"/>
        <fieldset class="form">
            <g:render template="form"/>
        </fieldset>

        <div class="form-actions">
            <g:actionSubmit class="btn btn-primary" action="update"
                            value="${message(code: 'default.button.update.label', default: 'Update')}"
                            onclick="return validateInput()"/>
            <g:render template="/_common/modals/deleteSymbolLink" model="[controllerLink:createLink([controller: 'csiSystem', action: 'createDeleteConfirmationText', absolute: true])]"/>
            <button class="btn" type="reset"><g:message code="default.button.reset.label" default="Reset"/></button>
        </div>
    </g:form>

</section>

<g:render template='jobGroupWeight' model="['jobGroupWeight': null, 'i': '_clone', 'hidden': true]"/>
</body>

</html>
