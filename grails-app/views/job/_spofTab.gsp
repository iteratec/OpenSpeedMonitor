<div id="option_spof" class="form-group ${hasErrors(bean: job, field: option_spof, 'error')}">
    <label for="inputField-option_spof" class="col-md-3 control-label">
        <g:message code="job.option_spof.label" default="option_spof" />
    </label>
    <div class="col-md-9">
        <textarea class="form-control" name="option_spof" rows="3" id="inputField-option_spof">${job?.option_spof?.trim()}</textarea>
        <div class="col-md-9">
            <p></p>
            <p><g:message code="job.option_spof.info" default="info"/></p>
        </div>
    </div>
</div>