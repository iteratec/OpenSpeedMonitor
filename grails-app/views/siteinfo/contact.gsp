<html>

<head>
	<title><g:message code="default.contact.title"/></title>
	<meta name="layout" content="kickstart" />
</head>

<body>

	<section id="intro">
		<p class="lead">
			For ongoing information about ${meta(name:'app.name')}, please read our company 
			<a href="http://wordpress.com/signup/">blog</a>. Also, feel free to
			contact us with service questions, partnership proposals, or media
			inquiries.
		</p>
	</section>

	<section id="address">
		<h1><g:message code="default.contact.address"/></h1>
		<address>
			<strong>${meta(name:'app.name')}, Inc.</strong><br>
			 123 Future Ave<br>
			 San Francisco, CA 94107<br>
			 <abbr title="Phone">P:</abbr> (123) 456-7890
		</address>
		<address>
			<strong>Email</strong><br>
			<a href="mailto:info@${meta(name:'app.name')}.com">info@${meta(name:'app.name')}.com</a>
		</address>
	</section>

</body>

</html>
