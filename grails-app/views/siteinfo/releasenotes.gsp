<html>

<head>
    <title><g:message code="de.iteratec.osm.releasenotes.title"/></title>
    <meta name="layout" content="kickstart_osm" />
</head>

<body>
<h1><g:message code="de.iteratec.osm.releasenotes.label" default="Releasenotes" locale="${lang}"/> </h1>

<section id="intro">
	<p class="lead">
		<g:if test="${lang.toString().equals('de')}">
			Diese Seite zeigt die Releasenotes zu ${meta(name: 'app.name')}'s letzten Releases.
		</g:if>
		<g:else>
			This page shows the release notes of ${meta(name: 'app.name')}'s latest releases.
		</g:else>
	</p>
</section>

<section id="additional">
	<% def bundle=java.util.ResourceBundle.getBundle('grails-app/i18n/releasenotes', new Locale("de"))
	System.out.println(bundle.getString("release.1.0.1"))
	%>
	<div class="tabbable tabs-left">
		<ul class="nav nav-tabs" data-tabs="tabs">
			<g:each status="num" in="${bundle.getKeys()}" var="note">
				<li class="${num==0?'active':''}"><a href="#${note.minus('release.').replaceAll('\\.', '-')}" data-toggle="tab">Release ${note.minus('release.').replaceAll('-', '.')}</a></li>
			</g:each>
		</ul>
		<div class="tab-content">
			<g:each status="num" in="${bundle.getKeys()}" var="note">
				<div class="${num==0?'tab-pane fade in active':'tab-pane fade in'}" id="${note.minus('release.').replaceAll('\\.', '-')}">
					<p><iteratec:releaseNotes text="${bundle.getString(note)}" /></p>
				</div>
			</g:each>
		</div>
	</div>
</section>

</body>

</html>
