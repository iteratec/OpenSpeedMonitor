<%@ page import="de.iteratec.osm.measurement.environment.WebPageTestServer" %>



			<div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'label', 'error')} required">
				<label for="label" class="control-label col-md-3 col-md-3">
					<g:message code="webPageTestServer.label.label" default="Label" />
					<span class="required-indicator">*</span>
				</label>
				<div class="col-md-6">
					<g:textField name="label" maxlength="150"  value="${webPageTestServer?.label}" class="form-control" />
				</div>
			</div>

			<div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'proxyIdentifier', 'error')} required">
				<label for="proxyIdentifier" class="control-label col-md-3"><g:message code="webPageTestServer.proxyIdentifier.label" default="Proxy Identifier" /><span class="required-indicator">*</span></label>
				<div class="col-md-6">
					<g:textField name="proxyIdentifier"  value="${webPageTestServer?.proxyIdentifier}" class="form-control" />
				</div>
			</div>

			<div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'active', 'error')} ">
				<label for="active" class="control-label col-md-3"><g:message code="webPageTestServer.active.label" default="Active" /></label>
				<div class="col-md-6">
					<bs:checkBox name="active" value="${webPageTestServer?.active}" />
				</div>
			</div>

			<div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'description', 'error')} ">
				<label for="description" class="control-label col-md-3"><g:message code="webPageTestServer.description.label" default="Description" /></label>
				<div class="col-md-6">
					<g:textArea name="description" cols="40" rows="5" value="${webPageTestServer?.description}" class="form-control" />
				</div>
			</div>

			<div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'baseUrl', 'error')} required">
				<label for="baseUrl" class="control-label col-md-3"><g:message code="webPageTestServer.baseUrl.label" default="Base Url" /><span class="required-indicator">*</span></label>
				<div class="col-md-6">
					<g:field type="url" name="baseUrl"  value="${webPageTestServer?.baseUrl}" class="form-control" />
				</div>
			</div>

			<div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'contactPersonName', 'error')} ">
				<label for="contactPersonName" class="control-label col-md-3"><g:message code="webPageTestServer.contactPersonName.label" default="Contact Person Name" /></label>
				<div class="col-md-6">
					<g:textField name="contactPersonName" maxlength="200" value="${webPageTestServer?.contactPersonName}" class="form-control" />
				</div>
			</div>

			<div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'contactPersonEmail', 'error')} ">
				<label for="contactPersonEmail" class="control-label col-md-3"><g:message code="webPageTestServer.contactPersonEmail.label" default="Contact Person Email" /></label>
				<div class="col-md-6">
					<g:field type="email" name="contactPersonEmail" value="${webPageTestServer?.contactPersonEmail}" class="form-control" />
				</div>
			</div>

            <div class="form-group fieldcontain ${hasErrors(bean: webPageTestServer, field: 'apiKey', 'error')}">
                <label for="api-key" class="control-label col-md-3">
                    <g:message code="webPageTestServer.apiKey.label" default="API key" />
                </label>
                <div class="col-md-6">
                    <g:passwordField name="apiKey" id="api-key" value="${webPageTestServer?.apiKey}" class="form-control" />
                </div>
            </div>

