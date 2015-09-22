<!-- 
This is a dialog to choose from different default csi mappings.
-->

<!-- Modal dialog -->
<div id="CsiMappingModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="ModalLabel" aria-hidden="true" onshow=changeText("${controllerLink}");>
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" ria-hidden="true">×</button>
        <h3 id="ModalLabel"><g:message code="de.iteratec.osm.csi.mapping.title" default="Mapping: load time &rarr; customer satisfaction"/></h3>
    </div>
    <div class="modal-body">
        <div id="spinner-position"></div>
        <g:render template="/chart/csi-mappings"
                  model="${['transformableMappings': defaultMappings, 'chartIdentifier': 'choose_default_csi',
                            'bottomOffsetXAxis': 364, 'yAxisRightOffset': 44, 'chartBottomOffset': 250,
                            'yAxisTopOffset': 8, 'bottomOffsetLegend': 220]}" />
    </div>
    <div class="modal-footer">
        <g:form>
            <g:hiddenField name="page" value="${pageInstance}"></g:hiddenField>
            <label for="selectedDefaultMapping">
                <g:message code="de.iteratec.osm.csi.mapping.demand" args="${pageInstance}" default="Choose one of the following mappings for the page {0}"/>:
            </label>
            <g:select from="${defaultMappings*.name.unique()}" name="selectedDefaultMapping" ></g:select>
            <a href="#" class="btn btn-primary" onclick="copyDefaultMappingToPageAsynchronously()">
                <g:message code="de.iteratec.osm.mapping.applydefault.button.label" default="Apply mapping"/>
            </a>
        </g:form>

    </div>
</div>
<r:script>

    function startSpinner(spinnerElement){
        var opts = {
            lines: 15, // The number of lines to draw
            length: 20, // The length of each line
            width: 10, // The line thickness
            radius: 30, // The radius of the inner circle
            corners: 1, // Corner roundness (0..1)
            rotate: 0, // The rotation offset
            direction: 1, // 1: clockwise, -1: counterclockwise
            color: '#000', // #rgb or #rrggbb or array of colors
            speed: 1, // Rounds per second
            trail: 60, // Afterglow percentage
            shadow: true, // Whether to render a shadow
            hwaccel: false, // Whether to use hardware acceleration
            className: 'spinner', // The CSS class to assign to the spinner
            zIndex: 2e9, // The z-index (defaults to 2000000000)
            top: '50%', // Top position relative to parent in px
            left: '50%' // Left position relative to parent in px
        };
        return new Spinner(opts).spin(spinnerElement);
    }

    function copyDefaultMappingToPageAsynchronously(){

        var  spinner = startSpinner(document.getElementById('spinner-position'));

        var selectedMapping = document.querySelector('select[name=selectedDefaultMapping]').value;
        $.ajax({
            type: 'POST',
            url: '${createLink(controller: 'page', action: 'applyMappingToPage', absolute: true)}',
            data: {page: '${pageInstance}', selectedDefaultMapping: selectedMapping},
            success : function(data) {
                spinner.stop();
                //TODO: updating underlying chart doesn't word for now :(
                //console.log(data)
                //graphBuilder_choose_default_csi.setData(data);
                $('#CsiMappingModal').modal('hide');
            },
            error: function(result) {
                spinner.stop();
                $('#CsiMappingModal').modal('hide');
                return false;
            }
        });
    }

</r:script>