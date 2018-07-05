<g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
	<li class="dropdown">
		
		<a class="dropdown-toggle" href="#">
            <i class="fas fa-info-circle"></i>
            <g:message code="default.info.label" locale="${lang}"/>
            <span class="caret"></span>
		</a>

        <ul class="dropdown-menu">

            <li>
                <a href="https://github.com/IteraSpeed/OpenSpeedMonitor/releases" target="_blank">
                    <i class="fab fa-github"></i>
                    <g:message code="de.iteratec.osm.releasenotes.manual.label" default="Release notes"/>
                </a>
            </li>
            <li>
                <a href="${createLink(uri: '/rest/man')}" target="_blank">
                    <i class="fas fa-code"></i>
                    <g:message code="de.iteratec.osm.api.manual.label" locale="${lang}"/>
                </a>
            </li>
            <li>
                <a href="${createLink(uri: '/about')}" target="_blank">
                    <i class="fas fa-info-circle"></i>
                    <g:message code="de.iteratec.osm.about.label" locale="${lang}"/>
                </a>
            </li>
            <li>
                <a href="mailto:osm@iteratec.de">
                    <i class="fas fa-envelope"></i>
                    <g:message code="de.iteratec.osm.contact.label" locale="${lang}"/>
                </a>
            </li>

		</ul>
	</li>
