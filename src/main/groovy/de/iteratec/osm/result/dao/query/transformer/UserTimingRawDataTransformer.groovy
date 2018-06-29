package de.iteratec.osm.result.dao.query.transformer

import de.iteratec.osm.result.UserTimingType
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.query.AggregationUtil
import de.iteratec.osm.result.dao.query.ProjectionProperty

class UserTimingRawDataTransformer implements EventResultTransformer{
    Set<ProjectionProperty> baseProjections

    @Override
    List<EventResultProjection> transformRawQueryResult(List<Map> rawQueryData) {
        List<EventResultProjection> projections = []
        rawQueryData.each { Map dbResult ->
            def userTimingValue = dbResult.type == UserTimingType.MEASURE ? dbResult.duration : dbResult.startTime
            EventResultProjection projection = getRelevantProjection(dbResult, projections)
            projection.projectedProperties.put(dbResult.name, userTimingValue)
            projection.projectedProperties.putAll(AggregationUtil.getMetaData(dbResult, baseProjections))
        }
        return projections
    }

    EventResultProjection getRelevantProjection(Map dbResult, List<EventResultProjection> projections) {
        EventResultProjection relevantProjection = projections.find { EventResultProjection projection ->
            projection.id == dbResult.id
        }
        if (!relevantProjection) {
            relevantProjection = new EventResultProjection(id: dbResult.id)
            projections.add(relevantProjection)
        }
        return relevantProjection
    }

}
