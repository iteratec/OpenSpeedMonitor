<%@ page import="grails.converters.JSON" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title>CSI CheckDashboard</title>
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
    </style>
</head>

<body>
<%-- main menu --%>
<g:render template="/layouts/mainMenu"/>
<%-- Überschrift --%>
<div class="row">
    <div class="span12">
        <div>
            <h3><g:message code="de.iteratec.isocsi.weights.heading" default="Gewichtungen CSI"/></h3>
        </div>
    </div>
</div>
<section id="list" class="first">

    <h4><g:message code="de.iteratec.isocsi.browser_weight" default="Browser"/></h4>
    %{-- Begin diagram view--}%
    <iteratec:matrixView
            chartIdentifier="browserConnectivityMatrixView"/>
    %{--End diagram View--}%
    <table class="table table-bordered">
        <tbody>
        <tr>
            <td class="text-info"><strong><g:message code="de.iteratec.isocsi.browser_weight"
                                                     default="Browser"/></strong></td>
            <g:each var="browser" in="${browsers ?}">
                <td>${browser.name}</td>
            </g:each>
        </tr>
        <tr>
            <td class="text-info"><strong><g:message code="de.iteratec.isocsi.weight" default="Gewichtung"/></strong>
            </td>
            <g:each var="browser" in="${browsers ?}">
                <td>${browser.weight}</td>
            </g:each>
        </tr>
        </tbody>
    </table>
    <g:link action="downloadBrowserWeights"><g:message code="de.iteratec.isocsi.csi.csvdownload"
                                                       default="CSV-Download"/></g:link>
    <g:if test="${errorMessagesCsi}">
        <ul>
            <g:each var="errMessage" in="${errorMessagesCsi ?}"><li class="text-error">${errMessage}</li></g:each>
        </ul>
    </g:if>
    <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
        <g:uploadForm action="uploadBrowserWeights">
            <input id="theBrowserCsvFile" type="file" name="browserCsv" style="display:none">

            <div class="input-append">
                <label><g:message code="de.iteratec.ism.label.upload_new_browser_weights"
                                  default="Neue Browser-Gewichtung hochladen (csv)"/></label>
                <input id="theBrowserCsvFileTwitter" class="input-large" type="text">
                <a class="btn" onclick="$('input[id=theBrowserCsvFile]').click();"><g:message
                        code="de.iteratec.ism.browse_file_system" default="Durchsuchen"/></a>
                <button type="submit" class="btn"><g:message code="de.iteratec.isocsi.upload_file"
                                                             default="Hochladen"/></button>
            </div>
        </g:uploadForm>
    </sec:ifAllGranted>
    <hr/>
    <h4><g:message code="de.iteratec.isocsi.page_weight" default="Page"/></h4>
    %{-- Begin diagram view--}%
    <iteratec:treemap
            chartIdentifier="pageWeightTreemap"/>
    %{--End diagram View--}%
    <table class="table table-bordered">
        <tbody>
        <tr>
            <td class="text-info"><strong><g:message code="de.iteratec.isocsi.page_weight" default="Page"/></strong>
            </td>
            <g:each var="page" in="${pages ?}">
                <td>${page.name}</td>
            </g:each>
        </tr>
        <tr>
            <td class="text-info"><strong><g:message code="de.iteratec.isocsi.weight" default="Gewichtung"/></strong>
            </td>
            <g:each var="page" in="${pages ?}">
                <td>${page.weight}</td>
            </g:each>
        </tr>
        </tbody>
    </table>
    <g:link action="downloadPageWeights"><g:message code="de.iteratec.isocsi.csi.csvdownload"
                                                    default="CSV-Download"/></g:link>
    <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
        <g:uploadForm action="uploadPageWeights">
            <input id="thePageCsvFile" type="file" name="pageCsv" style="display:none">

            <div class="input-append">
                <label><g:message code="de.iteratec.ism.label.upload_new_page_weights"
                                  default="Neue Page-Gewichtung hochladen (csv)"/></label>
                <input id="thePageCsvFileTwitter" class="input-large" type="text">
                <a class="btn" onclick="$('input[id=thePageCsvFile]').click();"><g:message
                        code="de.iteratec.ism.browse_file_system" default="Durchsuchen"/></a>
                <button type="submit" class="btn"><g:message code="de.iteratec.isocsi.upload_file"
                                                             default="Hochladen"/></button>
            </div>
        </g:uploadForm>
    </sec:ifAllGranted>

    <hr/>
    <h4><g:message code="de.iteratec.isocsi.hour_weight" default="Tageszeit"/></h4>
    <br/>
    <iteratec:barChart
            chartIdentifier="hoursOfDayBarchart"/>
    <br/>
%{--End diagram View--}%
    <g:link action="downloadHourOfDayWeights"><g:message code="de.iteratec.isocsi.csi.csvdownload"
                                                         default="CSV-Download"/></g:link>
    <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
        <g:uploadForm action="uploadHourOfDayWeights">
            <input id="theHourOfDayCsvFile" type="file" name="hourOfDayCsv" style="display:none">

            <div class="input-append">
                <label><g:message code="de.iteratec.ism.label.upload_new_hourofday_weights"
                                  default="Neue Tageszeit-Gewichtung hochladen (csv)"/></label>
                <input id="theHourOfDayCsvFileTwitter" class="input-large" type="text">
                <a class="btn" onclick="$('input[id=theHourOfDayCsvFile]').click();"><g:message
                        code="de.iteratec.ism.browse_file_system" default="Durchsuchen"/></a>
                <button type="submit" class="btn"><g:message code="de.iteratec.isocsi.upload_file"
                                                             default="Hochladen"/></button>
            </div>
        </g:uploadForm>
    </sec:ifAllGranted>
</section>
<content tag="include.bottom">
    <asset:javascript src="d3/matrixView.js"/>
    <asset:javascript src="d3/barChart.js"/>
    <asset:javascript src="d3/treemap.js"/>
    <asset:script type="text/javascript">
        $('input[id=theBrowserCsvFile]').change(function() {
            $('#theBrowserCsvFileTwitter').val($(this).val());
        });
        $('input[id=thePageCsvFile]').change(function() {
            $('#thePageCsvFileTwitter').val($(this).val());
        });
        $('input[id=theHourOfDayCsvFile]').change(function() {
            $('#theHourOfDayCsvFileTwitter').val($(this).val());
        });
        createMatrixView(${matrixViewData}, "browserConnectivityMatrixView");
        createTreemap(1200, 750, ${treemapData}, "rect", "pageWeightTreemap");
                createBarChart(1000, 750, ${barchartData},"clocks", "hoursOfDayBarchart");
    </asset:script>
</content>
</body>
</html>
