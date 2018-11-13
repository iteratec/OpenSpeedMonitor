package de.iteratec.osm.result.dao.query.transformer

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.UserTimingType
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.query.AggregationUtil
import de.iteratec.osm.result.dao.query.ProjectionProperty

class UserTimingPercentileDataTransformer implements EventResultTransformer {
    Set<ProjectionProperty> baseProjections
    private int percentage

    UserTimingPercentileDataTransformer(int percentage, Set<ProjectionProperty> baseProjections) {
        this.percentage = percentage
        this.baseProjections = baseProjections
    }

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
                int index = (valueList.size() * (percentage.toFloat() / 100f)).toInteger()
                index = Math.min( valueList.size()-1, Math.max(0, index) )
                erp.projectedProperties.put(nameKey, AggregationUtil.getPercentile(valueList, index))
            }
            result << erp
        }
        return result
    }
}
