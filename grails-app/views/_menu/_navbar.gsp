<g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
<g:if test="${controllerName.equals('eventResultDashboard') || controllerName.equals('tabularResultPresentation') || controllerName.equals('pageAggregation') || controllerName.equals('detailAnalysis')}">
    <g:set var="mainTab" value="results"/>
</g:if>
<g:elseif test="${controllerName.equals('csiDashboard')}"><g:set var="mainTab" value="csi"/></g:elseif>
<g:elseif test="${controllerName.equals('csiConfiguration')}"><g:set var="mainTab" value="csi"/></g:elseif>
<g:elseif test="${controllerName.equals('script')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:elseif test="${controllerName.equals('job')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:elseif test="${controllerName.equals('queueStatus')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:elseif test="${controllerName.equals('jobSchedule')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:elseif test="${controllerName.equals('jobResult')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:elseif test="${controllerName.equals('connectivityProfile')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:else><g:set var="mainTab" value="unknown"/></g:else>

<nav class="navbar navbar-inverse sidebar-fixed">
    <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#main-navbar">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
        </button>
        <g:link absolute="true" class="navbar-brand" uri="/">
            <img class="icon osm-icon-small" src="${resource(dir: 'images', file: 'OpenSpeedMonitor_small.svg')}"
                 alt="${meta(name: 'app.name')}"/>
            <img class="logo" src="${resource(dir: 'images', file: 'OpenSpeedMonitor.svg')}"
                 alt="${meta(name: 'app.name')}"/>
        </g:link>
    </div>
    <div class="collapse navbar-collapse" id="main-navbar">
        <ul class="nav navbar-nav">
            <li class="${controllerName.equals("landing") ? 'active' : ''}">
                <g:link absolute="true" uri="/"><i class="fa fa-home"></i>
                    <g:message code="navbar.home" default="Home"/>
                </g:link>
            </li>
            <li class="dropdown ${mainTab.equals('results') ? 'active open' : ''}">
                <a href="#" class="dropdown-toggle" role="button" aria-haspopup="true" aria-expanded="false">
                    <i class="fa fa-tachometer"></i>
                    <g:message code="de.iteratec.isr.measurementresults" default="Results"/> <span class="caret"></span>
                </a>
                <ul class="dropdown-menu">
                    <li class="${controllerName.equals('eventResultDashboard') ? 'active' : ''}" id="eventResultMainMenu">
                        <g:link controller="eventResultDashboard" action="showAll"
                                title="${message(code:'eventResultDashboard.label', default:'Time Series')}">
                            <i class="fa fa-line-chart"></i>
                                <g:message code="eventResultDashboard.label" default="Time Series"/>
                        </g:link>
                    </li>
                    <li class="${controllerName.equals('pageAggregation') ? 'active' : ''}" id="pageAggregationMainMenu">
                        <g:link controller="pageAggregation" action="show"
                                title="${message(code:'de.iteratec.pageAggregation.title', default:'Page Aggregation')}">
                            <i class="fa fa-bar-chart"></i>
                                <g:message code="de.iteratec.pageAggregation.title" default="Page Aggregation"/>
                        </g:link>
                    </li>
                    <g:if test="${grailsApplication.config.getProperty('grails.de.iteratec.osm.assetRequests.enablePersistenceOfAssetRequests')?.equals("true")}">
                        <li class="${controllerName.equals('detailAnalysis') ? 'active' : ''}" id="detailAnalysisMainMenu">
                            <g:link controller="detailAnalysis" action="show"
                                    title="Detail Analysis">
                                <i class="fa fa-pie-chart"></i>
                                    Detail Analysis
                            </g:link>
                        </li>
                    </g:if>
                    <li class="${controllerName.equals('tabularResultPresentation') ? 'active' : ''}" id="tabularResultMainMenu">
                        <g:link controller="tabularResultPresentation" action="listResults"
                                title="${message(code:'de.iteratec.result.title', default:'Result List')}">
                            <i class="fa fa-th-list"></i>
                                <g:message code="de.iteratec.result.title" default="Result List"/>
                        </g:link>
                    </li>
                </ul>
            </li>
            <li class="dropdown ${mainTab.equals('csi') ? 'active open' : ''}">
                <a href="#" class="dropdown-toggle" role="button" aria-haspopup="true" aria-expanded="false">
                    <i class="fa fa-users"></i>
                    <g:message code="customerSatisfation.label" default="Customer Satisfaction"/><span class="caret"></span>
                </a>
                <ul class="dropdown-menu">
                    <li class="${controllerName.equals('csiDashboard') ? 'active' : ''}">
                        <g:link controller="csiDashboard" action="showAll">
                            <i class="fa fa-line-chart"></i>
                            <g:message code="csiDashboard.label" default="CSI"/>
                        </g:link>
                    </li>
                    <li class="${controllerName.equals('csiConfiguration') ? 'active' : ''}">
                        <g:link controller="csiConfiguration" action="configurations">
                            <i class="fa fa-gears"></i>
                            <g:message code="de.iteratec.isocsi.csiConfiguration" default="Configuration"/>
                        </g:link>
                    </li>
                </ul>
            </li>
            <li class="dropdown ${mainTab.equals('management') ? 'active open' : ''}">
                <a href="#" class="dropdown-toggle" role="button" aria-haspopup="true" aria-expanded="false">
                    <i class="fa fa-tasks"></i>
                    <g:message code="de.iteratec.isr.managementDashboard" default="Measurement"/> <span class="caret"></span>
                </a>
                <ul class="dropdown-menu">
                    <li class="${controllerName.equals('job') ? 'active' : ''}">
                        <g:link controller="job" action="index" title="${message(code:'de.iteratec.isj.jobs', default:'Jobs')}">
                            <i class="fa fa-calendar"></i>
                            <g:message code="de.iteratec.isj.jobs" default="Jobs"/>
                        </g:link>
                    </li>
                    <li class="${controllerName.equals('script') ? 'active' : ''}">
                        <g:link controller="script" action="list">
                            <i class="fa fa-file-text-o"></i>
                            <g:message code="de.iteratec.iss.scripts" default="Skripte"/>
                        </g:link>
                    </li>
                    <li class="${controllerName.equals('queueStatus') ? 'active' : ''}">
                        <g:link controller="queueStatus" action="list">
                            <i class="fa fa-inbox"></i>
                            <g:message code="queue.status.label"/>
                        </g:link>
                    </li>
                    <li class="${controllerName.equals('jobSchedule') ? 'active' : ''}">
                        <g:link controller="jobSchedule" action="schedules">
                            <i class="fa fa-clock-o"></i>
                            <g:message code="job.Schedule.label"/>
                        </g:link>
                    </li>
                    <li class="${controllerName.equals('connectivityProfile') ? 'active' : ''}">
                        <g:link controller="connectivityProfile" action="list">
                            <i class="fa fa-signal"></i>
                            <g:message code="connectivityProfile.label.plural"/>
                        </g:link>
                    </li>
                    <li class="${controllerName.equals('jobResult') ? 'active' : ''}">
                        <g:link controller="jobResult" action="listFailed">
                            <i class="fa fa-exclamation-circle" aria-hidden="true"></i>
                            <g:message code="de.iteratec.osm.failedJobResults.buttonToPage" default="Failed JobResults"/>
                        </g:link>
                    </li>
                </ul>
            </li>
        </ul>
        <ul class="nav navbar-nav bottom">
            <g:render template="/_menu/user"/>
            <g:render template="/_menu/admin"/>
            <g:render template="/_menu/info"/>
            <g:render template="/_menu/language"/>
        </ul>
    </div>
</nav>
