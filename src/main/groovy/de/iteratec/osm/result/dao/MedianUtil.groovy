package de.iteratec.osm.result.dao

class MedianUtil {

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

    static List<String> getAggegatorNames(List<EventResultFilter> baseFilters){
        List<String> aggregators = baseFilters.collect{it.filteredFieldName}
        aggregators.removeAll([null])
        return aggregators
    }

    static String generateGroupKeyForMedianAggregators(Map eventResultProjection, List<String> aggregators){
        String key = ""
        aggregators.each {
            key += "_" + eventResultProjection."$it"
        }
        return key
    }

    private static List<String> getNonMeasurandNames(List<String> measurandNames, Map eventResultProjection){
        List<String> allPropertyNames = eventResultProjection.keySet() as List
        return allPropertyNames.findAll{!measurandNames.contains(it)}
    }

    static Map getMetaDataSample(List<String> measurandNames, Map eventResultProjection){
        List<String> metaDataKeys = getNonMeasurandNames(measurandNames, eventResultProjection)
        return eventResultProjection.subMap(metaDataKeys)
    }
}
