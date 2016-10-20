<%@ page import="de.iteratec.osm.measurement.environment.WebPageTestServer" %>



			<div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'label', 'error')} required">
				<label for="label" class="control-label"><g:message code="webPageTestServer.label.label" default="Label" /><span class="required-indicator">*</span></label>
				<div>
					<g:textField name="label" maxlength="150"  value="${webPageTestServer?.label}"/>
				</div>
			</div>

			<div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'proxyIdentifier', 'error')} required">
				<label for="proxyIdentifier" class="control-label"><g:message code="webPageTestServer.proxyIdentifier.label" default="Proxy Identifier" /><span class="required-indicator">*</span></label>
				<div>
					<g:textField name="proxyIdentifier"  value="${webPageTestServer?.proxyIdentifier}"/>
				</div>
			</div>

			<div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'active', 'error')} ">
				<label for="active" class="control-label"><g:message code="webPageTestServer.active.label" default="Active" /></label>
				<div>
					<bs:checkBox name="active" value="${webPageTestServer?.active}" />
				</div>
			</div>

			<div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'description', 'error')} ">
				<label for="description" class="control-label"><g:message code="webPageTestServer.description.label" default="Description" /></label>
				<div>
					<g:textArea name="description" cols="40" rows="5" value="${webPageTestServer?.description}"/>
				</div>
			</div>

			<div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'baseUrl', 'error')} required">
				<label for="baseUrl" class="control-label"><g:message code="webPageTestServer.baseUrl.label" default="Base Url" /><span class="required-indicator">*</span></label>
				<div>
					<g:field type="url" name="baseUrl"  value="${webPageTestServer?.baseUrl}"/>
				</div>
			</div>

			<div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'contactPersonName', 'error')} ">
				<label for="contactPersonName" class="control-label"><g:message code="webPageTestServer.contactPersonName.label" default="Contact Person Name" /></label>
				<div>
					<g:textField name="contactPersonName" maxlength="200" value="${webPageTestServer?.contactPersonName}"/>
				</div>
			</div>

			<div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'contactPersonEmail', 'error')} ">
				<label for="contactPersonEmail" class="control-label"><g:message code="webPageTestServer.contactPersonEmail.label" default="Contact Person Email" /></label>
				<div>
					<g:field type="email" name="contactPersonEmail" value="${webPageTestServer?.contactPersonEmail}"/>
				</div>
			</div>

            <div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'apiKey', 'error')}">
                <label for="api-key" class="control-label">
                    <g:message code="webPageTestServer.apiKey.label" default="API key" />
                </label>
                <div>
                    <g:passwordField name="apiKey" id="api-key" value="${webPageTestServer?.apiKey}" />
                </div>
            </div>

