<div class="form-group fieldcontain  <g:if test="${required}">required</g:if>">
    <g:render template="/_fields/labelTemplate"/>
    <div class="col-md-6">
        <bs:datePicker id="${property}" name="${property}" value="${value}" default="none" class="form-control" />
    </div>
</div>