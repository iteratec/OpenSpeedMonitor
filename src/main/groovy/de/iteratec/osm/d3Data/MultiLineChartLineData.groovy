package de.iteratec.osm.d3Data

/**
 * Represents one line in a line chart.
 */
class MultiLineChartLineData {
    // X and Y Points.
    // xPoints[i] belongs to yPoints[i]
    public static final String DEFAULT_NAME = "Line"
    List<Double> xPoints
    List<Double> yPoints
    String name
    long id

    MultiLineChartLineData() {
        name = DEFAULT_NAME
        xPoints = new ArrayList<>()
        yPoints = new ArrayList<>()
    }

    /**
     * Adds a data point.
     * The x value is added to the end of list xPoints,
     * the y value to the end of list yPoints.
     * @param x the x value of the point
     * @param y the y value of the point
     */
    void addDataPoint(double x, double y) {
        xPoints.add(x)
        yPoints.add(y)
    }
}
