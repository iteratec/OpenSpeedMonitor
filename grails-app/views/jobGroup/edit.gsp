<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<%@ defaultCodec="none" %>
<!doctype html>
<html>

<head>
    <g:set var="entityName" value="${message(code: 'jobGroup.label', default: 'JobGroup')}" scope="request"/>
    <parameter name="layout_nosecondarymenu" value="${true}"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="layoutOsm"/>
    <title><g:message code="default.edit.label" args="[jobGroup.name]"/></title>

    <asset:stylesheet src="tagit.css"/>
</head>

<body>
<g:render template="/_menu/submenubarWithoutDelete"/>
<section id="edit-jobGroup" class="first">

    <g:hasErrors bean="${jobGroup}">
        <div class="alert alert-danger">
            <g:renderErrors bean="${jobGroup}" as="list"/>
        </div>
    </g:hasErrors>

    <g:form resource="${jobGroup}" method="put" class="form-horizontal">
        <g:hiddenField name="id" value="${jobGroup?.id}"/>
        <g:hiddenField name="version" value="${jobGroup?.version}"/>
        <fieldset class="form">
            <g:render template="form"/>
        </fieldset>

        <div>
            <g:actionSubmit class="btn btn-primary" action="update"
                            value="${message(code: 'default.button.update.label', default: 'Update')}"/>
            <button class="btn" type="reset"><g:message code="default.button.reset.label" default="Reset"/></button>
        </div>
    </g:form>

</section>
</body>

</html>
