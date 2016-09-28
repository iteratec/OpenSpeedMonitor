<%@ page import="de.iteratec.osm.csi.CsiSystem" %>
<!doctype html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="kickstart"/>
	<g:set var="entityName" value="${message(code: 'csiSystem.label', default: 'CsiSystem')}"/>
	<title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>

<section id="list-csiSystem" class="first">

	<g:render template="/layouts/responsiveTable"/>


	<content tag="include.bottom">
		<asset:javascript src="responsiveTable/responsiveTable.js"/>
		<asset:script type="text/javascript">
			$(document).ready(
                init( '${createLink(action: 'updateTable', absolute: true)}',
                    {"next":"${message(code: 'de.iteratec.osm.batch.next.label', default: 'Next')}",
                    "previous":"${message(code: 'de.iteratec.osm.batch.previous.label', default: 'Previous')}"},
                    "label")
        );

		</asset:script>
	</content>
</section>
</body>

</html>