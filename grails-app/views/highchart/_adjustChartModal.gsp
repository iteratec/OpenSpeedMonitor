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
                <h4><g:message code="de.iteratec.chart.adjustment.name" default="Diagramm anpassen"/></h4>
            </div>

            <div id="collapseAdjustment" class="modal-body form-horizontal">
                <!-- Diagram-title -->
                <div class="form-group">
                    <label class="col-md-3 control-label" for="dia-title">
                        <g:message code="de.iteratec.chart.title.name"/>
                    </label>
                    <div class="col-md-8">
                        <input id="dia-title" class="form-control" type="text" value="${chartTitle}">
                    </div>
                </div>
                <!-- diagram-size -->
                <div class="form-group">
                    <label class="col-md-3 control-label" for="dia-width">
                        <g:message code="de.iteratec.chart.size.name"/>
                    </label>
                    <div class="col-md-3">
                        <div class="input-group">
                            <span class="input-group-addon"><g:message code="de.iteratec.chart.width.name"/></span>
                            <input class="form-control" id="dia-width" type="text" value="${initialChartWidth}"/>
                            <span class="input-group-addon">px</span>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="input-group">
                            <span class="input-group-addon"><g:message code="de.iteratec.chart.height.name"/></span>
                            <input class="form-control" id="dia-height" type="text" value="${initialChartHeight}"/>
                            <span class="input-group-addon">px</span>
                        </div>
                    </div>
                </div>
                <!-- Y-Axis -->
                <!-- rickshaw -->
                <div id="adjust_chart_y_axis_container">
                    <div class="adjust_chart_y_axis form-group">
                        <label class="col-md-3 control-label"><g:message code="de.iteratec.chart.axis.y.name"/></label>
                        <div class="col-md-3">
                            <div class="input-group ">
                                <span class="input-group-addon"><g:message code="de.iteratec.chart.axis.y.minimum.name"/></span>
                                <input class="form-control dia-y-axis-min" type="text"
                                       value="${yAxisMin ?: ''}">
                                <span class="input-group-addon minimumUnit"></span>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="input-group">
                                <span class="input-group-addon"><g:message code="de.iteratec.chart.axis.y.maximum.name"/></span>
                                <input class="form-control dia-y-axis-max" type="text"
                                       value="${yAxisMax ?: ''}">
                                <span class="input-group-addon maximumUnit"></span>
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
                <!-- Add Alias -->
                <div class="form-group">
                    <div class="col-md-8 col-md-offset-3" id="graphAliasChildlist"></div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-default" id="addAliasButton" onclick="addAlias();">
                    <i class="fa fa-plus"></i>
                    <g:message code="de.iteratec.chart.adjustment.aliases" default="aliases"/>
                </button>
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                <button type="button" class="btn btn-primary" id="adjustChartApply">
                    <g:message code="de.iteratec.ism.ui.button.apply.name"/>
                </button>
            </div>
        </div>
    </div>
</div>


<div id="graphAlias_clone" class="graphAlias-div" style="display:none;">
    <hr />
    <div class="form-group">
        <div class="col-md-9">
            <g:select name="graphName" from="${chartData*.label}" class="form-control"/>
        </div>
        <div class="col-md-2">
            %{--HTML5 color picker, not supported in IE--}%
            <input type="color" id="color" value="#FFFFFF" class="form-control">
        </div>
        <div class="col-md-1">
            <button type="button" class="close" id="removeButton" aria-label="Remove">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
    </div>
    <div class="form-group">
        <div class="col-md-12">
            <g:textField placeholder="${message(code: "de.iteratec.chart.adjustment.newAlias", default: "enter alias" )}"
                         name='alias' class="input-alias form-control"/>
        </div>
    </div>
</div>