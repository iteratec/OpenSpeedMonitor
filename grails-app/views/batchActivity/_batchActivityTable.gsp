<%@ page import="de.iteratec.osm.batch.BatchActivity" %>
<table class="table table-bordered" id="batchActivityTable">
    <thead>
    <tr>

        <g:sortableColumn property="name" title="${message(code: 'batchActivity.name.label', default: 'Name')}"/>

        <g:sortableColumn property="activity"
                          title="${message(code: 'batchActivity.activity.label', default: 'Activity')}"/>

        <g:sortableColumn property="status" title="${message(code: 'batchActivity.status.label', default: 'Status')}"/>

        <g:sortableColumn property="stage"
                          title="${message(code: 'batchActivity.stage.label', default: 'Stage')}"/>

        <g:sortableColumn property="stepInStage"
                          title="${message(code: 'batchActivity.stepInStage.label', default: 'Progress in Stage')}"/>

        <g:sortableColumn property="lastFailureMessage"
                          title="${message(code: 'batchActivity.lastFailureMessage.label', default: 'Last Failure Message')}"/>

        <g:sortableColumn property="startDate"
                          title="${message(code: 'batchActivity.startDate.label', default: 'Start Date')}"/>

        <g:sortableColumn property="lastUpdate"
                          title="${message(code: 'batchActivity.lastUpdated.label', default: 'Last Update')}"/>

        <g:sortableColumn property="endDate"
                          title="${message(code: 'batchActivity.endDate.label', default: 'End Date')}"/>

        <g:sortableColumn property="remainingTime"
                          title="${message(code: 'batchActivity.remainingTime.label', default: 'End Date')}"/>

    </tr>
    </thead>
    <tbody>
    <g:each in="${batchActivities}" status="i" var="batchActivityInstance">
            <g:render template="batchActivityRow" model="${['batchActivityInstance': batchActivityInstance,evenOdd:"${(i % 2) == 0 ? 'odd' : 'even'}"]}"/>
    </g:each>
    </tbody>
</table>