package de.iteratec.osm.result

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true, includeFields=true)
@EqualsAndHashCode(includeFields = true)
class UserTiming {
    String name
    Double startTime
    Double duration
    UserTimingType type

    static belongsTo = [eventResult: EventResult]

    static constraints = {
        name(nullable: false)
        startTime(nullable: false)
        duration(nullable: true)
    }
}
