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
            <div class="panel-body">
                <div class="col-md-11">
                    <!-- Diagram-title -->
                    <div class="row">
                        <div class="col-md-2 text-right"><g:message code="de.iteratec.chart.title.name"/></div>

                        <div class="col-md-9"><input id="dia-title" class="input-xxlarge" type="text"
                                                  value="${chartTitle}"></div>
                    </div>
                    <!-- diagram-size -->
                    <div class="row">
                        <div class="col-md-2 text-right"><g:message code="de.iteratec.chart.size.name"/></div>

                        <div class="col-md-9">
                            <div class="input-prepend input-append">
                                <span class="add-on"><g:message code="de.iteratec.chart.width.name"/></span>
                                <input class="col-md-1 content-box" id="dia-width" type="text"
                                       value="${initialChartWidth}">
                                <span class="add-on">px</span>
                            </div>

                            <div class="input-prepend input-append">
                                <span class="add-on"><g:message code="de.iteratec.chart.height.name"/></span>
                                <input class="col-md-1 content-box" id="dia-height" type="text"
                                       value="${initialChartHeight}">
                                <span class="add-on">px</span>
                            </div>
                            <button class="btn" id="dia-change-chartsize" style="vertical-align: top;"><g:message
                                    code="de.iteratec.ism.ui.button.apply.name"/></button>
                        </div>
                    </div>
                    <!-- Y-Axis -->
                    <!-- rickshaw -->
                    <div id="adjust_chart_y_axis" class="row">
                        <div class="col-md-2 text-right"><g:message code="de.iteratec.chart.axis.y.name"/></div>

                        <div class="col-md-9">
                            <div class="input-prepend input-append ">
                                <span class="add-on"><g:message code="de.iteratec.chart.axis.y.minimum.name"/></span>
                                <input class="col-md-1 content-box" id="dia-y-axis-min" type="text"
                                       value="${yAxisMin ?: ''}">
                                <span class="add-on" id="minimumUnit"></span>
                            </div>

                            <div class="input-append input-prepend">
                                <span class="add-on"><g:message code="de.iteratec.chart.axis.y.maximum.name"/></span>
                                <input class="col-md-1 content-box" id="dia-y-axis-max" type="text"
                                       value="${yAxisMax ?: ''}">
                                <span class="add-on" id="maximumUnit"></span>
                            </div>
                            <button class="btn" id="dia-change-yaxis" style="vertical-align: top;"><g:message
                                    code="de.iteratec.ism.ui.button.apply.name"/></button>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-2 text-right">
                            <g:message code="de.iteratec.chart.adjustment.aliases" default="aliases"/>
                            <i id="addAliasButton" class="fa fa-plus-circle clickable-icon" onclick="addAlias();"></i>
                        </div>

                        <div class="col-md-9" id="graphAliasChildlist"></div>
                    </div>
                    <div class="row">
                        <div class="col-md-2 text-right"><g:message code="de.iteratec.isocsi.csi.show.datamarkers"
                                                                 default="Datenpunkte anzeigen"/></div>

                        <div class="col-md-9"><g:checkBox id="to-enable-marker" name="toEnableMarker"
                                                       checked="${markerShouldBeEnabled}"/></div>
                    </div>
                    <div class="row">
                        <div class="col-md-2 text-right"><g:message code="de.iteratec.isocsi.csi.show.datalabels"
                                                                 default="Datenlabels anzeigen"/></div>

                        <div class="col-md-9"><g:checkBox id="to-enable-label" name="toEnableLabel"
                                                       checked="${labelShouldBeEnabled}"/></div>
                    </div>
                    <div class="row">
                        <div class="col-md-2 text-right"><g:message
                                code="de.iteratec.isocsi.csi.show.wideScreenDiagramMontage"
                                default="Exportierte Diagramme für Breitbild-Darstellung optimieren"/></div>

                        <div class="col-md-9"><g:checkBox id="wide-screen-diagram-montage" name="wideScreenDiagramMontage"
                                                       checked="${wideScreenDiagramMontage}"/></div>
                    </div>
                </div>
            </div>
        </div>

        <div class="accordion-footer">
        </div>
    </div>
</div>


<div id="graphAlias_clone" class="graphAlias-div" style="display:none;">
    <div class="row">
        <div class="col-md-9">
            <g:select name="graphName"
                      from="${chartData*.label}"/>
        </div>
    </div>

    <div class="row">
        <div class="col-md-6">
            <g:textField placeholder="${message(code: "de.iteratec.chart.adjustment.newAlias", default: "enter alias" )}" name='alias' class="input-xxlarge"/>
        </div>

        <div class="col-md-2">
            %{--HTML5 color picker, not supported in IE--}%
            <input type="color" id="color" value="#FFFFFF" style="width:50%;">
            <i id="removeButton" class="fa fa-minus-circle clickable-icon"> </i>
        </div>
    </div>
    <hr>
</div>