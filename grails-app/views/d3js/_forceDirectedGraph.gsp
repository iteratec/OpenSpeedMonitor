<asset:stylesheet src="d3Charts/forceDirectedGraph.css"/>
<asset:javascript src="d3/forceDirectedGraph.js"/>
%{--TODO COMMENTS--}%
%{--Data to inject: xValues : sortedList of xValues --> xValues have to be obvious
                    yValues : sortedList of yValues  --> the y-Element on position i is mapped to the x-Element on position i, so yValues and xValues have to be
                                                         of same length
                    width : diagram width in px
                    height : diagram height in px

                    xLabel : Label for the x axis
                    yLabel : Label for the y axis
                    img : one of 'clocks', 'none'
                    id : the id of the barChart on this page (unique)--}%

<svg class="chart" id=${id}></svg>
<asset:script type="text/javascript">
    $(document).ready (createForceDirectedGraph(${width}, ${height}, ${nodes}, ${weights}, ${elemPerChain}, "${id}"));
</asset:script>