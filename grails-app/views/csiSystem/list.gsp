
<%@ page import="de.iteratec.osm.csi.CsiSystem" %>
<!doctype html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart" />
	<g:set var="entityName" value="${message(code: 'csiSystem.label', default: 'CsiSystem')}" />
	<title><g:message code="default.list.label" args="[entityName]" /></title>
</head>

<body>
	
<section id="list-csiSystem" class="first">

	<table class="table table-bordered">
		<thead>
			<tr>
			
				<g:sortableColumn property="label" title="${message(code: 'csiSystem.label.label', default: 'Label')}" />
			
			</tr>
		</thead>
		<tbody>
		<g:each in="${csiSystemInstanceList}" status="i" var="csiSystemInstance">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			
				<td><g:link action="show" id="${csiSystemInstance.id}">${fieldValue(bean: csiSystemInstance, field: "label")}</g:link></td>
			
			</tr>
		</g:each>
		</tbody>
	</table>
	<div class="pagination">
		<bs:paginate total="${csiSystemInstanceTotal}" />
	</div>
</section>

</body>

</html>
