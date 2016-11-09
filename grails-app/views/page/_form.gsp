<%@ page import="de.iteratec.osm.csi.Page" %>

<div class="form-group fieldcontain ${hasErrors(bean: pageInstance, field: 'name', 'error')} ">
    <label for="name" class="control-label col-md-3"><g:message code="page.name.label" default="Name" /></label>
    <div class="col-md-6">
        <g:textArea name="name" cols="40" rows="5" maxlength="255" value="${pageInstance?.name}" class="form-control" />
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: pageInstance, field: 'weight', 'error')} required">
    <label for="weight" class="control-label col-md-3"><g:message code="page.weight.label" default="Weight" /><span class="required-indicator">*</span></label>
    <div class="col-md-6">
        <g:field type="number" name="weight" step="any" min="0.0" required="" value="${pageInstance.weight}" class="form-control"/>
    </div>
</div>