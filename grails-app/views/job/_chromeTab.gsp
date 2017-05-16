<p></p>

<g:each var="stringAttribute"  in="['option_cmdline']">
    <g:render template="inputField" model="${['attribute': stringAttribute, 'job': job]}" />
</g:each>

<g:each var="booleanAttribute" in="['option_lighthouse',
                                    'option_trace']">
	<g:render template="checkbox" model="${['booleanAttribute': booleanAttribute, 'job': job]}" />
</g:each>

<g:render template="checkbox" model="${['booleanAttribute': 'option_timeline', 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'option_timelineStack', 'job': job]}" />

<g:render template="checkbox" model="${['booleanAttribute': 'option_mobile', 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'option_mobileDevice', 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'option_dpr', 'job': job]}" />