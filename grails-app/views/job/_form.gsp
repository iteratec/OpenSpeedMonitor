<%@ page import="de.iteratec.osm.measurement.environment.Location"%>
<%@ page import="de.iteratec.osm.measurement.environment.WebPageTestServer" %>

<h1>${job?.label}</h1>

<div class="tabbable" style="margin-top: 1em;">
  <ul class="nav nav-tabs">
    <li class="active">
      <a id="jobSettingsLink" href="#1" data-toggle="tab"><g:message code="job.form.testSettings.label" default="Test Settings" /></a>
    </li>
    <li>
      <a id="scriptTabLink" href="#2" data-toggle="tab"><g:message code="job.form.script.label" default="Script" /></a>
    </li>
      <li>
          <a id="advancedSettingsTabLink" href="#3" data-toggle="tab"><g:message code="job.form.advanced.label" default="performance" /></a>
      </li>
  </ul>
  <div class="iteratec-tab-content">
    <div class="tab-pane active" id="1" style="margin-top:10px">
	      <g:render template="testSettingsTab" model="${['job': job, 'connectivites': connectivites]}" />
    </div>
    <div class="tab-pane" id="2" style="margin-top:10px">
          <g:render template="scriptTab" model="${['job': job]}" />
    </div>
      <div class="tab-pane" id="3" style="margin-top:10px">
          <g:render template="advancedSettingsTab" model="${['job': job]}" />
      </div>
  </div>
</div>

<!-- included because there is no way for the user to supply a value for validationRequest -->
<g:hiddenField name="validationRequest" value="" />