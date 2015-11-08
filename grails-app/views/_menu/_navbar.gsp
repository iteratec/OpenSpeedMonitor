<g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
<div id="Navbar" class="navbar navbar-fixed-top navbar-inverse">
	<div class="navbar-inner">
		<div class="container">
			<!-- .btn-navbar is used as the toggle for collapsed navbar content -->
			<a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
				<span class="icon-bar"></span>
            	<span class="icon-bar"></span>
            	<span class="icon-bar"></span>
			</a>

			<a class="brand" href="${createLink(uri: '/')}">
        <img class="logo" src="${resource(dir:'images',file:'OpenSpeedMonitor-onblack-monitorWritingLight_39pxHeight.gif')}" alt="${meta(name:'app.name')}" />
			</a>

       		<div class="nav-collapse">

	  			<div class="pull-left">
					<%--Left-side entries--%>
	  			</div>

	  			<div class="pull-right">
					<%--Right-side entries--%>
					<%--NOTE: the following menus are in reverse order due to "pull-right" alignment (i.e., right to left)--%>
					<g:render template="/_menu/language"/>														
					<g:render template="/_menu/info"/>														
					<g:render template="/_menu/admin"/>
					<g:render template="/_menu/user"/><!-- NOTE: the renderDialog for the "Register" modal dialog MUST be placed outside the NavBar (at least for Bootstrap 2.1.1): see bottom of main.gsp -->
					<ul class="nav">
						<li class="dropdown">
							<a class="dropdown-toggle" data-toggle="dropdown" href="#"><g:message code="navbar.browse" default="Browse" locale="${lang}"/> <b class="caret"></b></a>
							<ul class="dropdown-menu">

								<li class="controller">
									<g:link controller="job" action="list" title="${g.message([code:'de.iteratec.isr.managementDashboard', locale:lang])}">
										<i class="fa fa-calendar"></i>
										<g:message code="de.iteratec.isr.managementDashboard" locale="${lang}"/>
									</g:link>
								</li>
								<li class="controller">
									<g:link controller="eventResultDashboard" action="showAll" title="${g.message([code:'eventResultDashboard.description', locale:lang])}">
										<i class="fa fa-signal"></i>
										<g:message code="eventResultDashboard.label" locale="${lang}"/>
									</g:link>
								</li>
								<li class="controller">
									<g:link controller="csiDashboard" action="showAll" title="${g.message([code:'csiDashboard.description', locale:lang])}">
										<i class="fa fa-signal"></i>
										<g:message code="csiDashboard.label" locale="${lang}"/>
									</g:link>
								</li>
			                    
							</ul>
						</li>
					</ul>
	  			</div>

			</div>
			
		</div>
	</div>
</div>
