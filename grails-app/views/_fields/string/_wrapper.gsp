<div class="form-group fieldcontain ${hasErrors(bean: bean, field: 'property', 'error')}<g:if test="${required}">required</g:if>">
    <g:render template="/_fields/labelTemplate"/>
    <div>
        <g:textField  name="${property}" value="${value}" />
    </div>
</div>