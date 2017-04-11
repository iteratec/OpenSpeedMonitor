<div id="chart-container">
    <div id="filter-dropdown-group" class="btn-group">
        <button id="filter-dropdown" type="button" class="btn btn-default btn-sm dropdown-toggle" data-toggle="dropdown"
                aria-haspopup="true" aria-expanded="false">
            <g:message code="de.iteratec.osm.barchart.filter.label" default="Filter"/> <span
                class="caret"></span>
        </button>
        <ul class="dropdown-menu pull-right">
            <li id="all-bars-header" class="dropdown-header">
                <g:message code="de.iteratec.osm.barchart.filter.noFilterHeader" default="Sort"/>
            </li>
            <li>
                <a id="all-bars-desc" href="#">
                    <i class="fa fa-check filterActive" aria-hidden="true"></i>
                    <g:message code="de.iteratec.osm.barchart.filter.noFilterDesc"
                               default="Descending"/>
                </a>
            </li>
            <li>
                <a id="all-bars-asc" href="#">
                    <i class="fa fa-check filterInactive" aria-hidden="true"></i>
                    <g:message code="de.iteratec.osm.barchart.filter.noFilterAsc"
                               default="Ascending"/>
                </a>
            </li>
        </ul>
    </div>
    <div class="in-chart-buttons hidden">
        <a href="#adjustBarchartModal" id="adjust-barchart-modal" data-toggle="modal" data-target="#adjustBarchartModal"
           onclick="initModalDialogValues()">
            <i class="fa fa-sliders"></i>
        </a>
        <a href="#downloadAsPngModal" id="download-as-png-button"
           data-toggle="modal" role="button" onclick="setDefaultValues('svg-container')"
           title="${message(code: 'de.iteratec.ism.ui.button.save.name', default:'Download as PNG')}">
            <i class="fa fa-download"></i>
        </a>
    </div>

    <div id="svg-container">
        <svg></svg>
    </div>
</div>
<g:render template="adjustBarchartModal"/>
<asset:script type="text/javascript">
    $(window).load(function() {
      OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/csiBenchmark/csiBenchmarkChart.js" />',true,'barchart')
    });
</asset:script>
