<%@ page contentType="text/html;charset=UTF-8" %>
<%@ defaultCodec="none" %>
<html>
<head>
    <meta name="layout" content="layoutOsm"/>
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

    #tooltip {
        position: absolute;
        width: auto;
        height: auto;
        padding: 10px;
        background-color: white;
        -webkit-border-radius: 10px;
        -moz-border-radius: 10px;
        border-radius: 10px;
        -webkit-box-shadow: 4px 4px 10px rgba(0, 0, 0, 0.4);
        -moz-box-shadow: 4px 4px 10px rgba(0, 0, 0, 0.4);
        box-shadow: 4px 4px 10px rgba(0, 0, 0, 0.4);
        pointer-events: none;
    }

    #tooltip.hidden {
        display: none;
    }

    #tooltip p {
        margin: 0;
        font-family: sans-serif;
        font-size: 14px;
        line-height: 20px;
    }

    form {
        text-align: right;
    }
    </style>
</head>

<body>
    <h1><g:message code="queue.status.label"/></h1>
    <div class="form-group">
        <input type="text" class="form-control" id="schedule-filter" placeholder="<g:message code="de.iteratec.osm.ui.filter.location" default="Filter by location"/>" />
        %{--TODO: Add toggle buttons for selecting what to filter by--}%
    </div>

    <g:each in="${chartMap}" var="server" status="i">
        <div id="${server.key}" class="server-div">
            <h1>
                <span class="text-muted"><g:message code="de.iteratec.osm.webpagetest.server.label" default="WPT Server"/>:</span> ${server.key}
            </h1>
            <g:each in="${server.value}" var="location" status="j">
                <div id="${location.name}" class="location-div card">
                    <h2>
                        <span class="text-muted"><g:message code="de.iteratec.isocsi.csi.labels.filterLocations" default="Location:"/></span> ${location.name} (${location.agentCount} agents)
                    </h2>
                    <g:if test="${location.jobs.size() > 0}">
                        <iteratec:scheduleChart chartIdentifier="${i}${j}"/>
                    </g:if>
                    <g:else>
                        <g:message code="de.iteratec.osm.d3Data.multiLineChart.noJobsInInterval" default="no jobs within the next 24 hours"/>
                    </g:else>
                </div>
            </g:each>
        </div>
    </g:each>

    <content tag="include.bottom">
        <asset:javascript src="d3/scheduleChart.js"/>
        <asset:javascript src="timeago/jquery.timeago.js"/>
        <g:if test="${org.springframework.web.servlet.support.RequestContextUtils.getLocale(request).language.equals('de')}">
            <asset:javascript src="timeago/timeagoDe.js"/>
        </g:if>
        <asset:script type="text/javascript">
            <g:each in="${chartMap}" var="server" status="i">
                <g:each in="${server.value}" var="location" status="j">
                    <g:if test="${location.jobs.size() > 0}">
                        createScheduleChart(
                            ${location as grails.converters.JSON},
                            "${"ScheduleChart" + i + j}",
                            "${"duration-to-show" + i + j}",
                            "${"show-overused-queues" + i + j}",
                            "${createLink(controller: 'job', action: 'edit')}"
                        )
                    </g:if>
                </g:each>
            </g:each>
        </asset:script>
    </content>
</body>
</html>
