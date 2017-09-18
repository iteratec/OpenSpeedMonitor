<%@ page import="de.iteratec.osm.result.Threshold" %>
<%@ defaultCodec="none" %>


<div class="form-group fieldcontain ${hasErrors(bean: threshold, field: 'upperBoundary', 'error')} required">
    <label for="name" class="control-label col-md-3"><g:message code="sdsd" default="Upper Boundary"/><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:textField class="form-control" name="name" cols="40" rows="5" maxlength="255" value="${threshold?.upperBoundary}"/>
    </div>

    <label for="name" class="control-label col-md-3"><g:message code="sdsd" default="Upper Boundary"/><span
        class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:select id="graphiteServers" name="graphiteServers"
                  from=""
                  keys=""
        multiple="false"/>
    </div>
</div>
