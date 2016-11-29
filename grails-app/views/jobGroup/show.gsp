<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<%@ defaultCodec="none" %>
<!doctype html>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="kickstart_osm"/>
    <g:set var="entityName" value="${message(code: 'jobGroup.label', default: 'JobGroup')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
    <style>
    %{--Styles for MatrixView--}%
    .xAxisMatrix path,
    .xAxisMatrix line,
    .yAxisMatrix path,
    .yAxisMatrix line {
        fill: none;
        shape-rendering: inherit;
    }

    .xAxisMatrix text,
    .yAxisMatrix text {
        font-size: 12px;
    }

    .matrixViewAxisLabel {
        font-weight: bold;
    }

    %{--Styles for BarChart--}%
    .barRect {
        fill: steelblue;
    }

    .barRect:hover {
        fill: orange;
    }

    .chart .axisLabel {
        fill: black;
        text-anchor: end;
        font-weight: bold;
    }

    .xAxis path,
    .xAxis line,
    .yAxis path,
    .yAxis line {
        fill: none;
        stroke: black;
        shape-rendering: inherit;
    }

    /*Styles for the clocks*/
    svg.clock {
        stroke-linecap: round;
    }

    .minutetick.face {
        stroke-width: 1;
    }

    .hand {
        stroke: #336;
        stroke-width: 2;
    }

    .clockBorder {
        fill: #e1e1e1;
        stroke: black;
    }

    /*Styles for Treemap*/
    .node {
        overflow: hidden;
        position: absolute;
    }

    .browserText {
        fill: black;
        font-weight: bold;
        stroke-width: 0px;
    }

    .filterBox li {
        margin-left: 15px;

    }

    #tooltipMatrixView,
    #tooltipTreemap,
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

    #tooltipMatrixView.hidden,
    #tooltipTreemap.hidden,
    #tooltip.hidden {
        display: none;
    }

    #tooltipMatrixView p,
    #tooltip p {
        margin: 0;
        font-family: sans-serif;
        font-size: 16px;
        line-height: 20px;
    }

    %{--Styles for multi line chart--}%
    .axis path,
    .axis line {
        fill: none;
        stroke: black;
        shape-rendering: crisp-edges;
    }

    .line {
        fill: none;
        stroke-width: 2px;
    }

    .verticalLine,
    .horizontalLine {
        opacity: 0.3;
        stroke-dasharray: 3, 3;
        stroke: blue;
    }

    .xTextContainer,
    .tooltipTextContainer {
        opacity: 0.5;
    }
    </style>
</head>

<body>
<g:render template="/_menu/submenubarWithoutDelete"/>
<section id="show-jobGroup" class="first">

    <table class="table">
        <tbody>
        <tr class="prop">
            <td valign="top" class="name"><g:message code="jobGroup.name.label" default="Name"/></td>

            <td valign="top" class="value">${fieldValue(bean: jobGroup, field: "name")}</td>
        </tr>
        <g:if test="${grailsApplication.config.getProperty('grails.de.iteratec.osm.assetRequests.enablePersistenceOfAssetRequests')?.toLowerCase() == "true"}">
            <tr class="prop">
                <td valign="top" class="name"><g:message code="job.jobGroup.persistHar.label" default="Persist Detaildata"/></td>

                <td valign="top" class="value"><g:formatBoolean boolean="${jobGroup.persistDetailData}" /></td>
            </tr>
        </g:if>

        <tr class="prop">
            <td valign="top" class="name"><g:message code="jobGroup.graphiteServers.label"
                                                     default="Graphite Servers"/></td>
            <td valign="top" style="text-align: left;" class="value">
                <ul>
                    <g:each in="${jobGroup.graphiteServers}" var="g">
                        <li><g:link controller="graphiteServer" action="show"
                                    id="${g.id}">${g?.encodeAsHTML()}</g:link></li>
                    </g:each>
                </ul>
            </td>
        </tr>
        <g:if test="${jobGroup.csiConfiguration}">
            <tr class="prop">
                <td valign="top" class="name"><g:message code="jobGroup.csiConfiguration.label"
                                                         default="Csi Configuration"/></td>
                <td>${selectedCsiConfiguration.label}</td>
            </tr>
        </g:if>
        </tbody>
    </table>
</section>
<section>
    <g:if test="${jobGroup.csiConfiguration}">
        <g:set var="renderCsiConfiguration" value="true"></g:set>
        <g:render template="/csiConfiguration/confDetails" model="[readOnly               : true,
                                                                   showDefaultMappings    : false,
                                                                   defaultTimeToCsMappings: defaultTimeToCsMappings,
                                                                   pageMappingsExist      : pageMappingsExist]"/>
    </g:if>
</section>
<content tag="include.bottom">
    <asset:javascript src="d3/matrixView.js"/>
    <asset:javascript src="d3/barChart.js"/>
    <asset:javascript src="d3/treemap.js"/>
</content>

</body>

</html>
