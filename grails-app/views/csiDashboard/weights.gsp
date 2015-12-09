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
        %{--Styles for multi line chart--}%
        .axis path,
        .axis line {
            fill: none;
            stroke: black;
            shape-rendering: crisp-edges ;
        }
        .line {
            fill: none;
            stroke-width: 2px;
        }
        .verticalLine,
        .horizontalLine {
            opacity: 0.3;
            stroke-dasharray: 3,3;
            stroke: blue;
        }
        .xTextContainer,
        .tooltipTextContainer{
            opacity: 0.5;
        }
    </style>
</head>

<body>
<%-- main menu ---------------------------------------------------------------------------%>
<g:render template="/layouts/mainMenu"/>

<div class="row">
    <div class="span12">
        <div class="btn-group" data-toggle="buttons-radio">
            <button type="button" class="btn btn-small btn-info" id="btn-csi-mapping" onclick="$('#csi-mapping').show();$('#csi-weights').hide();">Mapping CSI</button>
            <button type="button" class="btn btn-small btn-info" id="btn-csi-weights" onclick="$('#csi-mapping').hide();$('#csi-weights').show();">Gewichtung CSI</button>
        </div>
    </div>
</div>

<%-- csi weights ---------------------------------------------------------------------------%>

<div id="csi-weights">

    <g:if test="${errorMessagesCsi}">
        <ul>
            <g:each var="errMessage" in="${errorMessagesCsi ?}"><li class="text-error">${errMessage}</li></g:each>
        </ul>
    </g:if>

    <hr/>

    <h3>
        <span class="muted"><g:message code="de.iteratec.isocsi.weight" default="Weight"/>:&nbsp;</span>
        <g:message code="de.iteratec.isocsi.browser_connectivity_weight" default="BrowserConnectivity"/>
    </h3>
    <g:link controller="csiConfigIO" action="downloadBrowserConnectivityWeights">
        <g:message code="de.iteratec.isocsi.csi.csvdownload" default="CSV-Download"/>
    </g:link>

    <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
        <g:uploadForm controller="csiConfigIO" action="uploadBrowserConnectivityWeights">
            <input id="theBrowserConnectivityCsvFile" type="file" name="browserConnectivityCsv" style="display:none">

            <div class="input-append">
                <label><g:message code="de.iteratec.ism.label.upload_new_browser_connectivity_weights"
                                  default="Neue Browser-Verbindung-Gewichtung hochladen (csv)"/>
                </label>
                <input id="theBrowserConnectivityCsvFileTwitter" class="input-large" type="text">
                <a class="btn" onclick="$('input[id=theBrowserConnectivityCsvFile]').click();">
                    <g:message code="de.iteratec.ism.browse_file_system" default="Durchsuchen"/>
                </a>
                <button type="submit" class="btn">
                    <g:message code="de.iteratec.isocsi.upload_file" default="Hochladen"/>
                </button>
            </div>
        </g:uploadForm>
    </sec:ifAllGranted>

    <iteratec:matrixView
            chartIdentifier="browserConnectivityMatrixView"/>

    <hr/>

    <h3>
        <span class="muted"><g:message code="de.iteratec.isocsi.weight" default="Weight"/>:&nbsp;</span>
        <g:message code="de.iteratec.isocsi.page_weight" default="Page"/>
    </h3>
    <g:link controller="csiConfigIO" action="downloadPageWeights">
        <g:message code="de.iteratec.isocsi.csi.csvdownload" default="CSV-Download"/>
    </g:link>
    <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
        <g:uploadForm controller="csiConfigIO" action="uploadPageWeights">
            <input id="thePageCsvFile" type="file" name="pageCsv" style="display:none">

            <div class="input-append">
                <label>
                    <g:message code="de.iteratec.ism.label.upload_new_page_weights"
                                  default="Neue Page-Gewichtung hochladen (csv)"/>
                </label>
                <input id="thePageCsvFileTwitter" class="input-large" type="text">
                <a class="btn" onclick="$('input[id=thePageCsvFile]').click();">
                    <g:message code="de.iteratec.ism.browse_file_system" default="Durchsuchen"/>
                </a>
                <button type="submit" class="btn">
                    <g:message code="de.iteratec.isocsi.upload_file" default="Hochladen"/>
                </button>
            </div>
        </g:uploadForm>
    </sec:ifAllGranted>

    <iteratec:treemap
            chartIdentifier="pageWeightTreemap"/>

    <hr/>

    <h3>
        <span class="muted"><g:message code="de.iteratec.isocsi.weight" default="Weight"/>:&nbsp;</span>
        <g:message code="de.iteratec.isocsi.hour_weight" default="Tageszeit"/>
    </h3>
    <g:link controller="csiConfigIO" action="downloadHourOfDayWeights">
        <g:message code="de.iteratec.isocsi.csi.csvdownload" default="CSV-Download"/>
    </g:link>
    <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
        <g:uploadForm controller="csiConfigIO" action="uploadHourOfDayWeights">
            <input id="theHourOfDayCsvFile" type="file" name="hourOfDayCsv" style="display:none">

            <div class="input-append">
                <label>
                    <g:message code="de.iteratec.ism.label.upload_new_hourofday_weights"
                                  default="Neue Tageszeit-Gewichtung hochladen (csv)"/>
                </label>
                <input id="theHourOfDayCsvFileTwitter" class="input-large" type="text">
                <a class="btn" onclick="$('input[id=theHourOfDayCsvFile]').click();">
                    <g:message code="de.iteratec.ism.browse_file_system" default="Durchsuchen"/>
                </a>
                <button type="submit" class="btn">
                    <g:message code="de.iteratec.isocsi.upload_file" default="Hochladen"/>
                </button>
            </div>
        </g:uploadForm>
    </sec:ifAllGranted>

    <iteratec:barChart
            chartIdentifier="hoursOfDayBarchart"/>

    <br/>
