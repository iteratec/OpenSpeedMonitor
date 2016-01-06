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

    </g:if>
    <iteratec:matrixView
            chartIdentifier="browserConnectivityMatrixView"/>

    <hr/>

    <h3>
        <span class="muted"><g:message code="de.iteratec.isocsi.weight" default="Weight"/>:&nbsp;</span>
        <g:message code="de.iteratec.isocsi.page_weight" default="Page"/>
    </h3>
    <g:if test="${!readOnly}">
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

    </g:if>
    <iteratec:treemap
            chartIdentifier="pageWeightTreemap"/>

    <hr/>

    <h3>
        <span class="muted"><g:message code="de.iteratec.isocsi.weight" default="Weight"/>:&nbsp;</span>
        <g:message code="de.iteratec.isocsi.hour_weight" default="Tageszeit"/>
    </h3>
    <g:if test="${!readOnly}">
        <g:link controller="csiConfigIO" action="downloadHourOfDayWeights">
            <g:message code="de.iteratec.isocsi.csi.csvdownload" default="CSV-Download"/>
        </g:link>
        <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
            <g:uploadForm controller="csiConfigIO" action="uploadHourOfDayWeights">
                <input id="theHourOfDayCsvFile" type="file" name="hourOfDayCsv" style="display:none">

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
                            <button type="submit" class="btn" id="defaultMappingUploadButton" onclick="showSpinner()">
                                <g:message code="de.iteratec.isocsi.upload_file" default="Hochladen"/>
                            </button>
                        </div>
                    </g:uploadForm>
                </sec:ifAllGranted>
            </div>
        </div>

    </g:if>
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
    <div>
        <g:set var="defaultIdentifier" value='default_csi_mappings'/>
       <sec:ifAllGranted roles="ROLE_SUPER_ADMIN">
            <g:select from="${JSON.parse(defaultTimeToCsMappings.toString()).lines.collect{it.name}}"
                      name="selectedDefaultMapping" id="select-default" onchange="defaultSelectChange(this.value)"
                      noSelection="${[null:message(code:'de.iteratec.osm.csi.mapping.select.default')]}"/>
            <button type="button" class="btn btn-small btn-danger" onclick="deleteDefault()" disabled="true" id="btn-delete-default">
            <g:message code="de.iteratec.osm.csiConfiguration.deleteDefaultCsiConfiguration"
                       default="Delete Default Mapping"/> </button>
            <g:javascript>
                function defaultSelectChange(value){
                    $('#btn-delete-default').prop('disabled', $('#select-default').val()=="null");
                    handleMappingSelect(value,"${defaultIdentifier}");
                }
                function deleteDefault(){
                    $('#btn-delete-default').prop('disabled', true);
                    $.post( "<g:createLink action="deleteDefaultCsiMapping" absolute="true"/>", {"name":$('#select-default').val()})
                      .done(function( data ) {
                            window.location.reload();
                      });
                }
            </g:javascript>
           <style>
               #select-default{
                   margin-top:10px;
               }
           </style>
        </sec:ifAllGranted>
    </div>

    <g:render template="/chart/csi-mappings"
              model="${['chartData'        : defaultTimeToCsMappings, 'chartIdentifier': defaultIdentifier,
                        'bottomOffsetXAxis': 364, 'yAxisRightOffset': 44, 'chartBottomOffset': 250,
                        'yAxisTopOffset'   : 8, 'bottomOffsetLegend': 220, 'modal': false]}"/>
</div>