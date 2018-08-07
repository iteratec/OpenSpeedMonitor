<%@ page import="de.iteratec.osm.api.ApiKey" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <g:set var="entityName" value="${message(code: 'apiKey.label', default: 'ApiKey')}" scope="request"/>
    <meta name="layout" content="kickstart"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<section id="list-apiKey" class="first">

    <g:render template="/layouts/responsiveTable"/>


    <content tag="include.bottom">
        <asset:javascript src="responsiveTable/responsiveTable.js"/>
        <asset:script type="text/javascript">
            $(OpenSpeedMonitor.responsiveTable.init( '${createLink(action: 'updateTable')}',
                    {"next":"${message(code: 'de.iteratec.osm.batch.next.label', default: 'Next')}",
                    "previous":"${message(code: 'de.iteratec.osm.batch.previous.label', default: 'Previous')}"},
                    "secretKey"));
        </asset:script>
    </content>
</section>
</body>

</html>