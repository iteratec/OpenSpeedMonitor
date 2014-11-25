<ul class="nav pull-right">
	<li class="dropdown dropdown-btn">
		
		<a class="dropdown-toggle" data-toggle="dropdown" href="#">
    		<i class="icon-info-sign"></i>
			<g:message code="default.info.label"/> <b class="caret"></b>
		</a>
		
		<ul class="dropdown-menu">
		
			<%-- Note: Links to pages without controller are redirected in conf/UrlMappings.groovy --%>
			<li class="">
				<a href="${createLink(uri: '/about')}" target="_blank">
					<i class="icon-info-sign"></i>
					<g:message code="de.iteratec.osm.about.label"/>
				</a>
			</li>
			<li class="">
				<a href="mailto:wpt@iteratec.de">
					<i class="icon-envelope"></i>
					<g:message code="de.iteratec.osm.contact.label"/>
				</a>
			</li>
						
<%--			<li class="divider"></li>--%>
<%--			<li class=""><a href="${createLink(uri: '/imprint')}">Imprint</a></li>--%>
<%--			<li class=""><a href="${createLink(uri: '/terms')}"><i>Terms of Use</i></a></li>--%>
		</ul>
	</li>
</ul>
