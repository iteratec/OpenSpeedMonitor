<!DOCTYPE html>
<html>
<head>
	<meta name="layout" content="layoutOsm" />
	<title><g:message code="spring.security.ui.forgotPassword.title" args="[entityName]" /></title>
</head>
<body>
<g:form class="form-horizontal" role="main">
	<g:if test="${flash.message}">
		<div class="message" role="status">${flash.message}</div>
	</g:if>
	<div class="row">
		<div class="col-md-12">
			<g:if test="${forgotPasswordCommand}">
				<g:hasErrors bean="${forgotPasswordCommand}">
					<div class="alert alert-danger">
						<strong><g:message code="de.iteratec.isocsi.CsiDashboardController.selectionErrors.title"/></strong>
						<ul>
							<g:eachError var="eachError" bean="${forgotPasswordCommand}">
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
		<div class="form-group">
			<div>
				<g:textField  name="username" value="" />
			</div>
		</div>
		<div>
			<g:actionSubmit class="btn btn-primary" action="forgotPassword" value="${message(code: 'spring.security.ui.forgotPassword.submit', default: 'Reset password')}" />
		</div>
	</g:else>
</g:form>
</body>
</html>

