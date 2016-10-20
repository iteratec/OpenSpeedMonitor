<g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
<ul class="nav pull-right">
	<li class="dropdown dropdown-btn">
		<sec:ifNotLoggedIn>



			<ul class="nav">
				<li class="dropdown">
					<a class="dropdown-toggle" data-toggle="dropdown" href="#"><g:message code="security.signin.label" default="Log in" locale="${lang}"/> <b class="caret"></b></a>
					<ul class="dropdown-menu">

						%{--measurement --------------------------------------------------------}%

						<li class="controller">
							<g:link controller="login" action="auth"><i class="fa fa-sign-in" aria-hidden="true"></i> <g:message
									code="security.signin.label" default="Log in"/></g:link>
						</li>
						<g:if test="${grailsApplication.config.getProperty('grails.mail.disabled')?.toLowerCase() == "false"}">
							<li class="controller">
								<g:link controller="register" action="register"><i class="fa fa-book" aria-hidden="true"></i> <g:message
										code="security.register.label" default="Register"/></g:link>
							</li>
						</g:if>


					</ul>
				</li>
			</ul>

		</sec:ifNotLoggedIn>
		<sec:ifLoggedIn>
			<ul class="nav">
				<li class="dropdown">
					<a class="dropdown-toggle" data-toggle="dropdown" href="#"><sec:username/> <b class="caret"></b></a>
					<ul class="dropdown-menu">


						%{--Logout --------------------------------------------------------}%

						<li class="controller">
							<g:link controller="logout" action="index"><i class="fa fa-sign-out" aria-hidden="true"></i>
								<g:message code="security.signout.label" default="Log out"/></g:link>
						</li>

						<sec:ifAnyGranted roles="ROLE_SUPER_ADMIN,ROLE_ADMIN">
							%{--csi --------------------------------------------------------}%
							<li class="divider"></li>

							<li class="controller">
								<g:link controller="user" action="index"><i class="fa fa-user" aria-hidden="true"></i> <g:message
										code="user.label" default="User"/></g:link>
							</li>

							<li class="controller">
								<g:link controller="role" action="index"><i class="fa fa-trophy" aria-hidden="true"></i> <g:message
										code="role.label" default="Role"/></g:link>
							</li>
							<li class="controller">
								<g:link controller="registrationCode" action="index"><i class="fa fa-key" aria-hidden="true"></i> <g:message
										code="registrationCode.label" default="RegistrationCode"/></g:link>
							</li>
						</sec:ifAnyGranted>

					</ul>
				</li>
			</ul>
		</sec:ifLoggedIn>
	</li>
</ul>

<noscript>
<ul class="nav pull-right">
	<li>
		<g:link controller="user" action="show"><g:message code="default.user.unknown.label"/></g:link>
	</li>
</ul>
</noscript>

