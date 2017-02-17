<%@ page import="de.iteratec.osm.csi.CsTargetValue" %>
<!doctype html>
<html>
<head>
    <g:set var="entityName" value="${message(code: 'csTargetValue.label', default: 'CsTargetValue')}" scope="request"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="kickstart"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>

<section id="list-csTargetValue" class="first">

    <g:render template="/layouts/responsiveTable"/>


    <content tag="include.bottom">
        <asset:javascript src="responsiveTable/responsiveTable.js"/>
        <asset:script type="text/javascript">
            $(document).ready(
                OpenSpeedMonitor.responsiveTable.init( '${createLink(action: 'updateTable')}',
                    {"next":"${message(code: 'de.iteratec.osm.batch.next.label', default: 'Next')}",
                    "previous":"${message(code: 'de.iteratec.osm.batch.previous.label', default: 'Previous')}"},
                    "date")
        );

        </asset:script>
    </content>
</section>
</body>

</html>