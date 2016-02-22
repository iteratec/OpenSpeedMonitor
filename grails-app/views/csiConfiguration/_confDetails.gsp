<%@ page import="grails.converters.JSON" %>
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

    <g:if test="${!readOnly}">

        <g:if test="${errorMessagesCsi}">
            <ul>
                <g:each var="errMessage" in="${errorMessagesCsi ?}"><li class="text-error">${errMessage}</li></g:each>
            </ul>
        </g:if>

        <br/>
    </g:if>
    <div class="row">
        <div class="span12">
            <h3>
                <span class="muted"><g:message code="de.iteratec.isocsi.weight" default="Weight"/>:&nbsp;</span>
                <g:message code="de.iteratec.isocsi.browser_connectivity_weight" default="BrowserConnectivity"/>
            </h3>
        </div>
    </div>
    <g:if test="${!readOnly}">
        <button class="btn btn-link" onclick="callControllerActionWithId('${createLink(controller: 'csiConfigIO', action: 'downloadBrowserConnectivityWeights', absolute: true)}')">
                id="${selectedCsiConfiguration.ident()}">
            <g:message code="de.iteratec.isocsi.csi.csvdownload" default="CSV-Download"/>
        </button>

        <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
            <g:uploadForm controller="csiConfigIO" action="uploadBrowserConnectivityWeights">
                <input id="theBrowserConnectivityCsvFile" type="file" name="browserConnectivityCsv"
                       style="display:none">
                <input id="uploadBrowserConnectivityWeightsCsiConfigurationId" type="text" name="selectedCsiConfigurationId" value="${selectedCsiConfiguration.ident()}"
                       value="${selectedCsiConfiguration.ident()}"
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

    </g:if>
    <iteratec:matrixView
            chartIdentifier="browserConnectivityMatrixView"/>

    <hr/>

    <h3>
        <span class="muted"><g:message code="de.iteratec.isocsi.weight" default="Weight"/>:&nbsp;</span>
        <g:message code="de.iteratec.isocsi.page_weight" default="Page"/>
    </h3>
    <g:if test="${!readOnly}">
        <button class="btn btn-link" onclick="callControllerActionWithId('${createLink(controller: 'csiConfigIO', action: 'downloadPageWeights', absolute: true)}')">
            <g:message code="de.iteratec.isocsi.csi.csvdownload" default="CSV-Download"/>
        </button>
        <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
            <g:uploadForm controller="csiConfigIO" action="uploadPageWeights">
                <input id="thePageCsvFile" type="file" name="pageCsv" style="display:none">
                <input id="uploadPageWeightsCsiConfigurationId" type="text" name="selectedCsiConfigurationId" value="${selectedCsiConfiguration.ident()}"
                       value="${selectedCsiConfiguration.ident()}"
                       style="display:none">

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

    </g:if>
    <iteratec:treemap
            chartIdentifier="pageWeightTreemap"/>

    <hr/>

    <h3>
        <span class="muted"><g:message code="de.iteratec.isocsi.weight" default="Weight"/>:&nbsp;</span>
        <g:message code="de.iteratec.isocsi.hour_weight" default="Tageszeit"/>
    </h3>
    <g:if test="${!readOnly}">
        <button class="btn btn-link" onclick="callControllerActionWithId('${createLink(controller: 'csiConfigIO', action: 'downloadHourOfDayWeights', absolute: true)}')">
            <g:message code="de.iteratec.isocsi.csi.csvdownload" default="CSV-Download"/>
        </button>
        <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
            <g:uploadForm controller="csiConfigIO" action="uploadHourOfDayWeights">
                <input id="theHourOfDayCsvFile" type="file" name="hourOfDayCsv" style="display:none">
                <input id="uploadHourOfDayWeightsCsiConfigurationId" type="text" name="selectedCsiConfigurationId" value="${selectedCsiConfiguration.ident()}"
                       value="${selectedCsiConfiguration.ident()}"
                       style="display:none">

                <div class="input-append">
                    <label>
                        <g:message code="de.iteratec.ism.label.upload_default_mappings"
                                   default="Neue Default-Mappings hochladen (CSV)"/>
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
    </g:if>
    <iteratec:barChart
            chartIdentifier="hoursOfDayBarchart"/>

    <br/>
</div>
<%-- csi mapping ---------------------------------------------------------------------------%>

