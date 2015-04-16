<%=packageName%>
<r:require modules="prettycron"/>
<g:render template="../editOrCreate" model="['mode': 'create', 'entityName': 'job', 'entityDisplayName': message(code: 'de.iteratec.isj.job', default: 'Job'), 'entity': job]" />
<r:script>
    $(document).ready(
        doOnDomReady("${g.createLink(action: 'nextExecution', absolute: true)}")
    );
</r:script>