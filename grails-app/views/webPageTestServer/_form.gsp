<%@ page import="de.iteratec.osm.measurement.environment.WebPageTestServer" %>



			<div class="control-group fieldcontain ${hasErrors(bean: webPageTestServerInstance, field: 'label', 'error')} required">
				<label for="label" class="control-label"><g:message code="webPageTestServer.label.label" default="Label" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<g:textField name="label" maxlength="150" required="" value="${webPageTestServerInstance?.label}"/>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: webPageTestServerInstance, field: 'proxyIdentifier', 'error')} ">
				<label for="proxyIdentifier" class="control-label"><g:message code="webPageTestServer.proxyIdentifier.label" default="Proxy Identifier" /></label>
				<div class="controls">
					<g:textField name="proxyIdentifier" value="${webPageTestServerInstance?.proxyIdentifier}"/>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: webPageTestServerInstance, field: 'active', 'error')} ">
				<label for="active" class="control-label"><g:message code="webPageTestServer.active.label" default="Active" /></label>
				<div class="controls">
					<bs:checkBox name="active" value="${webPageTestServerInstance?.active}" />
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: webPageTestServerInstance, field: 'description', 'error')} ">
				<label for="description" class="control-label"><g:message code="webPageTestServer.description.label" default="Description" /></label>
				<div class="controls">
					<g:textArea name="description" cols="40" rows="5" value="${webPageTestServerInstance?.description}"/>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: webPageTestServerInstance, field: 'baseUrl', 'error')} required">
				<label for="baseUrl" class="control-label"><g:message code="webPageTestServer.baseUrl.label" default="Base Url" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<g:field type="url" name="baseUrl" required="" value="${webPageTestServerInstance?.baseUrl}"/>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: webPageTestServerInstance, field: 'contactPersonName', 'error')} ">
				<label for="contactPersonName" class="control-label"><g:message code="webPageTestServer.contactPersonName.label" default="Contact Person Name" /></label>
				<div class="controls">
					<g:textField name="contactPersonName" maxlength="200" value="${webPageTestServerInstance?.contactPersonName}"/>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: webPageTestServerInstance, field: 'contactPersonEmail', 'error')} ">
				<label for="contactPersonEmail" class="control-label"><g:message code="webPageTestServer.contactPersonEmail.label" default="Contact Person Email" /></label>
				<div class="controls">
					<g:field type="email" name="contactPersonEmail" value="${webPageTestServerInstance?.contactPersonEmail}"/>
				</div>
			</div>

