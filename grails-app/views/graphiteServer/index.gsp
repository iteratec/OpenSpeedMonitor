<%@ page import="de.iteratec.osm.report.external.GraphiteServer" %>
<!doctype html>
<html>
<head>
    <g:set var="entityName" value="${message(code: 'graphiteServer.label', default: 'GraphiteServer')}" scope="request"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="kickstart"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>

<section id="list-graphiteServer" class="first">

    <g:render template="/layouts/responsiveTable"/>


    <content tag="include.bottom">
        <asset:javascript src="responsiveTable/responsiveTable.js"/>
        <asset:script type="text/javascript">
            $(OpenSpeedMonitor.responsiveTable.init( '${createLink(action: 'updateTable')}',
                    {"next":"${message(code: 'de.iteratec.osm.batch.next.label', default: 'Next')}",
                    "previous":"${message(code: 'de.iteratec.osm.batch.previous.label', default: 'Previous')}"},
                    "serverAdress")
                    );
        </asset:script>
    </content>
</section>
</body>

</html>
