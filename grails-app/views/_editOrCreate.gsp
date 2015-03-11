<!doctype html>
<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="kickstart_osm" />
<title><g:message code="default.${mode}.label" args="[entityDisplayName]" /></title>

<style type="text/css">
	.control-label{
		width: 250px !important;
		margin-right: 1em !important;
	}
	.form-group {
		margin-bottom: 0.4em !important;
	}
	.checkbox {
		margin-left: 263px;
	}
</style>
<script>
	function promptForDuplicateName() {
		var newName = prompt('${message(code: 'de.iteratec.actions.duplicate.prompt')}', $('input#label').val() + ' ${message(code: 'de.iteratec.actions.duplicate.copy')}');
		if (newName != null && newName != '') {
			$('input#label').val(newName);
			return true;
		} else {
			return false;
		}
	}
</script>
</head>

<body>
	<%-- main menu --%>
	<g:render template="/layouts/mainMenu" />

	<section id="${mode}-{$entityName}" class="first">
		<h1><g:message code="default.${mode}.label" args="[entityDisplayName]" /></h1>
		<g:render template="messages" />

		<p><g:message code="default.form.asterisk" /></p>

		<g:form method="post" role="form" class="form-horizontal">
			<g:hiddenField name="id" value="${entity?.id}" />
			<g:hiddenField name="version" value="${entity?.version}" />
			<fieldset class="form">
				<g:render template="form" />
			</fieldset>

			<div class="form-actions">
				<g:if test="${ mode == 'edit' }">
					<g:actionSubmit class="btn btn-primary" action="update"
						value="${message(code: 'default.button.save.label', default: 'Speichern')}" />
					<g:actionSubmit class="btn btn-primary" action="save"
						value="${message(code: 'de.iteratec.actions.duplicate', default: 'Kopie speichern')}" 
						onclick="return promptForDuplicateName();" />
				</g:if>
				<g:elseif test="${ mode == 'create' }">
					<g:actionSubmit class="btn btn-primary" action="save"
						value="${message(code: 'default.button.create.label', default: 'Create')}" />
				</g:elseif>
					
				<a href="<g:createLink action="list" />" class="btn btn-warning"
				 onclick="return confirm('${message(code: 'default.button.unsavedChanges.confirm.message', default: 'Sind Sie sicher?')}');">
					<g:message code="default.button.cancel.label" default="Abbrechen" />
				</a>
				
				<g:if test="${ mode == 'edit' }">
	   			<a href="#DeleteModal" role="button" class="btn btn-danger" data-toggle="modal"
                   onclick="return domainDeleteConfirmation('${message(code: 'default.button.unsavedChanges.confirm.message', default: 'Sind Sie sicher?')}',${entity?.id});">
                    ${message(code: 'default.button.delete.label', default: 'Delete')}
                </a>

  <%--
					<g:actionSubmit class="btn btn-danger" action="delete"
						value="${message(code: 'default.button.delete.label', default: 'Löschen')}"
						onclick="return confirm('${message(code: 'default.button.unsavedChanges.confirm.message', default: 'Sind Sie sicher?')}');" />  
 --%>			   
					<g:if test="${ entityName == 'job'}">
						<g:actionSubmit class="btn btn-info" action="execute" value="${message(code: 'de.iteratec.isj.job.test', default: 'Test')}" 
						    onclick="this.form.target='_blank';return true;" />
					</g:if> 
				</g:if>
			</div>
		</g:form>
	</section>
</body>
</html>