<div class="form-group fieldcontain  <g:if test="${required}">required</g:if>">
    <g:render template="/_fields/labelTemplate"/>
    <div class="controls">
        <bs:datePicker id="${property}" name="${property}" value="${value}" default="none"/>
    </div>
</div>