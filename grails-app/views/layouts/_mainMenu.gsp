<%@ page import="de.iteratec.osm.report.UserspecificEventResultDashboard" %>

<%-- determine main-tab an set variable respectively --%>
<g:if test="${controllerName.equals('eventResultDashboard') || controllerName.equals('tabularResultPresentation') || controllerName.equals('detailAnalysis')}">
    <g:set var="mainTab" value="results"/></g:if>
<g:elseif test="${controllerName.equals('csiDashboard')}"><g:set var="mainTab" value="csi"/></g:elseif>
<g:elseif test="${controllerName.equals('csiConfiguration')}"><g:set var="mainTab" value="csi"/></g:elseif>
<g:elseif test="${controllerName.equals('script')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:elseif test="${controllerName.equals('job')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:elseif test="${controllerName.equals('queueStatus')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:elseif test="${controllerName.equals('jobSchedule')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:elseif test="${controllerName.equals('connectivityProfile')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:else><g:set var="mainTab" value="unnknown"/></g:else>

<div id="main-menu" class="row">

    <%-- ---------------------------------------------------------------------------------------------- tabs --%>
    <div class="col-md-12">
        <ul class="nav nav-tabs col-md-12" data-role="listview" data-split-icon="gear" data-filter="true">
            <li class="controller ${mainTab.equals('management') ? 'active' : ''}">
                <g:link controller="job" action="index"><g:message code="de.iteratec.isr.managementDashboard"
                                                                   default="Verwaltung"/></g:link>
            </li>
            <li class="controller ${mainTab.equals('results') ? 'active' : ''}">
                <g:link controller="eventResultDashboard" action="showAll"><g:message
                        code="de.iteratec.isr.measurementresults" default="Mess-Ergebnisse"/></g:link>
            </li>
            <li class="controller ${mainTab.equals('csi') ? 'active' : ''}">
                <g:link controller="csiDashboard" action="showAll"><g:message code="de.iteratec.isocsi.csi"
                                                                              default="CSI"/></g:link>
            </li>
        </ul>
    </div>

    <%-- ---------------------------------------------------------------------------------------------- links --%>

    <div class="col-md-8">
        <ul class="nav nav-pills">

        <%-- Measurements --%>

            <g:if test="${mainTab.equals('management')}">
                <li class="controller ${controllerName.equals('job') ? 'active' : ''}">
                    <g:link controller="job" action="index"><i class="fa fa-calendar"></i> <g:message
                            code="de.iteratec.isj.jobs" default="Jobs"/></g:link>
                </li>
                <li class="controller ${controllerName.equals('script') ? 'active' : ''}">
                    <g:link controller="script" action="list"><i class="fa fa-align-left"></i> <g:message
                            code="de.iteratec.iss.scripts" default="Skripte"/></g:link>
                </li>
                <li class="controller ${controllerName.equals('queueStatus') ? 'active' : ''}">
                    <g:link controller="queueStatus" action="list"><i class="fa fa-inbox"></i> <g:message
                            code="queue.status.label"/></g:link>
                </li>
                <li class="controller ${controllerName.equals('jobSchedule') ? 'active' : ''}">
                    <g:link controller="jobSchedule" action="schedules"><i class="fa fa-clock-o"></i> <g:message
                            code="job.Schedule.label"/></g:link>
                </li>
                <li class="controller ${controllerName.equals('connectivityProfile') ? 'active' : ''}">
                    <g:link controller="connectivityProfile" action="list"><i class="fa fa-globe"></i> <g:message
                            code="connectivityProfile.label.plural"/></g:link>
                </li>
            </g:if>

        <%-- Results --%>

            <g:elseif test="${mainTab.equals('results')}">
                <li class="controller ${(controllerName.equals('eventResultDashboard') || controllerName.equals('detailAnalysis')) ? 'active' : ''}">
                    <g:link controller="eventResultDashboard" action="showAll"><i class="fa fa-signal"></i>
                        <g:message code="de.iteratec.isocsi.eventResultDashboard" default="Dashboard"/></g:link>
                </li>
                <li class="controller ${controllerName.equals('tabularResultPresentation') ? 'active' : ''}">
                    <g:link controller="tabularResultPresentation" action="listResults"><i
                            class="fa fa-th-list"></i> <g:message code="de.iteratec.result.title"
                                                                  default="Einzelergebnisse"/></g:link>
                </li>
            </g:elseif>

        <%-- CSI --%>

            <g:elseif test="${mainTab.equals('csi')}">
                <li class="controller ${actionName.equals('showAll') ? 'active' : ''}">
                    <g:link controller="csiDashboard" action="showAll"><i class="fa fa-signal"></i> <g:message
                            code="de.iteratec.isocsi.csiDashboard" default="Dashboard"/></g:link>
                </li>
                <li class="controller ${actionName.equals('configurations') ? 'active' : ''}">
                    <g:link controller="csiConfiguration" action="configurations"><i class="fa fa-gears"></i> <g:message
                            code="de.iteratec.osm.configuration.heading" default="Configuration"/></g:link>
                </li>
            </g:elseif>
        </ul>
    </div>
    <g:if test="${availableDashboards != null}">
        <div class="col-md-4">
            <ul class="nav nav-pills pull-right">
                <li role="presentation" class="dropdown active">
                    <a class="dropdown-toggle" data-toggle="dropdown" href="#" role="button"
                       aria-haspopup="true" aria-expanded="false">
                        <g:message code="de.iteratec.isocsi.dashBoardControllers.custom.select.label"
                                   default="Dashboard-Ansicht ausw&auml;hlen"/>
                        <span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu dropdown-menu-right" id="customDashBoardSelection">
                        <g:if test="${availableDashboards.size() > 0}">
                            <g:each in="${availableDashboards}" var="availableDashboard">
                                <li><g:link action="showAll"
                                            params="[dashboardID: availableDashboard.dashboardID]">${availableDashboard.dashboardName}</g:link></li>
                            </g:each>
                        </g:if>
                        <g:else>
                            <g:set var="anchorDashboardCreation" value="#"/>
                            <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_SUPER_ADMIN">
                                <g:set var="anchorDashboardCreation" value="#bottomCommitButtons"/>
                            </sec:ifAnyGranted>
                            <li><a href="${anchorDashboardCreation}"><g:message
                                    code="de.iteratec.isocsi.dashBoardControllers.custom.select.error.noneAvailable"
                                    default="Es sind keine verf&uuml;gbar - bitte legen Sie eine an!"/></a></li>
                        </g:else>
                    </ul>
                </li>
            </ul>
        </div>
    </g:if>
</div>