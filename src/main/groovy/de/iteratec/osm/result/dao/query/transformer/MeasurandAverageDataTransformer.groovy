package de.iteratec.osm.result.dao.query.transformer

import de.iteratec.osm.result.dao.AggregationUtil
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.ProjectionProperty

class MeasurandAverageDataTransformer implements EventResultTransformer {
    Set<ProjectionProperty> baseProperties

    @Override
    List<EventResultProjection> transformRawQueryResult(List<Map> rawQueryData) {
        List<EventResultProjection> eventResultProjections = []
        rawQueryData.each { Map dbResult ->
            EventResultProjection eventResultProjection = new EventResultProjection(
                    id: AggregationUtil.generateGroupKeyForMedianAggregators(dbResult, baseProperties)
            )
            eventResultProjection.projectedProperties = dbResult
            eventResultProjections += eventResultProjection
        }
        return eventResultProjections
    }
}
