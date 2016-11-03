<g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
<footer class="footer">
	<div class="container-fluid">
		<div class="row">
			<div class="col-md-2">
				<h4><g:message code="default.product.title" locale="${lang}"/></h4>
				<ul class="list-unstyled">
					<li>
			    		<i class="fa fa-home"></i>
						<a href="${createLink(uri: '/')}"><g:message code="default.home.label" locale="${lang}"/></a>
					</li>					

				</ul>
			</div>
			<div class="col-md-10">
				<h4><g:message code="default.info.title" locale="${lang}"/></h4>
                <ul class="list-unstyled">
                    <li>
                        <i class="fa fa-info-circle"></i>
                        <a href="${createLink(uri: '/about')}">
                            <g:message code="de.iteratec.osm.about.label" locale="${lang}"/>
                        </a>
                    </li>
                    <li>
                        <i class="fa fa-envelope-o"></i>
                        <a href="mailto:wpt@iteratec.de">
                            <g:message code="de.iteratec.osm.contact.label" locale="${lang}"/>
                        </a>
                    </li>
                </ul>
				<p>
			    <g:if test="${lang.toString().equals('de')}">
            Entwickelt von <a href="http://www.iteratec.de/" target="_blank">iteratec GmbH</a> (Niederlassung Hamburg).
			    </g:if>
			    <g:else>
            Developed by <a href="http://www.iteratec.de/" target="_blank">iteratec GmbH</a> (office Hamburg).
			    </g:else>
				</p>
				<p> 
				Designed and built with Twitter's <a href="http://twitter.github.com/bootstrap/" target="_blank">Bootstrap</a>. 
				Twitter bootstrap code licensed under the <a href="http://www.apache.org/licenses/LICENSE-2.0" target="_blank">Apache License v2.0</a>.
				</p>
			</div>
		</div>
	
		<p class="pull-right"><a href="#"><g:message code="default.back.top.label" locale="${lang}"/></a></p>
	</div>
</footer>