<!--
This is the dialog that informs about errors while calling the context menu
-->
<div id="ContextMenuErrorModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="ContextMenuErrorModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" ria-hidden="true">×</button>

        %{--<h3 id="DeleteModalLabel"><g:message code="default.button.delete.confirm.title" default="Delete Item"/></h3>--}%
        <h3><g:message code="de.iteratec.chart.errorHeader" default="Error"/></h3>
    </div>

    <div class="modal-body">
        <p><g:message code="de.iteratec.chart.datapointSelection.error.multipleServer"
                      default="Comparison of the filmstrips is only possible for measurements on the same server."/></p>
    </div>
</div>