
<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<!doctype html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart" />
	<g:set var="entityName" value="${message(code: 'jobGroup.label', default: 'JobGroup')}" />
	<title><g:message code="default.list.label" args="[entityName]" /></title>
</head>

<body>
	
<section id="list-jobGroup" class="first">

	<table class="table table-bordered">
		<thead>
			<tr>
			
				<g:sortableColumn property="name" title="${message(code: 'jobGroup.name.label', default: 'Name')}" />

                <g:sortableColumn property="csiConfiguration" title="${message(code: 'de.iteratec.osm.csi.configuration.label', default: 'CSI Configuration')}" />
			
			</tr>
		</thead>
		<tbody>
		<g:each in="${jobGroupInstanceList}" status="i" var="jobGroupInstance">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			
				<td><g:link action="show" id="${jobGroupInstance.id}">${fieldValue(bean: jobGroupInstance, field: "name")}</g:link></td>

                <td>${fieldValue(bean: jobGroupInstance, field: "csiConfiguration")}</td>
			
			</tr>
		</g:each>
		</tbody>
	</table>
	<div class="pagination">
		<bs:paginate total="${jobGroupInstanceTotal}" />
	</div>
</section>

</body>

</html>
