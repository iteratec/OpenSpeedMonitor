package de.iteratec.osm.api.dto

import de.iteratec.osm.result.MeasuredEvent


class JsonMeasuredEvent {

    long id
    String name
    JsonPage testedPage

    public static JsonMeasuredEvent create(MeasuredEvent measuredEvent) {
        JsonMeasuredEvent result = new JsonMeasuredEvent()

        result.id = measuredEvent.id
        result.name = measuredEvent.name
        result.testedPage = JsonPage.create(measuredEvent.testedPage)

        return result
    }

    public static Collection<JsonMeasuredEvent> create(Collection<MeasuredEvent> measuredEvents) {
        Set<JsonMeasuredEvent> result = []

        measuredEvents.each {
            result.add(create(it))
        }

        return result
    }
}
