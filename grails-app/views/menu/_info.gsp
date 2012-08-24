<ul class="nav">
	<li class="dropdown">
		<a class="dropdown-toggle" href="#"><g:message code="default.info.title"/> <b class="caret"></b></a>
		<ul class="dropdown-menu">
		
			<%-- Note: Links to pages without controller are redirected in conf/UrlMappings.groovy --%>
			<li class=""><a href="${createLink(uri: '/about')}"><g:message code="default.about.button"/></a></li>
			<li class=""><a href="http://wordpress.com/signup/"><i><g:message code="default.blog.button"/></i></a></li>
			<li class=""><a href="${createLink(uri: '/contact')}"><g:message code="default.contact.button"/></a></li>
			
			<li class="divider"></li>
			<g:if env="development">
			<li class=""><a href="${createLink(uri: '/dbconsole')}"><g:message code="default.dbconsole.button"/></a></li>
			</g:if>
			<li class=""><a href="${createLink(uri: '/systeminfo')}"><g:message code="default.systeminfo.button"/></a></li>
			
<%--			<li class="divider"></li>--%>
<%--			<li class=""><a href="${createLink(uri: '/imprint')}">Imprint</a></li>--%>
<%--			<li class=""><a href="${createLink(uri: '/terms')}"><i>Terms of Use</i></a></li>--%>
		</ul>
	</li>
</ul>
