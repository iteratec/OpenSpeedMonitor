<%@ page import="de.iteratec.osm.result.Threshold" %>
<%@ page import="de.iteratec.osm.result.Measurand" %>
<%@ page import="de.iteratec.osm.result.MeasuredEvent" %>
<%@ defaultCodec="none" %>

<div class="form-group fieldcontain required">

    <label for="name" class="control-label col-md-3"><g:message code="sdsd" default="Measurand"/><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:select id="measurand" name="measurand"
                  from="${Measurand.values()}"
                  class="form-control chosen"/>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: threshold, field: 'upperBoundary', 'error')} required">
    <label for="name" class="control-label col-md-3"><g:message code="sdsd" default="Upper Boundary"/><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:field class="form-control" type="number" name="upperBoundary" cols="40" rows="5" maxlength="255" value="${threshold?.upperBoundary}"/>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: threshold, field: 'lowerBoundary', 'error')} required">
    <label for="name" class="control-label col-md-3"><g:message code="sdsd" default="Lower Boundary"/><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:field class="form-control" type="number" name="lowerBoundary" cols="40" rows="5" maxlength="255" value="${threshold?.upperBoundary}"/>
    </div>
</div>

<div class="form-group fieldcontain required">

    <label for="name" class="control-label col-md-3"><g:message code="sdsd" default="Measurand"/><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:select id="measurand" name="measurand"
                  from="[]"
                  class="form-control chosen measured-event-select"/>
    </div>
</div>
<asset:script type="text/javascript">
    $(window).load(function() {
      OpenSpeedMonitor.thresholdMeasuredEventList.init({scriptId: "${job?.script?.id}" , targetUrl:"${createLink(controller: 'script', action: 'getMeasuredEventsForScript')}"});
    });
</asset:script>

