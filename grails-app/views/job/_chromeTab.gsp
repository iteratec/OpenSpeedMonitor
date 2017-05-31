<g:render template="inputField" model="${['attribute': 'cmdlineOptions', 'job': job]}" />
<g:render template="checkbox" model="${['booleanAttribute': 'performLighthouseTest', 'job': job]}" />
<g:render template="checkbox" model="${['booleanAttribute': 'trace', 'job': job]}" />
<g:render template="checkbox" model="${['booleanAttribute': 'captureTimeline', 'job': job]}" />

<div id="javascriptCallstack" class="form-group ${hasErrors(bean: job, field: javascriptCallstack, 'has-error')}">
    <label for="inputField-javascriptCallstack" class="col-md-4 control-label">
        <g:message code="job.javascriptCallstack.label" default="javascriptCallstack" />
    </label>
    <div  style="max-width: 100px;" class="col-md-5">
        <input id="javascriptCallstack" class="text short form-control" min="0" max="5" name="javascriptCallstack" value="${job?.javascriptCallstack ?: 0}" required="" type="number"/>
    </div>
</div>

<g:render template="checkbox" model="${['booleanAttribute': 'emulateMobile', 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'mobileDevice', 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'devicePixelRation', 'job': job]}" />