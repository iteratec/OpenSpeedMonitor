<%@ page import="de.iteratec.osm.measurement.environment.WebPageTestServer" %>
<!doctype html>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart" />
	<g:set var="entityName" value="${message(code: 'webPageTestServer.label', default: 'WebPageTestServer')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
</head>

<body>

<section id="edit-webPageTestServer" class="first">

	<g:hasErrors bean="${webPageTestServer}">
	<div class="alert alert-error">
		<g:renderErrors bean="${webPageTestServer}" as="list" />
	</div>
	</g:hasErrors>

	<g:form method="put" class="form-horizontal" >
		<g:hiddenField name="id" value="${webPageTestServer?.id}" />
		<g:hiddenField name="version" value="${webPageTestServer?.version}" />
		<fieldset class="form">
			<g:render template="form"/>
		</fieldset>
		<div class="form-actions">
			<g:actionSubmit class="btn btn-primary" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
			<a href="#DeleteModal" role="button" class="btn btn-danger" data-toggle="modal">${message(code: 'default.button.delete.label', default: 'Delete')}</a>
  
<%--			<g:actionSubmit class="btn btn-danger" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /> --%>
            <button class="btn" type="reset"><g:message code="default.button.reset.label" default="Reset" /></button>
		</div>
	</g:form>

</section>
			
</body>

</html>
