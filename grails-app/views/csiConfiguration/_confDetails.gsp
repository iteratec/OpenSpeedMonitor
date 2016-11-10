<%@ page import="grails.converters.JSON" %>
<%@ defaultCodec="none" %>


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


<div class="tab-content">
    <div id="csiMappingDetailsTabContent" class="tab-pane active">
        <h3 class="section">
            <span class="text-muted"><g:message code="de.iteratec.osm.csi.mapping.label"
                                                default="Mapping"/>:&nbsp;</span>
            <g:message code="de.iteratec.osm.csi.configuration.mapping.heading"
                       default="Mapping Load time&nbsp;&rArr;&nbsp;Customer satisfaction"/>
        </h3>

        <div class="row">
            <g:if test="${params.controller != 'jobGroup'}">
                <div class="col-md-2">
                    <ul class="nav nav-pills nav-stacked">
                        <li class="active">
                            <a data-toggle="tab" href="#csiAssignedMappingTabContent">
                                Assigned Mappings
                            </a>
                        </li>
                        <li>
                            <a data-toggle="tab" href="#csiAvailableMappingTabContent">
                                Available Mappings
                            </a>
                        </li>
                    </ul>
                </div>
            </g:if>

            <div class="col-md-10">
                <div class="tab-content">
                    %{-- Assigned Mappings --}%
                    <div id="csiAssignedMappingTabContent" class="tab-pane active">
                        <g:if test="${pageMappingsExist}">
                            <div class="row section">
                                <div class="col-md-12">
                                    <span class="inline">
                                        <span class="text-info">
                                            <strong><g:message
                                                    code="de.iteratec.osm.csiConfiguration.pageMappingsHeadline"
                                                    default="PageMappings"/></strong>
                                        </span>
                                        &nbsp;-&nbsp;
                                        <g:message code="de.iteratec.osm.csi.mapping.defaults.pageMappingsExplanation"
                                                   default="These Mappings are assigned to pages"/>
                                    </span>
                                </div>
                            </div>

                            <div class="row">
                                %{-- this chart is rendered in a class="col-md-8". see D3HtmlCreator.groovy --}%
                                <g:render template="/chart/csi-mappings"
                                              model="${['chartData'        : pageTimeToCsMappings, 'chartIdentifier': 'page_csi_mappings',
                                                        'bottomOffsetXAxis': 364, 'yAxisRightOffset': 44, 'chartBottomOffset': 250,
                                                        'yAxisTopOffset'   : 8, 'bottomOffsetLegend': 220, 'modal': false]}"/>
                                <div class="col-md-4">
                                    <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
                                        <button href="#" type="button" class="btn btn-primary"
                                                style="display: none;"
                                                id="removePageMapping"
                                                onclick="removeSelectedPageMapping('${createLink(controller: 'csiConfiguration', action: 'removePageMapping', absolute: true)}',
                                                        actualCsiConfigurationId);">
                                            <g:message
                                                    code="de.iteratec.osm.csi.configuration.pagemapping.remove.label"
                                                    default="Remove Mapping"/>
                                        </button>
                                        <div id="page-mapping-deletions"></div>
                                    </sec:ifAllGranted>
                                </div>
                            </div>
                        </g:if>
                        <g:else>
                            <div class="row">
                                <div class="col-md-12">
                                    <h5><g:message code="de.iteratec.osm.csiConfiguration.noPageMappings"
                                                   default="Keine Mappings vorhanden."/></h5>
                                </div>
                            </div>
                        </g:else>
                    </div>
                    %{-- Default Mappings --}%
                    <div id="csiAvailableMappingTabContent" class="tab-pane">
                        <g:if test="${showDefaultMappings}">
                            <div class="row section">
                                <div class="col-md-12">
                                    <span class="inline">
                                        <span class="text-info">
                                            <strong><g:message code="de.iteratec.osm.default.heading"
                                                               default="Defaults"/></strong>
                                        </span>
                                        &nbsp;-&nbsp;
                                        <g:message code="de.iteratec.osm.csi.mapping.defaults.explanation"
                                                   default="These Mappings can be assigned to pages"/>
                                    </span>
                                </div>
                            </div>

                            <g:set var="defaultIdentifier" value='default_csi_mappings'/>

                            <div class="row">
                                %{-- this chart is rendered in a class="col-md-8". see D3HtmlCreator.groovy --}%
                                <g:render template="/chart/csi-mappings"
                                          model="${['chartData'        : defaultTimeToCsMappings, 'chartIdentifier': defaultIdentifier,
                                                    'bottomOffsetXAxis': 364, 'yAxisRightOffset': 44, 'chartBottomOffset': 250,
                                                    'yAxisTopOffset'   : 8, 'bottomOffsetLegend': 220, 'modal': false]}"/>
                                <asset:script type="text/javascript">
                                    defaultGraphObject = createMultiLineGraph(${defaultTimeToCsMappings}, "${defaultIdentifier}", true, null, null);
                                </asset:script>
                                <div class="col-md-4">
                                    <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
                                        <g:uploadForm controller="csiConfigIO"
                                                      action="uploadDefaultTimeToCsMappings">
                                            <input id="defaultTimeToCsMappingCsvFile" type="file"
                                                   name="defaultTimeToCsMappingCsv"
                                                   style="display:none">

                                            <label for="defaultTimeToCsMappingCsvFileVisible">
                                                <g:message
                                                        code="de.iteratec.ism.label.upload_default_mappings"
                                                        default="Neue Default-Mappings hochladen (CSV)"/>
                                            </label>
                                            <div class="input-group">
                                                <span class="input-group-btn">
                                                    <a class="btn btn-default"
                                                       onclick="$('input[id=defaultTimeToCsMappingCsvFile]').click();">
                                                        <i class="fa fa-folder-open-o"
                                                           aria-hidden="true"></i>
                                                    </a>
                                                </span>
                                                <input id="defaultTimeToCsMappingCsvFileVisible"
                                                       class="form-control" type="text">
                                                <span class="input-group-btn">
                                                    <button type="submit" class="btn btn-default"
                                                            id="defaultMappingUploadButton"
                                                            onclick="document.getElementById('copyCsiConfigurationSpinner').appendChild(POSTLOADED.getLargeSpinner().el);">
                                                        <g:message code="de.iteratec.isocsi.upload_file"
                                                                   default="Upload"/>
                                                    </button>
                                                </span>
                                            </div>
                                        </g:uploadForm>
                                    </sec:ifAllGranted>
                                </div>
                            </div>

                            <div class="row">
                                <g:if test="${!readOnly}">
                                    <button class="btn btn-primary"
                                            onclick="callControllerActionWithId('${createLink(controller: 'csiConfigIO', action: 'downloadDefaultTimeToCsMappings', absolute: true)}')">
                                        <g:message code="de.iteratec.isocsi.csi.csvdownload" default="CSV-Download"/>
                                    </button>
                                </g:if>
                                <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
                                    <g:set var="customDefaultCsiMappingDeletePrefix" value='DeleteDefaultCsiMapping'/>
                                    <g:if test="${!readOnly}">
                                        <button type="button" class="btn btn-primary" data-toggle="modal"
                                                href="#CsiMappingModal"
                                                disabled="true"
                                                id="btn-apply-mapping" onclick="showMappingDialog()">
                                            <g:message code="de.iteratec.osm.csiConfiguration.applyMapping"
                                                       default="Apply Mapping"/></button>
                                        <button type="button" class="btn btn-danger" data-toggle="modal"
                                                href="#DeleteModal${customDefaultCsiMappingDeletePrefix}"
                                                disabled="true"
                                                id="btn-delete-default">
                                            <g:message
                                                    code="de.iteratec.osm.csiConfiguration.deleteDefaultCsiConfiguration"
                                                    default="Delete Default Mapping"/></button>
                                    </g:if>
                                    <g:render template="/_common/modals/deleteDialogCustomAction"
                                              model="[itemLabel: message(code: 'de.iteratec.osm.csi.DefaultTimeToCsMapping.label'), actionName: 'deleteDefaultCsiMapping', customPrefix: customDefaultCsiMappingDeletePrefix, customID: 'name', customController: 'CsiConfiguration']"/>
                                    <g:if test="${!readOnly}">
                                        <g:render template="/_common/modals/chooseCsiMapping"
                                                  model="[defaultMultiLineChart: defaultTimeToCsMappings, pages: pages, pageData: pageTimeToCsMappings ?: '[]']"/>
                                    </g:if>
                                </sec:ifAllGranted>
                            </div>
                        </g:if>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="csiConnectivityWeightDetailsTabContent" class="tab-pane">
        <h3 class="section">
            <span class="text-muted"><g:message code="de.iteratec.isocsi.weight"
                                                default="Weight"/>:&nbsp;</span>
            <g:message code="de.iteratec.isocsi.browser_connectivity_weight"
                       default="BrowserConnectivity"/>
        </h3>

        <g:if test="${!readOnly}">
            <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
                <div class="row section">
                    <div class="col-md-4">
                        <g:uploadForm controller="csiConfigIO" action="uploadBrowserConnectivityWeights">
                            <input id="theBrowserConnectivityCsvFile" type="file" name="browserConnectivityCsv"
                                   style="display:none">
                            <input id="uploadBrowserConnectivityWeightsCsiConfigurationId" type="text"
                                   name="selectedCsiConfigurationId"
                                   value="${selectedCsiConfiguration.ident()}"
                                   style="display:none">


                            <label for="theBrowserConnectivityCsvFileTwitter">
                                <g:message code="de.iteratec.ism.label.upload_new_browser_connectivity_weights"
                                           default="Upload new browser connectivity weights (CSV):"/>
                            </label>

                            <div class="input-group">
                                <span class="input-group-btn">
                                    <a class="btn btn-default"
                                       onclick="$('input[id=theBrowserConnectivityCsvFile]').click();">
                                        <i class="fa fa-folder-open-o" aria-hidden="true"></i>
                                    </a>
                                </span>
                                <input id="theBrowserConnectivityCsvFileTwitter" class="form-control" type="text">
                                <span class="input-group-btn">
                                    <button type="submit" class="btn btn-default">
                                        <g:message code="de.iteratec.isocsi.upload_file" default="Upload"/>
                                    </button>
                                </span>
                            </div>
                        </g:uploadForm>
                    </div>
                </div>
            </sec:ifAllGranted>
        </g:if>

        <iteratec:matrixView chartIdentifier="browserConnectivityMatrixView"/>

        <g:if test="${!readOnly}">
            <button class="btn btn-primary"
                    onclick="callControllerActionWithId('${createLink(controller: 'csiConfigIO', action: 'downloadBrowserConnectivityWeights', absolute: true)}')">
                <g:message code="de.iteratec.isocsi.csi.csvdownload" default="CSV-Download"/>
            </button>
        </g:if>
    </div>

    <div id="csiPageWeightDetailsTabContent" class="tab-pane">
        <h3 class="section">
            <span class="text-muted"><g:message code="de.iteratec.isocsi.weight"
                                                default="Weight"/>:&nbsp;</span>
            <g:message code="de.iteratec.isocsi.page_weight" default="Page"/>
        </h3>

        <g:if test="${!readOnly}">
            <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
                <g:uploadForm controller="csiConfigIO" action="uploadPageWeights">
                    <input id="thePageCsvFile" type="file" name="pageCsv" style="display:none">
                    <input id="uploadPageWeightsCsiConfigurationId" type="text"
                           name="selectedCsiConfigurationId"
                           value="${selectedCsiConfiguration.ident()}"
                           style="display:none">

                    <div class="row">
                        <div class="col-md-4 section">
                            <label for="thePageCsvFileTwitter">
                                <g:message code="de.iteratec.ism.label.upload_new_page_weights"
                                           default="Upload new page weights (CSV):"/>
                            </label>

                            <div class="input-group">
                                <span class="input-group-btn">
                                    <a class="btn btn-default" onclick="$('input[id=thePageCsvFile]').click();">
                                        <i class="fa fa-folder-open-o" aria-hidden="true"></i>
                                    </a>
                                </span>
                                <input id="thePageCsvFileTwitter" class="form-control" type="text">
                                <span class="input-group-btn">
                                    <button type="submit" class="btn btn-default">
                                        <g:message code="de.iteratec.isocsi.upload_file" default="Hochladen"/>
                                    </button>
                                </span>
                            </div>
                        </div>
                    </div>
                </g:uploadForm>
            </sec:ifAllGranted>
        </g:if>

        <iteratec:treemap chartIdentifier="pageWeightTreemap"/>

        <g:if test="${!readOnly}">
            <button class="btn btn-primary"
                    onclick="callControllerActionWithId('${createLink(controller: 'csiConfigIO', action: 'downloadPageWeights', absolute: true)}')">
                <g:message code="de.iteratec.isocsi.csi.csvdownload" default="CSV-Download"/>
            </button>
        </g:if>

    </div>

    <div id="csiTimeWeightDetailsTabContent" class="tab-pane">
        <h3 class="section">
            <span class="text-muted"><g:message code="de.iteratec.isocsi.weight" default="Weight"/>:&nbsp;</span>
            <g:message code="de.iteratec.isocsi.hour_weight" default="Hour of day"/>
        </h3>

        <g:if test="${!readOnly}">
            <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
                <g:uploadForm controller="csiConfigIO" action="uploadHourOfDayWeights">
                    <input id="theHourOfDayCsvFile" type="file" name="hourOfDayCsv" style="display:none">
                    <input id="uploadHourOfDayWeightsCsiConfigurationId" type="text" name="selectedCsiConfigurationId"
                           value="${selectedCsiConfiguration.ident()}"
                           style="display:none">

                    <div class="row">
                        <div class="col-md-4 section">
                            <label for="theHourOfDayCsvFileTwitter">
                                <g:message code="de.iteratec.ism.label.upload_default_mappings"
                                           default="Neue Default-Mappings hochladen (CSV)"/>
                            </label>

                            <div class="input-group">
                                <span class="input-group-btn">
                                    <a class="btn btn-default"
                                       onclick="$('input[id=theHourOfDayCsvFile]').click();">
                                        <i class="fa fa-folder-open-o" aria-hidden="true"></i>
                                    </a>
                                </span>
                                <input id="theHourOfDayCsvFileTwitter" class="form-control" type="text">
                                <span class="input-group-btn">
                                    <button type="submit" class="btn btn-default">
                                        <g:message code="de.iteratec.isocsi.upload_file" default="Upload"/>
                                    </button>
                                </span>
                            </div>
                        </div>
                    </div>
                </g:uploadForm>
            </sec:ifAllGranted>
        </g:if>

        <iteratec:barChart chartIdentifier="hoursOfDayBarchart"/>

        <g:if test="${!readOnly}">
            <button class="btn btn-primary"
                    onclick="callControllerActionWithId('${createLink(controller: 'csiConfigIO', action: 'downloadHourOfDayWeights', absolute: true)}')">
                <g:message code="de.iteratec.isocsi.csi.csvdownload" default="CSV-Download"/>
            </button>
        </g:if>
    </div>
