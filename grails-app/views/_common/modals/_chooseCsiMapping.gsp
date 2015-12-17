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
                <g:message code="de.iteratec.osm.csi.mapping.demand" args="${pageInstance}" default="Choose one of the following mappings for the page {0}"/>:
            </label>
            <g:select from="${defaultMappings*.name.unique()}" name="selectedDefaultMapping" onchange="handleMappingSelect(this.value)" noSelection="${[null:message(code:'de.iteratec.osm.csi.mapping.select.default')]}">
            </g:select>
            <a href="#" class="btn btn-primary"  disabled="true" id="applyMapping">
                <g:message code="de.iteratec.osm.mapping.applydefault.button.label" default="Apply mapping"/>
            </a>
        </g:form>

    </div>
</div>