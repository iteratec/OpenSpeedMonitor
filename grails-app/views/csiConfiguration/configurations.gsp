<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; grails.converters.JSON" contentType="text/html;charset=UTF-8" %>
<%@ defaultCodec="none" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title>CSI CheckDashboard</title>
    <style>
    .chart {
        display: block;
        margin: 0 auto;
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
<%-- main menu ---------------------------------------------------------------------------%>
<g:render template="/layouts/mainMenu"/>


%{-- dropdown button --}%%{--
<div class="row">
    <div class="col-md-2">
        <g:if test="${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_SUPER_ADMIN") || csiConfigurations.size() > 1}">
            <div class="btn btn-group pull-left">
                <button class="btn btn-sm btn-info dropdown-toggle text-right" data-toggle="dropdown">
                    <g:message code="de.iteratec.osm.csi.configuration.messages.actual-configuration"
                               default="This Configuration..."/>
                    <span class="caret"></span>
                </button>
                <ul class="dropdown-menu">

                --}%%{--features for actual configuration----------------------------}%%{--
                    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_SUPER_ADMIN">
                        <li>
                            <a href="#"
                               onclick="prepareConfigurationListAndCopy();">
                                <i class="fa fa-copy"></i>&nbsp;${message(code: 'de.iteratec.osm.csiConfiguration.saveAs', default: 'Copy')}
                            </a>
                        </li>
                        <li>
                            <a href="#"
                               onclick="return validatedDeletion()" id="deleteCsiConfigurationHref">
                                <i class="fa fa-remove"></i>&nbsp;${message(code: 'de.iteratec.osm.csi.ui.delete.label', default: 'delete')}
                            </a>
                        </li>
                    </sec:ifAnyGranted>

                --}%%{--submenu to show other configurations----------------------------}%%{--
                    <li class="dropdown-submenu" id="otherConfigsSubmenu">
                        <a tabindex="-1" href="#">
                            <i class="fa fa-share-square-o"></i>&nbsp;<g:message
                                code="de.iteratec.osm.csi.configuration.messages.select-different"
                                default="Switch to..."/>
                        </a>
                        <ul class="dropdown-menu" id="csiConfigurationSwitchMenu"></ul>
                    </li>
                </ul>
            </div>
        </g:if>
    </div>
</div>--}%


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
            <div class="alert alert-warning" id="warnAboutOverwritingBox">
                <strong>
                    <g:message code="de.iteratec.osm.defaults.confirmationMessage"/>
                </strong>
                <p id="warningsOverwriting"></p>
            </div>

            <div class="alert alert-danger" id="errorBoxDefaultMappingCsv">
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

        <div id="copyCsiConfigurationSpinner" class="spinner-large-content-spinner-25"></div>
    </div>
</div>


%{-- nav tabs for the mapping and the weights --}%
<ul class="nav nav-tabs">
    <li class="active">
        <a data-toggle="tab" href="#csiMappingDetailsTabContent">
            <g:message code="de.iteratec.osm.csi.navTab.mapping" default="Mappings"/>
        </a>
    </li>
    <li>
        <a data-toggle="tab" href="#csiConnectivityWeightDetailsTabContent">
            <g:message code="de.iteratec.osm.csi.navTab.connectivityWeights" default="Connectivity Weights"/>
        </a>
    </li>
    <li>
        <a data-toggle="tab" href="#csiPageWeightDetailsTabContent">
            <g:message code="de.iteratec.osm.csi.navTab.pageWeights" default="Page Weights"/>
        </a>
    </li>
    <li>
        <a data-toggle="tab" href="#csiTimeWeightDetailsTabContent">
            <g:message code="de.iteratec.osm.csi.navTab.timeWeights" default="Time Weights"/>
        </a>
    </li>
</ul>


%{-- mapping and weights details --}%
<g:render template="confDetails" model="[readOnly                : false,
                                         showDefaultMappings     : true,
                                         errorMessagesCsi        : errorMessagesCsi,
                                         defaultTimeToCsMappings : defaultTimeToCsMappings,
                                         selectedCsiConfiguration: selectedCsiConfiguration,
                                         pageMappingsExist       : pageMappingsExist]"/>


%{-- initially invisible modal dialog to update csi configuratuion via ajax --}%
<g:render template="/_common/modals/csi/updateCsiConfiguration"/>

<%-- include bottom ---------------------------------------------------------------------------%>

