<html>

<head>
    <title><g:message code="de.iteratec.osm.systeminfo.title"/></title>
    <meta name="layout" content="layoutOsm" />
</head>

<body>

	<section id="application">
	     <h1><g:message code="de.iteratec.osm.systeminfo.state"/></h1>
	     <ul>
			<li>App version: <g:meta name="app.version"/></li>
			<li>Grails version: <g:meta name="app.grails.version"/></li>
			<li>Groovy version: ${groovy.lang.GroovySystem.getVersion()}</li>
			<li>JVM version: ${System.getProperty('java.version')}</li>
			<li>Reloading active: ${grails.util.Environment.reloadingAgentEnabled}</li>
			<li>Controllers: ${grailsApplication.controllerClasses.size()}</li>
			<li>Domains: ${grailsApplication.domainClasses.size()}</li>
			<li>Services: ${grailsApplication.serviceClasses.size()}</li>
			<li>Tag Libraries: ${grailsApplication.tagLibClasses.size()}</li>
	     </ul>
	</section>
	
	<section id="resources">
	     <h1><g:message code="de.iteratec.osm.systeminfo.plugins"/></h1>
	     <ul>
	         <g:set var="pluginManager"
	                value="${applicationContext.getBean('pluginManager')}"></g:set>
	
	         <g:each var="plugin" in="${pluginManager.allPlugins.sort { it.name }}">
	             <li>${plugin.name} - ${plugin.version}</li>
	         </g:each>
	
	     </ul>
	</section>

</body>

</html>
