<div id="${attribute}" class="form-group ${hasErrors(bean: job, field: attribute, 'has-error')}">
    <label class="col-md-4 control-label" for="${attribute}">
        <abbr title="${description}"
              data-placement="bottom" rel="tooltip">
            ${label}
        </abbr>
    </label>

    <div class="col-xs-6">
        <input type="text" class="form-control" name="${attribute}" value="${job?."$attribute"}"
               id="inputField-${attribute}"/>
    </div>
</div>