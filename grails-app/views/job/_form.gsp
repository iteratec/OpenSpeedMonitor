<%@ page import="de.iteratec.osm.measurement.environment.Location"%>
<%@ page import="de.iteratec.osm.measurement.environment.WebPageTestServer" %>

<div class="row form-group ${hasErrors(bean: job, field: 'label', 'error')} required">
	<label for="label" class="col-md-2 control-label">
		<g:message code="job.label.label" default="label" /><span class="required-indicator">*</span>
	</label>
	<div class="col-md-5">
		<g:textField class="form-control job-label" name="label" value="${job?.label}" />
	</div>
</div>

<div class="row form-group ${hasErrors(bean: job, field: 'location', 'error')} required">
	<label class="col-md-2 control-label" for="location">
		<g:message code="job.location.label" default="location" /><span class="required-indicator">*</span>
	</label> 
	<div class="col-md-5">
		<select id="location" class="form-control chosen" name="location.id">
			<g:each in="${WebPageTestServer.findAllByActive(true)}" var="server">
				<optgroup label="${server.label}">
					<g:each in="${Location.findAllByWptServerAndActive(server, true)}" var="loc">
						<option value="${loc.id}" <g:if test="${job?.location?.id==loc.id}">selected</g:if>>${loc.uniqueIdentifierForServer ?: loc.location}</option>
					</g:each>
				</optgroup>
			</g:each>
		</select>
	</div>
</div>

<div class="tabbable" style="margin-top: 1em;">
  <ul class="nav nav-tabs">
    <li class="active">
      <a href="#1" data-toggle="tab"><g:message code="job.form.testSettings.label" default="Test Settings" /></a>
    </li>
    <li>
      <a id="scriptTabLink" href="#2" data-toggle="tab"><g:message code="job.form.script.label" default="Script" /></a>
    </li>
    <li>
      <a href="#3" data-toggle="tab"><g:message code="job.form.advanced.label" default="Advanced" /></a>
    </li>            
  </ul>
  <div class="iteratec-tab-content">
    <div class="tab-pane active" id="1">
	  <g:render template="testSettingsTab" model="${['job': job, 'connectivites': connectivites]}" />
    </div>
    <div class="tab-pane" id="2">
      <g:render template="scriptTab" model="${['job': job]}" />
    </div>
    <div class="tab-pane" id="3">
      <g:render template="advancedTab" model="${['job': job]}" />
    </div>
  </div>
</div>

<!-- included because there is no way for the user to supply a value for validationRequest -->
<g:hiddenField name="validationRequest" value="" />