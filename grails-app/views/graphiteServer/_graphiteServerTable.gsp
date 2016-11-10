<%@ page import="de.iteratec.osm.report.external.GraphiteServer" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('serverAdress'); return false;" >${message(code: 'graphiteServer.serverAdress.label', default: 'Server Adress')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('port'); return false;" >${message(code: 'graphiteServer.port.label', default: 'Port')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('webappUrl'); return false;" >${message(code: 'graphiteServer.webappUrl.label', default: 'Webapp Url')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('webappProtocol'); return false;" >${message(code: 'graphiteServer.webappProtocol.label', default: 'Webapp Protocol')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('webappPathToRenderingEngine'); return false;" >${message(code: 'graphiteServer.webappPathToRenderingEngine.label', default: 'Webapp Path To Rendering Engine')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('graphitePaths'); return false;" >${message(code: 'graphiteServer.graphitePaths.label', default: 'Graphite Paths')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('reportHealthMetrics'); return false;" >${message(code: 'graphiteServer.reportHealthMetrics.label', default: 'Report Health Metrics')}</g:link></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${graphiteServers}" status="i" var="graphiteServerInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${graphiteServerInstance.id}">${fieldValue(bean: graphiteServerInstance, field: "serverAdress")}</g:link></td>

            <td>${fieldValue(bean: graphiteServerInstance, field: "port")}</td>

            <td>${fieldValue(bean: graphiteServerInstance, field: "webappUrl")}</td>

            <td>${fieldValue(bean: graphiteServerInstance, field: "webappProtocol")}</td>

            <td>${fieldValue(bean: graphiteServerInstance, field: "webappPathToRenderingEngine")}</td>

            <td>
                <g:each in ="${graphiteServerInstance.graphitePaths}" var = "graphitePath">
                    <g:link controller="GraphitePath" action="show" id="${graphitePath.id}">${fieldValue(bean: graphitePath, field: "prefix")}${fieldValue(bean: graphitePath, field: "measurand")}</g:link>
                    <br>
                </g:each>
            </td>

            <td><g:formatBoolean boolean="${graphiteServerInstance.reportHealthMetrics}" /></td>

        </tr>
    </g:each>
    </tbody>
</table>
