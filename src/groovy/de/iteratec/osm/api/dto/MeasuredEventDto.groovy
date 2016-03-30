package de.iteratec.osm.api.dto

import de.iteratec.osm.result.MeasuredEvent


class MeasuredEventDto {

    long id
    String name
    PageDto testedPage

    public static MeasuredEventDto create(MeasuredEvent measuredEvent) {
        MeasuredEventDto result = new MeasuredEventDto()

        result.id = measuredEvent.id
        result.name = measuredEvent.name
        result.testedPage = PageDto.create(measuredEvent.testedPage)

        return result
    }

    public static Collection<MeasuredEventDto> create(Collection<MeasuredEvent> measuredEvents) {
        Set<MeasuredEventDto> result = []

        measuredEvents.each {
            result.add(create(it))
        }

        return result
    }
}
