package de.iteratec.osm.result.dao.query.transformer

import de.iteratec.osm.result.UserTimingType
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.query.AggregationUtil
import de.iteratec.osm.result.dao.query.ProjectionProperty

class UserTimingAverageDataTransformer implements EventResultTransformer {
    Set<ProjectionProperty> baseProjections

    @Override
    List<EventResultProjection> transformRawQueryResult(List<Map> rawQueryData) {
        List<EventResultProjection> projections = []
        rawQueryData.each { Map dbResult ->
            def userTimingValue = dbResult.type == UserTimingType.MEASURE ? dbResult.duration : dbResult.startTime
            String key = AggregationUtil.generateGroupedKeyForAggregations(dbResult, baseProjections)
            EventResultProjection projection = getRelevantProjection(projections, key)
            projection.projectedProperties.put(dbResult.name, userTimingValue)
            dbResult.remove('name')
            dbResult.remove('startTime')
            dbResult.remove('duration')
            dbResult.remove('type')
            projection.projectedProperties.putAll(dbResult)
        }
        return projections
    }

    EventResultProjection getRelevantProjection(List<EventResultProjection> projections, String key) {
        EventResultProjection relevantProjection = projections.find { EventResultProjection projection ->
            projection.id == key
        }
        if (!relevantProjection) {
            relevantProjection = new EventResultProjection(id: key)
            projections.add(relevantProjection)
        }
        return relevantProjection
    }
}
