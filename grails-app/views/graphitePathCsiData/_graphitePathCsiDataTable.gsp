<%@ page import="de.iteratec.osm.report.external.GraphitePathCsiData" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('prefix'); return false;" >${message(code: 'graphitePathRawData.prefix.label', default: 'Prefix')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('aggregationType'); return false;" >${message(code: 'graphitePathRawData.aggregationType.label', default: 'AggregationType')}</g:link></th>


    </tr>
    </thead>
    <tbody>
    <g:each in="${graphitePathsCsiData}" status="i" var="graphitePathCsiDataInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${graphitePathCsiDataInstance.id}">${fieldValue(bean: graphitePathCsiDataInstance, field: "prefix")}</g:link></td>

            <td>${fieldValue(bean: graphitePathCsiDataInstance, field: "aggregationType")}</td>
        </tr>
    </g:each>
    </tbody>
</table>