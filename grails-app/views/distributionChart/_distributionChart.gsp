<div id="chart-container">
    <div id="filter-dropdown-group" class="btn-group">
        <button id="filter-dropdown" type="button" class="btn btn-default btn-sm dropdown-toggle" data-toggle="dropdown"
                aria-haspopup="true" aria-expanded="false">
            <g:message code="de.iteratec.osm.distributionChart.filter.label" default="Filter"/> <span
                class="caret"></span>
        </button>
        <ul class="dropdown-menu pull-right">
            <li id="all-violins-header" class="dropdown-header">
                <g:message code="de.iteratec.osm.distributionChart.filter.noFilterHeader" default="Sort"/>
            </li>
            <li>
                <a id="all-violins-desc" href="#">
                    <i class="fa fa-check filterActive" aria-hidden="true"></i>
                    <g:message code="de.iteratec.osm.distributionChart.filter.noFilterDesc"
                               default="Descending Median"/>
                </a>
            </li>
            <li>
                <a id="all-violins-asc" href="#">
                    <i class="fa fa-check filterInactive" aria-hidden="true"></i>
                    <g:message code="de.iteratec.osm.distributionChart.filter.noFilterAsc"
                               default="Ascending Median"/>
                </a>
            </li>
            <li id="customer-journey-header" class="dropdown-header">
                <g:message code="de.iteratec.osm.distributionChart.filter.customerJourneyHeader"
                           default="Customer Journey"/>
            </li>
        </ul>
    </div>

    <div class="in-chart-buttons">
        <a href="#downloadAsPngModal" id="download-as-png-button"
           data-toggle="modal" role="button" onclick="setDefaultValues('svg-container')"
           title="${message(code: 'de.iteratec.ism.ui.button.save.name', default:'Download as PNG')}">
            <i class="fa fa-download"></i>
        </a>
    </div>

    <div id="svg-container"></div>
</div>