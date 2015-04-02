<%=packageName%>
<r:require modules="prettycron"/>
<g:render
        template="../editOrCreate"
        model="['mode': 'edit', 'entityName': 'job', 'entityDisplayName': message(code: 'de.iteratec.isj.job', default: 'Job'), 'entity': job]"
/>
<r:script>
    function getExecutionScheduleSetButInactiveLabel() {
        return '${message(code:'job.executionScheduleSetButInactive.label')}';
    }
    $(document).ready(
        doOnDomReady("${g.createLink(action: 'nextExecution', absolute: true)}")
    );
</r:script>