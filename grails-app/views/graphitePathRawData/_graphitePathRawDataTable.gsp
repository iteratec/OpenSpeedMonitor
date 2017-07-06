<%@ page import="de.iteratec.osm.report.external.GraphitePathRawData" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('prefix'); return false;" >${message(code: 'graphitePathRawData.prefix.label', default: 'Prefix')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('measurand'); return false;" >${message(code: 'graphitePathRawData.measurand.label', default: 'Measurand')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('cachedView'); return false;" >${message(code: 'graphitePathRawData.cachedView.label', default: 'CachedView')}</g:link></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${graphitePathsRawData}" status="i" var="graphitePathRawDataInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${graphitePathRawDataInstance.id}">${fieldValue(bean: graphitePathRawDataInstance, field: "prefix")}</g:link></td>

            <td>${fieldValue(bean: graphitePathRawDataInstance, field: "measurand")}</td>
            <td>${fieldValue(bean: graphitePathRawDataInstance, field: "cachedView")}</td>
        </tr>
    </g:each>
    </tbody>
</table>