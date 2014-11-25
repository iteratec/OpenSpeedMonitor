<%@ page import="de.iteratec.osm.measurement.schedule.ConnectivityProfile" %>
<!doctype html>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart_osm" />
	<g:set var="entityName" value="${message(code: 'connectivityProfile.label', default: 'ConnectivityProfile')}" />
	<title><g:message code="default.create.label" args="[entityName]" /></title>
</head>

<body>
<g:render template="/layouts/mainMenu" />
<section id="create-connectivityProfile" class="first">

	<g:hasErrors bean="${connectivityProfileInstance}">
	<div class="alert alert-error">
		<g:renderErrors bean="${connectivityProfileInstance}" as="list" />
	</div>
	</g:hasErrors>
	
	<g:form action="save" class="form-horizontal" >
		<fieldset class="form">
			<g:render template="form"/>
		</fieldset>
		<div class="form-actions">
			<g:submitButton name="create" class="btn btn-primary" value="${message(code: 'default.button.create.label', default: 'Create')}" />
            <button class="btn" type="reset"><g:message code="default.button.reset.label" default="Reset" /></button>
		</div>
	</g:form>
	
</section>
		
</body>

</html>
