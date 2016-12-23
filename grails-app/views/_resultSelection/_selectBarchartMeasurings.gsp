<div class="card form-horizontal" id="barchartMeasuringCard">
    <legend for="selectAggregatorUncachedHtmlId"><g:message
            code="de.iteratec.osm.selectMeasurings"
            default="Measurings"/></legend>

    <div id="measurandSeries-clone" class="hidden panel panel-default">
        <g:if test="${selectedAggrGroupValuesUnCached.size() == 0}"><g:set
                var="selectedAggrGroupValuesUnCached"
                value="${['docCompleteTimeInMillisecsUncached']}"/></g:if>

        <div class="panel-body" style="position: relative;">

            <div style="position: absolute; right: 5px; top: 2px;">
                <a href="#/" class="removeMeasurandSeriesButton">
                    <i class="fa fa-times" aria-hidden="true" style="color: #cccccc;"></i>
                </a>
            </div>

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

                <div class="col-sm-2 control-label" style="text-align: left;">
                    <a href="#/" class="addMeasurandButton">
                        <i class="fa fa-lg fa-plus-circle"></i>
                    </a>
                </div>
            </div>

            <div class="row form-group stackedSelectContainer hidden">
                <label class="col-sm-3 control-label">
                    <g:message code="de.iteratec.osm.dimple.barchart.stacked.label"
                               default="Stacked?"/>
                </label>
                <div class="col-sm-7">
                    <select class="stackedSelect form-control">
                        <option value="stacked"><g:message code="de.iteratec.osm.dimple.barchart.stacked.true.label"
                                                           default="stacked"/></option>
                        <option value="notStacked"><g:message code="de.iteratec.osm.dimple.barchart.stacked.false.label"
                                                              default="notStacked"/></option>
                    </select>
                </div>
            </div>

        </div>
    </div>

    <div id="additionalMeasurand-clone" class="row hidden addMeasurandRow">
        <div class="col-sm-7 col-sm-offset-3">
            <iteratec:optGroupedSelect dataMap="${aggrGroupValuesUnCached}"
                                       class="additionalMeasurand form-control"
                                       optionKey="value" optionValue="value"
                                       value="${selectedAggrGroupValuesUnCached}"/>
        </div>

        <div class="col-sm-2 control-label" style="text-align: left;">
            <a href="#/" class="addMeasurandButton">
                <i class="fa fa-lg fa-plus-circle" aria-hidden="true"></i>
            </a>
            <a href="#/" class="removeMeasurandButton">
                <i class="fa fa-lg fa-minus-circle" aria-hidden="true"></i>
            </a>
        </div>
    </div>

    <div class="row">
        <a href="#/" id="addMeasurandSeriesButton">
            <i class="fa fa-lg fa-plus-circle" aria-hidden="true"></i>
            <g:message code="de.iteratec.osm.dimple.barchart.addMeasurandSeriesButton.label"
                       default="Add Measurand Series"/>
        </a>
    </div>
</div>

<asset:script type="text/javascript">
    $(window).load(function() {
      OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/_resultSelection/selectBarchartMeasurings.js"
                                                               absolute="true"/>')
    });
</asset:script>