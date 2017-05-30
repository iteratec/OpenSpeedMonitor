<div id="urlsToBlock" class="form-group ${hasErrors(bean: job, field: urlsToBlock, 'error')}">
    <label for="inputField-urlsToBlock" class="col-md-3 control-label">
        <g:message code="job.urlsToBlock.label" default="urlsToBlock" />
    </label>
    <div class="col-md-9">
        <textarea style="max-width: 400px;" class="form-control" name="urlsToBlock" rows="3" id="inputField-urlsToBlock">${job?.urlsToBlock?.trim()}</textarea>
        <p style="max-width: 400px;margin-top: 5px"><g:message code="job.urlsToBlock.info" default="info"/></p>
    </div>
</div>