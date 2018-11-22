<%@ page import="de.iteratec.osm.measurement.environment.WebPageTestServer" %>
<!doctype html>
<html>

<head>
	<g:set var="entityName" value="${message(code: 'webPageTestServer.label', default: 'WebPageTestServer')}" scope="request"/>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="layoutOsm" />
	<title><g:message code="default.create.label" args="[entityName]" /></title>
</head>

<body>

<section id="create-webPageTestServer" class="first">

	<g:hasErrors bean="${webPageTestServer}">
	<div class="alert alert-danger">
		<g:renderErrors bean="${webPageTestServer}" as="list" />
	</div>
	</g:hasErrors>
	
	<g:form action="save" class="form-horizontal" >
		<fieldset class="form">
			<g:render template="form"/>
		</fieldset>
		<div>
			<g:submitButton name="create" class="btn btn-primary" value="${message(code: 'default.button.create.label', default: 'Create')}" />
            <button class="btn btn-default" type="reset"><g:message code="default.button.reset.label" default="Reset" /></button>
		</div>
	</g:form>
	
</section>
		
</body>

</html>
