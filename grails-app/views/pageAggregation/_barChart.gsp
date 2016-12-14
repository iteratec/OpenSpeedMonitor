<div id="chart-container">
    <div id="filter-dropdown-group" class="btn-group hidden">
        <button id="filter-dropdown" type="button" class="btn btn-default btn-sm dropdown-toggle" data-toggle="dropdown"
                aria-haspopup="true" aria-expanded="false">
            <g:message code="de.iteratec.osm.dimple.barchart.filter.label" default="filter"/> <span
                class="caret"></span>
        </button>
        <ul class="dropdown-menu pull-right">
            <li id="all-bars-header" class="dropdown-header"><g:message
                    code="de.iteratec.osm.dimple.barchart.filter.noFilterHeader" default="no filter"/></li>
            <li><a id="all-bars-desc" href="#"><g:message code="de.iteratec.osm.dimple.barchart.filter.noFilterDesc"
                                                          default="no filter, sorting desc"/></a></li>
            <li><a id="all-bars-asc" href="#"><g:message code="de.iteratec.osm.dimple.barchart.filter.noFilterAsc"
                                                         default="no filter, sorting asc"/></a></li>
            <li id="customer-journey-header" class="dropdown-header"><g:message
                    code="de.iteratec.osm.dimple.barchart.filter.customerJourneyHeader"
                    default="Customer Journey"/></li>
        </ul>
    </div>
    <a href="#adjustBarchartModal" id="adjust-barchart-modal" data-toggle="modal" data-target="#adjustBarchartModal"
       class="hidden" onclick="initModalDialogValues()">
        <i class="fa fa-sliders"></i>
    </a>

    <div id="svg-container">
    </div>
</div>
<g:render template="/pageAggregation/adjustBarchartModal"/>
<asset:script type="text/javascript">
    $(window).load(function() {
      OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/dimple/barchart.js" absolute="true"/>',true,'barchart')
    });
</asset:script>
