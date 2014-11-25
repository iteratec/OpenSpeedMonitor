<html>

<head>
	<title>Next Steps</title>
	<meta name="layout" content="kickstart" />
</head>

<body>

	<h1>Next Steps</h1>
	
	<section id="intro" class="first">
		<h3>Authentication via Spring Security</h3>
		<p>
			If you want to provide an authentication system I recommend 
			<a href="http://grails.org/plugin/spring-security-core">Spring Security</a>. 
			Kickstart comes with views for the login (/login/auth.gsp) and auth denied (/login/denied.gsp) views. 
		</p>
		<ol>
			<li>Install <a href="http://grails.org/plugin/spring-security-core">spring-security-core</a> in BuildConfig.groovy, e.g.: <pre>compile ":spring-security-core:1.2.7.3"</pre> </li>
			<li>Clean and compile your app to load the plugin</li>
			<li>Execute the <a href="http://grails-plugins.github.com/grails-spring-security-core/docs/manual/ref/Scripts/s2-quickstart.html">s2-quickstart script</a> to generate the required domain classes <pre>grails s2-quickstart com.yourapp User Role</pre></li>
			<ul>
				<li>BUT replace "com.yourapp" with your package structure (e.g., "com.${meta(name:'app.name')}.security")</li>
				<li>Do not overwrite the views auth.gsp and denied.gsp (or copy them from the Kickstart plugin)</li>
			</ul>
			<li>Set security configuration in your Config.groovy (see <a href="http://grails-plugins.github.com/grails-spring-security-core/docs/manual/guide/5%20Configuring%20Request%20Mappings%20to%20Secure%20URLs.html#5.2%20Simple%20Map%20in%20Config.groovy">here</a>)</li>
		</ol>
	</section>

</body>

</html>
	