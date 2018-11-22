package de.iteratec.osm.result

import grails.persistence.Entity
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true, includeFields=true)
@EqualsAndHashCode(includeFields = true)
@Entity
class UserTiming {

    String name
    Double startTime
    Double duration
    UserTimingType type

    static belongsTo = [EventResult]

    static constraints = {
        name(nullable: false)
        startTime(nullable: false)
        duration(nullable: true, validator: { currentDuration, userTimingInstance ->
            boolean  isMark = userTimingInstance.type == UserTimingType.MARK && currentDuration == null
            boolean  isHero = userTimingInstance.type == UserTimingType.HERO_MARK && currentDuration == null
            boolean isMeasure = userTimingInstance.type == UserTimingType.MEASURE && currentDuration != null
            return isMark || isMeasure || isHero
        })
    }

    Double getValue(){
        return this.type == (UserTimingType.MARK || UserTimingType.HERO_MARK) ? this.startTime : this.duration
    }
}
