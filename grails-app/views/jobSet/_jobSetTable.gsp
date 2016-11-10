<%@ page import="de.iteratec.osm.measurement.schedule.JobSet" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('name'); return false;" >${message(code: 'jobSet.name.label', default: 'Name')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('jobs'); return false;" >${message(code: 'jobSet.jobs.label', default: 'Jobs')}</g:link></th>


    </tr>
    </thead>
    <tbody>
    <g:each in="${jobSets}" status="i" var="jobSetInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${jobSetInstance.id}">${fieldValue(bean: jobSetInstance, field: "name")}</g:link></td>

            <td>
                <g:each in ="${jobSetInstance.jobs}" var = "job">
                    <g:link controller="Job" action="show" id="${job.id}">${fieldValue(bean: job, field: "label")}</g:link>
                    <br>
                </g:each>
            </td>

        </tr>
    </g:each>
    </tbody>
</table>
