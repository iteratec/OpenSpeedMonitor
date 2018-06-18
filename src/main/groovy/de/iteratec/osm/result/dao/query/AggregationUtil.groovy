package de.iteratec.osm.result.dao.query

import de.iteratec.osm.result.SelectedMeasurand

class AggregationUtil {

    static def getMedianFrom(List data) {
        data.removeAll([null])
        data.sort()
        if (data) {
            if (data.size() == 1) {
                return data.get(0)
            }
            if ((data.size() % 2) != 0) {
                return data.get((Integer) ((data.size() - 1) / 2))
            } else {
                return (data.get((Integer) (data.size() / 2)) +
                        data.get((Integer) ((data.size() - 1) / 2))) / 2
            }
        }
    }

    private static Set<String> getAggregatorAliases(Set<ProjectionProperty> projectionPropertySet) {
        return projectionPropertySet.collect { it.alias }
    }

    static String generateGroupedKeyForAggregations(Map eventResultProjection, Set<ProjectionProperty> projectionPropertySet) {
        Set aggregators = getAggregatorAliases(projectionPropertySet)
        String key = ""
        aggregators.each {
            key += "_" + eventResultProjection."$it"
        }
        return key
    }

    static Map getMetaDataSample(Map eventResultProjection, Set<ProjectionProperty> projectionPropertySet) {
        return eventResultProjection.subMap(getAggregatorAliases(projectionPropertySet))
    }

    static boolean checkIfTrimIsRelevantFor(SelectedMeasurand selectedMeasurand, MeasurandTrim trim) {
        if (!trim.onlyForSpecific) {
            return selectedMeasurand.measurandGroup == trim.measurandGroup
        } else {
            return trim.onlyForSpecific.eventResultField == selectedMeasurand.databaseRelevantName && selectedMeasurand.measurandGroup == trim.measurandGroup
        }
    }

    static List<Closure> findAllTrimsStillToBeSet(List<SelectedMeasurand> selectedMeasurands, List<MeasurandTrim> trims) {
        List<String> selectedMeasurandNames = selectedMeasurands.collect { it.databaseRelevantName }
        List<MeasurandTrim> stillToBeSet = trims.findAll {
            it.onlyForSpecific && !selectedMeasurandNames.contains(it.onlyForSpecific.eventResultField)
        }
        List<Closure> trimClosures = []
        stillToBeSet.each { MeasurandTrim trim ->
            trimClosures.add({
                "${trim.qualifier.getGormSyntax()}" "${trim.onlyForSpecific.eventResultField}", trim.value
            })
        }
        return trimClosures
    }
}
