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

%{--container for errors --}%
<div class="alert alert-error" id="errorDeletingCsiConfiguration">
    <strong><g:message
            code="de.iteratec.osm.csiConfiguration.deleteErrorTitle"/></strong>

    <p id="deletingCsiConfiguratioinErrors">
    </p>

</div>

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
                    <li><a id="button_${conf}" onclick="changeCsiConfiguration(this.getAttribute('value'))"
                           value="${conf[0]}">${conf[1]}</a>
                        %{--onclick="filterJobSet('${jobSet.name}', '${jobSet.jobs*.toString()}')">${jobSet.name}</a>--}%
                    </li>
                </g:each>
            </ul>
        </div>
    </div>
</div>

<hr>

<g:render template="weightDetails" model="[readOnly               : false,
                                           errorMessagesCsi       : errorMessagesCsi,
                                           defaultTimeToCsMappings: defaultTimeToCsMappings]"></g:render>

<div class="row">
    <div class="span12">
        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_SUPER_ADMIN">
            <div class="btn-group">
                <g:form method="post" role="form" class="form-horizontal">
                    <g:hiddenField name="sourceCsiConfigLabel" value="${selectedCsiConfiguration}"/>
                    <g:hiddenField name="label" value="${selectedCsiConfiguration}"/>

                    <g:actionSubmit class="btn btn-small btn-primary"
                                    value="${message(code: 'de.iteratec.osm.csiConfiguration.saveAs', default: 'Copy')}"
                                    onclick="return createCsiConfiguration('${message(code: 'de.iteratec.osm.csiConfiguration.nameAlreadyExists', default: 'name already exists')}')"
                                    action="saveCopy"/>

                    <g:actionSubmit class="btn btn-small btn-danger"
                                    value="${message(code: 'de.iteratec.osm.csiConfiguration.deleteCsiConfiguration', args: [selectedCsiConfiguration], default: 'Delete')}"
                                    action="deleteCsiConfiguration"
                                    onclick="return validateDeleting('${selectedCsiConfiguration}',
                                    '${message(code: 'de.iteratec.osm.csiConfiguration.sureDelete', args: [selectedCsiConfiguration], default: 'delete?')}',
                                    '${message(code: 'de.iteratec.osm.csiConfiguration.overwriteWarning', default: 'Overwriting')}')"/>

                </g:form>
            </div>
        </sec:ifAnyGranted>
    </div>
</div>


<%-- include bottom ---------------------------------------------------------------------------%>

<content tag="include.bottom">
    <asset:javascript src="d3/matrixView.js"/>
    <asset:javascript src="d3/barChart.js"/>
    <asset:javascript src="d3/treemap.js"/>
    <asset:javascript src="csidashboard/defaultMappingCsvValidator.js"/>
    <asset:javascript src="csidashboard/deleteCsiConfigValidation.js"/>
    <asset:script type="text/javascript">

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
        var registerEventHandlers = function () {

            registerEventHandlersForFileUploadControls();

            $("#btn-csi-mapping").click(function () {
                $('#csi-mapping').show();
                $('#csi-weights').hide();
            });
            $("#btn-csi-weights").click(function () {
                $('#csi-mapping').hide();
                $('#csi-weights').show();
            });

        };

        $(document).ready(function () {
            createMatrixView(${matrixViewData}, "browserConnectivityMatrixView");
            createTreemap(1200, 750, ${treemapData}, "rect", "pageWeightTreemap");
            createBarChart(1000, 750, ${barchartData}, "clocks", "hoursOfDayBarchart");

            registerEventHandlers();

            $("#errorDeletingCsiConfiguration").hide();
            $("#warnAboutOverwritingBox").hide();
            $("#errorBoxDefaultMappingCsv").hide();
            $("#defaultMappingUploadButton").prop("disabled", true);
            if (${showCsiWeights}) {
                $("#btn-csi-weights").click();
            } else {
                $("#btn-csi-mapping").click();
            }

        });

        $('#defaultTimeToCsMappingCsvFile').bind('change', function () {
            $("#warnAboutOverwritingBox").hide();
            $("#errorBoxDefaultMappingCsv").hide();
            $("#defaultMappingUploadButton").prop("disabled", true);

            validateDefaultMappingCsv(this.files[0])
        });

        function showSpinner() {
            var spinner = startSpinner(document.getElementById('spinner-position'));
            return true;
        }

        function startSpinner(spinnerElement) {
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

        function changeCsiConfiguration(id) {
            window.location.href = "http://localhost:8080/OpenSpeedMonitor/csiDashboard/weights/" + id;
        }

        function validateDeleting(label, sureDeleteMessage, overwriteWarningMessage) {
            $("#errorDeletingCsiConfiguration").hide();

            return validatedDeletion(label, sureDeleteMessage, overwriteWarningMessage);
        }

        function createCsiConfiguration(nameExistsErrorMessage) {
            var creatingOkay = POSTLOADED.promptForDuplicateName();
            var input = $('input#label').val();

            var csiConfiguration = ${csiConfigurations as grails.converters.JSON};

            for (var i = 0; i < csiConfiguration.length; i++) {
                var config = csiConfiguration[i];
                var configName = config[1];
                if (configName == input) {
                    alert(nameExistsErrorMessage);
                    creatingOkay = false;
                }
            }

            return creatingOkay;
        }

    </asset:script>
</content>
</body>
</html>
