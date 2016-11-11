<%@ page import="de.iteratec.osm.csi.Page" %>

<div class="form-group fieldcontain ${hasErrors(bean: pageInstance, field: 'name', 'error')} ">
    <label for="name" class="control-label col-md-3"><g:message code="page.name.label" default="Name" /></label>
    <div class="col-md-6">
        <g:textArea name="name" cols="20" rows="1" maxlength="255" value="${pageInstance?.name}" class="form-control" />
    </div>
</div>

