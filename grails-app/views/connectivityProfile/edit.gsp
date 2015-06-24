<%@ page import="de.iteratec.osm.measurement.schedule.ConnectivityProfile" %>
<!doctype html>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="kickstart_osm"/>
    <g:set var="entityName" value="${message(code: 'connectivityProfile.label', default: 'ConnectivityProfile')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<g:render template="/layouts/mainMenu"/>
<section id="edit-connectivityProfile" class="first">

    <g:hasErrors bean="${connectivityProfileInstance}">
        <div class="alert alert-error">
            <g:renderErrors bean="${connectivityProfileInstance}" as="list"/>
        </div>
    </g:hasErrors>

    <g:form method="post" class="form-horizontal">
        <g:hiddenField name="id" value="${connectivityProfileInstance?.id}"/>
        <g:hiddenField name="version" value="${connectivityProfileInstance?.version}"/>
        <fieldset class="form">
            <g:render template="form"/>
        </fieldset>

        <div class="form-actions">
            <%-- Save as copy--%>
            <g:actionSubmit class="btn btn-primary" action="save" value="${message(code: 'default.button.save.label', default: 'Save')}"/>

            <%-- Deactivate --%>
            <g:actionSubmit class="btn btn-danger" action="deactivate" value="${message(code: 'default.button.deactivate.label', default: 'Deactivate')}" />
            <%-- <a href="#DeleteModal" role="button" class="btn btn-danger" data-toggle="modal">${message(code: 'default.button.deactivate.label', default: 'Deactivate')}</a> --%>

            <%-- Cancel --%>
            <a href="<g:createLink action="list"/>" class="btn btn-warning" onclick="return confirm('${message(code: 'default.button.unsavedChanges.confirm.message', default: 'Sind Sie sicher?')}');">
                <g:message code="default.button.cancel.label" default="Abbrechen"/>
            </a>

            <%-- Reset --%>
            <button class="btn" type="reset"><g:message code="default.button.reset.label" default="Reset"/></button>

            <%--			<g:actionSubmit class="btn btn-danger" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />  --%>
        </div>
    </g:form>

</section>

</body>

</html>
