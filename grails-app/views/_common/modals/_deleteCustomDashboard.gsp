<!-- 
This modal is used to show a button that initiates the delete action.
-->

<!-- Button to trigger modal -->
	<%-- <a href="#DeleteModal" role="button" class="btn btn-danger" data-toggle="modal" title="${message(code: 'default.button.delete.label', default: 'Delete')}">
		<i class="icon-trash icon-large"></i> ${message(code: 'default.button.delete.label', default: 'Delete')}
	</a> --%>
<a href="#DeleteModal" role="button" class="btn btn-danger" data-toggle="modal" style="margin-top: 16px;">${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.delete', default: 'Delete')}</a>
	<g:render template="/_common/modals/deleteDialog" model="[item: item, entityName: params.dbname]"/>