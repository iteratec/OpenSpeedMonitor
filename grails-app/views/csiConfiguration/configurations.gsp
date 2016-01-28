<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; grails.converters.JSON" contentType="text/html;charset=UTF-8" %>
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
    #defaultMultilineGraphButtonLine{
        margin-bottom: 30px;
    }
    </style>
</head>

<body>
<%-- main menu ---------------------------------------------------------------------------%>
<g:render template="/layouts/mainMenu"/>

%{--container for errors --}%
<div class="alert alert-error" id="errorDeletingCsiConfiguration" style="display: none">
    <strong>
        <g:message code="de.iteratec.osm.csiConfiguration.deleteErrorTitle"/>
    </strong>
    <p id="deletingCsiConfiguratioinErrors"></p>
</div>

<div class="row">

    %{--Name and description of actual config----------------------------------------------}%
    <div class="span8">
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

    %{--dropdown button----------------------------------------------}%
    <div class="span2 offset1">

        <g:if test="${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_SUPER_ADMIN") || csiConfigurations.size()>1}">

            <div class="btn-group pull-left">
                <button class="btn btn-small btn-info dropdown-toggle text-right" data-toggle="dropdown">
                    <g:message code="de.iteratec.osm.csi.configuration.messages.actual-configuration" default="This Configuration..."></g:message>
                    <span class="caret"></span>
                </button>
                <ul class="dropdown-menu">

                    %{--features for actual configuration----------------------------}%
                    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_SUPER_ADMIN">
                        <li>
                            <a href="${createLink(absolute: true, controller: 'csiConfiguration', action: 'saveCopy')}"
                               onclick="return promptForNewName(this, '${message(code: 'de.iteratec.osm.csiConfiguration.nameAlreadyExists', default: 'Name already exists')}')"
                               disabled="disabled">
                                <i class="fa fa-copy"></i>&nbsp;${message(code: 'de.iteratec.osm.csiConfiguration.saveAs', default: 'Copy')}
                            </a>
                        </li>
                        <li>
                            <a href="${createLink(absolute: true, controller: 'csiConfiguration', action: 'deleteCsiConfiguration',
                                    params: [sourceCsiConfigLabel: selectedCsiConfiguration.label, label: selectedCsiConfiguration.label])}"
                               onclick="return validateDeleting('${selectedCsiConfiguration.label}',
                                       '${message(code: 'de.iteratec.osm.csiConfiguration.sureDelete', args: [selectedCsiConfiguration.label], default: 'delete?')}',
                                       '${message(code: 'de.iteratec.osm.csiConfiguration.overwriteWarning', default: 'Overwriting')}')">
                                <i class="fa fa-remove"></i>&nbsp;${message(code: 'de.iteratec.osm.csi.ui.delete.label', args: [selectedCsiConfiguration.label], default: 'delete')}
                            </a>
                        </li>
                    </sec:ifAnyGranted>

                    %{--submenu to show other configurations----------------------------}%
                    <g:if test="${csiConfigurations.size()>1}">
                        <li class="dropdown-submenu">
                            <a tabindex="-1" href="#">
                                <i class="fa fa-share-square-o"></i>&nbsp;<g:message
                                    code="de.iteratec.osm.csi.configuration.messages.select-different" default="leave"/>
                            </a>
                            <ul class="dropdown-menu">
                                <g:each in="${csiConfigurations.findAll{it[0]!=selectedCsiConfiguration.ident()}}" var="conf">
                                    <li>
                                        <a id="button_${conf}" onclick="changeCsiConfiguration(this.getAttribute('value'))" value="${conf[0]}">
                                            <g:message code="de.iteratec.osm.csi.ui.show.label" args="${[conf[1]]}" default="show ${conf[1]}"/>
                                        </a>
                                    </li>
                                </g:each>
                            </ul>
                        </li>
                    </g:if>
                </ul>
            </div>

        </g:if>

    </div>
</div>

%{--mapping and weights details----------------------------------------------}%
<g:render template="confDetails" model="[readOnly               : false,
                                           errorMessagesCsi       : errorMessagesCsi,
                                           defaultTimeToCsMappings: defaultTimeToCsMappings]" />

%{--initially invisible modal dialog to update csi configuratuion via ajax---------------}%
<g:render template="/_common/modals/csi/updateCsiConfiguration"/>

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

            $('#updateCsiConfModal').on('shown', function () {
                $('#confLabelFromModal').val( $('#headerCsiConfLabel').text() );
                $('#confDescriptionFromModal').val( $('#headerCsiConfDescription').text() );
                $('#updatingCsiConfigurationErrors').text('');
                $('#errorUpdatingCsiConfiguration').hide();
            })

        };

        $(document).ready(function () {
            actualCsiConfigurationId = ${selectedCsiConfiguration.ident()};
            createMatrixView(${matrixViewData}, "browserConnectivityMatrixView");
            createTreemap(1200, 750, ${treemapData}, "rect", "pageWeightTreemap");
            createBarChart(1000, 750, ${barchartData}, "clocks", "hoursOfDayBarchart");

            registerEventHandlers();

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
            window.location.href="<g:createLink action="configurations" absolute="true"/>/"+id;
        }

        function validateDeleting(label, sureDeleteMessage, overwriteWarningMessage) {
            $("#errorDeletingCsiConfiguration").hide();

            return validatedDeletion(label, sureDeleteMessage, overwriteWarningMessage);
        }

        /**
         * Asks for label of new csi config. If label is empty or a config with that label already exists
         * link will be broken (this method delivers false).
         * Otherwise previous and new label is added to links href before it can be followed.
         * @param anchor
         *          Anchor this function is called from (onclick handler).
         * @param nameExistsErrorMessage
         *          Internationalized error message.
         * @returns {boolean}
         *          True if new label chosen by user is ok, False otherwise.
         */
        function promptForNewName(anchor, nameExistsErrorMessage) {

            var actualLabel = $('#headerCsiConfLabel').text();
            var newName = prompt(
                    POSTLOADED.i18n_duplicatePrompt,
                    actualLabel + POSTLOADED.i18n_duplicateSuffix
            );
            if (newName === null || newName === '') {
                return false;
            }

            var csiConfigurations = ${csiConfigurations as grails.converters.JSON};

            for (var i = 0; i < csiConfigurations.length; i++) {
                var config = csiConfigurations[i];
                var configName = config[1];
                if (configName == newName) {
                    alert(nameExistsErrorMessage);
                    return false;
                }
            }

            anchor.href += '?'+'label='+newName+'&sourceCsiConfigLabel='+actualLabel
            return true;
        }

    </asset:script>
</content>
</body>
</html>
