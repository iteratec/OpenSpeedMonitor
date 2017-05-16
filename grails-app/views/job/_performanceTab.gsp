<p></p>


<div class="form-group ${hasErrors(bean: job, field: 'maxDownloadTimeInMinutes', 'error')}">
    <label class="col-md-2 control-label" for="maxDownloadTimeInMinutesValue">
        <abbr title="${message(code: 'job.maxDownloadTimeInMinutes.description', args: [defaultMaxDownloadTimeInMinutes])}"
              data-placement="bottom" rel="tooltip">
            <g:message code="job.maxDownloadTimeInMinutes.label" default="maxDownloadTimeInMinutes"/>
        </abbr>
        <span class="required-indicator">*</span>
    </label>

    <div class="col-md-8">
        <span id="maxDownloadTimeInMinutes">
            <g:set var="isEditable" value="${job?.maxDownloadTimeInMinutes != defaultMaxDownloadTimeInMinutes}"/>
            <input type="text" class="form-control ${isEditable ? '' : 'non-editable'}"
                   name="maxDownloadTimeInMinutes"
                   value="${job?.maxDownloadTimeInMinutes}" id="maxDownloadTimeInMinutesValue"
                   placeholder="${defaultMaxDownloadTimeInMinutes}"
                ${isEditable ? '' : 'readonly'}/>
            <g:message code="job.maxDownloadTimeInMinutes.label.unit"/>

            <a href="#" style="${isEditable ? 'display: none' : ''}">
                <g:message code="job.maxDownloadTimeInMinutes.change" default="Ändern"/>
            </a>
        </span>
    </div>
</div>

<div class="form-group ${hasErrors(bean: job, field: 'runs', 'error')} required">
    <label class="col-md-2 control-label" for="runs"><g:message
            code="job.runs.label" default="runs"/> <span
            class="required-indicator">*</span>
    </label>
    <div class="col-md-8">
        <g:textField class="form-control" name="runs" value="${job?.runs ?: 1}"/>
    </div>
</div>

<g:each var="integerAttribute" in="['option_iq']">
    <g:render template="inputField" model="${['attribute': integerAttribute, 'job': job]}" />
</g:each>

<g:each var="booleanAttribute" in="['web10',
                                    'noscript',
                                    'option_noopt',
                                    'option_noimages',
                                    'option_noheaders',
                                    'option_pngss',
                                    'option_mv',
                                    'option_htmlbody']">
    <g:render template="checkbox" model="${['booleanAttribute': booleanAttribute, 'job': job]}" />
</g:each>