<%@ page import="de.iteratec.osm.batch.BatchActivity" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <g:sortableColumn property="name" title="${message(code: 'batchActivity.name.label', default: 'Name')}"/>

        <g:sortableColumn property="activity"
                          title="${message(code: 'batchActivity.activity.label', default: 'Activity')}"/>

        <g:sortableColumn property="status" title="${message(code: 'batchActivity.status.label', default: 'Status')}"/>

        <g:sortableColumn property="progress"
                          title="${message(code: 'batchActivity.progress.label', default: 'Progress')}"/>

        <g:sortableColumn property="lastFailureMessage"
                          title="${message(code: 'batchActivity.lastFailureMessage.label', default: 'Last Failure Message')}"/>

        <g:sortableColumn property="startDate"
                          title="${message(code: 'batchActivity.startDate.label', default: 'Start Date')}"/>

        <g:sortableColumn property="lastUpdated"
                          title="${message(code: 'batchActivity.lastUpdated.label', default: 'Last Update')}"/>

        <g:sortableColumn property="endDate"
                          title="${message(code: 'batchActivity.endDate.label', default: 'End Date')}"/>

    </tr>
    </thead>
    <tbody>
    <g:each in="${batchActivities}" status="i" var="batchActivityInstance">
            <g:render template="batchActivityRow" model="${['batchActivityInstance': batchActivityInstance]}"/>
    </g:each>
    </tbody>
</table>