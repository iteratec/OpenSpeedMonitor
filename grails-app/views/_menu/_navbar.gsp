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
				<img class="logo" src="${resource(dir:'images',file:'OpenSpeedMonitor-onblack-monitorWritingLight_250pxHeight.gif')}" alt="${meta(name:'app.name')}" />
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
					<g:render template="/_menu/user"/><!-- NOTE: the renderDialog for the "Register" modal dialog MUST be placed outside the NavBar (at least for Bootstrap 2.1.1): see bottom of main.gsp -->
					<sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
						<g:render template="/_menu/admin"/>
					</sec:ifAllGranted>
	       			<ul class="nav">
						<li class="dropdown">
							<a class="dropdown-toggle" data-toggle="dropdown" href="#"><g:message code="navbar.browse" default="Browse" /> <b class="caret"></b></a>
							<ul class="dropdown-menu">
			                    
			                    <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
				                    <g:each var="c" in="${grailsApplication.controllerClasses.sort { it.fullName } }">
				                    <li class="controller">
				                    	<g:link controller="${c.logicalPropertyName}">
											<g:if test="${c.fullName.contains('HomeController')}">
								    		<i class="icon-home"></i>
											</g:if>
											<g:elseif test="${c.fullName.contains('DemoPageController')}">
								    		<i class="icon-beaker"></i>
											</g:elseif>
				                    		${c.fullName.substring(c.fullName.lastIndexOf('.')+1)}
				                    	</g:link>
				                    </li>
				                    </g:each>
			                    </sec:ifAllGranted>
			                    <sec:ifNotGranted roles="ROLE_SUPER_ADMIN">
									<li class="controller">
										<g:link controller="eventResultDashboard" action="showAll" title="${g.message([code:'eventResultDashboard.description'])}">
											<g:message code="eventResultDashboard.label" />
										</g:link>
									</li>
									<li class="controller">
										<g:link controller="csiDashboard" action="showAll" title="${g.message([code:'csiDashboard.description'])}">
											<g:message code="csiDashboard.label" />
										</g:link>
									</li>
										
								</sec:ifNotGranted>
			                    
			                    
							</ul>
						</li>
					</ul>
	  			</div>

			</div>
			
		</div>
	</div>
</div>
