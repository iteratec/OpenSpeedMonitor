package de.iteratec.osm.report.chart

/**
 * Contains data necessary to create a chart with rickshaw charting library.
 * Could contain all necessary data. At the moment some data (e.g. axis) iprovided separately.
 *
 * @author Created by nkuhn on 23.10.15.
 * @see http://code.shutterstock.com/rickshaw/
 */
class OsmRickshawChart {
    /**
     * All graphs to draw.
     * Parts common in all graph labels are removed from labels.
     */
    List<OsmChartGraph> osmChartGraphs
    /**
     * Common parts of all graph labels.
     */
    String osmChartGraphsCommonLabel
}
