<div id="spof" class="form-group ${hasErrors(bean: job, field: spof, 'error')}">
    <label for="inputField-spof" class="col-md-3 control-label">
        <g:message code="job.spof.label" default="spof" />
    </label>
    <div class="col-md-7">
        <textarea class="form-control" name="spof" rows="3" id="inputField-spof">${job?.spof?.trim()}</textarea>
            <p style="margin-top: 5px"><g:message code="job.spof.info" default="info"/></p>
    </div>
</div>