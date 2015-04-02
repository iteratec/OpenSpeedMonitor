<%@ page import="de.iteratec.osm.batch.BatchActivity" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="kickstart_osm"/>
    <g:set var="entityName" value="${message(code: 'de.iteratec.osm.batch.batchactivity.list.heading', default: 'Batch Activities')}"/>
    <r:require modules="batchactivity"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<h3><g:message code="de.iteratec.osm.batch.batchactivity.list.heading" default="Batch Activities"/></h3>

<p>
    <g:message code="de.iteratec.osm.batch.batchactivity.list.description" default="A list of all larger activities and their current status."/>
</p>

<section id="list-batchActivity" class="first">
    <div id="tabelle">
        <g:render template="batchActivityTable" model="${['batchActivities': batchActivities,'batchActivityCount':batchActivityCount]}"/>
    </div>
</section>
<div class="pagination">
    <bs:paginate total="${batchActivityCount}"/>
</div>
<r:script>
		$(document).ready(
			updateIfNecessary('${createLink(action: 'updateTable', absolute: true)}',
			'${createLink(action: 'checkForUpdate', absolute: true)}',
			'${createLink(action: 'getUpdate', absolute: true)}'
			)
		);
</r:script>
</body>

</html>
