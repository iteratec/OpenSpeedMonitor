package de.iteratec.osm.d3Data


/**
 * Created by mmi on 12.10.2015.
 */
class BarChartData {
    List<ChartEntry> bars;
    String xLabel
    String yLabel

    def BarChartData(){
        bars = new ArrayList<>()
        xLabel = "x-Axis"
        yLabel = "y-Axis"
    }

    def addDatum(ChartEntry c){
        bars.add(c)
    }
}
