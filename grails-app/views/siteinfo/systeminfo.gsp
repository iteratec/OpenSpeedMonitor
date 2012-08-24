<html>

<head>
    <title><g:message code="default.contact.title"/></title>
    <meta name="layout" content="kickstart" />
</head>

<body>

	<content tag="header">
		<header id="Header" class="jumbotron masthead">
			<div class="inner">
				<div class="container">
					<h1 class="title">Technical Info about ${meta(name:'app.name')}</h1>
					<p class="lead">This page presents some information about the ${meta(name:'app.name')} system.</p>
				</div>
			</div>
		</header>
	</content>
	
	<section id="resources">
	     <h1><g:message code="default.systeminfo.resources"/></h1>
	     <ul>
	         <li>Web Layout: <a href ="http://twitter.github.com/bootstrap/">Bootstrap</a>, from Twitter 
	         Licensed under the Apache License v2.0. Documentation licensed under CC BY 3.0. 
	         (@TwBootstrap , http://twitter.github.com/bootstrap/)</li>
	         <li>Datepicker: <a href ="https://github.com/eternicode/bootstrap-datepicker">Datepicker for Bootstrap</a>, 
	         Copyright 2012 Stefan Petre, Improvements by Andrew Rowls, Licensed under the Apache License v2.0</li>
	     </ul>
	</section>
	
	<section id="application">
	     <h1><g:message code="default.systeminfo.status"/></h1>
	     <ul>
			<li>App version: <g:meta name="app.version"/></li>
			<li>Grails version: <g:meta name="app.grails.version"/></li>
			<li>Groovy version: ${org.codehaus.groovy.runtime.InvokerHelper.getVersion()}</li>
			<li>JVM version: ${System.getProperty('java.version')}</li>
			<li>Reloading active: ${grails.util.Environment.reloadingAgentEnabled}</li>
			<li>Controllers: ${grailsApplication.controllerClasses.size()}</li>
			<li>Domains: ${grailsApplication.domainClasses.size()}</li>
			<li>Services: ${grailsApplication.serviceClasses.size()}</li>
			<li>Tag Libraries: ${grailsApplication.tagLibClasses.size()}</li>
	     </ul>
	</section>
	
	<section id="resources">
	     <h1><g:message code="default.systeminfo.plugins"/></h1>
	     <ul>
	         <g:set var="pluginManager"
	                value="${applicationContext.getBean('pluginManager')}"></g:set>
	
	         <g:each var="plugin" in="${pluginManager.allPlugins}">
	             <li>${plugin.name} - ${plugin.version}</li>
	         </g:each>
	
	     </ul>
	</section>

</body>

</html>
