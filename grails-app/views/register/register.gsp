<!DOCTYPE html>
<html>
<head>
	<meta name="layout" content="kickstart_osm" />
	<g:set var="entityName" value="${message(code: 'register.label', default: 'Register')}" />
	<title><g:message code="spring.security.ui.register.title" args="[entityName]" /></title>
</head>
<body>
<g:form class="form-horizontal" role="main">
	<g:if test="${flash.message}">
		<div class="message" role="status">${flash.message}</div>
	</g:if>
	<div class="row">
		<div class="col-md-12">
			<g:if test="${registerCommand}">
				<g:hasErrors bean="${registerCommand}">
					<div class="alert alert-error">
						<strong><g:message code="de.iteratec.isocsi.CsiDashboardController.selectionErrors.title"/></strong>
						<ul>
							<g:eachError var="eachError" bean="${registerCommand}">
								<li><g:message error="${eachError}"/></li>
							</g:eachError>
						</ul>
					</div>
				</g:hasErrors>
			</g:if>
		</div>
	</div>
	<g:if test='${emailSent}'>
		<br/>
		<g:message code='spring.security.ui.register.sent'/>
	</g:if>
	<g:else>
		<br/>
		<label class="control-label">
			<g:message code="security.user.label" default="Username" />
		</label>
		<div class="control-group">
			<div class="controls">
				<g:textField  name="username" value="" />
			</div>
		</div>
		<label class="control-label">
			<g:message code="security.email.label" default="E-mail" />
		</label>
		<div class="control-group">
			<div class="controls">
				<g:textField  name="email" value="" />
			</div>
		</div>
		<label class="control-label">
			<g:message code="security.password.label" default="Password" />
		</label>
		<div class="control-group">
			<div class="controls">
				<g:passwordField  name="password" value="" />
			</div>
		</div>
		<label class="control-label">
			<g:message code="security.password2.label" default="Password (again)" />
		</label>
		<div class="control-group">
			<div class="controls">
				<g:passwordField  name="password2" value="" />
			</div>
		</div>
		<div class="form-actions">
			<g:actionSubmit class="btn btn-primary" action="register" value="${message(code: 'spring.security.ui.register.submit', default: 'Create your account')}" />
		</div>
	</g:else>
</g:form>
</body>
</html>