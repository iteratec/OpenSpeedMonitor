<%@ page import="de.iteratec.osm.report.chart.ChartingLibrary" %>

%{--This template can be added to a view with a chart. It enables adjustments to the charts layout like adding a title, resizing the--}%
%{--whole chart or show/hide datapoints.--}%
%{--The rendering view can provide attribute chartRenderingLibrary to specify the charting library used to render the chart. If attribute is--}%
%{--missing the default charting library rickshaw is assumed.--}%

<div class="modal fade" tabindex="-1" role="dialog" id="adjustChartModal">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4><g:message code="de.iteratec.chart.adjustment.name" default="Adjust Chart"/></h4>
            </div>

            <div id="collapseAdjustment" class="modal-body form-horizontal">
                <!-- Diagram-title -->
                <div class="form-group">
                    <label class="col-md-3 control-label" for="dia-title">
                        <g:message code="de.iteratec.chart.title.name" default="Title"/>
                    </label>
                    <div class="col-md-8">
                        <input id="dia-title" class="form-control" type="text" value="${chartTitle}">
                    </div>
                </div>
                <!-- diagram-size -->
                <div class="form-group">
                    <label class="col-md-3 control-label" for="dia-width">
                        <g:message code="de.iteratec.chart.size.name" default="Size"/>
                    </label>
                    <div class="col-md-8">
                        <div class="input-group form-row">
                            <span class="input-group-addon adjustChartsizeAddon">
                                <g:message code="de.iteratec.chart.width.name"
                                           default="Width"/>
                            </span>
                            <input class="form-control adjustChartInput" id="dia-width" type="text" value="${initialChartWidth}"/>
                            <span class="input-group-addon adjustChartUnitAddon">px</span>
                        </div>

                        <div class="input-group form-row">
                            <span class="input-group-addon adjustChartsizeAddon">
                                <g:message code="de.iteratec.chart.height.name"
                                           default="Height"/>
                            </span>
                            <input class="form-control adjustChartInput" id="dia-height" type="text" value="${initialChartHeight}"/>
                            <span class="input-group-addon adjustChartUnitAddon">px</span>
                        </div>
                    </div>
                </div>
                <!-- Y-Axis -->
                <!-- rickshaw -->
                <div id="adjust_chart_y_axis_container">
                    <div class="adjust_chart_y_axis form-group">
                        %{-- This label gets filled via JavaScript function 'createYAxisAdjuster' in rickshawChartCreation.js --}%
                        <label class="col-md-3 control-label"></label>
                        <div class="col-md-8">
                            <div class="input-group form-row">
                                <span class="input-group-addon adjustRangeAddon">
                                    <g:message code="de.iteratec.chart.axis.y.maximum.name" default="Maximum"/>
                                </span>
                                <input class="form-control dia-y-axis-max adjustChartInput" type="text"
                                       value="${yAxisMax ?: ''}">
                                <span class="input-group-addon maximumUnit adjustChartUnitAddon"></span>
                            </div>

                            <div class="input-group form-row">
                                <span class="input-group-addon adjustRangeAddon">
                                    <g:message code="de.iteratec.chart.axis.y.minimum.name" default="Minimum"/>
                                </span>
                                <input class="form-control dia-y-axis-min adjustChartInput" type="text"
                                       value="${yAxisMin ?: ''}">
                                <span class="input-group-addon minimumUnit adjustChartUnitAddon"></span>
                            </div>
                        </div>
                        <input type="hidden" class="dia-y-axis-name" value="" />
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-md-8 col-md-offset-3">
                        <label class="checkbox-inline">
                            <g:checkBox id="to-enable-label" name="toEnableLabel" checked="${labelShouldBeEnabled}"/>
                            <g:message code="de.iteratec.isocsi.csi.show.datalabels" default="Datenlabels anzeigen"/>
                        </label>
                        <label class="checkbox-inline">
                            <g:checkBox id="to-enable-marker" name="toEnableMarker" checked="${markerShouldBeEnabled}"/>
                            <g:message code="de.iteratec.isocsi.csi.show.datamarkers" default="Datenpunkte anzeigen"/>
                        </label>
                    </div>
                </div>
                %{--TODO: Kind of works, but the markers/labels are preselected--}%
                %{--<div class="form-group">--}%
                    %{--<label class="col-md-3 control-label">--}%
                        %{--<g:message code="de.iteratec.chart.showElements.name" default="Show"/>--}%
                    %{--</label>--}%
                    %{--<div class="col-md-4" data-toggle="buttons">--}%
                        %{--<div class="btn-group btn-group-justified">--}%
                            %{--<label class="btn btn-default">--}%
                                %{--<input type="checkbox" id="to-enable-label" checked="${labelShouldBeEnabled}">--}%
                                %{--<g:message code="de.iteratec.isocsi.csi.show.datalabels" default="Data Labels"/>--}%
                            %{--</label>--}%
                            %{--<label class="btn btn-default">--}%
                                %{--<input type="checkbox" id="to-enable-marker" checked="${markerShouldBeEnabled}">--}%
                                %{--<g:message code="de.iteratec.isocsi.csi.show.datamarkers" default="Data Points"/>--}%
                            %{--</label>--}%
                        %{--</div>--}%
                    %{--</div>--}%
                %{--</div>--}%
                <!-- Add Alias -->
                <div class="form-group">
                    <div class="col-md-8 col-md-offset-3" id="graphAliasChildlist"></div>
                </div>
                <div class="form-group">
                    <div class="col-md-offset-3 col-md-8">
                        <a href="#" id="addAliasButton" onclick="addAlias();">
                            <i class="fa fa-lg fa-plus-circle" aria-hidden="true"></i>
                            <g:message code="de.iteratec.chart.adjustment.aliases" default="Add Graph Alias"/>
                        </a>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">
                    <g:message code="de.iteratec.ism.ui.button.close" default="Close" />
                </button>
                <button type="button" class="btn btn-primary" id="adjustChartApply">
                    <g:message code="de.iteratec.ism.ui.button.apply.name" default="Apply"/>
                </button>
            </div>
        </div>
    </div>
</div>


<div id="graphAlias_clone" class="graphAlias-div" style="display:none;">
    <hr />
    <div class="form-group">
        <div class="col-md-10">
            <g:select name="graphName" class="form-control chosen-select"
                      from="${chartData*.label}" noSelection="${['':"${message(code: 'de.iteratec.chart.adjustment.chooseGraph', default: 'Choose a Graph')}"]}"/>
        </div>
        <div class="col-md-2">
            <button type="button" class="form-control close" id="removeButton" aria-label="Remove">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
    </div>
    <div class="form-group">
        <div class="col-md-6">
            <g:textField placeholder="${message(code: "de.iteratec.chart.adjustment.newAlias", default: "Enter Alias" )}"
                         name='alias' class="input-alias form-control"/>
        </div>
        <div class="col-md-4">
            <div class="input-group colorpicker-component" id="assign-color-clone">
                <input type="text" id="color" value="#FFFFFF" class="form-control adjustChartInput"/>
                <span class="input-group-addon colorpicker-target adjustChartColorpickerAddon"><i></i></span>
            </div>
        </div>
    </div>
</div>