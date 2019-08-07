package de.iteratec.osm.result.dao.query.transformer

import de.iteratec.osm.result.UserTimingType
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.query.AggregationUtil
import de.iteratec.osm.result.dao.query.ProjectionProperty

class UserTimingRawDataTransformer implements EventResultTransformer {
    Set<ProjectionProperty> baseProjections

    @Override
    List<EventResultProjection> transformRawQueryResult(List<Map> rawQueryData) {
        Map<Object, List<Map>> rawDataById = rawQueryData.groupBy { Map dbResult ->
            dbResult.id
        }
        return rawDataById.collect { id, List<Map> rawDatasOfId ->
            EventResultProjection projectionOfId = new EventResultProjection(id: id)
            projectionOfId.projectedProperties.putAll(AggregationUtil.getMetaData(rawDatasOfId[0], baseProjections))
            rawDatasOfId.inject(projectionOfId) { EventResultProjection projection, Map rawDataOfId ->
                def userTimingValue = rawDataOfId.type == UserTimingType.MEASURE ? rawDataOfId.duration : rawDataOfId.startTime
                projection.projectedProperties.put(rawDataOfId.name, userTimingValue)
                return projection
            }
            return projectionOfId
        }
    }

}
