<%@ page import="de.iteratec.osm.report.external.GraphiteServer" %>
<!doctype html>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart" />
	<g:set var="entityName" value="${message(code: 'graphiteServer.label', default: 'GraphiteServer')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
</head>

<body>

<section id="edit-graphiteServer" class="first">

	<g:hasErrors bean="${graphiteServerInstance}">
	<div class="alert alert-error">
		<g:renderErrors bean="${graphiteServerInstance}" as="list" />
	</div>
	</g:hasErrors>

	<g:form method="post" class="form-horizontal" >
		<g:hiddenField name="id" value="${graphiteServerInstance?.id}" />
		<g:hiddenField name="version" value="${graphiteServerInstance?.version}" />
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
			
</body>

</html>
