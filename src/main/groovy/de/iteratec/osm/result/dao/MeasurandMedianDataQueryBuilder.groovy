package de.iteratec.osm.result.dao

import de.iteratec.osm.dao.ProjectionProperty
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.util.PerformanceLoggingService

class MeasurandMedianDataQueryBuilder extends MeasurandRawDataQueryBuilder {

    @Override
    List<EventResultProjection> getResultsForFilter(List<EventResultFilter> baseFilters, List<ProjectionProperty> baseProjections, List<MeasurandTrim> trims, PerformanceLoggingService performanceLoggingService) {
        List<String> aggregators = MedianUtil.getAggegatorNames(baseFilters)
        List<String> measurandNames = super.selectedMeasurands.collect {it.databaseRelevantName}
        List<Map> rawData = super.getRawQueryResults(baseFilters, baseProjections,trims)

        Map<String,List<Map>> groupedRawData = groupRawDataByAggregators(rawData, aggregators)
        List<EventResultProjection> groupedAndSummarized = summarizeGroupedRawData(groupedRawData,measurandNames)
        return getMediansFromSummarizedData(groupedAndSummarized, measurandNames)
    }

    private Map<String,List<Map>> groupRawDataByAggregators(List<Map> rawData, List<String> aggregators ){
        Map<String, List<Map>> groupedRawData = [:].withDefault { [] }
        rawData.each { ungrouped ->
            String key = MedianUtil.generateGroupKeyForMedianAggregators(ungrouped,aggregators)
            groupedRawData.get(key) << ungrouped
        }
        return groupedRawData
    }

    private List<EventResultProjection> summarizeGroupedRawData(Map<String,List<Map>> groupedRawData, List<String> measurandNames){
        Map<EventResultProjection, List<Map>> justAHelperMap = [:]
        groupedRawData.each {String key, List<Map> value ->
            EventResultProjection newKey = new EventResultProjection(id:key)
            Map metaDataSample = value[0]
            Map metaData = MedianUtil.getMetaDataSample(measurandNames, metaDataSample)
            newKey.projectedProperties.putAll(metaData)
            justAHelperMap.put(newKey, value)
        }

        justAHelperMap.each { EventResultProjection target, List<Map> groupedButNotSummarized ->
            measurandNames.each { String measurand ->
                target.projectedProperties."$measurand" = groupedButNotSummarized.collect {
                    it."$measurand"
                }
            }
        }
        Set<EventResultProjection> result = justAHelperMap.keySet()
        return  result as List
    }


    private List<EventResultProjection> getMediansFromSummarizedData(List<EventResultProjection> eventResultProjections, List<String> measurandNames) {
        return eventResultProjections.each { eventResultProjection ->
            measurandNames.each { measurandName ->
                List rawDataForMeasurand = eventResultProjection."$measurandName"
                eventResultProjection.projectedProperties.put(measurandName, MedianUtil.getMedianFrom(rawDataForMeasurand))
            }
        }
    }
}
