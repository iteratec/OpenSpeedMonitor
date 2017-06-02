<g:render template="checkboxTooltip" model="${['booleanAttribute': 'firstViewOnly', 'job': job]}"/>
<g:render template="checkbox" model="${['booleanAttribute': 'isPrivate', 'job': job]}"/>

<div class="form-group required">
    <label for="description" class="col-md-4 control-label">
        <g:message code="job.description.label" default="Description" />
    </label>

    <div class="col-md-6">
        <textarea  class="form-control" name="description" id="description"
                   rows="3">${job?.description?.trim()}</textarea>
    </div>
</div>

<div class="form-group">
    <label for="tags" class="col-md-4 control-label">
        <g:message code="job.tags.label" default="Tags" />
    </label>

    <div class="col-md-6">
        <ul  name="tags" id="tags">
            <g:each in="${job?.tags}">
                <li>${it}</li>
            </g:each>
        </ul>
    </div>
</div>

<g:render template="inputField" model="${['attribute': 'tester', 'job': job]}"/>
<g:render template="inputFieldTooltip" model="${['attribute': 'optionalTestTypes', 'job': job]}"/>

<div id="customHeaders" class="form-group">
    <label class="col-md-4 control-label" for="customHeaders">
        <abbr title="${message(code: "job.customHeaders.description", default: 'Adds custom HTTP headers to the request')}"
              data-placement="bottom" rel="tooltip">
            <g:message code="job.customHeaders.label" default="Custom Headers" />
        </abbr>
    </label>
    <div class="col-md-6">
        <textarea  class="form-control" name="customHeaders" rows="3" id="inputField-customHeaders">${job?.customHeaders?.trim()}</textarea>
    </div>
</div>

<div class="form-group ${hasErrors(bean: job, field: 'runs', 'has-error')} required">
    <label class="col-md-4 control-label" for="runs">
        <g:message code="job.runs.label" default="Runs*" />
    </label>

    <div class="col-md-2">
        <input style="max-width: 100px;" id="runs" class="text short form-control" min="1" max="9" name="runs"
               value="${job?.runs ?: 1}" required="" type="number"/>
    </div>

    <div id="persistNonMedianResults">
        <label for="chkbox-persistNonMedianResults" class="col-md-4 control-label">
            <g:message code="job.persistNonMedianResults.label" default="Persist Non Median Results" />
        </label>
        <div class="col-md-2">
            <div class="checkbox">
                <g:checkBox name="persistNonMedianResults" value="${job?.persistNonMedianResults}" id="chkbox-persistNonMedianResults"/>
            </div>
        </div>
    </div>

</div>

<g:render template="checkbox" model="${['booleanAttribute': 'web10', 'job': job]}"/>
<g:render template="checkbox" model="${['booleanAttribute': 'noscript', 'job': job]}"/>
<g:render template="checkbox" model="${['booleanAttribute': 'captureVideo', 'job': job]}"/>
<g:render template="checkbox" model="${['booleanAttribute': 'clearcerts', 'job': job]}"/>
<g:render template="checkbox" model="${['booleanAttribute': 'ignoreSSL', 'job': job]}"/>
<g:render template="checkbox" model="${['booleanAttribute': 'standards', 'job': job]}"/>
<g:render template="checkbox" model="${['booleanAttribute': 'tcpdump', 'job': job]}"/>

<div id="saveBodies" class="form-group">

    <label class="col-md-4 control-label" for="saveBodies">
        <abbr title="${message(code: "job.saveBodies.description", default: 'Save the content of text responses')}"
              data-placement="bottom" rel="tooltip">
            <g:message code="job.saveBodies.label" default="Save Page Content" />
        </abbr>
    </label>

    <div class="col-md-6">
        <select  id="inputField-saveBodies" name="saveBodies" class="form-control chosen">
            <option value="NONE"
                    <g:if test="${job?.saveBodies == de.iteratec.osm.measurement.schedule.Job.SaveBodies.NONE}">selected</g:if>><g:message
                    code="job.saveBodies.none" default="None" /></option>
            <option value="HTML"
                    <g:if test="${job?.saveBodies == de.iteratec.osm.measurement.schedule.Job.SaveBodies.HTML}">selected</g:if>><g:message
                    code="job.saveBodies.html" default="Base Page Only" /></option>
            <option value="ALL"
                    <g:if test="${job?.saveBodies == de.iteratec.osm.measurement.schedule.Job.SaveBodies.ALL}">selected</g:if>><g:message
                    code="job.saveBodies.all" default="All" /></option>
        </select>
    </div>
</div>

<div id="takeScreenshots" class="form-group">
    <label for="inputField-takeScreenshots" class="col-md-4 control-label">
        <g:message code="job.takeScreenshots.label" default="Screenshots" />
    </label>

    <div class="col-md-6">
        <select  id="inputField-takeScreenshots" name="takeScreenshots" class="form-control chosen">
            <option value="NONE"
                    <g:if test="${job?.takeScreenshots == de.iteratec.osm.measurement.schedule.Job.TakeScreenshots.NONE}">selected</g:if>><g:message
                    code="job.takeScreenshots.none" default="None" /></option>
            <option value="DEFAULT"
                    <g:if test="${job?.takeScreenshots == de.iteratec.osm.measurement.schedule.Job.TakeScreenshots.DEFAULT}">selected</g:if>><g:message
                    code="job.takeScreenshots.default" default="Standard" /></option>
            <option value="FULL"
                    <g:if test="${job?.takeScreenshots == de.iteratec.osm.measurement.schedule.Job.TakeScreenshots.FULL}">selected</g:if>><g:message
                    code="job.takeScreenshots.full" default="Full Resolution" /></option>
        </select>
    </div>
</div>

<g:render template="inputField" model="${['attribute': 'imageQuality', 'job': job]}"/>

<div id="userAgent" class="form-group">

    <label class="col-md-4 control-label" for="userAgent">
        <abbr title="${message(code: "job.userAgent.description", default: 'What to send as the User Agent. Default is append PTST to signify a page test')}"
              data-placement="bottom" rel="tooltip">
            <g:message code="job.userAgent.label" default="User Agent" />
        </abbr>
    </label>

    <div class="col-md-6">
        <select  id="inputField-userAgent" name="userAgent" class="form-control chosen">
            <option value="DEFAULT"
                    <g:if test="${job?.userAgent == de.iteratec.osm.measurement.schedule.Job.UserAgent.DEFAULT}">selected</g:if>><g:message
                    code="job.userAgent.default" default="Append PTST" /></option>
            <option value="ORIGINAL"
                    <g:if test="${job?.userAgent == de.iteratec.osm.measurement.schedule.Job.UserAgent.ORIGINAL}">selected</g:if>><g:message
                    code="job.userAgent.original" default="Append Nothing" /></option>
            <option value="APPEND"
                    <g:if test="${job?.userAgent == de.iteratec.osm.measurement.schedule.Job.UserAgent.APPEND}">selected</g:if>><g:message
                    code="job.userAgent.append" default="Append Suffix" /></option>
            <option value="OVERWRITE"
                    <g:if test="${job?.userAgent == de.iteratec.osm.measurement.schedule.Job.UserAgent.OVERWRITE}">selected</g:if>><g:message
                    code="job.userAgent.overwrite" default="Overwrite" /></option>
        </select>
    </div>
</div>

<g:render template="inputField" model="${['attribute': 'userAgentString', 'job': job]}"/>
<g:render template="inputField" model="${['attribute': 'appendUserAgent', 'job': job]}"/>
