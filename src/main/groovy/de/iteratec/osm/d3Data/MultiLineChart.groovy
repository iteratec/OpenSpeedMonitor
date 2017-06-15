package de.iteratec.osm.d3Data

/**
 * Allocates data for a D3 multi line chart
 */
class MultiLineChart {
    public static final String DEFAULT_X_LABEL = "x-Axis"
    public static final String DEFAULT_Y_LABEL = "y-Axis"
    List<MultiLineChartLineData> lines

    String xLabel
    String yLabel

    MultiLineChart() {
        lines = new ArrayList<>()
        xLabel = DEFAULT_X_LABEL
        yLabel = DEFAULT_Y_LABEL
    }

    def addLine(MultiLineChartLineData line) {
        lines.add(line)
    }
}
