<%@ page import="de.iteratec.osm.report.external.GraphiteEventSourcePath" %>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteEventSourcePath, field: 'staticPrefix', 'error')}">
    <label for="staticPrefix" class="control-label col-md-3"><g:message code="graphiteEventSourcePath.staticPrefix.label" default="Static Prefix" /></label>

    <div class="col-md-6">
        <g:textField name="staticPrefix" value="${graphiteEventSourcePath?.staticPrefix}" class="form-control"></g:textField>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteEventSourcePath, field: 'targetMetricName', 'error')} required">
    <label for="targetMetricName" class="control-label col-md-3"><g:message code="graphiteEventSourcePath.targetMetricName.label" default="Target Metric Name" /><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:textField name="targetMetricName" value="${graphiteEventSourcePath?.targetMetricName}" class="form-control"></g:textField>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteEventSourcePath, field: 'jobGroups', 'error')} ">
    <label for="jobGroups" class="control-label col-md-3"><g:message code="event.jobGroup.label" default="Job Group"/></label>

    <div class="col-md-6">
        <g:select name="jobGroups" from="${de.iteratec.osm.measurement.schedule.JobGroup.list()}" multiple="multiple"
                  optionKey="id" size="5" value="${graphiteEventSourcePath?.jobGroups*.id}" class="many-to-many form-control"/>
    </div>
</div>