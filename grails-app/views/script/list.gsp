<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta name="layout" content="kickstart_osm" />
<g:set var="entityName"	value="${message(code: 'de.iteratec.iss.script', default: 'Skript')}" />
<title><g:message code="de.iteratec.iss.scripts" /></title>
<script src="<g:resource dir="js/script" file="list.js" absolute="true" />"></script>
<style>
td, th {
	border: 1px #ccc solid;
	padding: 0.3em;
}
</style>
</head>
<body>
	<%-- main menu --%>
	<g:render template="/layouts/mainMenu"/>
	
	<div class="controlribbon">
		<h5><g:message code="script.list.label.filter" default="Skripte filtern" /></h5>
		<input type="text" id="filterByLabel" onkeyup="filterScriptList()" placeholder="<g:message code="script.list.filter.name" default="Nach Skript-Namen" />" />
	</div>
	<div class="controlribbon">
		<a href="<g:createLink action="create" />" class="btn btn-primary">
			<i class="icon-plus"></i> <g:message code="default.create.label" args="[entityName]" />
		</a>
	</div><br>
    <g:if test="${scripts.isEmpty()}">
	<p>
	    <g:message code="de.iteratec.isr.ui.EventResultListing.isEmpty.message" />
	</p>
	</g:if>
	<g:else>
	
	<table class="table-striped">
		<thead>
		<tr>
			<g:sortableColumn property="label" titleKey="de.iteratec.iss.script" />
			<g:sortableColumn property="description" titleKey="script.description.label" />
			<g:sortableColumn property="measuredEventsCount" titleKey="script.measuredEventsCount.label" />
			<g:sortableColumn property="testedPages.name" titleKey="script.testedPageNames.label" />
		</tr>
		</thead>
		<tbody>
		<g:each var="script" in="${scripts}">
			<tr>
				<td><strong><a class="scriptLabel" href="${createLink(action: 'edit', id: script.id, absolute: true)}">${script.label}</a></strong></td>
				<td>${script.description}</td>
				<td>${script.measuredEventsCount}</td>
				<td>${script.testedPages?.collect { it.name }.join(', ')}</td>
			</tr>
	     </g:each>
	    </tbody>
	</table>
	
				
	</g:else>
</body>
</html>