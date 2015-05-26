
<%@ page import="de.iteratec.osm.report.external.GraphiteServer" %>
<!doctype html>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart" />
	<g:set var="entityName" value="${message(code: 'graphiteServer.label', default: 'GraphiteServer')}" />
	<title><g:message code="default.show.label" args="[entityName]" /></title>
</head>

<body>

<section id="show-graphiteServer" class="first">

	<table class="table">
		<tbody>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="graphiteServer.serverAdress.label" default="Server Adress" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: graphiteServerInstance, field: "serverAdress")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="graphiteServer.port.label" default="Port" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: graphiteServerInstance, field: "port")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="graphiteServer.graphitePaths.label" default="Graphite Paths" /></td>
				
				<td valign="top" style="text-align: left;" class="value">
					<ul>
					<g:each in="${graphiteServerInstance.graphitePaths}" var="g">
						<li><g:link controller="graphitePath" action="show" id="${g.id}">${g?.encodeAsHTML()}</g:link></li>
					</g:each>
					</ul>
				</td>
				
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><g:message code="graphiteServer.graphiteEventSourcePaths.label" default="Graphite Event Source Paths" /></td>

				<td valign="top" style="text-align: left;" class="value">
					<ul>
						<g:each in="${graphiteServerInstance.graphiteEventSourcePaths}" var="g">
							<li><g:link controller="graphiteEventSourcePath" action="show" id="${g.id}">${g?.encodeAsHTML()}</g:link></li>
						</g:each>
					</ul>
				</td>

			</tr>
		
		</tbody>
	</table>
</section>

</body>

</html>
