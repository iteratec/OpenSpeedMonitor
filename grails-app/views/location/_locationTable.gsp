<%@ page import="de.iteratec.osm.measurement.environment.Location" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('label'); return false;" >${message(code: 'location.label.label', default: 'Label')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('uniqueIdentifierForServer'); return false;" >${message(code: 'location.uniqueIdentifierForServer.label', default: 'Unique Identifier For Server')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('active'); return false;" >${message(code: 'location.active.label', default: 'Active')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('wptServer'); return false;" >${message(code: 'location.wptServer.label', default: 'WptServer')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('location'); return false;" >${message(code: 'location.location.label', default: 'Location')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('browser'); return false;" >${message(code: 'location.browser.label', default: 'Browser')}</g:link></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${locations}" status="i" var="locationInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${locationInstance.id}">${fieldValue(bean: locationInstance, field: "label")}</g:link></td>

            <td>${fieldValue(bean: locationInstance, field: "uniqueIdentifierForServer")}</td>

            <td><g:formatBoolean boolean="${locationInstance.active}" /></td>

            <td><g:link controller="WptServer" action="show" id="${locationInstance.wptServer.id}">${fieldValue(bean: locationInstance, field: "wptServer")}</g:link></td>

            <td>${fieldValue(bean: locationInstance, field: "location")}</td>

            <td><g:link controller="Browser" action="show" id="${locationInstance.browser.id}">${fieldValue(bean: locationInstance, field: "browser")}</g:link></td>

        </tr>
    </g:each>
    </tbody>
</table>