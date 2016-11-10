<%@ page import="de.iteratec.osm.report.external.GraphiteEventSourcePath" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('staticPrefix'); return false;" >${message(code: 'graphiteEventSourcePath.staticPrefix.label', default: 'Static Prefix')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('targetMetricName'); return false;" >${message(code: 'graphiteEventSourcePath.targetMetricName.label', default: 'Target Metric Name')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('jobGroups'); return false;" >${message(code: 'graphiteEventSourcePath.jobGroups.label', default: 'Job Groups')}</g:link></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${graphiteEventSourcePaths}" status="i" var="graphiteEventSourcePathInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${graphiteEventSourcePathInstance.id}">${fieldValue(bean: graphiteEventSourcePathInstance, field: "staticPrefix")}</g:link></td>

            <td>${fieldValue(bean: graphiteEventSourcePathInstance, field: "targetMetricName")}</td>

            <td>
                <g:each in ="${graphiteEventSourcePathInstance.jobGroups}" var = "jobGroup">
                    <g:link controller="JobGroup" action="show" id="${jobGroup.id}">${fieldValue(bean: jobGroup, field: "name")}</g:link>
                    <br>
                </g:each>
            </td>

        </tr>
    </g:each>
    </tbody>
</table>