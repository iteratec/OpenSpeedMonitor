<%@ page import="de.iteratec.osm.result.MeasurandGroup" %>
<div class="card">
    <h2><g:message
            code="de.iteratec.isr.wptrd.labels.trimValues"
            default="Trim Values"/></h2>

    <label><g:message code="de.iteratec.isr.measurand.group.LOAD_TIMES" default="Ladezeiten [ms]"/></label>
    <div class="row">
        <div class="col-md-6 form-group">
            <label for="appendedInputBelowLoadTimes" class="sub">
                <g:message code="de.iteratec.isr.wptrd.labels.trimbelow" default="Trim below"/>
            </label>
            <div class="input-group">
                <input name="trimBelowLoadTimes" id="appendedInputBelowLoadTimes"
                       value="${trimBelowLoadTimes}" class="form-control trim-selection" type="text"
                       placeholder="...">
                <span class="input-group-addon">${de.iteratec.osm.result.MeasurandGroup.LOAD_TIMES.getUnit().getLabel()}</span>
            </div>
        </div>
        <div class="col-md-6 form-group">
            <label for="appendedInputAboveLoadTimes" class="sub">
                <g:message code="de.iteratec.isr.wptrd.labels.trimabove" default="Trim above"/>
            </label>
            <div class="input-group">
                <input name="trimAboveLoadTimes" id="appendedInputAboveLoadTimes"
                       value="${trimAboveLoadTimes}" class="form-control trim-selection" type="text"
                       placeholder="...">
                <span class="input-group-addon">${MeasurandGroup.LOAD_TIMES.getUnit().getLabel()}</span>
            </div>
        </div>
    </div>

    <label><g:message code="de.iteratec.isr.measurand.group.REQUEST_COUNTS"
                      default="Anzahl Requests [c]"/></label>
    <div class="row">
        <div class="col-md-6 form-group">
            <label for="appendedInputBelowRequestCounts" class="sub">
                <g:message code="de.iteratec.isr.wptrd.labels.trimbelow" default="Trim below"/>
            </label>
            <div class="input-group">
                <input name="trimBelowRequestCounts" id="appendedInputBelowRequestCounts"
                       value="${trimBelowRequestCounts}" class="form-control trim-selection" type="text"
                       placeholder="...">
                <span class="input-group-addon">${MeasurandGroup.REQUEST_COUNTS.getUnit().getLabel()}</span>
            </div>
        </div>
        <div class="col-md-6 form-group">
            <label for="appendedInputAboveRequestCounts" class="sub">
                <g:message code="de.iteratec.isr.wptrd.labels.trimabove" default="Trim above"/>
            </label>
            <div class="input-group form-group">
                <input name="trimAboveRequestCounts" id="appendedInputAboveRequestCounts"
                       value="${trimAboveRequestCounts}" class="form-control trim-selection" type="text"
                       placeholder="...">
                <span class="input-group-addon">${MeasurandGroup.REQUEST_COUNTS.getUnit().getLabel()}</span>
            </div>
        </div>
    </div>


    <label><g:message code="de.iteratec.isr.measurand.group.REQUEST_SIZES"
                      default="Gr&ouml;&szlig;e Requests [kb]"/></label>
    <div class="row">
        <div class="col-md-6 form-group">
            <label for="appendedInputBelowRequestSizes" class="sub">
                <g:message code="de.iteratec.isr.wptrd.labels.trimbelow"
                           default="Trim below"/>
            </label>
            <div class="input-group">
                <input name="trimBelowRequestSizes" id="appendedInputBelowRequestSizes"
                       value="${trimBelowRequestSizes}" class="form-control trim-selection" type="text"
                       placeholder="...">
                <span class="input-group-addon">${MeasurandGroup.REQUEST_SIZES.getUnit().getLabel()}</span>
            </div>
        </div>
        <div class="col-md-6 form-group">
            <label for="appendedInputAboveRequestSizes" class="sub">
                <g:message code="de.iteratec.isr.wptrd.labels.trimabove"
                           default="Trim above"/>
            </label>
            <div class="input-group">
                <input name="trimAboveRequestSizes" id="appendedInputAboveRequestSizes"
                       value="${trimAboveRequestSizes}" class="form-control trim-selection" type="text"
                       placeholder="...">
                <span class="input-group-addon">${MeasurandGroup.REQUEST_SIZES.getUnit().getLabel()}</span>
            </div>
        </div>
    </div>
</div>
