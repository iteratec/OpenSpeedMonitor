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

<g:each var="stringAttribute"  in="['option_tester',
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
                                    'tcpdump']">
    <g:render template="checkbox" model="${['booleanAttribute': booleanAttribute, 'job': job]}" />
</g:each>

<div id="option_saveBodies" class="form-group ${hasErrors(bean: job, field: option_saveBodies, 'error')}">
    <label for="inputField-option_saveBodies" class="col-md-2 control-label">
        <g:message code="job.option_saveBodies.label" default="option_saveBodies" />
    </label>
    <div class="col-md-5">
        <select id="inputField-option_saveBodies" name="option_saveBodies" class="form-control chosen">
            <option value="NONE" <g:if test="${job?.option_saveBodies==de.iteratec.osm.measurement.schedule.Job.SaveBodies.NONE}">selected</g:if>><g:message code="job.option_saveBodies.none" default="none" /></option>
            <option value="HTML" <g:if test="${job?.option_saveBodies==de.iteratec.osm.measurement.schedule.Job.SaveBodies.HTML}">selected</g:if>><g:message code="job.option_saveBodies.html" default="html" /></option>
            <option value="ALL"  <g:if test="${job?.option_saveBodies==de.iteratec.osm.measurement.schedule.Job.SaveBodies.ALL }">selected</g:if>><g:message code="job.option_saveBodies.all" default="all" /></option>
        </select>
    </div>
</div>

<div id="option_takeScreenshots" class="form-group ${hasErrors(bean: job, field: option_takeScreenshots, 'error')}">
    <label for="inputField-option_takeScreenshots" class="col-md-2 control-label">
        <g:message code="job.option_takeScreenshots.label" default="option_takeScreenshots" />
    </label>
    <div class="col-md-5">
        <select id="inputField-option_takeScreenshots" name="option_takeScreenshots" class="form-control chosen">
            <option value="NONE"    <g:if test="${job?.option_takeScreenshots==de.iteratec.osm.measurement.schedule.Job.TakeScreenshots.NONE   }">selected</g:if>><g:message code="job.option_takeScreenshots.none" default="none" /></option>
            <option value="DEFAULT" <g:if test="${job?.option_takeScreenshots==de.iteratec.osm.measurement.schedule.Job.TakeScreenshots.DEFAULT}">selected</g:if>><g:message code="job.option_takeScreenshots.default" default="default" /></option>
            <option value="FULL"    <g:if test="${job?.option_takeScreenshots==de.iteratec.osm.measurement.schedule.Job.TakeScreenshots.FULL   }">selected</g:if>><g:message code="job.option_takeScreenshots.full" default="full" /></option>
        </select>
    </div>
</div>

<g:render template="inputField" model="${['attribute': 'option_iq', 'job': job]}" />

<div id="option_userAgent" class="form-group ${hasErrors(bean: job, field: option_userAgent, 'error')}">
    <label for="inputField-option_userAgent" class="col-md-2 control-label">
        <g:message code="job.option_userAgent.label" default="option_userAgent" />
    </label>
    <div class="col-md-5">
        <select id="inputField-option_userAgent" name="option_userAgent" class="form-control chosen">
            <option value="DEFAULT"   <g:if test="${job?.option_userAgent==de.iteratec.osm.measurement.schedule.Job.UserAgent.DEFAULT  }">selected</g:if>><g:message code="job.option_userAgent.default" default="default" /></option>
            <option value="ORIGINAL"  <g:if test="${job?.option_userAgent==de.iteratec.osm.measurement.schedule.Job.UserAgent.ORIGINAL }">selected</g:if>><g:message code="job.option_userAgent.original" default="original" /></option>
            <option value="APPEND"    <g:if test="${job?.option_userAgent==de.iteratec.osm.measurement.schedule.Job.UserAgent.APPEND   }">selected</g:if>><g:message code="job.option_userAgent.append" default="append" /></option>
            <option value="OVERWRITE" <g:if test="${job?.option_userAgent==de.iteratec.osm.measurement.schedule.Job.UserAgent.OVERWRITE}">selected</g:if>><g:message code="job.option_userAgent.overwrite" default="overwrite" /></option>
        </select>
    </div>
</div>

<g:render template="inputField" model="${['attribute': 'option_uastring', 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'option_appendua', 'job': job]}" />