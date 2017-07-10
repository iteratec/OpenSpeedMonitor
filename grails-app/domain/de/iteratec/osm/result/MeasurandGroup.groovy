package de.iteratec.osm.result

import de.iteratec.osm.report.chart.Unit

/**
 * To group {@link de.iteratec.osm.report.chart.AggregatorType}s which are measureands.
 *
 * @author nkuhn
 *
 */
enum MeasurandGroup {
    LOAD_TIMES(Unit.MILLISECONDS),
    REQUEST_COUNTS(Unit.NUMBER),
    REQUEST_SIZES(Unit.MEGABYTE),
    PERCENTAGES(Unit.PERCENT),
    UNDEFINED(Unit.OTHER),
    NO_MEASURAND(Unit.OTHER)

    private Unit unit

    private MeasurandGroup(Unit unit){
        this.unit = unit
    }
    Unit getUnit(){
        return unit
    }
}