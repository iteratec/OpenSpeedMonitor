<div id="${attribute}" class="form-group ${hasErrors(bean: job, field: attribute, 'has-error')}">
    <label for="inputField-${attribute}" class="col-md-4 control-label">
        <g:message code="job.${attribute}.label" default="${attribute}"/>

    </label>

    <div class="col-xs-6">
        <input type="text" class="form-control" name="${attribute}" value="${job?."$attribute"}"
               id="inputField-${attribute}"/>
    </div>
</div>