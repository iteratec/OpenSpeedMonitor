package de.iteratec.osm.util

import de.iteratec.osm.report.chart.Measurand
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.report.chart.Unit

class MeasurandUtil {
    static  Map<MeasurandGroup, List<Measurand>> getAllMeasurandsByMeasurandGroup() {
        Map<MeasurandGroup, List<Measurand>> result = [:]
        MeasurandGroup.values().each { result.put(it, [])}
        Measurand.values().each {result.get(it.getMeasurandGroup()).add(it)}

        return Collections.unmodifiableMap(result)
    }

    static def normalizeValue(def value, String measurandString) {
        if(measurandString&&value){
            Measurand measurand = Measurand.valueOf(measurandString)
            MeasurandGroup measurandGroup = measurand.getMeasurandGroup()
            Unit unit = measurandGroup.getUnit()
            Double divisor = unit.getDivisor()
            return value/divisor
        }
        return value
    }
}
