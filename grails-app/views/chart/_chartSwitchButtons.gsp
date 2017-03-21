<g:set var="chartSwitchLinks" value="${[
        ['name': 'timeSeries', 'text': message(code: 'eventResultDashboard.label', default: 'Time Series'), 'icon': 'fa-line-chart'],
        ['name': 'pageAggregation', 'text': message(code: 'de.iteratec.isocsi.pageAggregation', default: 'Page Aggregation'), 'icon': 'fa-bar-chart'],
        ['name': 'jobGroupAggregation', 'text': message(code: 'de.iteratec.jobGroupAggregation.title', default: 'Job Group Aggregation'), 'icon': 'fa-bar-chart fa-rotate-90'],
        ['name': 'distribution', 'text': message(code: 'de.iteratec.osm.distributionChart', default: 'Distribution Chart'), 'icon': 'fa-area-chart'],
        ['name': 'pageComparison', 'text': message(code: 'de.iteratec.isocsi.pageComparision.title', default: 'Page Comparison'), 'icon': 'fa-balance-scale'],
        ['name': 'detailAnalysis', 'text': message(code: 'de.iteratec.isocsi.detailAnalysis', default: 'Detail Analysis'), 'icon': 'fa-pie-chart'],
        ['name': 'resultList', 'text': message(code: 'de.iteratec.result.title', default: 'Result List'), 'icon': 'fa-th-list']

]}"/>

<h1>
<g:each in="${chartSwitchLinks}">
    <g:if test="${currentChartName == it.name}">
        ${it.text}
    </g:if>
    <g:else>
        %{--show detailAnalysis link only if detailAnalysis is enabled--}%
        <g:if test="${it.name != 'detailAnalysis' || grailsApplication.config.getProperty('grails.de.iteratec.osm.detailAnalysis.enablePersistenceOfDetailAnalysisData')?.equals("true")}">
            <a href="#" class="btn hidden" id="${it.name}WithDataLink"><i class="fa ${it.icon}"></i></a>
        </g:if>
    </g:else>
</g:each>
</h1>
