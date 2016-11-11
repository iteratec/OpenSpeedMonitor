<%@ page import="de.iteratec.osm.csi.CsTargetGraph" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('label'); return false;" >${message(code: 'csTargetGraph.label.label', default: 'Label')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('description'); return false;" >${message(code: 'csTargetGraph.description.label', default: 'Description')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('pointOne'); return false;" >${message(code: 'csTargetGraph.pointOne.label', default: 'Point One')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('pointTwo'); return false;" >${message(code: 'csTargetGraph.pointTwo.label', default: 'Point Two')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('defaultVisibility'); return false;" >${message(code: 'csTargetGraph.defaultVisibility.label', default: 'Default Visibility')}</g:link></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${csTargetGraphs}" status="i" var="csTargetGraphInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${csTargetGraphInstance.id}">${fieldValue(bean: csTargetGraphInstance, field: "label")}</g:link></td>

            <td>${fieldValue(bean: csTargetGraphInstance, field: "description")}</td>

            <td><g:link controller="CsTargetValue" action="show" id="${csTargetGraphInstance.pointOne.id}">${fieldValue(bean: csTargetGraphInstance, field: "pointOne")}</g:link></td>

            <td><g:link controller="CsTargetValue" action="show" id="${csTargetGraphInstance.pointTwo.id}">${fieldValue(bean: csTargetGraphInstance, field: "pointTwo")}</g:link></td>

            <td><g:formatBoolean boolean="${csTargetGraphInstance.defaultVisibility}" /></td>

        </tr>
    </g:each>
    </tbody>
</table>