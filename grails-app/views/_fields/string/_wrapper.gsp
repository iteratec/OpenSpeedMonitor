<div class="control-group fieldcontain ${hasErrors(bean: bean, field: 'property', 'error')}<g:if test="${required}">required</g:if>">
    <label for="${property}" class="control-label">
        <g:message code="${bean.class.getCanonicalName()}.label" default="${label}" /><g:if test="${required}"><span class="required-indicator">*</span></g:if>
    </label>
    <div class="controls">
        <g:if test="${required}">
            <g:textField  name="${property}" value="${value}" required=""  />
        </g:if>
        <g:else>
            <g:textField  name="${property}" value="${value}"/>
        </g:else>
    </div>
</div>