</div>

<%-- csi mapping ---------------------------------------------------------------------------%>

<div id="csi-mapping">
    <div class="row">
        <div class="span12">
            <h3>
                <span class="muted"><g:message code="de.iteratec.osm.csi.mapping.label" default="Mapping"/>:&nbsp;</span>
                <g:message code="de.iteratec.osm.csi.configuration.mapping.heading" default="Mapping Load time&nbsp;&rArr;&nbsp;Customer satisfaction"/>
            </h3>
        </div>
    </div>

    %{--TODO: Implement visualisation of csi mapping by page (for complete csi configuration)--}%

    %{--<div class="row">--}%
        %{--<div class="span12">--}%
            %{--<h4 class="text-info"><g:message code="de.iteratec.osm.csi.weights.per-page.label" default="Per Page"/></h4>--}%
        %{--</div>--}%
    %{--</div>--}%
    %{--<g:render template="/chart/csi-mappings"--}%
              %{--model="${['chartData': defaultTimeToCsMappings, 'chartIdentifier': 'csi_mappings_pages',--}%
                        %{--'bottomOffsetXAxis': 364, 'yAxisRightOffset': 44, 'chartBottomOffset': 250,--}%
                        %{--'yAxisTopOffset': 8, 'bottomOffsetLegend': 220, 'modal': true]}" />--}%

    <div class="row">
        <div class="span12">
            <span class="inline">
                <span class="text-info">
                    <strong><g:message code="de.iteratec.osm.default.heading" default="Defaults"/></strong>
                </span>
                &nbsp;-&nbsp;<g:message code="de.iteratec.osm.csi.mapping.defaults.explanation" default="These Mappings can be assigned to pages"/>
            </span>
        </div>
    </div>
    <g:render template="/chart/csi-mappings"
              model="${['chartData': defaultTimeToCsMappings, 'chartIdentifier': 'default_csi_mappings',
                        'bottomOffsetXAxis': 364, 'yAxisRightOffset': 44, 'chartBottomOffset': 250,
                        'yAxisTopOffset': 8, 'bottomOffsetLegend': 220, 'modal': true]}" />
    <div class="row">
        <div class="span12">
            <g:link controller="csiConfigIO" action="downloadDefaultTimeToCsMappings">
                <g:message code="de.iteratec.isocsi.csi.csvdownload" default="CSV-Download"/>
            </g:link>
            <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
                <g:uploadForm controller="csiConfigIO" action="uploadDefaultTimeToCsMappings">
                    <input id="defaultTimeToCsMappingCsvFile" type="file" name="defaultTimeToCsMappingCsv" style="display:none">

                    <div class="input-append">
                        <label>
                            <g:message code="de.iteratec.ism.label.upload_new_hourofday_weights"
                                          default="Neue Tageszeit-Gewichtung hochladen (csv)"/>
                        </label>
                        <input id="defaultTimeToCsMappingCsvFileVisible" class="input-large" type="text">
                        <a class="btn" onclick="$('input[id=defaultTimeToCsMappingCsvFile]').click();">
                            <g:message code="de.iteratec.ism.browse_file_system" default="Durchsuchen"/>
                        </a>
                        <button type="submit" class="btn">
                            <g:message code="de.iteratec.isocsi.upload_file" default="Hochladen"/>
                        </button>
                    </div>
                </g:uploadForm>
            </sec:ifAllGranted>
        </div>
    </div>
</div>

<%-- include bottom ---------------------------------------------------------------------------%>

<content tag="include.bottom">
    <asset:javascript src="d3/matrixView.js"/>
    <asset:javascript src="d3/barChart.js"/>
    <asset:javascript src="d3/treemap.js"/>
    <asset:script type="text/javascript">

        var registerEventHandlersForFileUploadControls = function(){
            $('input[id=theBrowserConnectivityCsvFile]').change(function() {
                $('#theBrowserConnectivityCsvFileTwitter').val($(this).val());
            });
            $('input[id=theBrowserCsvFile]').change(function() {
                $('#theBrowserCsvFileTwitter').val($(this).val());
            });
            $('input[id=thePageCsvFile]').change(function() {
                $('#thePageCsvFileTwitter').val($(this).val());
            });
            $('input[id=theHourOfDayCsvFile]').change(function() {
                $('#theHourOfDayCsvFileTwitter').val($(this).val());
            });
            $('input[id=defaultTimeToCsMappingCsvFile]').change(function() {
                $('#defaultTimeToCsMappingCsvFileVisible').val($(this).val());
            });
        };
        var registerEventHandlers = function(){

            registerEventHandlersForFileUploadControls();

            $("#btn-csi-mapping").click(function(){
                $('#csi-mapping').show();
                $('#csi-weights').hide();
            });
            $("#btn-csi-weights").click(function(){
                $('#csi-mapping').hide();
                $('#csi-weights').show();
            });

        };

        $(document).ready(function(){

            createMatrixView(${matrixViewData}, "browserConnectivityMatrixView");
            createTreemap(1200, 750, ${treemapData}, "rect", "pageWeightTreemap");
            createBarChart(1000, 750, ${barchartData},"clocks", "hoursOfDayBarchart");

            registerEventHandlers();
            $("#btn-csi-mapping").click();

        });

    </asset:script>
</content>
</body>
</html>
