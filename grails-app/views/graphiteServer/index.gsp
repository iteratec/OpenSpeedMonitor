
<%@ page import="de.iteratec.osm.report.external.GraphiteServer" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'graphiteServer.label', default: 'GraphiteServer')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-graphiteServer" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-graphiteServer" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<g:sortableColumn property="serverAdress" title="${message(code: 'graphiteServer.serverAdress.label', default: 'Server Adress')}" />
					
						<g:sortableColumn property="port" title="${message(code: 'graphiteServer.port.label', default: 'Port')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${graphiteServerInstanceList}" status="i" var="graphiteServerInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${graphiteServerInstance.id}">${fieldValue(bean: graphiteServerInstance, field: "serverAdress")}</g:link></td>
					
						<td>${fieldValue(bean: graphiteServerInstance, field: "port")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${graphiteServerInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
