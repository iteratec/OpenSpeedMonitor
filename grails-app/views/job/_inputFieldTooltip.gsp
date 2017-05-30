<div id="${attribute}" class="form-group ${hasErrors(bean: job, field: attribute, 'error')}">
    <label class="col-md-4 control-label" for="${attribute}">
        <abbr title="${message(code: "job.${attribute}.description")}"
              data-placement="bottom" rel="tooltip">
            <g:message code="job.${attribute}.label" default="${attribute}"/>
        </abbr>
    </label>

    <div class="col-xs-6">
        <input style="max-width: 400px;" type="text" class="form-control" name="${attribute}" value="${job?."$attribute"}"
               id="inputField-${attribute}"/>
    </div>
</div>