<div class="modal fade" tabindex="-1" role="dialog" id="adjustBarchartModal">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4><g:message code="de.iteratec.chart.adjustment.name" default="Adjust Chart"/></h4>
            </div>

            <div id="collapseAdjustment" class="modal-body form-horizontal">
                %{-- x axis label --}%
                <div class="form-group">
                    <label class="col-sm-3 control-label"><g:message
                            code="de.iteratec.osm.chart.labels"
                            default="Labels"/></label>

                    <div class="col-sm-8 jobGroupAliasForm">
                        <div class="input-group form-row hidden jobGroupAliasFormRowClone">
                            <span class="input-group-addon adjustChartsizeAddon jobGroupName">
                            </span>
                            <input type="text" class="form-control adjustChartInput jobGroupAlias">
                        </div>
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

<asset:script type="text/javascript">
    function initModalDialogValues() {
        var chart = OpenSpeedMonitor.ChartModules.CsiBenchmarkBarChart;
        var mappings = chart.getLabelMappings();
        $(".jobGroupAliasRow").remove();
        for(var key in mappings) {
            var clone = $(".jobGroupAliasFormRowClone").clone();
            clone.find(".jobGroupName").text(key);
            clone.find(".jobGroupAlias").val(mappings[key]);
            clone.addClass("jobGroupAliasRow");
            clone.removeClass("hidden");
            clone.removeClass("jobGroupAliasFormRowClone");
            $(".jobGroupAliasForm").append(clone);
        }
    }

    function adjustBarchartApply() {
        var newMappings = {};
        $(".jobGroupAliasRow").each(function() {
            var jobGroupName = $(this).find(".jobGroupName").text();
            var aliasName = $(this).find(".jobGroupAlias").val();
            if(aliasName) {
                newMappings[jobGroupName] = aliasName;
            }
        });
        OpenSpeedMonitor.ChartModules.CsiBenchmarkBarChart.setLabelMappings(newMappings);
        $('#adjustBarchartModal').modal('hide');
    }
</asset:script>