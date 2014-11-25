<div class="modal hide" id="RegisterModal">
	<g:form controller="login" action="register" class="form-horizontal" method="post" name="register_form">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal">x</button>
		<h3><g:message code="security.register.title"/></h3>
	</div>
	<div class="modal-body">
		<div class="control-group">
			<label class="control-label" for="firstname">${message(code: 'security.firstname.label', default: 'Firstname')}</label>
			<div class="controls">
				<input type="text" class="span3" name="firstname" id="firstname" placeholder="${message(code: 'security.firstname.label', default: 'Firstname')}">
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="lastname">${message(code: 'security.lastname.label', default: 'Lastname')}</label>
			<div class="controls">
				<input type="text" class="span3" name="lastname" id="lastname" placeholder="${message(code: 'security.lastname.label', default: 'Lastname')}">
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="email">${message(code: 'security.email.label', default: 'Email')}</label>
			<div class="controls">
				<input type="text" class="span3" name="email" id="email" placeholder="${message(code: 'security.email.label', default: 'Email')}">
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="password">${message(code: 'security.password.label', default: 'Password')}</label>
			<div class="controls">
				<input type="password" class="span3" name="password" id="password" placeholder="${message(code: 'security.password.label', default: 'Password')}">
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="confirmpasswd">${message(code: 'security.password.confirm.label', default: 'Confirm')}</label>
			<div class="controls">
				<input type="password" class="span3" name="confirmpasswd" id="confirmpasswd" placeholder="${message(code: 'security.password.confirm.label', default: 'Confirm')}">
			</div>
		</div>
		<div class="control-group">
<%--			<label class="control-label" for="agreement">${message(code: 'security.agreement.label', default: 'I have read and agree with the Terms of Use.')}</label>--%>
			<div class="controls">
				<label class="checkbox" for="agreement">
					<input type="checkbox" value="" name="agreement" id="agreement" >
					${message(code: 'security.agreement.label', default: 'I have read and agree with the Terms of Use.')}
				</label>
			</div>
		</div>
	</div>
	<div class="modal-footer">
		<button type="submit" class="btn btn-primary"><g:message code="security.register.label"/></button>
	</div>
	</g:form>
</div>
