
<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'jobGroup.label', default: 'JobGroup')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-jobGroup" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-jobGroup" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<g:sortableColumn property="name" title="${message(code: 'jobGroup.name.label', default: 'Name')}" />
					
						<g:sortableColumn property="groupType" title="${message(code: 'jobGroup.groupType.label', default: 'Group Type')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${jobGroupInstanceList}" status="i" var="jobGroupInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${jobGroupInstance.id}">${fieldValue(bean: jobGroupInstance, field: "name")}</g:link></td>
					
						<td>${fieldValue(bean: jobGroupInstance, field: "groupType")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${jobGroupInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
