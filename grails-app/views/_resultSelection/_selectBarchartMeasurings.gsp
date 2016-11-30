<div class="card" id="barchartMeasuringCard">
    <legend for="selectAggregatorUncachedHtmlId"><g:message
            code="de.iteratec.osm.selectMeasurings"
            default="Measurings"/></legend>

    <div id="measurandSeries-clone" class="hidden panel panel-default">
        <g:if test="${selectedAggrGroupValuesUnCached.size() == 0}"><g:set
                var="selectedAggrGroupValuesUnCached"
                value="${['docCompleteTimeInMillisecsUncached']}"/></g:if>



        <div>
            <div class="row addMeasurandRow">
                <div class="col-md-3">
                    <label><g:message code="de.iteratec.osm.dimple.barchart.measurands"
                                      default="Measurands"/></label>
                </div>

                <div class="col-md-7">

                    <iteratec:optGroupedSelect dataMap="${aggrGroupValuesUnCached}"
                                               class="firstMeasurandSelect"
                                               name="selectedAggrGroupValuesUnCached"
                                               optionKey="value" optionValue="value"
                                               value="${selectedAggrGroupValuesUnCached}"/>
                </div>

                <div class="col-md-2">
                    <div class="row">
                        <div class="col-md-6 col-md-offset-6">
                            <a href="#" class="addMeasurandButton">
                                <i class="fa fa-lg fa-plus-circle"></i>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row stackedSelectContainer hidden">
            <div class="col-md-3">
                <label><g:message code="de.iteratec.osm.dimple.barchart.stacked.label" default="stacked?"/></label>
            </div>

            <div class="col-md-9">
                <select class="stackedSelect">
                    <option value="stacked"><g:message code="de.iteratec.osm.dimple.barchart.stacked.true.label"
                                                       default="stacked"/></option>
                    <option value="notStacked"><g:message code="de.iteratec.osm.dimple.barchart.stacked.false.label"
                                                          default="notStacked"/></option>
                </select>
            </div>
        </div>
    </div>

    <div id="additionalMeasurand-clone" class="row hidden addMeasurandRow" style="margin-bottom: 10px">
        <div class="col-md-7 col-md-offset-3">
            <iteratec:optGroupedSelect dataMap="${aggrGroupValuesUnCached}" class="additionalMeasurand"
                                       optionKey="value" optionValue="value"
                                       value="${selectedAggrGroupValuesUnCached}"/>
        </div>

        <div class="col-md-2">
            <div class="row">
                <div class="col-md-6">
                    <a href="#" class="removeMeasurandButton">
                        <i class="fa fa-lg fa-minus-circle"></i>
                    </a>
                </div>

                <div class="col-md-6">
                    <a href="#" class="addMeasurandButton">
                        <i class="fa fa-lg fa-plus-circle"></i>
                    </a>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-md-2">
            <br/>
            <a href="#" id="addMeasurandSeriesButton" class="btn btn-xs btn-default">
                <g:message code="de.iteratec.osm.dimple.barchart.addMeasurandSeriesButton.label"
                           default="add measurand series"/></a>
        </div>
    </div>
</div>

<asset:script type="text/javascript">
    $(window).load(function() {
      OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/_resultSelection/_selectBarchartMeasurings.js"
                                                               absolute="true"/>')
    });
</asset:script>