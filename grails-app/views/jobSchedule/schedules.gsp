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

    /*ScheduleChart styles*/
    .axis text {
        font: 10px sans-serif;
    }
    .axis path,
    .axis line {
        fill: none;
        stroke: #000;
        shape-rendering: crispEdges;
    }
    .xAxisGrid path,
    .xAxisGrid line {
        fill: none;
        stroke: grey;
        stroke-dasharray: 2, 2;
    }
    .xAxisGrid text {
        display: none;
    }
    .locationAxis path,
    .locationAxis line {
        fill: none;
        stroke: none;
    }
    .locationAxis text {
        font: 10px sans-serif;
    }
    .resetButton {
        fill: lightgrey;
    }
    .resetButtonText {
        fill: white;
        stroke: none;
        text-anchor: middle;
    }
    .verticalLine {
        opacity: 1;
        stroke-dasharray: 3, 3;
        stroke: blue;
    }
    </style>
</head>

<body>
<%-- main menu --%>
<g:render template="/layouts/mainMenu"/>

%{--<g:each in="${chartList}" var="notUsed" status="i">--}%
<g:each in="${chartMap}" var="server" status="i">
    <h3>${server.key}</h3>
    <g:each in="${server.value}" var="location" status="j">
        <h4>${location.name}</h4>
        <iteratec:scheduleChart chartIdentifier="${i}${j}"/>
        <br />
    </g:each>
    <br/>
    <br/>
</g:each>

</content tag="include.bottom">
<asset:javascript src="d3/scheduleChart.js"/>
<asset:javascript src="timeago/jquery.timeago.js"/>
<g:if test="${org.springframework.web.servlet.support.RequestContextUtils.getLocale(request).language.equals('de')}">
    <asset:javascript src="timeago/timeagoDe.js"/>
</g:if>
<asset:script type="text/javascript">
    <g:each in="${chartMap}" var="server" status="i">
        <g:each in="${server.value}" var="location" status="j">
            createScheduleChart(${location as grails.converters.JSON}, "${"ScheduleChart" + i + j}")
        </g:each>
    </g:each>
</asset:script>
</content>
</body>
</html>