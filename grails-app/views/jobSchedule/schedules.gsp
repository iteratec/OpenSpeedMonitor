<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="queue.status.label"/></title>
    <asset:stylesheet src="queueStatus/list.css"/>
    <style>
    label {
        display: inline;
    }
    </style>
</head>

<body>
<%-- main menu --%>
<g:render template="/layouts/mainMenu"/>

<g:each in="${chartList}" var="notUsed" status="i">
    <g:render template="/d3js/scheduleChart" model="[id: i]"/>
    <br/>
    <br/>
</g:each>

</content tag="include.bottom">
    <asset:javascript src="timeago/jquery.timeago.js"/>
    <g:if test="${org.springframework.web.servlet.support.RequestContextUtils.getLocale(request).language.equals('de')}">
        <asset:javascript src="timeago/timeagoDe.js"/>
    </g:if>
    <asset:script type="text/javascript">
        <g:each in="${chartList}" var="chartData" status="i">
            createScheduleChart(${chartData as grails.converters.JSON}, "${"ScheduleChart" + i}")
        </g:each>
    </asset:script>
</content>
</body>
</html>