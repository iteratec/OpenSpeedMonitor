package de.iteratec.osm.result.dao

class MedianCalculator {

    List<EventResultProjection> calculateFor(List<EventResultProjection> rawData, List<String> aggregators, List<String> measurandNames){
        Map<String,List<EventResultProjection>> groupedRawData = groupRawDataByAggregators(rawData, aggregators)
        List<EventResultProjection> groupedAndSummarized = summarizeGroupedRawData(groupedRawData,measurandNames)
        return getMediansFromSummarizedData(groupedAndSummarized, measurandNames)
    }

    private Map<String,List<EventResultProjection>> groupRawDataByAggregators(List<EventResultProjection> rawData, List<String> aggregators ){
        Map<String, List<EventResultProjection>> groupedRawData = [:].withDefault { [] }
        rawData.each { ungrouped ->
            String key = generateGroupKeyForMedianAggregators(ungrouped,aggregators)
            groupedRawData.get(key) << ungrouped
        }
        return groupedRawData
    }

    private String generateGroupKeyForMedianAggregators(EventResultProjection eventResultProjection, List<String> aggregators){
        String key = ""
        aggregators.each {
            key += "_" + eventResultProjection."$it"
        }
        return key
    }

   private List<EventResultProjection> summarizeGroupedRawData(Map<String,List<EventResultProjection>> groupedRawData, List<String> measurandNames){
        Map<EventResultProjection, List<EventResultProjection>> justAHelperMap = [:]
        groupedRawData.each {String key, List<EventResultProjection> value ->
            EventResultProjection newKey = new EventResultProjection()
            EventResultProjection metaDataSample = value[0]
            List<String> metaDataKeys = getNonMeasurandNames(measurandNames, metaDataSample)
            newKey.projectedProperties.putAll(metaDataSample.projectedProperties.subMap(metaDataKeys))
            justAHelperMap.put(newKey, value)
        }

        justAHelperMap.each { EventResultProjection target, List<EventResultProjection> groupedButNotSummarized ->
            measurandNames.each { String measurand ->
                target.projectedProperties."$measurand" = groupedButNotSummarized.collect {
                    it."$measurand"
                }
            }
        }
       Set<EventResultProjection> result = justAHelperMap.keySet()
       return  result as List
    }

    private List<String> getNonMeasurandNames(List<String> measurandNames, EventResultProjection eventResultProjection){
        List<String> allPropertyNames = eventResultProjection.projectedProperties.keySet() as List
        return allPropertyNames.findAll{!measurandNames.contains(it)}
    }


    private List<EventResultProjection> getMediansFromSummarizedData(List<EventResultProjection> eventResultProjections, List<String> measurandNames) {
        return eventResultProjections.each { eventResultProjection ->
            measurandNames.each { measurandName ->
                List rawDataForMeasurand = eventResultProjection."$measurandName"
                eventResultProjection.projectedProperties.put(measurandName, getMedian(rawDataForMeasurand))
            }
        }
    }

    private def getMedian(List data) {
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
}
