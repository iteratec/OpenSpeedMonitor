<!-- 
This is the standard dialog that initiates the delete action.
-->

<!-- Modal dialog -->
<div id="DeleteModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="DeleteModalLabel" aria-hidden="true">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3 id="DeleteModalLabel"><g:message code="default.button.delete.confirm.title" default="Delete Item"/></h3>
	</div>
	<div class="modal-body">
		<p><g:message code="default.button.delete.confirm.message" args="[entityName]" default="Do you really want to delete this item?"/></p>
	</div>
	<div class="modal-footer">
		<g:form>
			<button class="btn" data-dismiss="modal" aria-hidden="true"><g:message code="default.button.cancel.label" default="Cancel"/></button>
<%--		<g:link action="delete" id="${params.id}" class="btn btn-danger">--%>
<%--			<g:message code="default.button.delete.label" default="Delete"/>--%>
<%--		</g:link>--%>
			<g:hiddenField name="id" value="${item ? item.id : params.id}" />
			<span class="button"><g:actionSubmit class="btn btn-danger" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}"/></span>
		</g:form>
		
	</div>
</div>