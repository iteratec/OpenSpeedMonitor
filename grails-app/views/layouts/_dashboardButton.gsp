<%@ page import="de.iteratec.osm.report.UserspecificEventResultDashboard" %>
<div class="btn-group">
    <g:link controller="${affectedController}" action="showAll" class="btn btn-default"><i class="fa fa-line-chart"></i>
        <g:message code="de.iteratec.isocsi.eventResultDashboard" default="Dashboard"/></g:link>
    <a class="dropdown-toggle btn btn-default" data-toggle="dropdown" href="#" role="button"
       aria-haspopup="true" aria-expanded="false" id="customDashboardDropdownButton">
        <span class="caret"></span>
    </a>
    <ul class="dropdown-menu" id="customDashBoardSelection">
        <li class="dropdown-header">
            <g:message code="de.iteratec.isocsi.dashBoardControllers.custom.select.label"
                       default="View a custom Dashboard"/>
        </li>
        <g:if test="${availableDashboards}">
            <g:each in="${availableDashboards}" var="availableDashboard">
                <li><g:link controller="${affectedController}" action="showAll"
                            params="[dashboardID: availableDashboard.dashboardID]">${availableDashboard.dashboardName}</g:link></li>
            </g:each>
        </g:if>
        <g:else>
            <g:set var="anchorDashboardCreation" value="#"/>
            <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_SUPER_ADMIN">
                <g:set var="anchorDashboardCreation" value="#bottomCommitButtons"/>
            </sec:ifAnyGranted>
            <li><g:link controller="${affectedController}" action="showAll" fragment="${anchorDashboardCreation}"><g:message
                    code="de.iteratec.isocsi.dashBoardControllers.custom.select.error.noneAvailable"
                    default="Es sind keine verf&uuml;gbar - bitte legen Sie eine an!"/></g:link></li>
        </g:else>
    </ul>
</div>
