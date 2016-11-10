<%@ page import="de.iteratec.osm.report.UserspecificEventResultDashboard" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('dashboardName'); return false;" >${message(code: 'userspecificEventResultDashboard.dashboardName.label', default: 'Dashboard Name')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('username'); return false;" >${message(code: 'userspecificEventResultDashboard.username.label', default: 'Username')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('publiclyVisible'); return false;" >${message(code: 'userspecificEventResultDashboard.publiclyVisible.label', default: 'Publicly Visible')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('fromDate'); return false;" >${message(code: 'userspecificEventResultDashboard.fromDate.label', default: 'From Date')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('toDate'); return false;" >${message(code: 'userspecificEventResultDashboard.toDate.label', default: 'To Date')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('fromHour'); return false;" >${message(code: 'userspecificEventResultDashboard.fromHour.label', default: 'From Hour')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('toHour'); return false;" >${message(code: 'userspecificEventResultDashboard.toHour.label', default: 'To Hour')}</g:link></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${userspecificEventResultDashboards}" status="i" var="userspecificEventResultDashboardInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${userspecificEventResultDashboardInstance.id}">${fieldValue(bean: userspecificEventResultDashboardInstance, field: "dashboardName")}</g:link></td>

            <td>${fieldValue(bean: userspecificEventResultDashboardInstance, field: "username")}</td>

            <td><g:formatBoolean boolean="${userspecificEventResultDashboardInstance.publiclyVisible}" /></td>

            <td><g:formatDate boolean="${userspecificEventResultDashboardInstance.fromDate}" /></td>

            <td><g:formatDate boolean="${userspecificEventResultDashboardInstance.toDate}" /></td>

            <td>${fieldValue(bean: userspecificEventResultDashboardInstance, field: "fromHour")}</td>

            <td>${fieldValue(bean: userspecificEventResultDashboardInstance, field: "toHour")}</td>

        </tr>
    </g:each>
    </tbody>
</table>