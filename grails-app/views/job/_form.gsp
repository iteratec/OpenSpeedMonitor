<%@ page import="de.iteratec.osm.measurement.environment.Location"%>
<%@ page import="de.iteratec.osm.measurement.environment.WebPageTestServer" %>

<h1 id="JobName">${job?.label}</h1>

<div class="tabbable" style="margin-top: 1em;">
  <ul class="nav nav-tabs">
    <li class="active">
      <a id="jobSettingsTabLink" href="#jobSettingsTab" data-toggle="tab"><g:message code="job.form.testSettings.label" default="Test Settings" /></a>
    </li>
    <li>
      <a id="scriptTabLink" href="#scriptTab" data-toggle="tab"><g:message code="job.form.script.label" default="Script" /></a>
    </li>
      <li>
          <a id="advancedSettingsTabLink" href="#advancedSettingsTab" data-toggle="tab"><g:message code="job.form.advanced.label" default="performance" /></a>
      </li>
  </ul>
  <div class="iteratec-tab-content">
    <div class="tab-pane active" id="jobSettingsTab" style="margin-top:20px">
	      <g:render template="jobSettingsTab" model="${['job': job, 'connectivites': connectivites]}" />
    </div>
    <div class="tab-pane" id="scriptTab" style="margin-top:20px">
          <g:render template="scriptTab" model="${['job': job]}" />
    </div>
      <div class="tab-pane" id="advancedSettingsTab" style="margin-top:20px">
          <g:render template="advancedSettingsTab" model="${['job': job]}" />
      </div>
  </div>
</div>

<!-- included because there is no way for the user to supply a value for validationRequest -->
<g:hiddenField name="validationRequest" value="" />