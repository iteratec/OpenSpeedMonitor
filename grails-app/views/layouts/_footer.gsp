<footer class="footer">
	<div class="container">
		<div class="row">
			<div class="span2">
				<h4><g:message code="default.product.title"/></h4>
				<ul class="unstyled">
					<li>
			    		<i class="icon-home"></i>
						<a href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
					</li>					

				</ul>
			</div>
			<div class="span10">
				<h4><g:message code="default.info.title"/></h4>
                <ul class="unstyled">
                    <li>
                        <i class="icon-info-sign"></i>
                        <a href="${createLink(uri: '/about')}">
                            <g:message code="de.iteratec.osm.about.label"/>
                        </a>
                    </li>
                    <li>
                        <i class="icon-envelope"></i>
                        <a href="mailto:wpt@iteratec.de">
                            <g:message code="de.iteratec.osm.contact.label"/>
                        </a>
                    </li>
                </ul>
				<p>
				Developed by <a href="http://www.iteratec.de/" target="_blank">iteratec GmbH</a> (office Hamburg).
				</p>
				<p> 
				Designed and built with Twitter's <a href="http://twitter.github.com/bootstrap/" target="_blank">Bootstrap</a>. 
				Twitter bootstrap code licensed under the <a href="http://www.apache.org/licenses/LICENSE-2.0" target="_blank">Apache License v2.0</a>.
				</p>
			</div>
		</div>
	
		<p class="pull-right"><a href="#"><g:message code="default.back.top.label"/></a></p>
	</div>
</footer>