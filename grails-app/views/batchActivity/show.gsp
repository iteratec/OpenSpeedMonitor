
<%@ page import="de.iteratec.osm.batch.BatchActivity" %>
<!doctype html>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart_osm" />
	<g:set var="entityName" value="${message(code: 'batchActivity.label', default: 'BatchActivity')}" />
	<title><g:message code="default.show.label" args="[entityName]" /></title>
</head>

<body>

<section id="show-batchActivity" class="first">

	<table class="table">
		<tbody>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="batchActivity.domain.label" default="Domain" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: batchActivityInstance, field: "domain")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="batchActivity.idWithinDomain.label" default="Id Within Domain" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: batchActivityInstance, field: "idWithinDomain")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="batchActivity.name.label" default="Name" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: batchActivityInstance, field: "name")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="batchActivity.activity.label" default="Activity" /></td>
				
				<td valign="top" class="value"><g:message code="${batchActivityInstance?.activity?.i18nCode}" default="Activity" /></td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="batchActivity.startDate.label" default="Start Date" /></td>
				
				<td valign="top" class="value"><g:formatDate date="${batchActivityInstance?.startDate}" /></td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="batchActivity.status.label" default="Status" /></td>
				
				<td valign="top" class="value"><g:message code="${batchActivityInstance?.status?.i18nCode}" default="Status" /></td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="batchActivity.progress.label" default="Progress" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: batchActivityInstance, field: "progress")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="batchActivity.successfulActions.label" default="Successful Actions" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: batchActivityInstance, field: "successfulActions")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="batchActivity.stage.label" default="Stage" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: batchActivityInstance, field: "stage")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="batchActivity.progressWithinStage.label" default="Progress Within Stage" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: batchActivityInstance, field: "progressWithinStage")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="batchActivity.failures.label" default="Failures" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: batchActivityInstance, field: "failures")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="batchActivity.lastFailureMessage.label" default="Last Failure Message" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: batchActivityInstance, field: "lastFailureMessage")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="batchActivity.endDate.label" default="End Date" /></td>
				
				<td valign="top" class="value"><g:formatDate date="${batchActivityInstance?.endDate}" /></td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="batchActivity.lastUpdated.label" default="Last Updated" /></td>
				
				<td valign="top" class="value"><g:formatDate date="${batchActivityInstance?.lastUpdated}" /></td>
				
			</tr>
		
		</tbody>
	</table>
</section>

</body>

</html>
