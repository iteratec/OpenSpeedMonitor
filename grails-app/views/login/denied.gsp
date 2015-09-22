<g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
<html>
<head>
	<title><g:message code="springSecurity.denied.title"/></title>
	<meta name="layout" content="kickstart" />

	<g:set var="layout_nomainmenu"		value="${true}" scope="request"/>
	<g:set var="layout_nosecondarymenu"	value="${true}" scope="request"/>
</head>

<body>
	<content tag="header">
		<!-- Empty Header -->
	</content>

  	<section id="Error" class="">
		<div class="big-message">
			<div class="container">
				<h1><g:message code="springSecurity.denied.title" locale="${lang}"/>!</h1>
		    	<h2><g:message code="springSecurity.denied.message" locale="${lang}"/></h2> 
				
				<div class="actions margin-top-large">
					<a href="${createLink(uri: '/')}" class="btn btn-large btn-primary">
						<i class="fa fa-chevron-left"></i>
						<g:message code="error.button.backToHome" locale="${lang}"/>
					</a>
					<a href="${createLink(uri: '/login')}" class="btn btn-large btn-success">
						<i class="fa fa-user"></i>
						<g:message code="error.button.Login" locale="${lang}"/>
					</a>					
				</div>
			</div>
		</div>
	</section>

<script type='text/javascript'>
	<!--
	(function() {
		document.forms['loginForm'].elements['j_username'].focus();
	})();
	// -->
</script>

</body>
</html>
