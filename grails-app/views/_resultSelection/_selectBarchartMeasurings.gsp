<div class="card form-horizontal" id="barchartMeasuringCard">
    <h2 for="selectAggregatorUncachedHtmlId">
        <g:message
            code="${multipleSeries ? 'de.iteratec.osm.selectMeasurings' : 'de.iteratec.osm.selectMeasurings.MeasurandLabel'}"
            default="Measurand Series"/>
    </h2>

    <div id="measurandSeries-clone" class="hidden">
        <g:if test="${selectedAggrGroupValuesUnCached.size() == 0}">
            <g:set var="selectedAggrGroupValuesUnCached"
            value="${['docCompleteTimeInMillisecsUncached']}"/>
        </g:if>

        <g:if test="${multipleSeries}">
            <div class="removeMeasurandSeriesContainer">
                <a href="#/" class="removeMeasurandSeriesButton">
                    <i class="fa fa-times" aria-hidden="true"></i>
                </a>
            </div>
        </g:if>

        <div class="row form-group addMeasurandRow">
            <label class="col-sm-3 control-label" for="selectedAggrGroupValuesUnCached">
                <g:message code="de.iteratec.osm.dimple.barchart.measurands"
                           default="Measurands:"/>
            </label>

            <div class="col-sm-7">
                <iteratec:optGroupedSelect dataMap="${aggrGroupValuesUnCached}"
                                           class="firstMeasurandSelect form-control"
                                           name="selectedAggrGroupValuesUnCached"
                                           optionKey="value" optionValue="value"
                                           value="${selectedAggrGroupValuesUnCached}"/>
            </div>

            <g:if test="${multipleMeasurands}">
                <div class="col-sm-2 control-label removeAddMeasurands">
                    <a href="#/" class="addMeasurandButton">
                        <i class="fa fa-lg fa-plus-circle"></i>
                    </a>
                </div>
            </g:if>

        </div>

        <div class="row form-group stackedSelectContainer hidden">
            <div class="col-sm-offset-3 col-sm-7">
                <div class="btn-group btn-group-justified stackedOptions" data-toggle="buttons">
                    <label class="btn btn-default active">
                        <input type="radio" class="stackedOption" autocomplete="off" value="stacked" checked>
                        <g:message code="de.iteratec.osm.dimple.barchart.stacked.true.label"
                                   default="stacked"/>
                    </label>
                    <label class="btn btn-default">
                        <input type="radio" class="stackedOption" autocomplete="off" value="notStacked">
                        <g:message code="de.iteratec.osm.dimple.barchart.stacked.false.label"
                                   default="notStacked"/>
                    </label>
                </div>
            </div>
        </div>
    </div>

    <div id="additionalMeasurand-clone" class="row hidden form-group addMeasurandRow">
        <div class="col-sm-7 col-sm-offset-3">
            <iteratec:optGroupedSelect dataMap="${aggrGroupValuesUnCached}"
                                       class="additionalMeasurand form-control"
                                       optionKey="value" optionValue="value"
                                       value="${selectedAggrGroupValuesUnCached}"/>
        </div>

        <div class="col-sm-2 control-label removeAddMeasurands">
            <a href="#/" class="addMeasurandButton">
                <i class="fa fa-lg fa-plus-circle" aria-hidden="true"></i>
            </a>
            <a href="#/" class="removeMeasurandButton">
                <i class="fa fa-lg fa-minus-circle" aria-hidden="true"></i>
            </a>
        </div>
    </div>

    <g:if test="${multipleSeries}">
        <div class="row">
            <a href="#/" id="addMeasurandSeriesButton">
                <i class="fa fa-lg fa-plus-circle" aria-hidden="true"></i>
                <g:message code="de.iteratec.osm.dimple.barchart.addMeasurandSeriesButton.label"
                           default="Add Measurand Series"/>
            </a>
        </div>
    </g:if>
</div>

<asset:script type="text/javascript">
    $(window).load(function() {
      OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/_resultSelection/selectBarchartMeasurings.js"/>', true, 'barchartMeasurings')
    });
</asset:script>
