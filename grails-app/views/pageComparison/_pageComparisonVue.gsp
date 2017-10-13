<div class="card form-horizontal" id="pageComparisonSelectionCard">
    <h2><g:message code="de.iteratec.osm.pageComparison.card.title" default="Pages"/></h2>
    <div class="col-md-12" id="pageComparisonCard">
        <div v-for="(comparison,index) in comparisons" id="measurandSeries-clone"
             class="row form-group addPageComparisonRow">
            <comparison-component v-bind:comparisonData="comparison" v-bind:jobgroups="jobGroups"
                                  v-bind:grouptopagesmap="groupToPagesMap" v-bind:index="index"></comparison-component>
        </div>
        <div class="col-md-2"></div>
        <div class="col-md-8">
            <button type="button" id="addComparison" class="btn btn-default btn-block">
                <i class="fa fa-plus"></i>
                <g:message code="de.iteratec.osm.pageComparison.card.addRow" default="Pages"/>
            </button>
        </div>
    </div>
</div>
<g:render template="pageComparisonRowComponentVue"/>
<asset:javascript src="pageComparison/pageComparisonVue.js"/>