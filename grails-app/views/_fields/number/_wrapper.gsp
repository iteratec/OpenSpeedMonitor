<div class="control-group fieldcontain ${hasErrors(bean: bean, field: 'property', 'error')}<g:if test="${required}">required</g:if>">
    <label for="${property}" class="control-label">
        <g:message code="${bean.class.getCanonicalName()}.label" default="${label}" /><g:if test="${required}"><span class="required-indicator">*</span></g:if>
    </label>
    <div class="controls">
        <g:field type="number"  name="${property}" id="${property}" value="${value}" />
    </div>

</div>