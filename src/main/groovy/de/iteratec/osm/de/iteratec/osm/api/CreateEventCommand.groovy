package de.iteratec.osm.de.iteratec.osm.api

import de.iteratec.osm.api.ApiKey
import de.iteratec.osm.measurement.schedule.JobGroup
import grails.validation.Validateable
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

import static de.iteratec.osm.util.Constants.DEFAULT_ACCESS_DENIED_MESSAGE

/**
 * Parameters of rest api function /rest/event/create.
 * Created by nkuhn on 08.05.15.
 */
class CreateEventCommand implements Validateable{

    public static final DateTimeFormatter API_DATE_FORMAT = ISODateTimeFormat.basicDateTimeNoMillis()

    String apiKey
    String shortName
    List<String> system = [].withLazyDefault { new String() }
    String eventTimestamp
    String description
    Boolean globallyVisible

    static transients = { ['eventTimeStampAsDateTime', 'jobGroups'] }

    /**
     * Constraints needs to fit.
     */
    static constraints = {
        apiKey(validator: { String currentKey, CreateEventCommand cmd ->
            ApiKey validApiKey = ApiKey.findBySecretKey(currentKey)
            if (!validApiKey?.allowedForCreateEvent) return [DEFAULT_ACCESS_DENIED_MESSAGE]
            else return true
        })
        shortName(nullable: false, blank: false)
        system(validator: { List<String> currentSystems, CreateEventCommand cmd ->
            if (currentSystems.size() == 0) return ["You have to submit at least one job group for the event."]
            int invalidJobGroups = 0
            currentSystems.each { String system ->
                if (!JobGroup.findByName(system)) invalidJobGroups++
            }
            if (invalidJobGroups > 0) return ["At least one of the submitted job groups doesn't exist."]
            else return true
        })
        eventTimestamp(nullable: true, validator: { String currentTimestamp, CreateEventCommand cmd ->
            if (currentTimestamp != null) {
                try {
                    API_DATE_FORMAT.parseDateTime(currentTimestamp)
                } catch (Exception e) {
                    return ["The date has the wrong format. Has to be in format ISO 8601. The 1st January 2014, 11 PM (UTC) in that format: 20140101T230000Z."]
                }
            }
            return true
        })
        description(nullable: true)
        globallyVisible(nullable: true)
    }

    public DateTime getEventTimeStampAsDateTime() {
        return eventTimestamp != null ? API_DATE_FORMAT.parseDateTime(eventTimestamp) : new DateTime();
    }

    public List<JobGroup> getJobGroups() {
        return system.collect { JobGroup.findByName(it) }
    }
}
