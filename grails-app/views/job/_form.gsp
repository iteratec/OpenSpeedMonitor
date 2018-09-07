<%@ page import="de.iteratec.osm.measurement.environment.Location"%>
<%@ page import="de.iteratec.osm.measurement.environment.WebPageTestServer" %>

<h1 id="JobName">${job?.label}</h1>

<div class="tabbable">
  <ul class="nav nav-tabs">
    <li class="active">
      <a id="jobSettingsTabLink" href="#jobSettingsTab" data-toggle="tab"><g:message code="job.form.testSettings.label" default="Job Settings"/></a>
    </li>
    <li>
      <a id="scriptTabLink" href="#scriptTab" data-toggle="tab"><g:message code="job.form.script.label" default="Script"/></a>
    </li>
    <li>
      <a id="advancedSettingsTabLink" href="#advancedSettingsTab" data-toggle="tab"><g:message
              code="job.form.advanced.label" default="Advanced Settings"/></a>
    </li>
    <li>
      <a id="thresholdsTabLink" href="#thresholdsTab" data-toggle="tab"><g:message code="job.form.threshold.label"
                                                                                   default="Thresholds"/></a>
    </li>

  </ul>
  <div class="tab-content">
    <div class="tab-pane active" id="jobSettingsTab" >
      <g:render template="jobSettingsTab" model="${['job': job, 'connectivites': connectivites]}" />
    </div>
    <div class="tab-pane" id="scriptTab" >
      <g:render template="scriptTab" model="${['job': job]}" />
    </div>
    <div class="tab-pane" id="advancedSettingsTab" >
      <g:render template="advancedSettingsTab" model="${['job': job, 'globalUserAgentSuffix': globalUserAgentSuffix]}" />
    </div>
    <div class="tab-pane" id="thresholdsTab">
      <g:render template="thresholdsTab" model="${['job': job]}"/>
    </div>
  </div>
</div>

<!-- included because there is no way for the user to supply a value for validationRequest -->
<g:hiddenField name="validationRequest" value="" />
