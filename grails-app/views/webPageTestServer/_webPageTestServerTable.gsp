<%@ page import="de.iteratec.osm.measurement.environment.WebPageTestServer" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('label'); return false;" >${message(code: 'webPageTestServer.label.label', default: 'label')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('proxyIdentifier'); return false;" >${message(code: 'webPageTestServer.proxyIdentifier.label', default: 'proxyIdentifier')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('dateCreated'); return false;" >${message(code: 'webPageTestServer.dateCreated.label', default: 'dateCreated')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('lastUpdated'); return false;" >${message(code: 'webPageTestServer.lastUpdated.label', default: 'lastUpdated')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('active'); return false;" >${message(code: 'webPageTestServer.active.label', default: 'active')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('description'); return false;" >${message(code: 'webPageTestServer.description.label', default: 'description')}</g:link></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${webPageTestServers}" status="i" var="webPageTestServerInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${webPageTestServerInstance.id}">${fieldValue(bean: webPageTestServerInstance, field: "label")}</g:link></td>

            <td>${fieldValue(bean: webPageTestServerInstance, field: "proxyIdentifier")}</td>

            <td><g:formatDate boolean="${webPageTestServerInstance.dateCreated}" /></td>

            <td><g:formatDate boolean="${webPageTestServerInstance.lastUpdated}" /></td>

            <td><g:formatBoolean boolean="${webPageTestServerInstance.active}" /></td>

            <td>${fieldValue(bean: webPageTestServerInstance, field: "description")}</td>

        </tr>
    </g:each>
    </tbody>
</table>
