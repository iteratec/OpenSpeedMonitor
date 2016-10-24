<div class="row">
    <div class="col-md-4">
        <div class="card">
            <label for="selectAggregatorUncachedHtmlId"><g:message
                    code="de.iteratec.isr.wptrd.labels.filterFirstView"
                    default="First View"/></label>
            <g:if test="${selectedAggrGroupValuesUnCached.size() == 0}"><g:set
                    var="selectedAggrGroupValuesUnCached"
                    value="${['docCompleteTimeInMillisecsUncached']}"/></g:if>
            <iteratec:optGroupedSelect dataMap="${aggrGroupValuesUnCached}"
                                       id="selectAggregatorUncachedHtmlId"
                                       class="iteratec-element-select-higher"
                                       name="selectedAggrGroupValuesUnCached"
                                       optionKey="value" optionValue="value"
                                       multiple="true"
                                       value="${selectedAggrGroupValuesUnCached}"/>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card">
            <label for="selectAggregatorCachedHtmlId"><g:message
                    code="de.iteratec.isr.wptrd.labels.filterRepeatedView"
                    default="Repeated View:"/></label>
            <iteratec:optGroupedSelect id="selectAggregatorCachedHtmlId"
                                       dataMap="${aggrGroupValuesCached}"
                                       multiple="true" id="selectAggregatorCachedHtmlId"
                                       class="iteratec-element-select-higher"
                                       name="selectedAggrGroupValuesCached" optionKey="value"
                                       optionValue="value"
                                       value="${selectedAggrGroupValuesCached}"/>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card">
            <h6><g:message code="de.iteratec.isr.measurand.group.LOAD_TIMES"
                           default="Ladezeiten [ms]"/></h6>

            <div class="input-group">
                <label for="appendedInputBelowLoadTimes">
                    <g:message code="de.iteratec.isr.wptrd.labels.trimbelow"
                               default="Trim below"/>
                </label>
                <input name="trimBelowLoadTimes" id="appendedInputBelowLoadTimes"
                       value="${trimBelowLoadTimes}" class="col-md-1 content-box" type="text"
                       placeholder="...">
                <span class="input-group-addon">ms</span>
            </div>
            <div class="input-group">
                <label for="appendedInputAboveLoadTimes">
                    <g:message code="de.iteratec.isr.wptrd.labels.trimabove"
                               default="Trim above"/>
                </label>
                <input name="trimAboveLoadTimes" id="appendedInputAboveLoadTimes"
                       value="${trimAboveLoadTimes}" class="col-md-1 content-box" type="text"
                       placeholder="...">
                <span class="input-group-addon">ms</span>
            </div>
            <h6><g:message code="de.iteratec.isr.measurand.group.REQUEST_COUNTS"
                           default="Anzahl Requests [c]"/></h6>

            <div class="input-group">
                <label for="appendedInputBelowRequestCounts">
                    <g:message code="de.iteratec.isr.wptrd.labels.trimbelow"
                               default="Trim below"/>
                </label>
                <input name="trimBelowRequestCounts" id="appendedInputBelowRequestCounts"
                       value="${trimBelowRequestCounts}" class="col-md-1 content-box" type="text"
                       placeholder="...">
                <span class="input-group-addon">REQ</span>
            </div>

            <div class="input-group">
                <label for="appendedInputAboveRequestCounts">
                    <g:message code="de.iteratec.isr.wptrd.labels.trimabove"
                               default="Trim above"/>
                </label>
                <input name="trimAboveRequestCounts" id="appendedInputAboveRequestCounts"
                       value="${trimAboveRequestCounts}" class="col-md-1 content-box" type="text"
                       placeholder="...">
                <span class="input-group-addon">REQ</span>
            </div>
            <h6><g:message code="de.iteratec.isr.measurand.group.REQUEST_SIZES"
                           default="Gr&ouml;&szlig;e Requests [kb]"/></h6>

            <div class="input-group">
                <label for="appendedInputBelowRequestSizes">
                    <g:message code="de.iteratec.isr.wptrd.labels.trimbelow"
                               default="Trim below"/>
                </label>
                <input name="trimBelowRequestSizes" id="appendedInputBelowRequestSizes"
                       value="${trimBelowRequestSizes}" class="col-md-1 content-box" type="text"
                       placeholder="...">
                <span class="input-group-addon">KB</span>
            </div>

            <div class="input-group">
                <label for="appendedInputAboveRequestSizes">
                    <g:message code="de.iteratec.isr.wptrd.labels.trimabove"
                               default="Trim above"/>
                </label>
                <input name="trimAboveRequestSizes" id="appendedInputAboveRequestSizes"
                       value="${trimAboveRequestSizes}" class="col-md-1 content-box" type="text"
                       placeholder="...">
                <span class="input-group-addon">KB</span>
            </div>
        </div>
    </div>
</div>