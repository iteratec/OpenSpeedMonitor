<div id="chart-container">
    <div id="filter-dropdown-group" class="btn-group">
        <button id="filter-dropdown" type="button" class="btn btn-default btn-sm dropdown-toggle" data-toggle="dropdown"
                aria-haspopup="true" aria-expanded="false">
            <g:message code="de.iteratec.osm.distributionChart.filter.label" default="Filter"/> <span
                class="caret"></span>
        </button>
        <ul class="dropdown-menu pull-right">
            <li id="customer-journey-header" class="dropdown-header">
                <g:message code="de.iteratec.osm.distributionChart.filter.customerJourneyHeader"
                           default="Customer Journey"/>
            </li>
        </ul>
    </div>

    <div id="svg-container"></div>
</div>