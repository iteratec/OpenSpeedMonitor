<%@ page import="de.iteratec.osm.batch.BatchActivity" %>
<table class="table table-bordered" id="batchActivityTable">
    <thead>
    <tr>
        <th><g:link action="index" onclick="sortBy('name'); return false;" >${message(code: 'batchActivity.name.label', default: 'Name')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('activity'); return false;" >${message(code: 'batchActivity.activity.label', default: 'Activity')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('status'); return false;" >${message(code: 'batchActivity.status.label', default: 'Status')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('actualStage'); return false;" >${message(code: 'batchActivity.stage.label', default: 'Stage')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('stepInStage'); return false;" >${message(code: 'batchActivity.stepInStage.label', default: 'Progress in Stage')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('lastFailureMessage'); return false;" >${message(code: 'batchActivity.lastFailureMessage.label', default: 'Last Failure Message')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('startDate'); return false;" >${message(code: 'batchActivity.startDate.label', default: 'Start Date')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('lastUpdate'); return false;" >${message(code: 'batchActivity.lastUpdated.label', default: 'Last Update')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('endDate'); return false;" >${message(code: 'batchActivity.endDate.label', default: 'End Date')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('remainingTime'); return false;" >${message(code: 'batchActivity.remainingTime.label', default: 'Remaining Time')}</g:link></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${batchActivities}" status="i" var="batchActivityInstance">
            <g:render template="batchActivityRow" model="${['batchActivityInstance': batchActivityInstance,evenOdd:"${(i % 2) == 0 ? 'odd' : 'even'}"]}"/>
    </g:each>
    </tbody>
</table>