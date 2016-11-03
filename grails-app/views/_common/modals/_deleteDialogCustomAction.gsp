<!--
This is the standard dialog that initiates the delete action.
-->
<div id="DeleteModal${customPrefix}" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="DeleteModalLabel" aria-hidden="true" onshow="POSTLOADED.setDeleteConfirmationInformations('${controllerLink}');">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 id="DeleteModalLabel" class="modal-title"><g:message code="default.button.delete.confirm.title" default="Delete Item"/></h4>
            </div>
            <div class="modal-body">
                <p><g:message code="custom.button.delete.confirm"  default="Do you really want to delete this item?"/></p>
                <p id="itemConfirm" style="font-weight: bold"></p>
                <div id="spinner-position"></div>
            </div>
            <div class="modal-footer">
                <g:form controller="${customController}">
                    <button class="btn btn-default" data-dismiss="modal" aria-hidden="true"><g:message code="default.button.cancel.label" default="Cancel"/></button>
                    <g:hiddenField id='deleteValue' name="${customID?:'id'}" value="${item ? item.id : params.id}" />
                    <g:hiddenField name="_method" value="POST" />
                    <span class="button">
                        <g:actionSubmit class="btn btn-danger" action="${actionName}" value="${message(code: 'default.button.delete.label', default: 'Delete')}"/>
                    </span>
                </g:form>

            </div>
        </div>
    </div>
</div>
<asset:script>
    function changeValueToDelete(value, customPrefix){
        $('#DeleteModal'+customPrefix).find('#deleteValue').attr("value", value);
        $('#DeleteModal'+customPrefix).find('#itemConfirm').html("${itemLabel}: " + value);
    }
</asset:script>