
<%@ page import="de.iteratec.osm.report.external.GraphiteServer" %>
<!doctype html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart" />
	<g:set var="entityName" value="${message(code: 'graphiteServer.label', default: 'GraphiteServer')}" />
	<title><g:message code="default.list.label" args="[entityName]" /></title>
</head>

<body>
	
<section id="list-graphiteServer" class="first">

	<table class="table table-bordered">
		<thead>
			<tr>
			
				<g:sortableColumn property="serverAdress" title="${message(code: 'graphiteServer.serverAdress.label', default: 'Server Adress')}" />
			
				<g:sortableColumn property="port" title="${message(code: 'graphiteServer.port.label', default: 'Port')}" />
			
			</tr>
		</thead>
		<tbody>
		<g:each in="${graphiteServerInstanceList}" status="i" var="graphiteServerInstance">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			
				<td><g:link action="show" id="${graphiteServerInstance.id}">${fieldValue(bean: graphiteServerInstance, field: "serverAdress")}</g:link></td>
			
				<td>${fieldValue(bean: graphiteServerInstance, field: "port")}</td>
			
			</tr>
		</g:each>
		</tbody>
	</table>
	<div class="pagination">
		<bs:paginate total="${graphiteServerInstanceTotal}" />
	</div>
</section>

</body>

</html>