</div>

<content tag="include.bottom">
    <asset:script type="text/javascript">
        var legendEntryClickCallback = function(nameOfClickedLegendEntry){
            var btnRemovePageMapping = $('#removePageMapping');
            if (nameOfClickedLegendEntry != undefined && btnRemovePageMapping){
                btnRemovePageMapping.show();
            } else if (btnRemovePageMapping){
                btnRemovePageMapping.hide();
            }
        };
        var graphData = ${pageTimeToCsMappings};
        var pageMappingDiagram = createMultiLineGraph(graphData, 'page_csi_mappings', true, null, legendEntryClickCallback);

        function defaultSelectChange(){
                var possibleChosen = d3.select("#${defaultIdentifier}").select("[chosen=true]");
                $('#btn-delete-default').prop('disabled', possibleChosen[0][0] == null);
                $('#btn-apply-mapping').prop('disabled', possibleChosen[0][0] == null);
                changeValueToDelete($(this).find("text").html(), '${customDefaultCsiMappingDeletePrefix}');
        }

        function showMappingDialog(){
            var chosen = d3.select("${defaultIdentifier}").selectAll(".diagramKey").select("")
            showPageSelect(defaultGraphObject.getSelectedName(), defaultGraphObject.getColorForName(defaultGraphObject.getSelectedName()));
        }

        $(document).ready(function () {

            createMatrixView(${matrixViewData}, "browserConnectivityMatrixView");
            createTreemap(1200, 750, ${treemapData}, "rect", "pageWeightTreemap");
            createBarChart(1000, 750, ${barchartData}, "clocks", "hoursOfDayBarchart");

            $("#${defaultIdentifier}").find(".diagramKey").click(defaultSelectChange);
        });
    </asset:script>
</content>