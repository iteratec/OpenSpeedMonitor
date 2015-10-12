<iteratec:csiMappingChart
        chartIdentifier="${chartIdentifier}"
        bottomOffsetXAxis="${bottomOffsetXAxis}"
        yAxisRightOffset="${yAxisRightOffset}"
        chartBottomOffset="${chartBottomOffset}"
        yAxisTopOffset="${yAxisTopOffset}"
        bottomOffsetLegend="${bottomOffsetLegend}"/>

<content tag="include.rickshaw-init">
    <asset:javascript src="d3/d3.v3.js"/>
    <asset:javascript src="rickshaw/rickshaw.min.js"/>
    <asset:javascript src="rickshaw/SimpleGraphBuilder.js"/>

    <asset:script>

        var graphBuilder_${chartIdentifier};

        $(document).ready(function(){
            var palette = new Rickshaw.Color.Palette();
            var args = {
                defaultMappings: ${defaultMappingsJson},
                chartIdentifier: '${chartIdentifier}'
            };
            graphBuilder_${chartIdentifier} = new SimpleGraphBuilder(args);
        });

    </asset:script>
</content>