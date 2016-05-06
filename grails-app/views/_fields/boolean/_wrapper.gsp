<div class="control-group fieldcontain">
    <label for="${property}" class="control-label"><g:message code="${bean.class.getCanonicalName()}.label" default="${label}" /></label>
    <div class="controls">
        <bs:checkBox name="${property}" value="${value}" />
    </div>
</div>