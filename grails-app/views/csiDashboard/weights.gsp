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
<%-- main menu ---------------------------------------------------------------------------%>
<g:render template="/layouts/mainMenu"/>

<div class="row">
    <div class="span12">
        <div class="btn-group pull-left">
            <a id="csiConfigurationSelectButton" class="btn btn-small btn-info dropdown-toggle" data-toggle="dropdown"
               href="#">
                ${selectedCsiConfiguration}
                <span class="caret"></span>
            </a>
            <ul class="dropdown-menu">
                <g:each in="${csiConfigurations}" var="conf">
                    <li><a id="button_${conf}" onclick="changeCsiConfiguration(this.getAttribute('value'))" value="${conf[0]}">${conf[1]}</a>
                        %{--onclick="filterJobSet('${jobSet.name}', '${jobSet.jobs*.toString()}')">${jobSet.name}</a>--}%
                    </li>
                </g:each>
            </ul>
        </div>
    </div>
</div>

<hr>

<div class="row">
    <div class="span12">
        <div class="btn-group" data-toggle="buttons-radio">
            <button type="button" class="btn btn-small btn-info" id="btn-csi-mapping"
                    onclick="$('#csi-mapping').show();
                    $('#csi-weights').hide();"><g:message code="de.iteratec.osm.csi.weights.mappingCSIButton"
                                                          default="Weights CSI"/></button>
            <button type="button" class="btn btn-small btn-info" id="btn-csi-weights"
                    onclick="$('#csi-mapping').hide();
                    $('#csi-weights').show();"><g:message code="de.iteratec.osm.csi.weights.weightCSIButton"
                                                          default="Mapping CSI"/></button>
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

    <br/>

    <div class="row">
        <div class="span12">
            <h3>
                <span class="muted"><g:message code="de.iteratec.isocsi.weight" default="Weight"/>:&nbsp;</span>
                <g:message code="de.iteratec.isocsi.browser_connectivity_weight" default="BrowserConnectivity"/>
            </h3>
        </div>
    </div>
    <g:link controller="csiConfigIO" action="downloadBrowserConnectivityWeights">
        <g:message code="de.iteratec.isocsi.csi.csvdownload" default="CSV-Download"/>
    </g:link>

    <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
        <g:uploadForm controller="csiConfigIO" action="uploadBrowserConnectivityWeights">
            <input id="theBrowserConnectivityCsvFile" type="file" name="browserConnectivityCsv"
                   style="display:none">

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

<div id="csi-mapping"></div>

<div class="alert" id="warnAboutOverwritingBox">
    <strong><g:message
            code="de.iteratec.osm.defaults.confirmationMessage"/></strong>

    <p id="warningsOverwriting">
    </p>

</div>

<div class="alert alert-error" id="errorBoxDefaultMappingCsv">
    <strong><g:message
            code="de.iteratec.osm.csi.csvErrors.title"/></strong>

    <p id="defaultMappingCsvErrors">
    </p>

</div>
<br/>

<div class="row">
    <div class="span12">
        <h3>
            <span class="muted"><g:message code="de.iteratec.osm.csi.mapping.label"
                                           default="Mapping"/>:&nbsp;</span>
            <g:message code="de.iteratec.osm.csi.configuration.mapping.heading"
                       default="Mapping Load time&nbsp;&rArr;&nbsp;Customer satisfaction"/>
        </h3>
    </div>
</div>

<div id="spinner-position"></div>

<div class="row">
    <div class="span12">
        <g:link controller="csiConfigIO" action="downloadDefaultTimeToCsMappings">
            <g:message code="de.iteratec.isocsi.csi.csvdownload" default="CSV-Download"/>
        </g:link>
        <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
            <g:uploadForm controller="csiConfigIO" action="uploadDefaultTimeToCsMappings">
                <input id="defaultTimeToCsMappingCsvFile" type="file" name="defaultTimeToCsMappingCsv"
                       style="display:none">

                <div class="input-append">
                    <label>
                        <g:message code="de.iteratec.ism.label.upload_new_hourofday_weights"
                                   default="Neue Tageszeit-Gewichtung hochladen (csv)"/>
                    </label>
                    <input id="defaultTimeToCsMappingCsvFileVisible" class="input-large" type="text">
                    <a class="btn" onclick="$('input[id=defaultTimeToCsMappingCsvFile]').click();">
                        <g:message code="de.iteratec.ism.browse_file_system" default="Durchsuchen"/>
                    </a>
                    <button type="submit" class="btn" id="defaultMappingUploadButton" onclick="showSpinner()">
                        <g:message code="de.iteratec.isocsi.upload_file" default="Hochladen"/>
                    </button>
                </div>
            </g:uploadForm>
        </sec:ifAllGranted>
    </div>
