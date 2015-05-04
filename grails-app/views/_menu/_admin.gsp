<g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
<ul class="nav pull-right">
	<li class="dropdown">
		<a class="dropdown-toggle" data-toggle="dropdown" href="#">
    		<i class="icon-wrench"></i>
			<g:message code="default.admin.label" locale="${lang}"/><b class="caret"></b>
		</a>
		<ul class="dropdown-menu">
			<li class="">
				%{--All modal dialogs are located in /grails-app/views/_modals/--}%
				<a href="#modal-p13n" data-toggle="modal" >
					<i class="icon-star-empty"></i>
					<g:message code="de.iteratec.osm.p13n.cookiebased.label" locale="${lang}"/>
				</a>
			</li>
			<sec:ifAnyGranted roles="ROLE_SUPER_ADMIN,ROLE_ADMIN">
				<li class="">
					<a href="${createLink(controller: 'batchActivity')}">
						<i class="icon-tasks"></i>
						<g:message code="de.iteratec.osm.batch.batchactivity.list.heading" locale="${lang}"/>
					</a>
				</li>
				<li class="">
					<a href="${createLink(uri: '/systeminfo')}">
						<i class="icon-info-sign"></i>
						<g:message code="default.systeminfo.label" locale="${lang}"/>
					</a>
				</li>
				<li class="dropdown-submenu">
					<a tabindex="-1" href="#">
						<i class="icon-sitemap"></i>
						All Controller
					</a>
					<ul class="dropdown-menu scrollable">
						<g:each var="c" in="${grailsApplication.controllerClasses.sort { it.logicalPropertyName } }">
              <g:if test="${c.logicalPropertyName != 'home'}">
	              <g:if test="${['AdminManageController', 'ApiKeyController', 'BrowserController', 'BrowserAliasController', 'CsTargetGraphController', 'CsTargetValueController', 'EventController', 'GraphitePathController', 'GraphiteServerController', 'JobGroupController', 'LocationController', 'MeasuredEventController', 'OsmConfigurationController', 'PageController', 'WebPageTestServerController'].contains(c?.fullName?.substring(c?.fullName?.lastIndexOf('.')+1))}">
									<li class="controller"><g:link controller="${c.logicalPropertyName}">${c?.fullName?.substring(c?.fullName?.lastIndexOf('.')+1)}</g:link></li>
	              </g:if>
              </g:if>
						</g:each>
					</ul>
				</li>
			</sec:ifAnyGranted>
			<g:if env="development">
				<li class="">
					<a href="${createLink(uri: '/admin/dbconsole')}" target="_blank">
						<i class="icon-dashboard"></i>
						<g:message code="de.iteratec.osm.persistence.dbconsole.label" locale="${lang}"/>
					</a>
				</li>
			</g:if>
		</ul>
	</li>
</ul>
