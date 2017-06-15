package de.iteratec.osm.d3Data

/**
 * This class represents a model for the creation of a d3 bar chart
 * A bar chart consists of a specified amount of ChartEntries containing a name and a weight
 */
class BarChartData {
    public static final String DEFAULT_X_LABEL = "x-Axis"
    public static final String DEFAULT_Y_LABEL = "y-Axis"
    List<ChartEntry> bars
    String xLabel
    String yLabel

    def BarChartData(){
        bars = new ArrayList<>()
        xLabel = DEFAULT_X_LABEL
        yLabel = DEFAULT_Y_LABEL
    }

    def addDatum(ChartEntry c){
        bars.add(c)
    }
}
