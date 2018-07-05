<!-- 
This modal is used to show a button that initiates the delete action.
-->

<!-- Button to trigger modal if Javascript is available -->
	<a href="#DeleteModal" role="button" data-toggle="modal" title="${message(code: 'default.button.delete.label', default: 'Delete')}">
		<i class="fas fa-trash-alt"></i>
		<g:message code="default.button.delete.label" default="Delete"/>
	</a>

	<g:render template="/_common/modals/deleteDialog" model="[item: item]"/>
