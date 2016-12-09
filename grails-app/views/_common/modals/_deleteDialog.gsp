<!-- 
This is the standard dialog that initiates the delete action.
-->
<div id="DeleteModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="DeleteModalLabel"
     aria-hidden="true" onshow="OpenSpeedMonitor.postLoaded.setDeleteConfirmationInformations('${controllerLink}');">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title" id="DeleteModalLabel">
                    <g:message code="default.button.delete.confirm.title" default="Delete Item"/>
                </h4>
            </div>

            <div class="modal-body">
                <p><g:message code="default.button.delete.confirm.messageWithArgument" args="[entityName]"
                              default="Do you really want to delete this item?"/></p>
            </div>

            <div class="modal-footer">
                <g:form>
                    <button class="btn btn-default" data-dismiss="modal" aria-hidden="true"><g:message code="default.button.cancel.label"
                                                                                           default="Cancel"/></button>
                    <g:hiddenField name="id" value="${item ? item.id : params.id}"/>
                    <g:hiddenField name="_method" value="DELETE"/>
                    <span class="button">
                        <g:actionSubmit class="btn btn-danger" action="delete"
                                        value="${message(code: 'default.button.delete.label', default: 'Delete')}"/>
                    </span>
                </g:form>

            </div>
        </div>
    </div>
</div>