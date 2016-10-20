<!DOCTYPE html>
<html>
<head>
	<meta name="layout" content="kickstart_osm" />
	<title><g:message code="spring.security.ui.resetPassword.title" args="[entityName]" /></title>
</head>
<body>
<g:form class="form-horizontal" role="main">
	<div class="row">
		<div class="col-md-12">
			<g:if test="${resetPasswordCommand}">
				<g:hasErrors bean="${resetPasswordCommand}">
					<div class="alert alert-error">
						<strong><g:message code="de.iteratec.isocsi.CsiDashboardController.selectionErrors.title"/></strong>
						<ul>
							<g:eachError var="eachError" bean="${resetPasswordCommand}">
								<li><g:message error="${eachError}"/></li>
							</g:eachError>
						</ul>
					</div>
				</g:hasErrors>
			</g:if>
		</div>
	</div>
	<br/>
	<g:hiddenField name='t' value='${token}'/>
	<label class="control-label">
		<g:message code="security.password.label" default="Username" />
	</label>
	<div class="control-group">
		<div class="controls">
			<g:passwordField  name='password' value="" />
		</div>
	</div>
	<label class="control-label">
		<g:message code="security.password2.label" default="Username" />
	</label>
	<div class="control-group">
		<div class="controls">
			<g:passwordField name='password2' value="" />
		</div>
	</div>
	<div class="form-actions">
		<g:actionSubmit class="btn btn-primary" action="resetPassword" value="${message(code: 'spring.security.ui.resetPassword.submit', default: 'Reset password')}" />
	</div>
</g:form>
</body>
</html>



