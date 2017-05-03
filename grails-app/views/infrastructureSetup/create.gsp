<%@ page defaultCodec="none" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.springframework.web.util.HtmlUtils" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm" />
    <title><g:message code="de.iteratec.osm.ui.setupwizards.infra.title" default="Setup infrastructure"></g:message></title>

    <asset:stylesheet src="script/scriptManifest.css"/>
    <asset:stylesheet src="setupWizard/setupWizard.css"/>
</head>
<body>
<h1></h1>

%{-- error message --}%
<g:if test="${flash.error}">
    <div class="alert alert-danger" style="display: block">
        <g:message code="de.iteratec.osm.ui.setupwizards.infra.errorInfo" default="errorInfo"></g:message>
    </div>
</g:if>

%{-- title --}%
<h1><g:message code="de.iteratec.osm.ui.setupwizards.infra.title" default="Setup infrastructure"/></h1>
<p>
    <g:message code="de.iteratec.osm.ui.setupwizards.infra.description" default="description"
               encodeAs="raw" args="${[
            link(controller: 'job', action: 'list') { message(code:'de.iteratec.isj.jobs', default:'jobs')},
    ]}"/>
</p>

<div class="card">
    %{-- bar --}%
    <div class="progress" id="setupWizardProgressBarContainer">
        <div class="progress-bar" id="setupWizardProgressBar" role="progressbar" style="width:100%"></div>
    </div>
    <div class="col-sm-12">
        <div class="wizardStep active wasActive">
            <a data-toggle="tab" id="setWPTKeyTab">
                <i class="fa fa-server fa-2x" aria-hidden="true"></i>
            </a>
        </div>
    </div>

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