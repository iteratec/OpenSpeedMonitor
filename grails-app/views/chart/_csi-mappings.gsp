<iteratec:multiLineChart
        chartIdentifier="${chartIdentifier}"
        modal="${modal}"/>

<content tag="include.rickshaw-init">
    <asset:javascript src="d3/d3.v3.js"/>
    <asset:javascript src="d3/multiLineChart.js" />
    <asset:script>
        createMultiLineGraph(${chartData},  '${chartIdentifier}');
    </asset:script>
</content>