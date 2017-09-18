<div class="card form-horizontal" id="barchartMeasuringCard">
    <h2 for="selectAggregatorUncachedHtmlId">
        <g:message
            code="${multipleSeries ? 'de.iteratec.osm.selectMeasurings' : 'de.iteratec.osm.selectMeasurings.MeasurandLabel'}"
            default="Measurand Series"/>
    </h2>

    <div id="measurandSeries" class="measurandSeries">
        <div id="measurands">
            <div class="row form-group addMeasurandRow">

                <label class="col-sm-3 control-label" for="selectedAggrGroupValuesUnCached">
                    <g:message code="de.iteratec.osm.barchart.measurands"
                               default="Measurands:"/>
                </label>

                <div class="col-sm-7">
                    <iteratec:optGroupedSelect id="selectedAggrGroupValuesUnCached"
                                               dataMap="${aggrGroupValuesUnCached}"
                                               class="firstMeasurandSelect form-control measurand-select"
                                               name="selectedAggrGroupValuesUnCached"
                                               optionKey="value" optionValue="value"
                                               value="${selectedAggrGroupValuesUnCached}"/>
                </div>
            </div>
        </div>
        <g:if test="${multipleMeasurands}">
            <div class="row">
                <div class="col-sm-7 col-sm-offset-3">
                    <button type="button" id="addMeasurandButton" class="btn btn-default btn-block">
                        <i class="fa fa-plus"></i>
                        <g:message code="de.iteratec.osm.barchart.addMeasurandSeriesButton.label" default="Add Measurand"/>
                    </button>
                </div>
            </div>
        </g:if>
    </div>

    <div id="additionalMeasurand-clone" class="row hidden form-group addMeasurandRow">
        <div class="col-sm-7 col-sm-offset-3">
            <iteratec:optGroupedSelect dataMap="${aggrGroupValuesUnCached}"
                                       class="additionalMeasurand form-control measurand-select"
                                       optionKey="value" optionValue="value"
                                       value="${selectedAggrGroupValuesUnCached}"/>
        </div>
        <div class="col-sm-1 control-label removeAddMeasurands">
            <a href="#" role="button" class="removeMeasurandButton">
                <i class="fa fa-times" aria-hidden="true"></i>
            </a>
        </div>
    </div>
</div>

<asset:script type="text/javascript">
    $(window).load(function() {
      OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/_resultSelection/selectBarchartMeasurings.js"/>', 'barchartMeasurings');
    });
</asset:script>
