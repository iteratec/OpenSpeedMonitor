
<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<!doctype html>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart" />
	<g:set var="entityName" value="${message(code: 'jobGroup.label', default: 'JobGroup')}" />
	<title><g:message code="default.show.label" args="[entityName]" /></title>
</head>

<body>

<section id="show-jobGroup" class="first">

	<table class="table">
		<tbody>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="jobGroup.name.label" default="Name" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: jobGroupInstance, field: "name")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="jobGroup.groupType.label" default="Group Type" /></td>
				
				<td valign="top" class="value">${jobGroupInstance?.groupType?.encodeAsHTML()}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="jobGroup.graphiteServers.label" default="Graphite Servers" /></td>
				
				<td valign="top" style="text-align: left;" class="value">
					<ul>
					<g:each in="${jobGroupInstance.graphiteServers}" var="g">
						<li><g:link controller="graphiteServer" action="show" id="${g.id}">${g?.encodeAsHTML()}</g:link></li>
					</g:each>
					</ul>
				</td>
				
			</tr>
		
		</tbody>
	</table>
</section>

</body>

</html>
