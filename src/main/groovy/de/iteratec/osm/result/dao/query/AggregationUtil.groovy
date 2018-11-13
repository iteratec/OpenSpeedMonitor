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

    static Map getMetaData(Map eventResultProjection, Set<ProjectionProperty> projectionPropertySet) {
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

    static def getPercentile(List<Number> data, int nthIndex) {
        if(data.size() > nthIndex){
            def rawData = data.toArray()
            int left = 0
            int right = data.size() - 1
            while(true) {
                if(left == right) {
                    return rawData[left]
                }
                int pivot = right
                pivot = partitionArray(rawData, left, right, pivot)

                if(nthIndex == pivot) {
                    return rawData[nthIndex]
                }
                else if(nthIndex < pivot) {
                    right = pivot -1
                }
                else {
                    left = pivot +1
                }
            }
        }
        throw new IllegalStateException("Something went wrong during calculation of the percentile")
    }

    private static def partitionArray(Object[] data, int left, int right, int pivot) {
        def pivotVal = data[pivot]
        data.swap(pivot, right)
        int i = left
        int k = right - 1
        while(true) {
            while(data[i] < pivotVal && i<=k)i++
            while(data[k] >= pivotVal && k>i)k--
            if(i >= k)break
            data.swap(i, k)
        }
        data.swap(i, right)
        return i
    }
}
