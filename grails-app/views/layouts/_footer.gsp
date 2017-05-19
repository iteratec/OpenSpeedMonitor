<g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
<footer class="footer">
	<div class="container-fluid">
		<g:if test="${!disableBackToTop}">
			<p class="pull-right"><a href="#"><g:message code="default.back.top.label" locale="${lang}"/></a></p>
		</g:if>

		<p>
			<g:if test="${lang.toString().equals('de')}">
				Entwickelt von <a href="http://www.iteratec.de/" target="_blank">iteratec GmbH</a> (Niederlassung Hamburg).
			</g:if>
			<g:else>
				Developed by <a href="http://www.iteratec.de/" target="_blank">iteratec GmbH</a> (office Hamburg).
			</g:else>
			Designed and built with <a href="http://twitter.github.com/bootstrap/" target="_blank">Bootstrap</a>.
		</p>
	</div>
</footer>
