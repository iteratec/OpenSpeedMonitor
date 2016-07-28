<%@ page import="de.iteratec.osm.report.chart.Event" %>
<!doctype html>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart" />
	<g:set var="entityName" value="${message(code: 'event.label', default: 'Event')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
</head>

<body>

    <section id="edit-event" class="first">

        <g:hasErrors bean="${eventInstance}">
        <div class="alert alert-error">
            <g:renderErrors bean="${eventInstance}" as="list" />
        </div>
        </g:hasErrors>

        <g:form resource="${eventInstance}" method="put" class="form-horizontal" >
            <g:hiddenField name="id" value="${eventInstance?.id}" />
            <g:hiddenField name="version" value="${eventInstance?.version}" />
            <fieldset class="form">
                <g:render template="form"/>
            </fieldset>
            <div class="form-actions">
                <g:actionSubmit class="btn btn-primary" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
          <g:render template="/_common/modals/deleteSymbolLink"/>
          <button class="btn" type="reset"><g:message code="default.button.reset.label" default="Reset" /></button>
            </div>
        </g:form>

    </section>

    <content tag="include.bottom">
        <asset:javascript src="event/event.js" />
        <asset:script type="text/javascript">
            $(document).ready(doOnDomReady());
        </asset:script>
    </content>

</body>

</html>
