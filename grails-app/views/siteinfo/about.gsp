<g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
<html>

<head>
	<title><g:message code="de.iteratec.osm.about.label" default="about" locale="${lang}"/> ${meta(name:'info.app.name')}</title>
	<meta name="layout" content="kickstart_osm" />
	<style>

	</style>
</head>

<body>
	<h1><g:message code="de.iteratec.osm.about.label" default="about" locale="${lang}"/> ${meta(name:'info.app.name')} <small>version ${meta(name:'info.app.version')}</small></h1>
<div class="row">
	<div class="col-md-6">
		<section class="card" id="intro">
			<p class="lead">
				<g:if test="${lang.toString().equals('de')}">
					${meta(name: 'info.app.name')} automatisiert Web-Performance-Messungen basierend auf <a title="webpagetest" href="http://webpagetest.org/">webpagetest</a>. Außerdem verarbeitet, aggregiert und visualisiert die Ergebnisse.
		  Seine technische Basis ist <a title="grails" href="http://grails.org/">Grails</a>, ein JVM-basiertes Web-Framework welches die Programmiersprache <a title="groovy programming language" href="http://groovy.codehaus.org/">Groovy</a> nutzt.
		  Its technical base is grails, a jvm based web framework leveraged by the programming language groovy.
				</g:if>
				<g:else>
					${meta(name: 'info.app.name')} automates web performance measurements based on <a title="webpagetest" href="http://webpagetest.org/">webpagetest</a>. It also processes, aggregates and visualizes the results.
		  Its technical base is <a title="grails" href="http://grails.org/">grails</a>, a jvm based web framework leveraging the programming language <a title="groovy programming language" href="http://groovy.codehaus.org/">groovy</a>.
				</g:else>
			</p>
		</section>
	</div>
	<div class="col-md-6">
		<section  class="card" id="additional">
			<h2><g:message code="de.iteratec.osm.license.label" default="about" locale="${lang}"/></h2>
			<p>
				<g:if test="${lang.toString().equals('de')}">
					Lizenziert nach den Bedingungen der Apache-Lizenz, Version 2.0 (die "Lizenz");<br>
					Sie dürfen diese System nur in Übereinstimmung mit der Lizenz verwenden.<br>
					Sie können eine Kopie der Lizenz erhalten unter:<br><br>

					&emsp;<a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a><br><br>

					Sofern nicht durch geltendes Recht gefordert oder schriftlich vereinbart, wird<br>
					Software,welche der Lizenz unterliegt, ohne jegliche Mängelgewähr, Garantie<br>
					oder Zusicherung vertrieben - weder ausdrücklich noch stillschweigend.<br>
					Spezifische sprachenrelevante Rechte und Einschränkungen im Geltungsbereich<br>
					der Lizenz finden Sie in dieser.
				</g:if>
				<g:else>
					Licensed under the Apache License, Version 2.0 (the "License");<br>
					you may not use this system except in compliance with the License.<br>
					You may obtain a copy of the License at<br><br>
					&emsp;<a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a><br><br>
					Unless required by applicable law or agreed to in writing, software<br>
					distributed under the License is distributed on an "AS IS" BASIS,<br>
					WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.<br>
					See the License for the specific language governing permissions and<br>
					limitations under the License.
				</g:else>
			</p>
			<h2><g:message code="de.iteratec.osm.oslUsed.label" default="about" locale="${lang}"/></h2>
			<p>
				<g:if test="${lang.toString().equals('de')}">
					Dieses Projekt enthält zahlreiche Bibliotheken und Plugins, welche von anderen Autoren stammen.<br>
					Eine vollständige Übersicht enthält die <a href="https://github.com/IteraSpeed/OpenSpeedMonitor/blob/master/NOTICE">NOTICE</a>-Datei.<br><br>
					Wir bedanken uns bei allen Beitragenden.
				</g:if>
				<g:else>
					This project includes several libraries and plugins from other authors.<br>
					For a full overview, see the <a href="https://github.com/IteraSpeed/OpenSpeedMonitor/blob/master/NOTICE">NOTICE</a> file.<br><br>
					Thanks to all contributors.
				</g:else>
			</p>


		</section>
	</div>
</div>

</body>

</html>
