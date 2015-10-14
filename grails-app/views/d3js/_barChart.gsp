<asset:stylesheet src="d3Charts/barChart.css"/>
<asset:stylesheet src="d3Charts/clocks.css"/>
<asset:javascript src="d3/barChart.js"/>

%{--Data to inject:
                    data : de.iteratec.osm.d3data.BarChartData-Object as JSON

                    img : one of 'clocks', 'none'
                    id : the id of the barChart on this page (unique)--}%
<div class="row">
    <div class="span12" id="barChartSpan">
        <svg class="chart" id= ${id}></svg>
    </div>
</div>