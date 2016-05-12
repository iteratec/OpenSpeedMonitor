<div class="control-group fieldcontain  <g:if test="${required}">required</g:if>">
    <label for="${property}" class="control-label">${label}<g:if test="${required}"><span class="required-indicator">*</span></g:if></label>
    <div class="controls">
        <bs:datePicker id="${property}" name="${property}" value="${value}" default="none"/>
    </div>
</div>