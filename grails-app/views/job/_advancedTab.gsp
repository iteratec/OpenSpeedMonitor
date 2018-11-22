<g:render template="checkboxTooltip" model="${['booleanAttribute': 'firstViewOnly','label': "${message(code: 'job.firstViewOnly.label', default: 'First View Only')}",'description': "${message(code: 'job.firstViewOnly.description', default: 'If not checked there will be a test with a clean cache followed by a test with the same cache')}", 'job': job]}"/>
<g:render template="checkbox" model="${['booleanAttribute': 'isPrivate','label': "${message(code: 'job.isPrivate.label', default: 'Private')}", 'job': job]}"/>

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

<g:render template="inputField" model="${['attribute': 'tester','label': "${message(code: 'job.tester.label', default: 'WPT Test Agent')}", 'job': job]}"/>
<g:render template="inputFieldTooltip" model="${['attribute': 'optionalTestTypes','label': "${message(code: 'job.optionalTestTypes.label', default: 'Test Type')}",'description': "${message(code: 'job.optionalTestTypes.description', default: 'For running alternative test types, can specify &apos;traceroute&apos; or &apos;lighthouse&apos; (lighthouse as a test type is only supported on wptagent agents)')}", 'job': job]}"/>

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
        <label for="chkbox-persistNonMedianResults" class="col-md-3 control-label">
            <g:message code="job.persistNonMedianResults.label" default="Persist Non Median Results" />
        </label>
        <div class="col-md-2">
            <div class="checkbox">
                <g:checkBox name="persistNonMedianResults" value="${job?.persistNonMedianResults}" id="chkbox-persistNonMedianResults"/>
            </div>
        </div>
    </div>

</div>

<g:render template="checkbox" model="${['booleanAttribute': 'web10','label': "${message(code: 'job.web10.label', default: 'Stop Test at Document Complete')}", 'job': job]}"/>
<g:render template="checkbox" model="${['booleanAttribute': 'noscript','label': "${message(code: 'job.noscript.label', default: 'Disable Javascript')}", 'job': job]}"/>
<g:render template="checkbox" model="${['booleanAttribute': 'captureVideo','label': "${message(code: 'job.captureVideo.label', default: 'Capture Video')}", 'job': job]}"/>
<g:render template="checkbox" model="${['booleanAttribute': 'clearcerts','label': "${message(code: 'job.clearcerts.label', default: 'Clear SSL Certificate Caches')}", 'job': job]}"/>
<g:render template="checkbox" model="${['booleanAttribute': 'ignoreSSL','label': "${message(code: 'job.ignoreSSL.label', default: 'Ignore SSL Certificate Errors')}", 'job': job]}"/>
<g:render template="checkbox" model="${['booleanAttribute': 'standards','label': "${message(code: 'job.standards.label', default: 'Disable Compatibility View (IE Only)')}", 'job': job]}"/>
<g:render template="checkbox" model="${['booleanAttribute': 'tcpdump','label': "${message(code: 'job.tcpdump.label', default: 'Capture network packet trace (tcpdump)')}", 'job': job]}"/>
<g:render template="checkbox" model="${['booleanAttribute': 'heroElementTimes','label': "${message(code: 'job.heroElementTimes.label', default: 'Hero element time')}", 'job': job]}"/>

<div id="heroElements" class="form-group">
    <label class="col-md-4 control-label" for="heroElements">
        <g:message code="job.heroElements.label" default="Custom Hero Elements (JSON)"/>
    </label>
    <div class="col-md-6">
        <textarea  class="form-control" name="heroElements" rows="3" id="inputField-heroElements">${job?.heroElements?.trim()}</textarea>
    </div>
</div>

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

<g:render template="inputField" model="${['attribute': 'imageQuality','label': "${message(code: 'job.imageQuality.label', default: 'JPEG Compression Level (30-100)')}", 'job': job]}"/>

<g:if test="${globalUserAgentSuffix}">
    <div id="globalUserAgent">
        <g:render template="checkbox" model="${['booleanAttribute': 'useGlobalUASuffix',
                                                'label': "${message(code: 'job.userAgent.useGlobalSuffix',default: 'Use global User Agent Suffix:')} " + globalUserAgentSuffix,
                                                'job': job]}"/>
    </div>
</g:if>

<div id="userAgent">
    <div class="form-group">
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

    <g:render template="inputField" model="${['attribute': 'userAgentString','label': "${message(code: 'job.userAgentString.label', default: 'New User Agent')}", 'job': job]}"/>
    <g:render template="inputField" model="${['attribute': 'appendUserAgent','label': "${message(code: 'job.appendUserAgent.label', default: 'Append UA')}", 'job': job]}"/>
</div>