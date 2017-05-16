<p></p>

<g:each var="stringAttribute"  in="['option_cmdline']">
    <g:render template="inputField" model="${['attribute': stringAttribute, 'job': job]}" />
</g:each>

<g:each var="integerAttribute" in="['option_timelineStack']">
    <g:render template="inputField" model="${['attribute': integerAttribute, 'job': job]}" />
</g:each>

<g:each var="booleanAttribute" in="['option_timeline',
                                    'option_lighthouse',
                                    'option_trace']">
	<g:render template="checkbox" model="${['booleanAttribute': booleanAttribute, 'job': job]}" />
</g:each>

<g:render template="checkbox" model="${['booleanAttribute': 'option_mobile', 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'option_mobileDevice', 'job': job]}" />
<g:render template="inputField" model="${['attribute': 'option_dpr', 'job': job]}" />