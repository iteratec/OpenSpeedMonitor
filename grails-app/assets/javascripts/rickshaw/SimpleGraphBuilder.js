/**
 *
 * Created by nkuhn on 06.09.15.
 */
function SimpleGraphBuilder(args){
    var self = this;
    this.graph;

    this.initialize = function(args){

        self.graph = new Rickshaw.Graph( {
            element: document.getElementById("chart_"+args.chartIdentifier),
            width: 450,
            height: 230,
            renderer: 'line',
            series: args.defaultMappings,
        } );

        var hoverDetail = new Rickshaw.Graph.HoverDetail( {
            graph: self.graph,
            xFormatter: function(x) { return x + " ms" },
            yFormatter: function(y) { return Math.floor(y) + " %" }
        } );

        var legend = new Rickshaw.Graph.Legend( {
            graph: self.graph,
            element: document.getElementById("legend_"+args.chartIdentifier)

        } );

        var shelving = new Rickshaw.Graph.Behavior.Series.Toggle( {
            graph: self.graph,
            legend: legend
        } );

        var y_axis = new Rickshaw.Graph.Axis.Y( {
            graph: self.graph,
            orientation: 'left',
            tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
            element: document.getElementById("y_axis_"+args.chartIdentifier),
        } );
        var x_axis = new Rickshaw.Graph.Axis.X( {
            graph: self.graph,
            orientation: 'bottom',
            element: document.getElementById("x_axis_"+args.chartIdentifier),
        } );

        self.graph.render();
    }
    this.setData = function(data){
        //TODO: doesn't work for now :(
        self.graph.series.addData(data);
        self.graph.render();
    }

    this.initialize(args);
}
