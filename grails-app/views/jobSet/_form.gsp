<fieldset class="form">

    <div class="form-group fieldcontain ${hasErrors(bean: jobSet, field: 'name', 'error')} ">
        <label for="name" class="control-label col-md-3"><g:message code="jobSet.name.label" default="Name"/></label>
        <div class="col-md-6">
            <g:textField name="name" value="${jobSet?.name}" class="form-control" />
        </div>
    </div>


    <div class="form-group fieldcontain ${hasErrors(bean: csiSystem, field: 'jobGroupWeights', 'error')} ">
        <label for="jobs" class="control-label col-md-3">
            <g:message code="jobSet.jobs.label" default="Jobs"/>
        </label>
        <div class="col-md-6">
            <g:select name="jobs" from="${allJobs}" optionKey="id" value="${jobSet.jobs?.id}"
                      multiple="true" class="form-control "/>
        </div>
    </div>
</fieldset>
