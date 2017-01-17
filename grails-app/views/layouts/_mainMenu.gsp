<%@ page import="de.iteratec.osm.report.UserspecificEventResultDashboard" %>

<%-- determine main-tab an set variable respectively --%>
<g:if test="${controllerName.equals('eventResultDashboard') || controllerName.equals('tabularResultPresentation') || controllerName.equals('pageAggregation') || controllerName.equals('detailAnalysis')}">
    <g:set var="mainTab" value="results"/></g:if>
<g:elseif test="${controllerName.equals('csiDashboard')}"><g:set var="mainTab" value="csi"/></g:elseif>
<g:elseif test="${controllerName.equals('csiConfiguration')}"><g:set var="mainTab" value="csi"/></g:elseif>
<g:elseif test="${controllerName.equals('script')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:elseif test="${controllerName.equals('job')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:elseif test="${controllerName.equals('queueStatus')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:elseif test="${controllerName.equals('jobSchedule')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:elseif test="${controllerName.equals('jobResult')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:elseif test="${controllerName.equals('connectivityProfile')}"><g:set var="mainTab" value="management"/></g:elseif>
<g:else><g:set var="mainTab" value="unknown"/></g:else>
<div id="main-menu" class="row" style="display: none">
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
                <li class="controller ${controllerName.equals('jobResult') ? 'active' : ''}">
                    <g:link controller="jobResult" action="listFailed"><i class="fa fa-exclamation-circle" aria-hidden="true"></i> <g:message
                            code="de.iteratec.osm.failedJobResults.buttonToPage" default="Failed JobResults"/></g:link>
                </li>
            </g:if>

        <%-- Results --%>

            <g:elseif test="${mainTab.equals('results')}">
                <li class="controller ${(controllerName.equals('eventResultDashboard') || controllerName.equals('detailAnalysis')) ? 'active' : ''}" id="eventResultMainMenu">
                    <g:render template="/layouts/dashboardButton" model="${[
                            'availableDashboards': availableDashboards,
                            'affectedController' : 'eventResultDashboard'
                    ]}"/>
                </li>
                <li class="controller ${controllerName.equals('pageAggregation') ? 'active' : ''}" id="pageAggregationMainMenu">
                    <g:link controller="pageAggregation" action="show"><i
                            class="fa fa-bar-chart"></i> <g:message code="de.iteratec.pageAggregation.title"
                                                                  default="Page Aggregation"/></g:link>
                </li>
                <li class="controller ${controllerName.equals('tabularResultPresentation') ? 'active' : ''}" id="tabularResultMainMenu">
                    <g:link controller="tabularResultPresentation" action="listResults"><i
                            class="fa fa-th-list"></i> <g:message code="de.iteratec.result.title"
                                                                  default="Result List"/></g:link>
                </li>
            </g:elseif>

        <%-- CSI --%>

            <g:elseif test="${mainTab.equals('csi')}">
                <li class="controller ${actionName.equals('showAll') ? 'active' : ''}">
                    <g:render template="/layouts/dashboardButton" model="${[
                            'availableDashboards': availableDashboards,
                            'affectedController' : 'csiDashboard'
                    ]}"/>
                </li>
                <li class="controller ${actionName.equals('configurations') ? 'active' : ''}">
                    <g:render template="/layouts/csiConfigurationButton" model="${[
                            'availableCsiConfigurations': csiConfigurations,
                            'affectedController'     : 'csiConfiguration'
                    ]}"/>
                </li>
            </g:elseif>
        </ul>
    </div>

    <div class="col-md-4" id="selectionSummary">
        <g:if test="${help}">
            <a href="#" id="help" class="btn btn-default pull-right" data-toggle="popover"
               title="${g.message(code: 'de.iteratec.osm.joblist.activeruns.title', default: 'Active job runs')}"
               data-placement="bottom" data-content="${help}" data-trigger="hover" data-html="true">
                <i class="fa fa-info"></i>
            </a>
        </g:if>
        <g:if test="${controllerName == 'eventResultDashboard'}">
            <table>
                <tr id="selectionConstraintBrowser">
                    <th>
                        <g:message code="browser.label" default="Browser"/>
                        &nbsp;|&nbsp;
                        <g:message code="job.location.label" default="Location"/>:
                    </th>
                    <td></td>
                </tr>
                <tr id="selectionConstraintConnectivity">
                    <th>
                        <g:message code="de.iteratec.osm.result.connectivity.label" default="Connectivity"/>:
                    </th>
                    <td></td>
                </tr>
                <tr id="selectionConstraintFirstView">
                    <th>
                        <g:message code="job.firstView.label" default="First View"/>:
                    </th>
                    <td></td>
                </tr>
                <tr id="selectionConstraintRepeatView">
                    <th>
                        <g:message code="job.repeatedView.label" default="Repeated View"/>:
                    </th>
                    <td></td>
                </tr>
                <tr id="selectionConstraintTrim">
                    <th>
                        <g:message code="de.iteratec.isr.wptrd.labels.trim" default="Trim"/>:
                    </th>
                    <td></td>
                </tr>
            </table>
        </g:if>
        <g:elseif test="${controllerName == 'csiConfiguration'}">
            <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_SUPER_ADMIN">
                <div class="pull-right">
                    <button class="btn btn-primary"
                            onclick="prepareConfigurationListAndCopy()">
                        <i class="fa fa-files-o" aria-hidden="true"></i>
                        ${message(code: 'de.iteratec.osm.csiConfiguration.saveAs', default: 'Save a copy')}
                    </button>
                    <a href="#" onclick="return validatedDeletion()" id="deleteCsiConfigurationHref" class="btn btn-danger">
                        <i class="fa fa-trash-o" aria-hidden="true"></i> ${message(code: 'de.iteratec.osm.csiConfiguration.deleteCsiConfiguration', default: 'Delete')}
                    </a>
                </div>
            </sec:ifAnyGranted>
        </g:elseif>
    </div>
</div>
