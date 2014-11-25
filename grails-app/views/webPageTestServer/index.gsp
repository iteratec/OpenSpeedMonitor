
<%@ page import="de.iteratec.osm.measurement.environment.WebPageTestServer" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'webPageTestServer.label', default: 'WebPageTestServer')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-webPageTestServer" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-webPageTestServer" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<g:sortableColumn property="label" title="${message(code: 'webPageTestServer.label.label', default: 'Label')}" />
					
						<g:sortableColumn property="proxyIdentifier" title="${message(code: 'webPageTestServer.proxyIdentifier.label', default: 'Proxy Identifier')}" />
					
						<g:sortableColumn property="dateCreated" title="${message(code: 'webPageTestServer.dateCreated.label', default: 'Date Created')}" />
					
						<g:sortableColumn property="lastUpdated" title="${message(code: 'webPageTestServer.lastUpdated.label', default: 'Last Updated')}" />
					
						<g:sortableColumn property="active" title="${message(code: 'webPageTestServer.active.label', default: 'Active')}" />
					
						<g:sortableColumn property="description" title="${message(code: 'webPageTestServer.description.label', default: 'Description')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${webPageTestServerInstanceList}" status="i" var="webPageTestServerInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${webPageTestServerInstance.id}">${fieldValue(bean: webPageTestServerInstance, field: "label")}</g:link></td>
					
						<td>${fieldValue(bean: webPageTestServerInstance, field: "proxyIdentifier")}</td>
					
						<td><g:formatDate date="${webPageTestServerInstance.dateCreated}" /></td>
					
						<td><g:formatDate date="${webPageTestServerInstance.lastUpdated}" /></td>
					
						<td><g:formatBoolean boolean="${webPageTestServerInstance.active}" /></td>
					
						<td>${fieldValue(bean: webPageTestServerInstance, field: "description")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${webPageTestServerInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
