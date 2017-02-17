<%@ page import="de.iteratec.osm.report.external.GraphiteEventSourcePath" %>
<!doctype html>
<html>
<head>
    <g:set var="entityName" value="${message(code: 'graphiteEventSourcePath.label', default: 'GraphiteEventSourcePath')}" scope="request"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="kickstart"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>

<section id="list-graphiteEventSourcePath" class="first">

    <g:render template="/layouts/responsiveTable"/>


    <content tag="include.bottom">
        <asset:javascript src="responsiveTable/responsiveTable.js"/>
        <asset:script type="text/javascript">
            $(document).ready(
                OpenSpeedMonitor.responsiveTable.init( '${createLink(action: 'updateTable')}',
                    {"next":"${message(code: 'de.iteratec.osm.batch.next.label', default: 'Next')}",
                    "previous":"${message(code: 'de.iteratec.osm.batch.previous.label', default: 'Previous')}"},
                    "staticPrefix")
        );

        </asset:script>
    </content>
</section>
</body>

</html>