package de.iteratec.osm.d3Data

/**
 * Allocates data for a D3 multi line chart
 */
class MultiLineChart {
    List<MultiLineChartLineData> lines

    String xLabel
    String yLabel

    MultiLineChart() {
        lines = new ArrayList<>()
        xLabel = "x-Axis"
        yLabel = "y-Axis"
    }

    def addLine(MultiLineChartLineData line) {
        lines.add(line)
    }
}
