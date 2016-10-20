<%@ page import="de.iteratec.osm.csi.Page" %>

<div class="form-group fieldcontain ${hasErrors(bean: pageInstance, field: 'name', 'error')} ">
    <label for="name" class="control-label"><g:message code="page.name.label" default="Name" /></label>
    <div>
        <g:textArea name="name" cols="40" rows="5" maxlength="255" value="${pageInstance?.name}"/>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: pageInstance, field: 'weight', 'error')} required">
    <label for="weight" class="control-label"><g:message code="page.weight.label" default="Weight" /><span class="required-indicator">*</span></label>
    <div>
        <g:field type="number" name="weight" step="any" min="0.0" required="" value="${pageInstance.weight}"/>
    </div>
</div>