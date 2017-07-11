package de.iteratec.osm.util

import de.iteratec.osm.result.Measurand
import de.iteratec.osm.result.MeasurandGroup
import de.iteratec.osm.result.EventResult

class MeasurandUtil {
    static  Map<MeasurandGroup, List<Measurand>> getAllMeasurandsByMeasurandGroup() {
        Map<MeasurandGroup, List<Measurand>> result = [:]
        MeasurandGroup.values().findAll{mg -> Measurand.values().findAll{it.getMeasurandGroup() == mg}.size() > 0}.each { result.put(it, [])}
        Measurand.values().each {result.get(it.getMeasurandGroup()).add(it)}
        return Collections.unmodifiableMap(result)
    }

    static def normalizeValue(def value, String measurandString) {
        if(measurandString&&value){
            return normalizeValue(value,Measurand.valueOf(measurandString));
        }
        return value
    }

    static def normalizeValue(def value, Measurand measurand) {
        if(value){
            return value/measurand.getMeasurandGroup().getUnit().getDivisor()
        }
        return value
    }

    static Double getEventResultPropertyForCalculation(Measurand measurand, EventResult result) {
        return result.getProperty(measurand.getEventResultField()) != null ? Double.valueOf(result.getProperty(measurand.getEventResultField())) : null
    }
}
