<div class="card form-horizontal" id="barchartMeasuringCard">
    <h2 for="selectAggregatorUncachedHtmlId">
        <g:message
            code="${multipleSeries ? 'de.iteratec.osm.selectMeasurings' : 'de.iteratec.osm.selectMeasurings.MeasurandLabel'}"
            default="Measurand Series"/>
    </h2>

    <div id="measurandSeries" class="measurandSeries">
        %{--<g:if test="${selectedAggrGroupValuesUnCached.size() == 0}">--}%
            <g:set var="selectedAggrGroupValuesUnCached"
            value="${['docCompleteTimeInMillisecsUncached']}"/>
        %{--</g:if>--}%
        <div id="measurands">
            <div class="row form-group addMeasurandRow">

                <label class="col-sm-3 control-label" for="selectedAggrGroupValuesUnCached">
                    <g:message code="de.iteratec.osm.barchart.measurands"
                               default="Measurands:"/>
                </label>

                <div class="col-sm-7">
                    <iteratec:optGroupedSelect dataMap="${aggrGroupValuesUnCached}"
                                               class="firstMeasurandSelect form-control"
                                               name="selectedAggrGroupValuesUnCached"
                                               optionKey="value" optionValue="value"
                                               value="${selectedAggrGroupValuesUnCached}"/>
                </div>
            </div>
        </div>
        <g:if test="${multipleMeasurands}">
            <button type="button" id="addMeasurandButton" class="btn btn-primary btn-block">Add Measurand</button>
        </g:if>
    </div>

    <div id="additionalMeasurand-clone" class="row hidden form-group addMeasurandRow">
        <div class="col-sm-7 col-sm-offset-3">
            <iteratec:optGroupedSelect dataMap="${aggrGroupValuesUnCached}"
                                       class="additionalMeasurand form-control"
                                       optionKey="value" optionValue="value"
                                       value="${selectedAggrGroupValuesUnCached}"/>
        </div>

        <div class="col-sm-2 control-label removeAddMeasurands">
            <a href="#/" class="removeMeasurandButton">
                <i class="fa fa-lg fa-minus-circle" aria-hidden="true"></i>
            </a>
        </div>
    </div>
</div>

<asset:script type="text/javascript">
    $(window).load(function() {
      OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/_resultSelection/selectBarchartMeasurings.js"/>', true, 'barchartMeasurings')
    });
</asset:script>