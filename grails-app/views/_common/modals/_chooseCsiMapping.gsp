<!-- 
This is a dialog to choose from different default csi mappings.
-->

<!-- Modal dialog -->
<div id="CsiMappingModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="ModalLabel" aria-hidden="true" onshow="POSTLOADED.setDeleteConfirmationInformations('${controllerLink}')";>
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" ria-hidden="true">×</button>
        <h3 id="ModalLabel"><g:message code="de.iteratec.osm.csi.mapping.title" default="Mapping: load time &rarr; customer satisfaction"/></h3>
    </div>
    <div class="modal-body row">
        <div id="spinner-position"></div>
        <g:render template="/chart/csi-mappings"
                  model="${['chartData': defaultMultiLineChart, 'chartIdentifier': 'choose_default_csi',
                            'bottomOffsetXAxis': 364, 'yAxisRightOffset': 44, 'chartBottomOffset': 250,
                            'yAxisTopOffset': 8, 'bottomOffsetLegend': 220, 'modal': true]}" />
    </div>
    <div class="modal-footer">
        <g:form>
            <g:hiddenField name="page" value="${pageInstance}"></g:hiddenField>
            <label for="selectedDefaultMapping">
                <g:message code="de.iteratec.osm.csi.mapping.demand" default="Choose one of the following pages"/>:
            </label>
            <g:select from="${pages}" optionValue="name" optionKey="id" id="selectPageMapping" name="selectPage" noSelection="${[null:message(code:'de.iteratec.osm.csi.mapping.select.page.default')]}"/>
            <a href="#" class="btn btn-primary"  disabled="true" id="applyMapping">
                <g:message code="de.iteratec.osm.mapping.applydefault.button.label" default="Apply mapping"/>
            </a>
        </g:form>

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
                                          .range(["#DBDBDB", color])
        handlePageDefaultSelect($());


        function handlePageDefaultSelect(elementId){
            var newGraph = {};
            var pageName = elementId.find(":selected").text();
            var selectedPage = $.grep(pages.lines, function(e){ return e.name == pageName; })
            newGraph.lines = selectedDefault.concat(selectedPage);
            modalGraph.clearGraph();
            createMultiLineGraph(newGraph,'choose_default_csi', colorScale);
        }
    }
</asset:script>