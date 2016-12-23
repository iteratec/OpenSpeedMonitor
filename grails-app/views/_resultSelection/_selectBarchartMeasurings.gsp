<div class="card" id="barchartMeasuringCard">
    <legend for="selectAggregatorUncachedHtmlId"><g:message
            code="de.iteratec.osm.selectMeasurings"
            default="Measurings"/></legend>

    <div id="measurandSeries-clone" class="hidden panel panel-default">
        <g:if test="${selectedAggrGroupValuesUnCached.size() == 0}"><g:set
                var="selectedAggrGroupValuesUnCached"
                value="${['docCompleteTimeInMillisecsUncached']}"/></g:if>

        <div class="panel-body">

            <div class="row addMeasurandRow">
                <label class="col-sm-3"><g:message code="de.iteratec.osm.dimple.barchart.measurands"
                                                   default="Measurands"/></label>

                <iteratec:optGroupedSelect dataMap="${aggrGroupValuesUnCached}"
                                           class="firstMeasurandSelect col-sm-7"
                                           name="selectedAggrGroupValuesUnCached"
                                           optionKey="value" optionValue="value"
                                           value="${selectedAggrGroupValuesUnCached}"/>

                <div class="col-sm-2">
                    <a href="#/" class="addMeasurandButton">
                        <i class="fa fa-lg fa-plus-circle"></i>
                    </a>
                </div>
            </div>

            <div class="row stackedSelectContainer hidden">
                <label class="col-sm-3"><g:message code="de.iteratec.osm.dimple.barchart.stacked.label"
                                                   default="stacked?"/></label>

                <select class="stackedSelect col-sm-7">
                    <option value="stacked"><g:message code="de.iteratec.osm.dimple.barchart.stacked.true.label"
                                                       default="stacked"/></option>
                    <option value="notStacked"><g:message code="de.iteratec.osm.dimple.barchart.stacked.false.label"
                                                          default="notStacked"/></option>
                </select>
            </div>

            <div class="row">
                <div class="col-sm-3">
                    <a href="#" class="btn btn-xs btn-primary removeMeasurandSeriesButton">
                        <g:message code="de.iteratec.osm.dimple.barchart.removeMeasurandSeriesButton.label"
                                   default="remove measurand series"/></a>
                </div>
            </div>
        </div>
    </div>

    <div id="additionalMeasurand-clone" class="row hidden addMeasurandRow">
        <iteratec:optGroupedSelect dataMap="${aggrGroupValuesUnCached}"
                                   class="additionalMeasurand col-sm-7 col-sm-offset-3"
                                   optionKey="value" optionValue="value"
                                   value="${selectedAggrGroupValuesUnCached}"/>

        <div class="col-sm-2">
            <a href="#/" class="addMeasurandButton">
                <i class="fa fa-lg fa-plus-circle"></i>
            </a>
            <a href="#/" class="removeMeasurandButton">
                <i class="fa fa-lg fa-minus-circle"></i>
            </a>
        </div>
    </div>

    <div class="row">
        <div class="col-sm-2">
            <br/>
            <a href="#/" id="addMeasurandSeriesButton" class="btn btn-xs btn-primary">
                <g:message code="de.iteratec.osm.dimple.barchart.addMeasurandSeriesButton.label"
                           default="add measurand series"/></a>
        </div>
    </div>
</div>

<asset:script type="text/javascript">
    $(window).load(function() {
      OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/_resultSelection/selectBarchartMeasurings.js"
                                                               absolute="true"/>')
    });
</asset:script>