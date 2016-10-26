<%@ page import="de.iteratec.osm.report.chart.ChartingLibrary" %>

%{--This template can be added to a view with a chart. It enables adjustments to the charts layout like adding a title, resizing the--}%
%{--whole chart or show/hide datapoints.--}%
%{--The rendering view can provide attribute chartRenderingLibrary to specify the charting library used to render the chart. If attribute is--}%
%{--missing the default charting library rickshaw is assumed.--}%

<div class="col-md-12 panel-group" id="accordion2">
    <div class="panel panel-default">
        <div class="panel-heading">
            <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseAdjustment">
                <g:message code="de.iteratec.chart.adjustment.name" default="Diagramm anpassen"/>
            </a>
        </div>

        <div id="collapseAdjustment" class="panel-collapse collapse in">
            <div class="panel-body form-horizontal">
                <!-- Diagram-title -->
                <div class="form-group">
                    <label class="col-md-2 control-label" for="dia-title">
                        <g:message code="de.iteratec.chart.title.name"/>
                    </label>
                    <div class="col-md-6">
                        <input id="dia-title" class="form-control" type="text" value="${chartTitle}">
                    </div>
                </div>
                <!-- diagram-size -->
                <div class="form-group">
                    <label class="col-md-2 control-label" for="dia-width">
                        <g:message code="de.iteratec.chart.size.name"/>
                    </label>
                    <div class="col-md-2">
                        <div class="input-group">
                            <span class="input-group-addon"><g:message code="de.iteratec.chart.width.name"/></span>
                            <input class="form-control" id="dia-width" type="text" value="${initialChartWidth}"/>
                            <span class="input-group-addon">px</span>
                        </div>
                    </div>
                    <div class="col-md-2">
                        <div class="input-group">
                            <span class="input-group-addon"><g:message code="de.iteratec.chart.height.name"/></span>
                            <input class="form-control" id="dia-height" type="text" value="${initialChartHeight}"/>
                            <span class="input-group-addon">px</span>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <button class="btn btn-default" id="dia-change-chartsize">
                            <g:message code="de.iteratec.ism.ui.button.apply.name"/>
                        </button>
                    </div>
                </div>
                <!-- Y-Axis -->
                <!-- rickshaw -->
                <div id="adjust_chart_y_axis_container">
                    <div class="adjust_chart_y_axis form-group">
                        <label class="col-md-2 control-label"><g:message code="de.iteratec.chart.axis.y.name"/></label>
                        <div class="col-md-2">
                            <div class="input-group ">
                                <span class="input-group-addon"><g:message code="de.iteratec.chart.axis.y.minimum.name"/></span>
                                <input class="form-control dia-y-axis-min" type="text"
                                       value="${yAxisMin ?: ''}">
                                <span class="input-group-addon minimumUnit"></span>
                            </div>
                        </div>
                        <div class="col-md-2">
                            <div class="input-group">
                                <span class="input-group-addon"><g:message code="de.iteratec.chart.axis.y.maximum.name"/></span>
                                <input class="form-control dia-y-axis-max" type="text"
                                       value="${yAxisMax ?: ''}">
                                <span class="input-group-addon maximumUnit"></span>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <button class="btn btn-default dia-change-yaxis">
                                <g:message code="de.iteratec.ism.ui.button.apply.name"/>
                            </button>
                        </div>
                    </div>
                </div>
                <!-- Add Alias -->
                <div class="form-group">
                    <div class="col-md-2">
                        <button class="btn btn-default pull-right" id="addAliasButton" onclick="addAlias();">
                            <i class="fa fa-plus"></i>
                            <g:message code="de.iteratec.chart.adjustment.aliases" default="aliases"/>
                        </button>
                    </div>
                    <div class="col-md-6" id="graphAliasChildlist"></div>
                </div>
                <div class="form-group">
                    <div class="col-md-10 col-md-offset-2">
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
            </div>
        </div>
    </div>
</div>


<div id="graphAlias_clone" class="graphAlias-div" style="display:none;">
    <div class="form-group">
        <div class="col-md-10">
            <g:select name="graphName" from="${chartData*.label}" class="form-control"/>
        </div>
        <div class="col-md-1">
            %{--HTML5 color picker, not supported in IE--}%
            <input type="color" id="color" value="#FFFFFF" class="form-control">
        </div>
        <div class="col-md-1">
            <button type="button" class="btn btn-default" id="removeButton" style="color: #a94442;">
                <i class="fa fa-minus"></i>
            </button>
        </div>
    </div>
    <div class="form-group">
        <div class="col-md-12">
            <g:textField placeholder="${message(code: "de.iteratec.chart.adjustment.newAlias", default: "enter alias" )}"
                         name='alias' class="input-alias form-control"/>
        </div>
    </div>
    <hr>
</div>