<%@ page defaultCodec="none" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.springframework.web.util.HtmlUtils" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm" />
    <title><g:message code="de.iteratec.osm.ui.setupwizards.infra.title" default="Setup infrastructure"></g:message></title>

    <asset:stylesheet src="script/scriptManifest.css"/>
    <asset:stylesheet src="infrastructureSetup/infrastructureSetup.css"/>

    <g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
    <footer class="footer" style="margin-left: -50px;margin-bottom: 15px;">
        <div class="container-fluid">
            <p>
                <g:if test="${lang.toString().equals('de')}">
                    Entwickelt von <a href="http://www.iteratec.de/" target="_blank">iteratec GmbH</a> (Niederlassung Hamburg).
                </g:if>
                <g:else>
                    Developed by <a href="http://www.iteratec.de/" target="_blank">iteratec GmbH</a> (office Hamburg).
                </g:else>
                Designed and built with <a href="http://twitter.github.com/bootstrap/" target="_blank">Bootstrap</a>.
            </p>
        </div>
    </footer>
</head>
<body>

%{-- title --}%
<div id="logo">
    <img src="${resource(dir: 'images', file: 'OpenSpeedMonitor_dark.svg')}"
          alt="${meta(name: 'app.name')}" style="margin: 20px auto; width: 300px; display: block;"/>
</div>

%{-- error message --}%
<g:if test="${flash.error}">
    <div class="alert alert-danger" style="display: block">
        <g:message code="de.iteratec.osm.ui.setupwizards.infra.errorInfo" default="error"></g:message>
    </div>
</g:if>

<div class="card"  id="card">
    %{-- bar --}%
    <h1 style="font-size: 42px" id="headline"><g:message code="landing.headline" default="Welcome" /></h1>

    %{-- tab-content --}%
    <g:form action="save" id="setServersForm">
        <div class="tab-content">
            <div class="tab-pane active" id="setServers">
                <g:render template="/infrastructureSetup/selectServer"/>
            </div>
        </div>
    </g:form>
</div>

<content tag="include.bottom">
    <asset:javascript src="/infrastructureSetup/infrastructureSetupWizard.js"/>
</content>
</body>
</html>