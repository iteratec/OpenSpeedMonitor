package de.iteratec.osm.result.dao.query.transformer

import de.iteratec.osm.result.UserTimingType
import de.iteratec.osm.result.dao.EventResultProjection

class UserTimingRawDataTransformer implements EventResultTransformer{

    @Override
    List<EventResultProjection> transformRawQueryResult(List<Map> rawQueryData) {
        List<EventResultProjection> projections = []
        rawQueryData.each { Map dbResult ->
            def userTimingValue = dbResult.type == UserTimingType.MEASURE ? dbResult.duration : dbResult.startTime
            EventResultProjection projection = getRelevantProjection(dbResult, projections)
            projection.projectedProperties.put(dbResult.name, userTimingValue)
            dbResult.remove('id')
            dbResult.remove('name')
            dbResult.remove('startTime')
            dbResult.remove('duration')
            dbResult.remove('type')
            projection.projectedProperties.putAll(dbResult)
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
