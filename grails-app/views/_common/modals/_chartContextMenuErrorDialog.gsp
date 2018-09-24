<!--
This is the dialog that informs about errors while calling the context menu
-->
<div id="ContextMenuErrorModal" class="modal fade" tabindex="-1" role="dialog"
     aria-labelledby="ContextMenuErrorModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title"><g:message code="de.iteratec.chart.errorHeader" default="Error"/></h4>
            </div>
            <div class="modal-body">
                <p><g:message code="de.iteratec.chart.datapointSelection.error.multipleServer"
                              default="Comparison of the filmstrips is only possible for measurements on the same server."/></p>
            </div>
            <div class="modal-footer">
                <button class="btn btn-default" data-dismiss="modal" aria-hidden="true">
                    <g:message code="default.button.cancel.label" default="Cancel"/>
                </button>
            </div>
        </div>
    </div>
</div>