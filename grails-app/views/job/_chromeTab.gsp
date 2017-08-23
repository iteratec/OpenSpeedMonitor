<g:render template="inputField" model="${['attribute': 'cmdlineOptions','label': "${message(code: 'job.cmdlineOptions.label', default: 'Commandline Options')}", 'job': job]}" />
<g:render template="checkbox" model="${['booleanAttribute': 'performLighthouseTest','label': "${message(code: 'job.performLighthouseTest.label', default: 'Perform Lighthouse Test')}", 'job': job]}" />
<g:render template="checkbox" model="${['booleanAttribute': 'trace','label': "${message(code: 'job.trace.label', default: 'Capture Chrome Trace')}", 'job': job]}" />

<div id="traceCategories" class="form-group">
    <label for="inputField-traceCategories" class="col-md-4 control-label">
        <g:message code="job.traceCategories.label" default="Trace Categories"/>
    </label>
    <div class="col-md-6">
        <textarea  class="form-control" name="traceCategories" rows="3" id="inputField-traceCategories">${job?.traceCategories?.trim()}</textarea>
    </div>
</div>

<g:render template="checkbox" model="${['booleanAttribute': 'captureTimeline','label': "${message(code: 'job.captureTimeline.label', default: 'Capture The Dev Tools Timeline')}", 'job': job]}" />

<div id="javascriptCallstack" class="form-group ${hasErrors(bean: job, field: javascriptCallstack, 'has-error')}">
    <label for="inputField-javascriptCallstack" class="col-md-4 control-label">
        <g:message code="job.javascriptCallstack.label" default="Include Javascript Callstack" />
    </label>
    <div  style="max-width: 100px;" class="col-md-5">
        <input id="javascriptCallstack" class="text short form-control" min="0" max="5" name="javascriptCallstack" value="${job?.javascriptCallstack ?: 0}" required="" type="number"/>
    </div>
</div>

<g:render template="checkbox" model="${['booleanAttribute': 'emulateMobile','label': "${message(code: 'job.emulateMobile.label', default: 'Emulate Mobile Browser')}", 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'mobileDevice','label': "${message(code: 'job.mobileDevice.label', default: 'Mobile Device')}", 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'devicePixelRation','label': "${message(code: 'job.devicePixelRation.label', default: 'Device Pixel Ratio')}", 'job': job]}" />
