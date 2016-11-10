<%@ page import="de.iteratec.osm.report.external.GraphitePath" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('prefix'); return false;" >${message(code: 'graphitePath.prefix.label', default: 'Prefix')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('measurand'); return false;" >${message(code: 'graphitePath.measurand.label', default: 'Measurand')}</g:link></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${graphitePaths}" status="i" var="graphitePathInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${graphitePathInstance.id}">${fieldValue(bean: graphitePathInstance, field: "prefix")}</g:link></td>

            <td>${fieldValue(bean: graphitePathInstance, field: "measurand")}</td>

        </tr>
    </g:each>
    </tbody>
</table>