</div>

<div class="row">
    <div class="span12">
        <span class="inline">
            <span class="text-info">
                <strong><g:message code="de.iteratec.osm.default.heading" default="Defaults"/></strong>
            </span>
            &nbsp;-&nbsp;<g:message code="de.iteratec.osm.csi.mapping.defaults.explanation"
                                    default="These Mappings can be assigned to pages"/>
        </span>
    </div>
</div>

<g:render template="/chart/csi-mappings"
          model="${['chartData'        : defaultTimeToCsMappings, 'chartIdentifier': 'default_csi_mappings',
                    'bottomOffsetXAxis': 364, 'yAxisRightOffset': 44, 'chartBottomOffset': 250,
                    'yAxisTopOffset'   : 8, 'bottomOffsetLegend': 220, 'modal': false]}"/>


%{--Todomarcus IT-720--}%
<div class="row">
    <div class="span12">
        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_SUPER_ADMIN">
            <div class="btn-group">
                <button type="button" class="btn btn-small btn-primary" id="btn-save-csi-configuration">
                    <g:message code="de.iteratec.osm.csiConfiguration.saveAs" default="Save as"/></button>

                <button type="button" class="btn btn-small btn-danger" id="btn-delete-csi-configuration">
                    <g:message code="de.iteratec.osm.csiConfiguration.deleteCsiConfiguration"
                               args="${[selectedCsiConfiguration]}"
                               default="Save as"/></button>
            </div>
        </sec:ifAnyGranted>
    </div>
</div>
</div>


<%-- include bottom ---------------------------------------------------------------------------%>

<content tag="include.bottom">
    <asset:javascript src="d3/matrixView.js"/>
    <asset:javascript src="d3/barChart.js"/>
    <asset:javascript src="d3/treemap.js"/>
    <asset:javascript src="csidashboard/defaultMappingCsvValidator.js"/>
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

            $("#warnAboutOverwritingBox").hide();
            $("#errorBoxDefaultMappingCsv").hide();
            $("#defaultMappingUploadButton").prop("disabled", true);
            if(${showCsiWeights}) {
                $("#btn-csi-weights").click();
            } else {
                $("#btn-csi-mapping").click();
            }

        });

        $('#defaultTimeToCsMappingCsvFile').bind('change', function() {
            $("#warnAboutOverwritingBox").hide();
            $("#errorBoxDefaultMappingCsv").hide();
            $("#defaultMappingUploadButton").prop("disabled", true);

            validateDefaultMappingCsv(this.files[0])
        });

        function showSpinner () {
            var spinner = startSpinner(document.getElementById('spinner-position'));
            return true;
        }

         function startSpinner(spinnerElement){
            var opts = {
                lines: 15, // The number of lines to draw
                length: 20, // The length of each line
                width: 10, // The line thickness
                radius: 30, // The radius of the inner circle
                corners: 1, // Corner roundness (0..1)
                rotate: 0, // The rotation offset
                direction: 1, // 1: clockwise, -1: counterclockwise
                color: '#000', // #rgb or #rrggbb or array of colors
                speed: 1, // Rounds per second
                trail: 60, // Afterglow percentage
                shadow: true, // Whether to render a shadow
                hwaccel: false, // Whether to use hardware acceleration
                className: 'spinner', // The CSS class to assign to the spinner
                zIndex: 2e9, // The z-index (defaults to 2000000000)
                top: '50%', // Top position relative to parent in px
                left: '50%' // Left position relative to parent in px
            };
        return new Spinner(opts).spin(spinnerElement);
        }

        function changeCsiConfiguration(id){
                window.location.href="http://localhost:8080/OpenSpeedMonitor/csiDashboard/weights/"+id;
        }

    </asset:script>
</content>
</body>
</html>
