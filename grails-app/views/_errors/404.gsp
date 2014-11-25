<html>
	<head>
		<title><g:message code="error.404.title"/></title>
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
				<h1>
					<g:message code="error.404.callout"/>
				</h1>
				<h2>
					<g:message code="error.404.title"/>
				</h2>
				<p>
					<g:message code="error.404.message"/>
				</p>
				
				<div class="actions">
					<a href="${createLink(uri: '/')}" class="btn btn-large btn-primary">
						<i class="icon-chevron-left icon-white"></i>
						<g:message code="error.button.backToHome"/>
					</a>
					<a href="${createLink(uri: '/contact')}" class="btn btn-large btn-success">
						<i class="icon-envelope"></i>
						<g:message code="error.button.contactSupport"/>
					</a>					
				</div>
			</div>
		</div>
	</section>
  
  
  </body>
</html>