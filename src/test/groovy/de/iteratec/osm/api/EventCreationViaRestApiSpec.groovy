package de.iteratec.osm.api

import de.iteratec.osm.de.iteratec.osm.api.CreateEventCommand
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.Event
import de.iteratec.osm.report.chart.EventDaoService
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import spock.lang.Specification

import static de.iteratec.osm.util.Constants.*

@Build([ApiKey, JobGroup])
class EventCreationViaRestApiSpec extends Specification implements BuildDataTest,
        ControllerUnitTest<GeneralMeasurementApiController> {

    static String APIKEY_ALLOWED = 'allowed'
    static String APIKEY_NOT_ALLOWED = 'not-allowed'
    static JobGroup group1, group2

    Closure doWithSpring() {
        return {
            eventDaoService(EventDaoService)
        }
    }

    void setup(){
        createTestDataCommonToAllTests()
        controller.eventDaoService = grailsApplication.mainContext.getBean('eventDaoService')
    }

    void setupSpec() {
        mockDomains(Job, ApiKey, JobGroup, Event)
    }

    //apiKey constraint violation ////////////////////////////////////

    void "fails cause of not allowed key"(){

        setup:
        //test specific data
        String shortName = 'my-event'
        String description = 'description'
        boolean globalVisibility = false

        when:
        params.apiKey = APIKEY_NOT_ALLOWED
        CreateEventCommand cmd = new CreateEventCommand(
                apiKey: APIKEY_NOT_ALLOWED,
                shortName: shortName,
                system: [group1.name, group2.name],
                eventTimestamp: '20140101T110000Z',
                description: description,
                globallyVisible: globalVisibility
        )
        cmd.validate()
        controller.securedViaApiKeyCreateEvent(cmd)

        then:
        //test written event
        Event.list().size() == 0
        response.status == 400
        response.contentAsString == "Error field apiKey: "+ DEFAULT_ACCESS_DENIED_MESSAGE + "\n"
    }

    //shortName constraint violation ////////////////////////////////////

    void "fails cause shortName is null"(){

        setup:
        //test specific data
        String shortName
        String description = 'description'
        boolean globalVisibility = false

        when:
        params.apiKey = APIKEY_ALLOWED
        CreateEventCommand cmd = new CreateEventCommand(
                apiKey: APIKEY_ALLOWED,
                shortName: shortName,
                system: [group1.name, group2.name],
                eventTimestamp: '20140101T110000Z',
                description: description,
                globallyVisible: globalVisibility
        )
        cmd.validate()
        controller.securedViaApiKeyCreateEvent(cmd)

        then:
        //test written event
        Event.list().size() == 0
        //test json representation
        response.status == 400
        response.text == "Error field shortName: nullable\n"
    }

    void "fails cause shortName is empty string"(){

        setup:
        //test specific data
        String shortName = ""
        String description = 'description'
        boolean globalVisibility = false

        when:
        params.apiKey = APIKEY_ALLOWED
        CreateEventCommand cmd = new CreateEventCommand(
                apiKey: APIKEY_ALLOWED,
                shortName: shortName,
                system: [group1.name, group2.name],
                eventTimestamp: '20140101T110000Z',
                description: description,
                globallyVisible: globalVisibility
        )
        cmd.validate()
        controller.securedViaApiKeyCreateEvent(cmd)

        then:
        //test written event
        Event.list().size() == 0
        //test json representation
        response.status == 400
        response.text == "Error field shortName: blank\n"
    }

    //system constraint violation ////////////////////////////////////

    void "fails cause no system is submitted"(){

        setup:
        //test specific data
        String shortName = "my-shortname"
        String description = 'description'
        boolean globalVisibility = false

        when:
        params.apiKey = APIKEY_ALLOWED
        CreateEventCommand cmd = new CreateEventCommand(
                apiKey: APIKEY_ALLOWED,
                shortName: shortName,
                eventTimestamp: '20140101T110000Z',
                description: description,
                globallyVisible: globalVisibility
        )
        cmd.validate()
        controller.securedViaApiKeyCreateEvent(cmd)

        then:
        //test written event
        Event.list().size() == 0
        //test json representation
        response.status == 400
        response.text == "Error field system: You have to submit at least one job group for the event.\n"
    }

    void "fails cause at least one of the submitted job gtoups doesn't exist"(){

        setup:
        //test specific data
        String shortName = "my-shortname"
        String description = 'description'
        boolean globalVisibility = false

        when:
        params.apiKey = APIKEY_ALLOWED
        CreateEventCommand cmd = new CreateEventCommand(
                apiKey: APIKEY_ALLOWED,
                shortName: shortName,
                eventTimestamp: '20140101T110000Z',
                system: [group1.name, 'NO_JOBGROUP_WITH_THIS_NAME_EXISTS'],
                description: description,
                globallyVisible: globalVisibility
        )
        cmd.validate()
        controller.securedViaApiKeyCreateEvent(cmd)

        then:
        //test written event
        Event.list().size() == 0
        //test json representation
        response.status == 400
        response.text == "Error field system: At least one of the submitted job groups doesn't exist.\n".encodeAsHTML()
    }

    //eventTimestamp constraint violation ////////////////////////////////////

    void "fails cause eventTimestamp is submitted with wrong format"(){

        setup:
        //test specific data
        String shortName = "my-shortname"
        String description = 'description'
        boolean globalVisibility = false

        when:
        params.apiKey = APIKEY_ALLOWED
        CreateEventCommand cmd = new CreateEventCommand(
                apiKey: APIKEY_ALLOWED,
                shortName: shortName,
                eventTimestamp: '2014-01-01 11:00',
                system: [group1.name, group2.name],
                description: description,
                globallyVisible: globalVisibility
        )
        cmd.validate()

        then:
        IllegalArgumentException e = thrown()
    }

    // successful event creation //////////////////////////////////////////////////////////////

    void "successful creation without defaults"(){

        setup:
        //test specific data
        String shortName = 'my-event'
        String description = 'description'
        DateTime expectedDate = new DateTime(2014, 1, 1, 11, 0, 0, DateTimeZone.UTC)
        boolean globalVisibility = false

        when:
        params.apiKey = APIKEY_ALLOWED
        CreateEventCommand cmd = new CreateEventCommand(
                apiKey: APIKEY_ALLOWED,
                shortName: shortName,
                system: [group1.name, group2.name],
                eventTimestamp: '20140101T110000Z',
                description: description,
                globallyVisible: globalVisibility
        )
        cmd.validate()
        controller.securedViaApiKeyCreateEvent(cmd)

        then:
        //test written event
        List<Event> persistedEvents = Event.list()
        persistedEvents.size() == 1
        Event persistedEvent = persistedEvents[0]
        persistedEvent.shortName == shortName
        persistedEvent.description == description
        persistedEvent.eventDate == expectedDate.toDate()
        persistedEvent.globallyVisible == globalVisibility
        List<JobGroup> associatedJobGroups = persistedEvent.jobGroups
        associatedJobGroups.size() == 2
        associatedJobGroups.contains(group1)
        associatedJobGroups.contains(group2)
        //test json representation
        response.json.shortName == shortName
        response.json.description == description
        new DateTime(response.json.eventDate) == expectedDate
        response.json.globallyVisible == globalVisibility
        response.json.jobGroups.size() == 2
    }

    void "successful creation with defaults"(){

        setup:
        //test specific data
        String shortName = 'my-event'
        boolean expectedGlobalVisibility = false

        when:
        params.apiKey = APIKEY_ALLOWED
        CreateEventCommand cmd = new CreateEventCommand(
                apiKey: APIKEY_ALLOWED,
                shortName: shortName,
                system: [group1.name, group2.name],
        )
        cmd.validate()
        controller.securedViaApiKeyCreateEvent(cmd)

        then:
        //test written event
        List<Event> persistedEvents = Event.list()
        persistedEvents.size() == 1
        Event persistedEvent = persistedEvents[0]
        persistedEvent.shortName == shortName
        persistedEvent.description == null
        new Duration(new DateTime(persistedEvent.eventDate), new DateTime()).standardMinutes < 5
        persistedEvent.globallyVisible == expectedGlobalVisibility
        List<JobGroup> associatedJobGroups = persistedEvent.jobGroups
        associatedJobGroups.size() == 2
        associatedJobGroups.contains(group1)
        associatedJobGroups.contains(group2)
        //test json representation
        response.json.shortName == shortName
        response.json.description.equals(null)
        new Duration(new DateTime(response.json.eventDate), new DateTime()).standardMinutes < 5
        response.json.globallyVisible == expectedGlobalVisibility
        response.json.jobGroups.size() == 2
    }

    private Object createTestDataCommonToAllTests() {
        return ApiKey.withTransaction {
            ApiKey.build(secretKey: APIKEY_ALLOWED, valid: true, allowedForCreateEvent: true)
            ApiKey.build(secretKey: APIKEY_NOT_ALLOWED, valid: true, allowedForCreateEvent: false)
            group1 = JobGroup.build()
            group2 = JobGroup.build()
        }
    }
}
