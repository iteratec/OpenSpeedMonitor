<%@ page contentType="text/html;charset=UTF-8" %>

<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="default.product.title"/></title>
    <asset:stylesheet src="landing/index"/>
</head>

<body>

<img src="${resource(dir: 'images', file: 'OpenSpeedMonitor_dark.svg')}"
     alt="${meta(name: 'app.name')}" style="margin: 20px auto; width: 300px; display: block;"/>
<div class="card jumbotron">
    <h1><g:message code="landing.headline" default="Welcome" /></h1>
    <p><g:message code="landing.headlineText"
                  default="OpenSpeedMonitor - Open Source Web Performance Monitoring." /></p>
    <g:if test="${flash.success}">
        <div class="alert alert-success">
            <g:message code="de.iteratec.osm.ui.setupwizards.infra.success" default="success" args="${flash.success}"/>
        </div>
    </g:if>
    <g:if test="${!isSetupFinished}">
        <a href="/infrastructureSetup" class="btn btn-primary" id="setup-wpt-server-button">
            <g:message code="de.iteratec.osm.ui.setupwizards.infra.continueButton" default="continueButton" />
        </a>
    </g:if>
</div>

<div class="row card-well">
    <div class="col-md-4">
        <div class="card intro-card">
            <h2><g:message code="de.iteratec.isr.measurementresults" default="Results"/> <i class="fa fa-tachometer"></i></h2>
            <img src="${resource(dir: 'images', file: 'results.png')}"
                 alt="Page Aggregation Chart" class="intro-img img-rounded"/>
            <p class="intro-text">
                <g:message code="landing.results.text"
                           default="Review and analyze the results of your measurements. Show {0} by page, the {1} or dive into the {2}."
                           encodeAs="raw" args="[
                        link(controller: 'pageAggregation', action: 'show') { message(code:'landing.results.link.pageAggregation', default:'aggregated results')},
                        link(controller: 'eventResultDashboard', action: 'showAll') { message(code:'landing.results.link.timeSeries', default:'development over time')},
                        (grailsApplication.config.getProperty('grails.de.iteratec.osm.detailAnalysis.enablePersistenceOfDetailAnalysisData') == 'true' ?
                                link(controller: 'detailAnalysis', action: 'show') { message(code:'landing.results.link.detailAnalysis', default:'request details')}
                                : message(code:'landing.results.link.detailAnalysis', default:'request details'))
                ]"/>
            </p>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card intro-card">
            <h2><g:message code="customerSatisfation.label" default="Customer Satisfaction"/> <i class="fa fa-users"></i></h2>
            <img src="${resource(dir: 'images', file: 'csi.png')}"
                 alt="CSI Graph" class="intro-img img-rounded"/>
            <div class="intro-text">
                <p>
                    <g:message code="landing.csi.text"
                       default="Show how satisfied customers would be with the performance of your web application. Compare the {0} (CSI) of your application with others."
                       encodeAs="raw" args="[
                            link(controller: 'csiDashboard', action: 'showAll') { message(code:'landing.csi.linkText', default:'Customer Satisfaction Index')}
                        ]"/>
                </p>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card intro-card">
            <h2><g:message code="de.iteratec.isr.managementDashboard" default="Measurement"/> <i class="fa fa-tasks"></i></h2>
            <img src="${resource(dir: 'images', file: 'jobs.png')}"
                 alt="Job List" class="intro-img img-rounded"/>
            <div class="intro-text">
                <p>
                    <g:message code="landing.measurement.text"
                               default="Set up and control {0}. Configure what, how and when you want to measure your web application."
                               encodeAs="raw" args="[
                            link(controller: 'job', action: 'index') { message(code:'landing.measurement.linkText', default:'your measurements')}
                    ]"/>
                </p>
                    <a <g:if test="${isSetupFinished}">href="/measurementSetup/create"</g:if> <g:if test="${isSetupFinished}">title="<g:message code="de.iteratec.osm.ui.setupwizards.infra.continueInfo" default="continueButton" />"</g:if> class="btn btn-primary <g:if test="${!isSetupFinished}">submit disabled</g:if>">
                        <g:message code="landing.measurement.createNew" default="Create New Measurement" /></a>
            </div>
        </div>
    </div>
</div>
</body>
