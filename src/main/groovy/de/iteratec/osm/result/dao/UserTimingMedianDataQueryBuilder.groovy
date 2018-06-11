package de.iteratec.osm.result.dao

import de.iteratec.osm.dao.ProjectionProperty
import de.iteratec.osm.result.UserTimingType
import de.iteratec.osm.util.PerformanceLoggingService

class UserTimingMedianDataQueryBuilder extends UserTimingRawDataQueryBuilder{

    private static List<String> userTimingDataKeysList = ['name','type','startTime','duration']

    @Override
    List<EventResultProjection> getResultsForFilter(List<EventResultFilter> baseFilters, List<ProjectionProperty> baseProjections, List<MeasurandTrim> trims, PerformanceLoggingService performanceLoggingService) {
        List<String> aggregators = MedianUtil.getAggegatorNames(baseFilters)
        List<Map> rawData = super.getRawQueryResults(baseFilters, baseProjections,trims, performanceLoggingService)
        List<EventResultProjection> result = []

        Map<String, Map> metaDataForAggregations = [:].withDefault {[:]}
        Map<String, Map<String,List>> groupedAggregations = [:].withDefault { [:].withDefault { [] } }
        rawData.each { ungrouped ->
            def value = ungrouped.type == UserTimingType.MEASURE ? ungrouped.duration : ungrouped.startTime
            String key = MedianUtil.generateGroupKeyForMedianAggregators(ungrouped,aggregators)
            groupedAggregations.get(key).get(ungrouped.name) << value
            metaDataForAggregations.get(key) << MedianUtil.getMetaDataSample(userTimingDataKeysList, ungrouped)
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
