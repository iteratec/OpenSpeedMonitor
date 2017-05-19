<p></p>

<g:each var="stringAttribute"  in="['option_cmdline']">
    <g:render template="inputField" model="${['attribute': stringAttribute, 'job': job]}" />
</g:each>

<g:each var="booleanAttribute" in="['option_lighthouse',
                                    'option_trace']">
	<g:render template="checkbox" model="${['booleanAttribute': booleanAttribute, 'job': job]}" />
</g:each>

<g:render template="checkbox" model="${['booleanAttribute': 'option_timeline', 'job': job]}" />
<div id="option_timelineStack" class="form-group ${hasErrors(bean: job, field: option_timelineStack, 'error')}">
    <label for="inputField-option_timelineStack" class="col-md-3 control-label">
        <g:message code="job.option_timelineStack.label" default="option_timelineStack" />
    </label>
    <div class="col-md-2">
        <input id="option_timelineStack" class="text short form-control" min="0" max="5" name="option_timelineStack" value="${job?.option_timelineStack ?: 0}" required="" type="number"/>
    </div>
</div>

<g:render template="checkbox" model="${['booleanAttribute': 'option_mobile', 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'option_mobileDevice', 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'option_dpr', 'job': job]}" />