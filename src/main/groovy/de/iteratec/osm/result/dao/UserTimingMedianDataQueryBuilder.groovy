package de.iteratec.osm.result.dao

import de.iteratec.osm.result.UserTimingType
import de.iteratec.osm.util.PerformanceLoggingService

class UserTimingMedianDataQueryBuilder extends UserTimingRawDataQueryBuilder{

    @Override
    List<EventResultProjection> getResultsForFilter(List<Closure> baseFilters, Set<ProjectionProperty> baseProjections, List<MeasurandTrim> trims, PerformanceLoggingService performanceLoggingService) {
        List<Map> rawData = super.getRawQueryResults(baseFilters, baseProjections,trims, performanceLoggingService)
        List<EventResultProjection> result = []

        Map<String, Map> metaDataForAggregations = [:].withDefault {[:]}
        Map<String, Map<String,List>> groupedAggregations = [:].withDefault { [:].withDefault { [] } }
        rawData.each { ungrouped ->
            def value = ungrouped.type == UserTimingType.MEASURE ? ungrouped.duration : ungrouped.startTime
            String key = MedianUtil.generateGroupKeyForMedianAggregators(ungrouped,baseProjections)
            groupedAggregations.get(key).get(ungrouped.name) << value
            metaDataForAggregations.get(key) << MedianUtil.getMetaDataSample(ungrouped, baseProjections)
        }
        groupedAggregations.each { String key, Map<String, List> valueMap ->
            EventResultProjection erp = new EventResultProjection(id:key)
            erp.projectedProperties.putAll(metaDataForAggregations.get(key))
            valueMap.each { String nameKey, List valueList ->
                erp.projectedProperties.put(nameKey, MedianUtil.getMedianFrom(valueList))
            }
            result << erp
        }
        return  result
    }
}
