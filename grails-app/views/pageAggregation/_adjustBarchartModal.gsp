<div class="modal fade" tabindex="-1" role="dialog" id="adjustBarchartModal">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4><g:message code="de.iteratec.chart.adjustment.name" default="adjust chart"/></h4>
            </div>

            <div id="collapseAdjustment" class="modal-body form-horizontal">
                <!-- x axis label -->
                <div class="form-group">
                    <label class="col-md-3 control-label" for="x-axis-label">
                        <g:message code="de.iteratec.osm.dimple.xAxis.label" default="x-axis label"/>
                    </label>

                    <div class="col-md-8">
                        <input id="x-axis-label" class="form-control" type="text">
                    </div>
                </div>
                %{--chart width & height--}%
                <div class="form-group">
                    <label for="inputChartWidth" class="col-sm-2 col-sm-offset-1 control-label"><g:message
                            code="de.iteratec.chart.width.name"
                            default="width"/></label>

                    <div class="col-sm-2">
                        <input type="number" id="inputChartWidth" min="0" step="1" data-bind="value:replyNumber"/>
                    </div>
                    <label for="inputChartHeight" class="col-sm-2 control-label"><g:message
                            code="de.iteratec.chart.height.name"
                            default="height"/></label>

                    <div class="col-sm-2">
                        <input type="number" id="inputChartHeight" min="0" step="1" data-bind="value:replyNumber"/>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><g:message
                        code="de.iteratec.ism.ui.button.close" default="close"/></button>
                <button type="button" class="btn btn-primary" id="adjustBarchartApply" onclick="adjustBarchartApply()">
                    <g:message code="de.iteratec.ism.ui.button.apply.name"/>
                </button>
            </div>
        </div>
    </div>
</div>

<asset:script>
    function initModalDialogValues() {
        $("#x-axis-label").val(OpenSpeedMonitor.ChartModules.PageAggregationBarChart.getXLabel());
        $("#inputChartWidth").val(OpenSpeedMonitor.ChartModules.PageAggregationBarChart.getWidth());
        $("#inputChartHeight").val(OpenSpeedMonitor.ChartModules.PageAggregationBarChart.getHeight());
    }

    function adjustBarchartApply() {
        OpenSpeedMonitor.ChartModules.PageAggregationBarChart.adjustChart();
        $('#adjustBarchartModal').modal('hide');
    }
</asset:script>