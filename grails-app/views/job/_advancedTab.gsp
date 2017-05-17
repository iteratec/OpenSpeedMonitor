<div class="form-group ${hasErrors(bean: job, field: 'firstViewOnly', 'error')}">
    <label for="chkbox-first-view" class="col-md-2 control-label">
        <g:message code="job.firstView.label" default="first view"/>
    </label>

    <div class="col-md-10">
        <div class="checkbox">
            <input type="checkbox" id="chkbox-first-view" checked disabled>
        </div>
    </div>
</div>

<div class="form-group ${hasErrors(bean: job, field: 'firstViewOnly', 'error')}">
    <label for="chkbox-repeated-view" class="col-md-2 control-label">
        <g:message code="job.repeatedView.label" default="repeated view"/>
    </label>

    <div class="col-md-10">
        <div class="checkbox">
            <g:checkBox name="repeatedView" value="${job && !job.firstViewOnly ? true : false}" id="chkbox-repeated-view"/>
        </div>
    </div>
</div>

<g:render template="checkbox" model="${['booleanAttribute': 'persistNonMedianResults', 'job': job]}"/>

<g:each var="booleanAttribute" in="['option_isPrivate']">
    <g:render template="checkbox" model="${['booleanAttribute': booleanAttribute, 'job': job]}" />
</g:each>

<div class="form-group ${hasErrors(bean: job, field: 'description', 'error')} required">
    <label for="description" class="col-md-2 control-label">
        <g:message code="job.description.label" default="description"/>
    </label>

    <div class="col-md-10">
        <textarea class="form-control" name="description" id="description"
                  rows="3">${job?.description?.trim()}</textarea>
    </div>
</div>

<div class="form-group">
    <label for="tags" class="col-md-2 control-label">
        <g:message code="job.tags.label" default="tags"/>
    </label>

    <div class="col-md-10">
        <ul name="tags" id="tags">
            <g:each in="${job?.tags}">
                <li>${it}</li>
            </g:each>
        </ul>
    </div>
</div>

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

<g:each var="stringAttribute"  in="['option_uastring',
                                    'option_tester',
                                    'option_appendua',
                                    'option_type',
                                    'option_customHeaders']">
    <g:render template="inputField" model="${['attribute': stringAttribute, 'job': job]}" />
</g:each>

<g:each var="booleanAttribute" in="['web10',
                                    'noscript',
                                    'captureVideo',
                                    'clearcerts',
                                    'ignoreSSL',
                                    'standards',
                                    'tcpdump',
                                    'bodies',
                                    'continuousVideo',
                                    'keepua',
                                    'option_noimages',
                                    'option_pngss',
                                    'option_htmlbody']">
    <g:render template="checkbox" model="${['booleanAttribute': booleanAttribute, 'job': job]}" />
</g:each>