package de.iteratec.osm.result.dao.query.transformer

import de.iteratec.osm.result.dao.EventResultProjection

interface EventResultTransformer {

    List<EventResultProjection> transformRawQueryResult(List<Map> rawQueryData)
}
