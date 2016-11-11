<%@ page import="de.iteratec.osm.report.chart.Event" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('eventDate'); return false;" >${message(code: 'event.date.label', default: 'Date')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('shortName'); return false;" >${message(code: 'event.shortName.label', default: 'Short Name')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('description'); return false;" >${message(code: 'event.description.label', default: 'Description')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('globallyVisible'); return false;" >${message(code: 'event.globallyVisible.label', default: 'Globally Visible')}</g:link></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${events}" status="i" var="eventInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${eventInstance.id}"><g:formatDate date="${eventInstance.eventDate}" /></g:link></td>

            <td>${fieldValue(bean: eventInstance, field: "shortName")}</td>

            <td>${fieldValue(bean: eventInstance, field: "description")}</td>

            <td><g:formatBoolean boolean="${eventInstance.globallyVisible}" /></td>

        </tr>
    </g:each>
    </tbody>
</table>