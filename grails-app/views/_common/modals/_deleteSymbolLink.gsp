<!-- 
This modal is used to show a button that initiates the delete action.
-->

<!-- Button to trigger modal -->
	<%-- <a href="#DeleteModal" role="button" class="btn btn-danger" data-toggle="modal" title="${message(code: 'default.button.delete.label', default: 'Delete')}">
		<i class="glyphicon-trash glyphicon-large"></i> ${message(code: 'default.button.delete.label', default: 'Delete')}
	</a> --%>
<a href="#DeleteModal" role="button" class="btn btn-danger" data-toggle="modal">${message(code: 'default.button.delete.label', default: 'Delete')}</a>
	<g:render template="/_common/modals/deleteDialog" model="[item: item]"/>
