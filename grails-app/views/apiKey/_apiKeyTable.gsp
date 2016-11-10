<%@ page import="de.iteratec.osm.api.ApiKey" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('secretKey'); return false;" >${message(code: 'apiKey.secretKey.label', default: 'Secret Key')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('description'); return false;" >${message(code: 'apiKey.description.label', default: 'Description')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('valid'); return false;" >${message(code: 'apiKey.valid.label', default: 'valid')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('allowedForJobActivation'); return false;" >${message(code: 'apiKey.allowedForJobActivation.label', default: 'Allowed For Job Activation')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('allowedForJobDeactivation'); return false;" >${message(code: 'apiKey.allowedForJobDeactivation.label', default: 'Allowed For Job Deactivation')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('allowedForJobSetExecutionSchedule'); return false;" >${message(code: 'apiKey.allowedForJobSetExecutionSchedule.label', default: 'Allowed For Job Set ExecutionSchedule')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('allowedForCreateEvent'); return false;" >${message(code: 'apiKey.allowedForCreateEvent.label', default: 'Allowed For Create Event')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('allowedForMeasurementActivation'); return false;" >${message(code: 'apiKey.allowedForMeasurementActivation.label', default: 'Allowed For Measurement Activation')}</g:link></th>

        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('allowedForNightlyDatabaseCleanupActivation'); return false;" >${message(code: 'apiKey.allowedForNightlyDatabaseCleanupActivation.label', default: 'Allowed For Nightly Database Cleanup Activation')}</g:link></th>


    </tr>
    </thead>
    <tbody>
    <g:each in="${apiKeys}" status="i" var="apiKeyInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${apiKeyInstance.id}">${fieldValue(bean: apiKeyInstance, field: "secretKey")}</g:link></td>

            <td>${fieldValue(bean: apiKeyInstance, field: "description")}</td>

            <td><g:formatBoolean boolean="${apiKeyInstance.valid}" /></td>

            <td><g:formatBoolean boolean="${apiKeyInstance.allowedForJobActivation}" /></td>

            <td><g:formatBoolean boolean="${apiKeyInstance.allowedForJobDeactivation}" /></td>

            <td><g:formatBoolean boolean="${apiKeyInstance.allowedForJobSetExecutionSchedule}" /></td>

            <td><g:formatBoolean boolean="${apiKeyInstance.allowedForCreateEvent}" /></td>

            <td><g:formatBoolean boolean="${apiKeyInstance.allowedForMeasurementActivation}" /></td>

            <td><g:formatBoolean boolean="${apiKeyInstance.allowedForNightlyDatabaseCleanupActivation}" /></td>

        </tr>
    </g:each>
    </tbody>
</table>
