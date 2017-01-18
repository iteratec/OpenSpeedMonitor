<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; grails.converters.JSON" contentType="text/html;charset=UTF-8" %>
<%@ defaultCodec="none" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title>CSI CheckDashboard</title>
    <style>
    .chart {
        display: block;
    }
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
    #tooltip{
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

    #defaultMultilineGraphButtonLine {
        margin-bottom: 30px;
    }
    </style>
</head>

<body>
%{-- container for errors --}%
<div class="row">
    <div class="col-md-12">
        <div class="alert alert-danger" id="errorDeletingCsiConfiguration" style="display: none">
            <strong>
                <g:message code="de.iteratec.osm.csiConfiguration.deleteErrorTitle"/>
            </strong>

            <p id="deletingCsiConfiguratioinErrors"></p>
        </div>

        <g:if test="${!readOnly}">
            <div class="alert alert-warning" id="warnAboutOverwritingBox" style="display: none">
                <strong>
                    <g:message code="de.iteratec.osm.defaults.confirmationMessage"/>
                </strong>
                <p id="warningsOverwriting"></p>
            </div>

            <div class="alert alert-danger" id="errorBoxDefaultMappingCsv" style="display: none">
                <strong>
                    <g:message code="de.iteratec.osm.csi.csvErrors.title"/>
                </strong>
                <p id="defaultMappingCsvErrors"></p>
            </div>
        </g:if>

        <g:if test="${!readOnly}">
            <g:if test="${errorMessagesCsi}">
                <ul>
                    <g:each var="errMessage" in="${errorMessagesCsi ?}">
                        <li class="text-danger">${errMessage}</li>
                    </g:each>
                </ul>
            </g:if>
        </g:if>
    </div>
</div>


%{-- name and description of actual config --}%
<div class="row">
    <div class="col-md-12">
        <blockquote>
            <p class="text-info">
                <strong id="headerCsiConfLabel">${selectedCsiConfiguration.label}</strong>
                <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_SUPER_ADMIN">
                    <a href="#updateCsiConfModal" class="fa fa-edit"
                       style="text-decoration:none;color: #3a87ad;" data-toggle="modal"></a>
                </sec:ifAnyGranted>
            </p>
            <span id="headerCsiConfDescription">${selectedCsiConfiguration.description}</span>
        </blockquote>
    </div>
</div>


%{-- mapping and weights details --}%
<g:render template="confDetails" model="[readOnly                : false,
                                         showDefaultMappings     : true,
                                         errorMessagesCsi        : errorMessagesCsi,
                                         defaultTimeToCsMappings : defaultTimeToCsMappings,
                                         selectedCsiConfiguration: selectedCsiConfiguration,
                                         pageMappingsExist       : pageMappingsExist,
                                         matrixViewData          : matrixViewData,
                                         treemapData             : treemapData,
                                         barchartData            : barchartData,
                                         pageTimeToCsMappings    : pageTimeToCsMappings]"/>


%{-- initially invisible modal dialog to update csi configuratuion via ajax --}%
<g:render template="/_common/modals/csi/updateCsiConfiguration"/>

%{-- include bottom ---------------------------------------------------------------------------}%
<content tag="include.bottom">
    <asset:javascript src="d3/matrixView.js"/>
    <asset:javascript src="d3/barChart.js"/>
    <asset:javascript src="d3/treemap.js"/>
    <asset:javascript src="csi/defaultMappingCsvValidator.js"/>
    <asset:script type="text/javascript">

        var osm = {};
        osm.actualCsiConfigurationId = ${selectedCsiConfiguration.ident()};

        var actualCsiConfigurationId = ${selectedCsiConfiguration.ident()};
        var actualCsiConfigurationLabel = '${selectedCsiConfiguration.label}';
        var allCsiConfigurations = ${csiConfigurations as grails.converters.JSON};

        $(document).ready(function () {
            $('#updateCsiConfModal').on('shown', function () {
                $('#confLabelFromModal').val( $('#headerCsiConfLabel').text() );
                $('#confDescriptionFromModal').val( $('#headerCsiConfDescription').text() );
                $('#updatingCsiConfigurationErrors').text('');
                $('#errorUpdatingCsiConfiguration').hide();
            });
        });

        $(window).load(function() {
            OpenSpeedMonitor.postLoader.loadJavascript(
                '<g:assetPath src="csi/configurationPost.js" absolute="true"/>',
                true
            )
        });

    </asset:script>
</content>
</body>
</html>
