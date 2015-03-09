<%@ page import="de.iteratec.osm.batch.BatchActivity" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="kickstart"/>
    <g:set var="entityName" value="${message(code: 'batchActivity.label', default: 'BatchActivity')}"/>
    <r:require modules="batchactivity"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>

<section id="list-batchActivity" class="first">
    <div id="tabelle">

        <g:render template="batchActivityTable" model="${['batchActivities': batchActivities]}"/>
        <div class="pagination">
            <bs:paginate total="${batchActivities.size()}"/>
        </div>
    </div>
</section>
<r:script>
		$(document).ready(
			updateBatchActivity('${createLink(action: 'updateTable', absolute: true)}')
		);
</r:script>
</body>

</html>
