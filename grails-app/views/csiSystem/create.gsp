<%@ page import="de.iteratec.osm.csi.CsiSystem" %>
<!doctype html>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart" />
	<g:set var="entityName" value="${message(code: 'csiSystem.label', default: 'CsiSystem')}" />
	<title><g:message code="default.create.label" args="[entityName]" /></title>
</head>

<body>

<section id="create-csiSystem" class="first">

	<g:hasErrors bean="${csiSystemInstance}">
	<div class="alert alert-error">
		<g:renderErrors bean="${csiSystemInstance}" as="list" />
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

<g:render template='jobGroupWeight' model="['jobGroupWeight':null,'i':'_clone','hidden':true]"/>
		
</body>

</html>