<content tag="include.bottom">
    <asset:javascript src="d3/matrixView.js"/>
    <asset:javascript src="d3/barChart.js"/>
    <asset:javascript src="d3/treemap.js"/>
    <asset:javascript src="csi/defaultMappingCsvValidator.js"/>
    <asset:script type="text/javascript">

        var registerEventHandlers = function () {

            registerEventHandlersForFileUploadControls();

            %{--$("#btn-csi-mapping").click(function () {--}%
                %{--$('#csi-mapping').show();--}%
                %{--$('#csi-weights').hide();--}%
            %{--});--}%
            %{--$("#btn-csi-weights").click(function () {--}%
                %{--$('#csi-mapping').hide();--}%
                %{--$('#csi-weights').show();--}%
            %{--});--}%

            $('#updateCsiConfModal').on('shown', function () {
                $('#confLabelFromModal').val( $('#headerCsiConfLabel').text() );
                $('#confDescriptionFromModal').val( $('#headerCsiConfDescription').text() );
                $('#updatingCsiConfigurationErrors').text('');
                $('#errorUpdatingCsiConfiguration').hide();
            });

            $('#defaultTimeToCsMappingCsvFile').bind('change', function () {
                $("#warnAboutOverwritingBox").hide();
                $("#errorBoxDefaultMappingCsv").hide();
                $("#defaultMappingUploadButton").prop("disabled", true);

                validateDefaultMappingCsv(this.files[0])
            });

        };

        var registerEventHandlersForFileUploadControls = function () {
            $('input[id=theBrowserConnectivityCsvFile]').change(function () {
                $('#theBrowserConnectivityCsvFileTwitter').val($(this).val());
            });
            $('input[id=theBrowserCsvFile]').change(function () {
                $('#theBrowserCsvFileTwitter').val($(this).val());
            });
            $('input[id=thePageCsvFile]').change(function () {
                $('#thePageCsvFileTwitter').val($(this).val());
            });
            $('input[id=theHourOfDayCsvFile]').change(function () {
                $('#theHourOfDayCsvFileTwitter').val($(this).val());
            });
            $('input[id=defaultTimeToCsMappingCsvFile]').change(function () {
                $('#defaultTimeToCsMappingCsvFileVisible').val($(this).val());
            });
        };

        var initializeSomeControls = function(){
            $("#warnAboutOverwritingBox").hide();
            $("#errorBoxDefaultMappingCsv").hide();
            $("#defaultMappingUploadButton").prop("disabled", true);
            if (${showCsiWeights}) {
                $("#btn-csi-weights").click();
            } else {
                $("#btn-csi-mapping").click();
            }
        };

        var prepareConfigurationListAndCopy = function(){
            return copyCsiConfiguration(${csiConfigurations as grails.converters.JSON})
        };

        var osm = {};
        osm.actualCsiConfigurationId = ${selectedCsiConfiguration.ident()};

        var actualCsiConfigurationId = ${selectedCsiConfiguration.ident()};
        var actualCsiConfigurationLabel = '${selectedCsiConfiguration.label}';
        var allCsiConfigurations = ${csiConfigurations as grails.converters.JSON};

%{--        function refreshCsiConfigurationSwitchMenu() {

            if(allCsiConfigurations.length <= 1){
                $('#otherConfigsSubmenu').hide();
            }else {
                var listOfOtherCsiConfigurations = document.getElementById('csiConfigurationSwitchMenu');
                listOfOtherCsiConfigurations.innerHTML = "";
                allCsiConfigurations.forEach(function(csiConfig){
                    if(csiConfig.id != actualCsiConfigurationId){
                        var anchor = document.createElement('a');
                        anchor.addEventListener("click", function() {
                            changeCsiConfiguration(csiConfig.id);
                        });
                        anchor.innerHTML = csiConfig.label;
                        var li = document.createElement('li');
                        li.appendChild(anchor);
                        listOfOtherCsiConfigurations.appendChild(li);
                    }
                });
                $('#otherConfigsSubmenu').show();
            }

        }--}%

        $(document).ready(function () {

            createMatrixView(${matrixViewData}, "browserConnectivityMatrixView");
            createTreemap(1200, 750, ${treemapData}, "rect", "pageWeightTreemap");
            createBarChart(1000, 750, ${barchartData}, "clocks", "hoursOfDayBarchart");

            registerEventHandlers();

            initializeSomeControls();
            %{--refreshCsiConfigurationSwitchMenu();--}%

        });
        $( window ).load(function() {
            OpenSpeedMonitor.postLoader.loadJavascript(
                '<g:assetPath src="csi/configurationPost.js" absolute="true"/>',
                true
            )
        });

    </asset:script>
</content>
</body>
</html>
