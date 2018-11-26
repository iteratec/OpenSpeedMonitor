<%@ page defaultCodec="none" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.springframework.web.util.HtmlUtils" %>
<html>
<head>
    <meta name="layout" content="layoutOsm" />
    <title><g:message code="de.iteratec.osm.ui.setupwizards.infra.title" default="Setup infrastructure"></g:message></title>

    <asset:stylesheet src="script/scriptManifest.css"/>
    <asset:stylesheet src="infrastructureSetup/infrastructureSetup.css"/>

    <g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
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

<div class="card">
    <h1 id="headline"><g:message code="landing.headline" default="Welcome" /></h1>
    <form action="/infrastructureSetup/save" data-toggle="validator" role="form" id="setServersForm"
          data-feedback="{success: 'fa-check', error: 'fa-times'}">
        <div class="tab-content">
            <div class="tab-pane active" id="setServers">
                <g:render template="/infrastructureSetup/selectServer"/>
            </div>
        </div>
    </form>
</div>

<content tag="include.bottom">
    <asset:javascript src="/infrastructureSetup/infrastructureSetupWizard.js"/>
</content>
</body>
</html>
