<%@ page import="de.iteratec.osm.result.Measurand" %>
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
                                           class="form-control measurand-select"
                                           name="selectedMeasurandSeries"
                                           optionKey="value" optionValue="value"
                                           value="${[de.iteratec.osm.result.Measurand.DOC_COMPLETE_TIME]}"/>
            </div>
        </div>
    </div>
</div>

<asset:script type="text/javascript">
    $(window).load(function() {
      OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="_resultSelection/selectUserTimingsCard.js" />');
    });
</asset:script>