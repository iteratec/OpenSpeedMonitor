package de.iteratec.osm.result.dao.query.transformer

import de.iteratec.osm.result.UserTimingType
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.query.AggregationUtil
import de.iteratec.osm.result.dao.query.ProjectionProperty

class UserTimingMedianDataTransformer implements EventResultTransformer {
    Set<ProjectionProperty> baseProjections

    @Override
    List<EventResultProjection> transformRawQueryResult(List<Map> rawQueryData) {
        List<EventResultProjection> result = []

        Map<String, Map> metaDataForAggregations = [:].withDefault { [:] }
        Map<String, Map<String, List>> groupedAggregations = [:].withDefault { [:].withDefault { [] } }
        rawQueryData.each { ungrouped ->
            def value = ungrouped.type == UserTimingType.MEASURE ? ungrouped.duration : ungrouped.startTime
            String key = AggregationUtil.generateGroupedKeyForAggregations(ungrouped, baseProjections)
            groupedAggregations.get(key).get(ungrouped.name) << value
            metaDataForAggregations.get(key) << AggregationUtil.getMetaData(ungrouped, baseProjections)
        }
        groupedAggregations.each { String key, Map<String, List> valueMap ->
            EventResultProjection erp = new EventResultProjection(id: key)
            erp.projectedProperties.putAll(metaDataForAggregations.get(key))
            valueMap.each { String nameKey, List valueList ->
                erp.projectedProperties.put(nameKey, AggregationUtil.getMedianFrom(valueList))
            }
            result << erp
        }
        return result
    }
}
