<div class="card">
    <legend><g:message
            code="de.iteratec.isr.wptrd.labels.trimValues"
            default="Trim Values"/></legend>

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
                <span class="input-group-addon">ms</span>
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
                <span class="input-group-addon">ms</span>
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
                <span class="input-group-addon">REQ</span>
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
                <span class="input-group-addon">REQ</span>
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
                <span class="input-group-addon">KB</span>
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
                <span class="input-group-addon">KB</span>
            </div>
        </div>
    </div>
</div>