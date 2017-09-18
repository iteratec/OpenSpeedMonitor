<%@ page import="de.iteratec.osm.result.Threshold" %>
<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<div class="form-group" id="scriptFormGroup">
    <label>
        <g:message code="job.Thresholds.label" default="Thresholds"/>
    </label>
</div>

<div>
    <g:render template="thresholdTable"/>

    <button class="btn btn-default" type="button" id="copyToClipboard">
        <g:message code="job.threshold.copyToClipboard" default="Copy To Clipboard"/>
    </button>

    <a id="thresholdModalLink" href="#thresholdModal" data-toggle="modal"
       title="Create New Threshold">
        <button id="threshold_button_create" class="btn btn-default" type="button"/>
        <g:message code="job.threshold.create.new" default="Add Threshold"/>
    </a>
</div>

<g:render template="/_common/modals/createThresholdModal"/>