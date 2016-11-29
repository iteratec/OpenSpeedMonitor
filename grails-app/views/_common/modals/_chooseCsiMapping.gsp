<!-- 
This is a dialog to choose from different default csi mappings.
-->
<%@ defaultCodec="none" %>
<!-- Modal dialog -->
<div id="CsiMappingModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="ModalLabel" aria-hidden="true" onshow="POSTLOADED.setDeleteConfirmationInformations('${controllerLink}')";>
    <div id="csiMappingModalDialog" class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 id="ModalLabel" class="modal-title"><g:message code="de.iteratec.osm.csi.mapping.title" default="Mapping: load time &rarr; customer satisfaction"/></h4>
            </div>
            <div class="modal-body row">
                <g:render template="/chart/csi-mappings"
                          model="${['chartData': defaultMultiLineChart, 'chartIdentifier': 'choose_default_csi',
                                    'bottomOffsetXAxis': 364, 'yAxisRightOffset': 44, 'chartBottomOffset': 250,
                                    'yAxisTopOffset': 8, 'bottomOffsetLegend': 220, 'modal': true]}" />
            </div>
            <div class="modal-footer">
                <g:form class="form-inline">
                    <div class="form-group">
                        <g:hiddenField name="page" value="${pageInstance}"></g:hiddenField>
                        <label for="selectPageMapping">
                            <g:message code="de.iteratec.osm.csi.mapping.demand" default="Choose one of the following pages"/>:
                        </label>
                        <g:select class="form-control" from="${pages.sort{it.name}}" optionValue="name" optionKey="id" id="selectPageMapping" name="selectPage" noSelection="${["-1":message(code:'de.iteratec.osm.csi.mapping.select.page.default')]}"/>

                        <button href="#" class="btn btn-primary"  disabled="true" id="applyMapping">
                            <g:message code="de.iteratec.osm.mapping.applydefault.button.label" default="Apply mapping"/>
                        </button>
                    </div>
                </g:form>
            </div>
        </div>
    </div>
</div>

<asset:script>
    modalGraph = createMultiLineGraph(${defaultMultiLineChart}, 'choose_default_csi');
    function showPageSelect(newLine, color){
        var defaults = ${defaultMultiLineChart}
        var selectedDefault = $.grep(defaults.lines, function(e){ return e.name == newLine; });
        var pages = ${pageData};
        $("#CsiMappingModal").find("#selectPageMapping").change(function(d){handlePageDefaultSelect($(this))});
        var colorScale = d3.scale.ordinal()
                                          .domain([newLine, ""])
                                          .range(["#DBDBDB", color]);

        $("#applyMapping").click(applyPageMapping);
        handlePageDefaultSelect($());

        function handlePageDefaultSelect(elementId){
            var newGraph = {};
            var pageName = elementId.find(":selected").text();
            var selectedPage = $.grep(pages.lines, function(e){ return e.name == pageName; })
            newGraph.lines = selectedDefault.concat(selectedPage);
            modalGraph.clearGraph();
            createMultiLineGraph(newGraph,'choose_default_csi', false, colorScale);
            handleApplyButtonDisable(elementId);
         }

        function handleApplyButtonDisable(elementId){
            var selectedValue = elementId.find(":selected").val();
            var enableButton = (selectedValue === "-1" || selectedValue == null);
            $("#applyMapping").prop('disabled',enableButton);
        }
        function applyPageMapping(){
            var select = $("#CsiMappingModal").find("#selectPageMapping");
            select.prop('disabled', true);
            var spinner = OpenSpeedMonitor.Spinner('#choose_default_csi');
            spinner.start();
            $("#applyMapping").prop('disabled', true);
            var pageId = select.find(":selected").val()
            jQuery.ajax({
                type: 'GET',
                url: "<g:createLink action="applyNewMappingToPage" absolute="true"/>",
                data:{'defaultMappingName':newLine, 'csiConfigurationId':actualCsiConfigurationId, 'pageId':pageId},
                success: function (content) {
                    window.location.href="<g:createLink action="configurations" absolute="true"/>/"+actualCsiConfigurationId;
                }
         });
    }


    }

</asset:script>