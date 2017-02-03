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
</div>

<div class="row card-well">
    <div class="col-md-4">
        <div class="card intro-card">
            <h2><g:message code="de.iteratec.isr.measurementresults" default="Results"/> <i class="fa fa-tachometer"></i></h2>
            <img src="${resource(dir: 'images', file: 'results.png')}"
                 alt="Page Aggregation Chart" class="intro-img img-rounded"/>
            <p class="intro-text">
                Review and analyze the results of your measurements.
                Show <g:link controller="pageAggregation" action="show">aggregated results</g:link> by page,
                the <g:link controller="eventResultDashboard" action="showAll">development over time</g:link>
                or
                <g:if test="${grailsApplication.config.getProperty('grails.de.iteratec.osm.assetRequests.enablePersistenceOfAssetRequests')?.equals("true")}">
                    dive into the <g:link controller="detailAnalysis" action="show">request details</g:link>.
                </g:if>
                <g:else>dive into the request details.</g:else>
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
                    Show how satisfied customers would be with the performance of your web application. Compare the
                    <g:link controller="csiDashboard" action="showAll"><g:message code="landing.csi.linkText" default="Customer Satisfaction Index" /></g:link> (CSI) of your
                    application with others.
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
                        default="Set up and control your measurements. Configure what, how and when you want to measure your web application." />
                    </p>
                <a href="#" class="btn btn-primary">
                    <g:message code="landing.measurement.createNew" default="Create New Measurement" /></a>
            </div>
        </div>
    </div>
</div>
</body>
