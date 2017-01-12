<g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
<nav class="navbar navbar-inverse sidebar-fixed">
    <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#main-navbar">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="#">
            <img class="icon" src="${resource(dir: 'images', file: 'OpenSpeedMonitor_Icon_36px.png')}"
                 alt="${meta(name: 'app.name')}"/>
            <img class="logo" src="${resource(dir: 'images', file: 'OpenSpeedMonitor.svg')}"
                 alt="${meta(name: 'app.name')}"/>
        </a>
    </div>
    <div class="collapse navbar-collapse" id="main-navbar">
        <ul class="nav navbar-nav">
            <li class="${controllerName.equals('landing') ? 'active' : ''}">
                <g:link controller="landing" action="index">
                    <i class="fa fa-home"></i> Home
                </g:link>
            </li>
            <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                    <i class="fa fa-tachometer"></i>
                    <g:message code="de.iteratec.isr.measurementresults" default="Results"/> <span class="caret"></span>
                </a>
                <ul class="dropdown-menu">
                    <li class="${controllerName.equals('eventResultDashboard') ? 'active' : ''}">
                        <g:link controller="eventResultDashboard" action="showAll"
                                title="${message(code:'eventResultDashboard.label', default:'Time Series')}">
                            <i class="fa fa-line-chart"></i>
                            <span class="description">
                                <g:message code="eventResultDashboard.label" default="Time Series"/>
                            </span>
                        </g:link>
                    </li>
                    <li class="${controllerName.equals('pageAggregation') ? 'active' : ''}">
                        <g:link controller="pageAggregation" action="show"
                                title="${message(code:'de.iteratec.pageAggregation.title', default:'Page Aggregation')}">
                            <i class="fa fa-bar-chart"></i>
                            <span class="description">
                                <g:message code="de.iteratec.pageAggregation.title" default="Page Aggregation"/>
                            </span>
                        </g:link>
                    </li>
                    <li class="${controllerName.equals('detailAnalysis') ? 'active' : ''}">
                        <g:link controller="detailAnalysis" action="show"
                                title="Detail Analysis">
                            <i class="fa fa-pie-chart"></i>
                            <span class="description">
                                Detail Analysis
                            </span>
                        </g:link>
                    </li>
                    <li class="${controllerName.equals('tabularResultPresentation') ? 'active' : ''}">
                        <g:link controller="tabularResultPresentation" action="listResults"
                                title="${message(code:'de.iteratec.result.title', default:'Result List')}">
                            <i class="fa fa-th-list"></i>
                            <span class="description">
                                <g:message code="de.iteratec.result.title" default="Result List"/>
                            </span>
                        </g:link>
                    </li>
                </ul>
            </li>
            <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                    <i class="fa fa-tasks"></i>
                    <g:message code="de.iteratec.isr.managementDashboard" default="Measurement"/> <span class="caret"></span>
                </a>
                <ul class="dropdown-menu">
                    <li class="${controllerName.equals('job') ? 'active' : ''}">
                        <g:link controller="job" action="index" title="${message(code:'de.iteratec.isj.jobs', default:'Jobs')}">
                            <i class="fa fa-calendar"></i>
                            <span class="description">
                                <g:message code="de.iteratec.isj.jobs" default="Jobs"/>
                            </span>
                        </g:link>
                    </li>
                    <li class="${controllerName.equals('script') ? 'active' : ''}">
                        <g:link controller="script" action="list">
                            <i class="fa fa-file-text-o"></i>
                            <span class="description">
                                <g:message code="de.iteratec.iss.scripts" default="Skripte"/>
                            </span>
                        </g:link>
                    </li>
                    <li class="${controllerName.equals('queueStatus') ? 'active' : ''}">
                        <g:link controller="queueStatus" action="list">
                            <i class="fa fa-inbox"></i>
                            <span class="description">
                                <g:message code="queue.status.label"/>
                            </span>
                        </g:link>
                    </li>
                    <li class="${controllerName.equals('jobSchedule') ? 'active' : ''}">
                        <g:link controller="jobSchedule" action="schedules">
                            <i class="fa fa-clock-o"></i>
                            <span class="description"><g:message code="job.Schedule.label"/></span>
                        </g:link>
                    </li>
                    <li class="${controllerName.equals('connectivityProfile') ? 'active' : ''}">
                        <g:link controller="connectivityProfile" action="list">
                            <i class="fa fa-signal"></i>
                            <span class="description">
                                <g:message code="connectivityProfile.label.plural"/>
                            </span>
                        </g:link>
                    </li>
                    <li class="${controllerName.equals('jobResult') ? 'active' : ''}">
                        <g:link controller="jobResult" action="listFailed">
                            <i class="fa fa-exclamation-circle" aria-hidden="true"></i>
                            <span class="description">
                                <g:message code="de.iteratec.osm.failedJobResults.buttonToPage" default="Failed JobResults"/>
                            </span>
                        </g:link>
                    </li>
                </ul>
            </li>
            <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                    <i class="fa fa-users"></i>
                    <g:message code="csiDashboard.label" default="Customer Satisfaction"/><span class="caret"></span>
                </a>
                <ul class="dropdown-menu">
                    <li class="${controllerName.equals('csiDashboard') ? 'active' : ''}">
                        <g:link controller="csiDashboard" action="showAll"
                                title="${message(code:'eventResultDashboard.label', default:'Time Series')}">
                            <i class="fa fa-line-chart"></i>
                            <span class="description">
                                <g:message code="eventResultDashboard.label" default="Time Series"/>
                            </span>
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