<div id="csi-mapping">

    <g:if test="${!readOnly}">

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
    </g:if>
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


    %{--Page Mappings--}%
    <div class="row">
        <div class="span12">
            <span class="inline">
                <span class="text-info">
                    <strong><g:message code="de.iteratec.osm.csiConfiguration.pageMappingsHeadline"
                                       default="PageMappings"/></strong>
                </span>
                &nbsp;-&nbsp;<g:message code="de.iteratec.osm.csi.mapping.defaults.pageMappingsExplanation"
                                        default="These Mappings are assigned to pages"/>
            </span>
        </div>
    </div>
    <g:if test="${pageTimeToCsMappings}">
        <div class="row">
            <div class="span12">
                <g:render template="/chart/csi-mappings"
                          model="${['chartData'        : pageTimeToCsMappings, 'chartIdentifier': 'page_csi_mappings',
                                    'bottomOffsetXAxis': 364, 'yAxisRightOffset': 44, 'chartBottomOffset': 250,
                                    'yAxisTopOffset'   : 8, 'bottomOffsetLegend': 220, 'modal': false]}"/>
            </div>
        </div>
        <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
            <div class="row-fluid">
                <div class="span12">
                    <div class="row-fluid">
                        <div class="span2">
                            <button href="#" type="button" class="btn btn-primary" style="display: none;"
                                    id="removePageMapping"
                                    onclick="removeSelectedPageMapping('${createLink(controller: 'csiConfiguration', action: 'removePageMapping', absolute: true)}',
                                            actualCsiConfigurationId);">
                                <g:message code="de.iteratec.osm.csi.configuration.pagemapping.remove.label"
                                           default="Remove Mapping"/>
                            </button>
                        </div>

                        <div class="span10" id="page-mapping-deletions"></div>
                    </div>
                </div>
            </div>
        </sec:ifAllGranted>
        <asset:script type="text/javascript">
            var legendEntryClickCallback = function(nameOfClickedLegendEntry){
                var btnRemovePageMapping = $('#removePageMapping');
                if(nameOfClickedLegendEntry != undefined && btnRemovePageMapping){
                    btnRemovePageMapping.show();
                }else if(btnRemovePageMapping){
                    btnRemovePageMapping.hide();
                }
            };
            var graphData = ${pageTimeToCsMappings};
            var pageMappingDiagram = createMultiLineGraph(graphData, 'page_csi_mappings', true, null, legendEntryClickCallback);
        </asset:script>
    </g:if>
    <g:else>
        <div class="row">
            <div class="span12">
                <h5><g:message code="de.iteratec.osm.csiConfiguration.noPageMappings"
                               default="Keine Mappings vorhanden."/></h5>
            </div>
        </div>
    </g:else>

%{--Default Mappings--}%
    <g:if test="${showDefaultMappings}">

        <div class="row">
            <div class="span12">
                <hr>
                <span class="inline">
                    <span class="text-info">
                        <strong><g:message code="de.iteratec.osm.default.heading" default="Defaults"/></strong>
                    </span>
                    &nbsp;-&nbsp;<g:message code="de.iteratec.osm.csi.mapping.defaults.explanation"
                                            default="These Mappings can be assigned to pages"/>
                </span>
            </div>
        </div>
        <g:set var="defaultIdentifier" value='default_csi_mappings'/>
        <g:render template="/chart/csi-mappings"
                  model="${['chartData'        : defaultTimeToCsMappings, 'chartIdentifier': defaultIdentifier,
                            'bottomOffsetXAxis': 364, 'yAxisRightOffset': 44, 'chartBottomOffset': 250,
                            'yAxisTopOffset'   : 8, 'bottomOffsetLegend': 220, 'modal': false]}"/>
        <asset:script type="text/javascript">
            defaultGraphObject = createMultiLineGraph(${defaultTimeToCsMappings}, "${defaultIdentifier}", true, null, null);
        </asset:script>

        <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
            <g:set var="customDefaultCsiMappingDeletePrefix" value='DeleteDefaultCsiMapping'/>
            <div class="span6" id="defaultMultilineGraphButtonLine">
                <g:if test="${!readOnly}">
                    <button type="button" class="btn btn-small btn-primary" data-toggle="modal" href="#CsiMappingModal"
                            disabled="true"
                            id="btn-apply-mapping" onclick="showMappingDialog()">
                        <g:message code="de.iteratec.osm.csiConfiguration.applyMapping"
                                   default="Delete Default Mapping"/></button>
                    <button type="button" class="btn btn-small btn-danger" data-toggle="modal"
                            href="#DeleteModal${customDefaultCsiMappingDeletePrefix}" disabled="true"
                            id="btn-delete-default">
                        <g:message code="de.iteratec.osm.csiConfiguration.deleteDefaultCsiConfiguration"
                                   default="Delete Default Mapping"/></button>
                    <asset:script type="text/javascript">
                        $(document).ready(function(){
                                $("#${defaultIdentifier}").find(".diagramKey").click(defaultSelectChange);
                    });
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
                    </asset:script>
                </g:if>
            </div>
            <style>
            #select-default {
                margin-top: 10px;
            }
            </style>
            <g:render template="/_common/modals/deleteDialogCustomAction"
                      model="[itemLabel: message(code: 'de.iteratec.osm.csi.DefaultTimeToCsMapping.label'), actionName: 'deleteDefaultCsiMapping', customPrefix: customDefaultCsiMappingDeletePrefix, customID: 'name', customController: 'CsiConfiguration']"/>
            <g:if test="${!readOnly}">
                <g:render template="/_common/modals/chooseCsiMapping"
                          model="[defaultMultiLineChart: defaultTimeToCsMappings, pages: pages, pageData: pageTimeToCsMappings]"/>
            </g:if>
        </sec:ifAllGranted>

        <g:if test="${!readOnly}">
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
                                    <g:message code="de.iteratec.ism.label.upload_default_mappings"
                                               default="Neue Default-Mappings hochladen (CSV)"/>
                                </label>
                                <input id="defaultTimeToCsMappingCsvFileVisible" class="input-large" type="text">
                                <a class="btn" onclick="$('input[id=defaultTimeToCsMappingCsvFile]').click();">
                                    <g:message code="de.iteratec.ism.browse_file_system" default="Durchsuchen"/>
                                </a>
                                <button type="submit" class="btn" id="defaultMappingUploadButton"
                                        onclick="showSpinner()">
                                    <g:message code="de.iteratec.isocsi.upload_file" default="Hochladen"/>
                                </button>
                            </div>
                        </g:uploadForm>
                    </sec:ifAllGranted>
                </div>
            </div>
        </g:if>
    </g:if>
</div>
