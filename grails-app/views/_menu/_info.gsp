<g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
<ul class="nav pull-right">
	<li class="dropdown dropdown-btn">
		
		<a class="dropdown-toggle" data-toggle="dropdown" href="#">
    		<i class="fa fa-info-circle"></i>
			<g:message code="default.info.label" locale="${lang}"/> <b class="caret"></b>
		</a>

<ul class="dropdown-menu">

		<%-- Note: Links to pages without controller are redirected in conf/UrlMappings.groovy --%>
        <li class="dropdown-submenu">
            <a tabindex="-1" href="#">
				<i class="fa fa-info-circle"></i>
                <g:message code="de.iteratec.osm.releasenotes.manual.label" default="Release notes"/>
            </a>
            <ul class="dropdown-menu scrollable">
                <li class="controller">
                    <g:message
                            code="de.iteratec.osm.releasenotes.temporary-explanation"
                            default="Release notes coming soon..."/>
                </li>
            </ul>
        </li>
		<li class="">
			<a href="${createLink(uri: '/rest/man')}" target="_blank">
				<i class="fa fa-info-circle"></i>
				<g:message code="de.iteratec.osm.api.manual.label" locale="${lang}"/>
			</a>
		</li>
		<li class="">
			<a href="${createLink(uri: '/about')}" target="_blank">
				<i class="fa fa-info-circle"></i>
				<g:message code="de.iteratec.osm.about.label" locale="${lang}"/>
			</a>
		</li>

		<li class="">
			<a href="mailto:wpt@iteratec.de">
				<i class="fa fa-envelope-o"></i>
				<g:message code="de.iteratec.osm.contact.label" locale="${lang}"/>
			</a>
		</li>
	<%--			<li class="divider"></li>--%>
<%--			<li class=""><a href="${createLink(uri: '/imprint')}">Imprint</a></li>--%>
<%--			<li class=""><a href="${createLink(uri: '/terms')}"><i>Terms of Use</i></a></li>--%>
		</ul>
	</li>
</ul>
