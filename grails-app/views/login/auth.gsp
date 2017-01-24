<g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
<html>
<head>
	<title><g:message code="springSecurity.login.title" locale="${lang}"/></title>
	<meta name="layout" content="kickstart_osm" />
	<style>
		#login {
			width: 30em;
			margin: 70px auto 0 auto;
		}
	</style>
</head>

<body>

<section id="login" class="card">
	<h2><g:message code="springSecurity.login.header" locale="${lang}"/></h2>
	<form id='loginForm' class='form-horizontal' action='${postUrl}' method='POST' autocomplete='off'>
		<fieldset>
			<div class="form-group">
				<label for='username' class="col-md-6 control-label">
					<g:message code="springSecurity.login.username.label" locale="${lang}"/>:
				</label>
				<div class='col-md-6'>
					<input type='text' class='form-control' name='username' id='username'/>
				</div>
			</div>

			<div class="form-group">
				<label for='password' class="control-label col-md-6">
					<g:message code="springSecurity.login.password.label" locale="${lang}"/>:
				</label>
				<div class='col-md-6'>
					<input type='password' class='form-control' name='password' id='password'/>
				</div>
			</div>

			<div id="remember_me_holder" class="form-group">
				<label for='remember_me' class="control-label col-md-6">
					<g:message code="springSecurity.login.remember.me.label" locale="${lang}"/>
				</label>
				<div class="col-md-6 checkbox">
					<bs:checkBox name="${rememberMeParameter}" value="${hasCookie}" id="remember_me" />
				</div>
			</div>
		</fieldset>
		<div class="clearfix">
			<input type='submit' id="submit" class="btn btn-success pull-right"
				   value='${message([code:'springSecurity.login.button', locale:lang])}'/>
			<g:if test="${grailsApplication.config.getProperty('grails.mail.disabled')?.toLowerCase() == "false"}">
				<span class="forgot-link">
					<g:link controller='register' action='forgotPassword'><g:message code='spring.security.ui.login.forgotPassword'/></g:link>
				</span>
			</g:if>
		</div>
	</form>
</section>

<script type='text/javascript'>
	<!--
	(function() {
		document.forms['loginForm'].elements['username'].focus();
	})();
	// -->
</script>

</body>
</html>
