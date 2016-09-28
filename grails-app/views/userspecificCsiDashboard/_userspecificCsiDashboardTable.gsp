<%@ page import="de.iteratec.osm.report.UserspecificCsiDashboard" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="sortBy('dashboardName'); return false;" >${message(code: 'userspecificCsiDashboard.dashboardName.label', default: 'Dashboard Name')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('username'); return false;" >${message(code: 'userspecificCsiDashboard.username.label', default: 'Username')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('publiclyVisible'); return false;" >${message(code: 'userspecificCsiDashboard.publiclyVisible.label', default: 'Publicly Visible')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('fromDate'); return false;" >${message(code: 'userspecificCsiDashboard.fromDate.label', default: 'From Date')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('toDate'); return false;" >${message(code: 'userspecificCsiDashboard.toDate.label', default: 'To Date')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('fromHour'); return false;" >${message(code: 'userspecificCsiDashboard.fromHour.label', default: 'From Hour')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('toHour'); return false;" >${message(code: 'userspecificCsiDashboard.toHour.label', default: 'To Hour')}</g:link></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${userspecificCsiDashboards}" status="i" var="userspecificCsiDashboardInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${userspecificCsiDashboardInstance.id}">${fieldValue(bean: userspecificCsiDashboardInstance, field: "dashboardName")}</g:link></td>

            <td>${fieldValue(bean: userspecificCsiDashboardInstance, field: "username")}</td>

            <td><g:formatBoolean boolean="${userspecificCsiDashboardInstance.publiclyVisible}" /></td>

            <td><g:formatDate boolean="${userspecificCsiDashboardInstance.fromDate}" /></td>

            <td><g:formatDate boolean="${userspecificCsiDashboardInstance.toDate}" /></td>

            <td>${fieldValue(bean: userspecificCsiDashboardInstance, field: "fromHour")}</td>

            <td>${fieldValue(bean: userspecificCsiDashboardInstance, field: "toHour")}</td>

        </tr>
    </g:each>
    </tbody>
</table>