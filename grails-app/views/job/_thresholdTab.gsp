<div id="threshold" class="form-group ${hasErrors(bean: job, field: customMetrics, 'has-error')}">
    <label>
        <g:message code="job.Thresholds.label" default="Thresholds"/>
    </label>
    <div>
        <g:render template="thresholdTable"/>
    </div>
</div>
