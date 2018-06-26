package de.iteratec.osm.result.dao.query.transformer

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.query.AggregationUtil
import de.iteratec.osm.result.dao.query.ProjectionProperty

class MeasurandMedianDataTransformer implements EventResultTransformer {
    Set<ProjectionProperty> baseProjections
    List<SelectedMeasurand> selectedMeasurands

    @Override
    List<EventResultProjection> transformRawQueryResult(List<Map> rawQueryData) {
        List<String> measurandNames = selectedMeasurands.findAll { !it.selectedType.isUserTiming() }.collect {
            it.databaseRelevantName
        }
        Map<String, List<Map>> groupedRawData = groupRawDataByAggregators(rawQueryData, baseProjections)
        Set<EventResultProjection> groupedAndSummarized = summarizeGroupedRawData(groupedRawData, baseProjections, measurandNames)
        return getMediansFromSummarizedData(groupedAndSummarized, measurandNames) as List
    }

    private Map<String, List<Map>> groupRawDataByAggregators(List<Map> rawData, Set<ProjectionProperty> baseProjections) {
        Map<String, List<Map>> groupedRawData = [:].withDefault { [] }
        rawData.each { ungrouped ->
            String key = AggregationUtil.generateGroupedKeyForAggregations(ungrouped, baseProjections)
            groupedRawData.get(key) << ungrouped
        }
        return groupedRawData
    }

    private Set<EventResultProjection> summarizeGroupedRawData(Map<String, List<Map>> groupedRawData, Set<ProjectionProperty> baseProjections, List<String> measurandNames) {
        Map<EventResultProjection, List<Map>> justAHelperMap = [:]
        groupedRawData.each { String key, List<Map> value ->
            EventResultProjection newKey = new EventResultProjection(id: key)
            Map metaDataSample = value[0]
            Map metaData = AggregationUtil.getMetaData(metaDataSample, baseProjections)
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
        return justAHelperMap.keySet()
    }


    private Set<EventResultProjection> getMediansFromSummarizedData(Set<EventResultProjection> eventResultProjections, List<String> measurandNames) {
        return eventResultProjections.each { eventResultProjection ->
            measurandNames.each { measurandName ->
                List rawDataForMeasurand = eventResultProjection."$measurandName"
                eventResultProjection.projectedProperties.put(measurandName, AggregationUtil.getMedianFrom(rawDataForMeasurand))
            }
        }
    }
}
