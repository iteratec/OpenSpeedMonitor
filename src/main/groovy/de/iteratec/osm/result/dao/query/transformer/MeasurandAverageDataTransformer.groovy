package de.iteratec.osm.result.dao.query.transformer

import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.query.AggregationUtil
import de.iteratec.osm.result.dao.query.ProjectionProperty

class MeasurandAverageDataTransformer implements EventResultTransformer {
    Set<ProjectionProperty> baseProjections

    @Override
    List<EventResultProjection> transformRawQueryResult(List<Map> rawQueryData) {
        List<EventResultProjection> eventResultProjections = []
        rawQueryData.each { Map dbResult ->
            String key = AggregationUtil.generateGroupedKeyForAggregations(dbResult, baseProjections)
            if (key) {
                EventResultProjection eventResultProjection = new EventResultProjection(
                        id: key
                )
                eventResultProjection.projectedProperties = dbResult
                eventResultProjections += eventResultProjection
            }
        }
        return eventResultProjections
    }
}
