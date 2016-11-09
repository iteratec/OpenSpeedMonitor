<div class="form-group fieldcontain ${hasErrors(bean: bean, field: 'property', 'error')}<g:if test="${required}">required</g:if>">
    <g:render template="/_fields/labelTemplate"/>
    <div class="col-md-6">
        <g:field type="number"  name="${property}" id="${property}" value="${value}" class="form-control" />
    </div>

</div>