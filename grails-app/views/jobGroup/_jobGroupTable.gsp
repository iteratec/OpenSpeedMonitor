<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('name'); return false;" >${message(code: 'jobGroup.name.label', default: 'Name')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('csiConfiguration'); return false;" >${message(code: 'jobGroup.csiConfiguration.label', default: 'CSI Configuration')}</g:link></th>


    </tr>
    </thead>
    <tbody>
    <g:each in="${jobGroups}" status="i" var="jobGroupInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${jobGroupInstance.id}">${fieldValue(bean: jobGroupInstance, field: "name")}</g:link></td>

            <td><g:link controller="CsiConfiguration" action="configurations" id="${jobGroupInstance.csiConfiguration?.id}">${fieldValue(bean: jobGroupInstance, field: "csiConfiguration")}</g:link></td>


        </tr>
    </g:each>
    </tbody>
</table>
String name

CsiConfiguration csiConfiguration