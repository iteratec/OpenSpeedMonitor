package de.iteratec.osm.result.dao.query.transformer

import de.iteratec.osm.result.dao.EventResultProjection


class MeasurandRawDataTransformer implements EventResultTransformer {
    @Override
    List<EventResultProjection> transformRawQueryResult(List<Map> rawQueryData) {
        List<EventResultProjection> eventResultProjections = []
        rawQueryData.each {Map dbResult ->
            EventResultProjection eventResultProjection = new EventResultProjection(
                    id: dbResult.id
            )
            dbResult.remove('id')
            eventResultProjection.projectedProperties = dbResult
            eventResultProjections += eventResultProjection
        }
        return eventResultProjections
    }
}
