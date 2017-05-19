<div class="card form-horizontal" id="distributionChartMeasuringCard">
    <h2>
        <g:message code="de.iteratec.osm.selectMeasurings"
                   default="Measurings"/>
    </h2>

    <div id="measurandSeries">
        <div class="row form-group">
            <label class="col-sm-3 control-label" for="selectedMeasurandSeries">
                <g:message code="de.iteratec.osm.barchart.measurands"
                           default="Measurands:"/>
            </label>

            <div class="col-sm-7">
                <iteratec:optGroupedSelect dataMap="${measurandsUncached}"
                                           id="measurandSelection"
                                           class="form-control"
                                           name="selectedMeasurandSeries"
                                           optionKey="value" optionValue="value"
                                           value="${['docCompleteTimeInMillisecsUncached']}"/>
            </div>
        </div>
    </div>
</div>
