<!-- 
This modal is used to show a button that initiates the delete action.
-->
<!-- Button to trigger modal -->
<a href="#DeleteModal" role="button" data-toggle="modal">
	${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.delete', default: 'Delete')}
</a>
<g:render template="/_common/modals/deleteDialog" model="[item: [id: params.dashboardID], entityName: params.dashboardID]"/>