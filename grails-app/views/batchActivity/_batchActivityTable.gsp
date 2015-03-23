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
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

            <td><g:link action="show"
                        id="${batchActivityInstance.id}">${fieldValue(bean: batchActivityInstance, field: "name")}</g:link></td>

            <td>${message(code: batchActivityInstance.activity.getI18nCode(),default: batchActivityInstance.activity)}</td>

            <td>${message(code: batchActivityInstance.status.getI18nCode(),default: batchActivityInstance.status)}</td>

            <td>${fieldValue(bean: batchActivityInstance, field: "progress")}</td>

            <td>${fieldValue(bean: batchActivityInstance, field: "lastFailureMessage")}</td>

            <td><g:formatDate type="datetime" date="${batchActivityInstance.startDate}" style="LONG"
                              timeStyle="SHORT"/></td>

            <td><g:formatDate type="datetime" date="${batchActivityInstance.lastUpdated}" style="LONG"
                              timeStyle="SHORT"/></td>

            <td><g:formatDate type="datetime" date="${batchActivityInstance.endDate}" style="LONG"
                              timeStyle="SHORT"/></td>

        </tr>
    </g:each>
    </tbody>
</table>