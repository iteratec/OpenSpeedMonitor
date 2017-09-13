<%@ page import="de.iteratec.osm.result.Threshold" %>
<div class="form-group" id="scriptFormGroup">
    <label>
        <g:message code="job.Thresholds.label" default="Thresholds"/>
    </label>
</div>
<g:render template="thresholdTable"/>

<button class="btn btn-default" id="" type="button" id="copyToClipboard">
    <g:message code="job.threshold.copyToClipboard" default="Copy To Clipboard"/>
</button>
<a href="/threshold/create" target="_blank">
    <button id="threshold_button_edit" class="btn btn-default" type="button" id="edit">
        <g:message code="job.threshold.add" default="Add Threshold"/>
    </button>
</a>