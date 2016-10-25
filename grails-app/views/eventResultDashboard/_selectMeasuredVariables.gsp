<div class="row">
    <div class="col-md-4">
        <div class="card">
            <legend for="selectAggregatorUncachedHtmlId"><g:message
                    code="de.iteratec.isr.wptrd.labels.filterFirstView"
                    default="First View"/></legend>
            <g:if test="${selectedAggrGroupValuesUnCached.size() == 0}"><g:set
                    var="selectedAggrGroupValuesUnCached"
                    value="${['docCompleteTimeInMillisecsUncached']}"/></g:if>
            <iteratec:optGroupedSelect dataMap="${aggrGroupValuesUnCached}"
                                       id="selectAggregatorUncachedHtmlId"
                                       class="form-control long-select"
                                       name="selectedAggrGroupValuesUnCached"
                                       optionKey="value" optionValue="value"
                                       multiple="true"
                                       value="${selectedAggrGroupValuesUnCached}"/>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card">
            <legend><g:message
                    code="de.iteratec.isr.wptrd.labels.filterRepeatedView"
                    default="Repeated View"/></legend>
            <iteratec:optGroupedSelect id="selectAggregatorCachedHtmlId"
                                       dataMap="${aggrGroupValuesCached}"
                                       multiple="true" id="selectAggregatorCachedHtmlId"
                                       class="form-control long-select"
                                       name="selectedAggrGroupValuesCached" optionKey="value"
                                       optionValue="value"
                                       value="${selectedAggrGroupValuesCached}"/>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card">
            <legend><g:message
                    code="de.iteratec.isr.wptrd.labels.trimValues"
                    default="Trim Values"/></legend>

            <label><g:message code="de.iteratec.isr.measurand.group.LOAD_TIMES" default="Ladezeiten [ms]"/></label>
            <div class="row">
                <div class="col-md-6 form-group">
                    <label for="appendedInputAboveLoadTimes" class="sub">
                        <g:message code="de.iteratec.isr.wptrd.labels.trimabove" default="Trim above"/>
                    </label>
                    <div class="input-group">
                        <input name="trimAboveLoadTimes" id="appendedInputAboveLoadTimes"
                               value="${trimAboveLoadTimes}" class="form-control" type="text"
                               placeholder="...">
                        <span class="input-group-addon">ms</span>
                    </div>
                </div>
                <div class="col-md-6 form-group">
                    <label for="appendedInputBelowLoadTimes" class="sub">
                        <g:message code="de.iteratec.isr.wptrd.labels.trimbelow" default="Trim below"/>
                    </label>
                    <div class="input-group">
                        <input name="trimBelowLoadTimes" id="appendedInputBelowLoadTimes"
                               value="${trimBelowLoadTimes}" class="form-control" type="text"
                               placeholder="...">
                        <span class="input-group-addon">ms</span>
                    </div>
                </div>
            </div>

            <label><g:message code="de.iteratec.isr.measurand.group.REQUEST_COUNTS"
                           default="Anzahl Requests [c]"/></label>
            <div class="row">
                <div class="col-md-6 form-group">
                    <label for="appendedInputAboveRequestCounts" class="sub">
                        <g:message code="de.iteratec.isr.wptrd.labels.trimabove" default="Trim above"/>
                    </label>
                    <div class="input-group form-group">
                        <input name="trimAboveRequestCounts" id="appendedInputAboveRequestCounts"
                               value="${trimAboveRequestCounts}" class="form-control" type="text"
                               placeholder="...">
                        <span class="input-group-addon">REQ</span>
                    </div>
                </div>
                <div class="col-md-6 form-group">
                    <label for="appendedInputAboveRequestCounts" class="sub">
                        <g:message code="de.iteratec.isr.wptrd.labels.trimbelow" default="Trim below"/>
                    </label>
                    <div class="input-group">
                        <input name="trimBelowRequestCounts" id="appendedInputBelowRequestCounts"
                               value="${trimBelowRequestCounts}" class="form-control" type="text"
                               placeholder="...">
                        <span class="input-group-addon">REQ</span>
                    </div>
                </div>
            </div>


            <label><g:message code="de.iteratec.isr.measurand.group.REQUEST_SIZES"
                           default="Gr&ouml;&szlig;e Requests [kb]"/></label>
            <div class="row">
                <div class="col-md-6 form-group">
                    <label for="appendedInputAboveRequestSizes" class="sub">
                        <g:message code="de.iteratec.isr.wptrd.labels.trimabove"
                                   default="Trim above"/>
                    </label>
                    <div class="input-group">
                        <input name="trimAboveRequestSizes" id="appendedInputAboveRequestSizes"
                               value="${trimAboveRequestSizes}" class="form-control" type="text"
                               placeholder="...">
                        <span class="input-group-addon">KB</span>
                    </div>
                </div>
                <div class="col-md-6 form-group">
                    <label for="appendedInputBelowRequestSizes" class="sub">
                        <g:message code="de.iteratec.isr.wptrd.labels.trimbelow"
                                   default="Trim below"/>
                    </label>
                    <div class="input-group">
                        <input name="trimBelowRequestSizes" id="appendedInputBelowRequestSizes"
                               value="${trimBelowRequestSizes}" class="form-control" type="text"
                               placeholder="...">
                        <span class="input-group-addon">KB</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>