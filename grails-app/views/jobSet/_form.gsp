<fieldset class="form">

    <div class="form-group fieldcontain ${hasErrors(bean: jobSet, field: 'name', 'error')} ">
        <label for="name" class="control-label"><g:message code="jobSet.name.label" default="Name"/></label>

        <div>
            <g:textField name="name" value="${jobSet?.name}"/>
        </div>
    </div>


    <div class="form-group fieldcontain ${hasErrors(bean: csiSystem, field: 'jobGroupWeights', 'error')} ">
        <label for="jobs" class="control-label"><g:message code="jobSet.jobs.label"
                                                           default="Jobs"/></label>

        <div>

            <g:select name="jobs" from="${allJobs}" optionKey="id" value="${jobSet.jobs?.id}" multiple="true"/>

        </div>
    </div>
</fieldset>
