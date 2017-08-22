<div class="card form-horizontal" id="pageComparisonSelectionCard">
    <h2><g:message
            code="de.iteratec.osm.pageComparison.card.title"
            default="Pages"/></h2>

    <div id="measurandSeries-clone" class="row form-group addPageComparisonRow">
        <label class="col-sm-2 control-label">
            <g:message code="de.iteratec.osm.pageComparison.firstPageTitle"
                       default="First Page:"/>
        </label>

        <div class="col-sm-3">
            <g:select from="${jobGroups}" name="firstJobGroupSelect" class="form-control jobGroupSelect" optionKey="key"
                      optionValue="value" noSelection="['': ' - choose a jobGroup - ']"/>
            <g:select from="${pages}" name="firstPageSelect" class="form-control pageSelect" optionKey="key" optionValue="value"
                      noSelection="['': ' - choose a page - ']"/>
        </div>

        <label class="col-sm-2 control-label">
            <g:message code="de.iteratec.osm.pageComparison.secondPageTitle"
                       default="Second Page:"/>
        </label>

        <div class="col-sm-3">
            <g:select from="${jobGroups}" name="secondJobGroupSelect" class="form-control jobGroupSelect" optionKey="key"
                      optionValue="value" noSelection="['': ' - choose a jobGroup - ']"/>
            <g:select from="${pages}" name="secondPageSelect" class="form-control pageSelect" optionKey="key" optionValue="value"
                      noSelection="['': ' - choose a page - ']"/>
        </div>

        <div class="col-sm-2 control-label removeAddMeasurands">
            <a href="#/" class="addMeasurandButton">
                <i class="fa fa-lg fa-plus-circle"></i>
            </a>
            <a href="#/" class="removeMeasurandButton hidden">
                <i class="fa fa-lg fa-minus-circle"></i>
            </a>
        </div>
    </div>
</div>

<asset:script type="text/javascript">
    $(window).load(function() {
      OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/pageComparison/pageComparisonSelectionCard.js"/>', 'pageComparisonSelectionCard');
    });
</asset:script>
