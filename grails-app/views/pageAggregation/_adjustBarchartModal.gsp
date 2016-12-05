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
                <div class="form-group row">
                    <label class="col-sm-3 control-label" for="x-axis-label">
                        <g:message code="de.iteratec.osm.dimple.xAxis.label" default="x-axis label"/>
                    </label>

                    <div class="col-sm-8">
                        <input id="x-axis-label" class="form-control" type="text">
                    </div>
                </div>
                %{--y labels--}%
                <div class="form-group row">
                    <label class="col-sm-3 control-label"><g:message
                            code="de.iteratec.osm.dimple.yAxis.label"
                            default="y-axis label"/></label>

                    <div class="col-sm-9" id="y-axis-alias-container">

                    </div>
                </div>
                %{--chart width & height--}%
                <div class="form-group row">
                    <div class="row">
                        <label class="col-sm-3 control-label"><g:message
                                code="de.iteratec.osm.dimple.barchart.size.name"
                                default="height"/></label>

                        <div class="col-sm-9">
                            <div class="row">
                                <label for="inputChartWidth" class="col-sm-5 control-label"><g:message
                                        code="de.iteratec.chart.width.name"
                                        default="width"/></label>

                                <input class="col-sm-3" type="number" id="inputChartWidth" min="0" step="1"
                                       data-bind="value:replyNumber"/>
                            </div>

                            <div class="row">
                                <label for="inputChartHeight" class="control-label col-sm-5"><g:message
                                        code="de.iteratec.chart.height.name"
                                        default="height"/></label>
                                <input class="col-sm-3" type="number" id="inputChartHeight" min="0" step="1"
                                       data-bind="value:replyNumber"/>
                            </div>
                        </div>
                    </div>
                </div>
                %{--assign colors--}%
                <div class="form-group row">
                    <label class="col-sm-3 control-label"><g:message
                            code="de.iteratec.osm.dimple.barchart.assignColors.name"
                            default="assign Colors"/></label>

                    <div class="col-sm-9" id="assign-color-container">

                    </div>
                </div>
            </div>

            <div class="row hidden" id="assign-color-clone">
                <label class="col-sm-5 control-label">
                </label>
                <input class="col-sm-1" type="color" value="#FFFFFF" class="form-control">
            </div>

            <div class="row hidden" id="y-axis-alias-clone">
                <label class="col-sm-5 control-label">
                </label>

                <input class="col-sm-3 labelInput" type="text">
                <input class="hidden unitInput" type="text">
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
        $("#assign-color-container").empty();
        $("#y-axis-alias-container").empty();
        var colorAssignments = OpenSpeedMonitor.ChartModules.PageAggregationBarChart.getColorAssignments();
        colorAssignments.forEach(function(assignment) {
            var clone = $("#assign-color-clone").clone();
            clone.removeAttr("id");
            clone.removeClass("hidden");
            clone.appendTo($("#assign-color-container"));
            clone.find("label").html(assignment.label);
            clone.find("input").val(assignment.color);
        });
        var yLabels = OpenSpeedMonitor.ChartModules.PageAggregationBarChart.getYLabels();
        yLabels.forEach(function(label) {
            var clone = $("#y-axis-alias-clone").clone();
            clone.removeAttr("id");
            clone.removeClass("hidden");
            clone.appendTo($("#y-axis-alias-container"));
            clone.find("label").html(label.label);
            clone.find("input.labelInput").val(label.label);
            clone.find("input.unitInput").val(label.unit);
        });
    }

    function adjustBarchartApply() {
        OpenSpeedMonitor.ChartModules.PageAggregationBarChart.adjustChart();
        $('#adjustBarchartModal').modal('hide');
    }
</asset:script>