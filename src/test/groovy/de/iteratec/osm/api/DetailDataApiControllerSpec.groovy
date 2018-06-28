package de.iteratec.osm.api

import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiDay
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TimeToCsMapping
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.DefaultJobGroupDaoService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.MeasuredEvent
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.converters.JSON
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.web.json.JSONObject
import spock.lang.Specification

@Build([Page, Browser, CsiConfiguration, JobGroup, MeasuredEvent, Location, WebPageTestServer, Job])
class DetailDataApiControllerSpec extends Specification implements BuildDataTest,
        ControllerUnitTest<DetailDataApiController> {

    public static final String LABEL_JOB_1 = "job1"
    public static final String LABEL_JOB_2 = "job2"
    public static final String UNIQUE_IDENTIFIER_LOCATION_1 = "location1"
    public static final String UNIQUE_IDENTIFIER_LOCATION_2 = "location2"
    public static final String NAME_MEASURED_EVENT_1 = "measuredEvent1"
    public static final String NAME_MEASURED_EVENT_2 = "measuredEvent2"

    public static final String NAME_PAGE1 = "testPage1"
    public static final String NAME_PAGE2 = "testPage2"
    public static final String NAME_BROWSER1 = "browser1"
    public static final String NAME_BROWSER2 = "browser2"
    public static final String NAME_JOBGROUP_1_WITH_CONFIG = "jobGroup1"
    public static final String NAME_JOBGROUP_2_WITH_CONFIG = "jobGroup2"
    public static final String NAME_JOBGROUP_1_WITHOUT_CONFIG = "jobGroup3"
    public static final String NAME_JOBGROUP_2_WITHOUT_CONFIG = "jobGroup4"

    CsiConfiguration csiConfiguration
    Page page1
    Page page2
    Browser browser1
    Browser browser2
    JobGroup jobGroupWithCsiConfiguration1
    JobGroup jobGroupWithCsiConfiguration2
    JobGroup jobGroupWithoutCsiConfiguration1
    JobGroup jobGroupWithoutCsiConfiguration2

    Closure doWithSpring() {
        return {
            defaultJobGroupDaoService(DefaultJobGroupDaoService)
            browserService(BrowserService)
        }
    }

    void setup() {
        createTestDataCommonToAllTests()
        initInnerServices()
    }

    void setupSpec() {
        mockDomains(CsiConfiguration, CsiDay, Page, TimeToCsMapping, JobGroup, MeasuredEvent, Page, Browser, Location,
                WebPageTestServer, Job, Script, ConnectivityProfile)
    }

    void "getting correct mappings for domain classes"() {
        given: "some domain data in db"
        Job.build(label: LABEL_JOB_1)
        Job.build(label: LABEL_JOB_2)

        when: "user requests mappings"
        params.requestedDomains = ["${requestedDomain}": requestedIDs]
        controller.getNamesForIds()

        then: "response contains correct mappings"
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.target[requestedDomain].size() == expectedMappings.size()
        resultJSON.target[requestedDomain].each { key, value ->
            expectedMappings[key] == value
        }

        where:
        requestedDomain | requestedIDs || expectedMappings
        "JobGroup"      | [1, 2, 3, 4] || [1: NAME_JOBGROUP_1_WITH_CONFIG, 2: NAME_JOBGROUP_2_WITH_CONFIG, 3: NAME_JOBGROUP_1_WITHOUT_CONFIG, 4: NAME_JOBGROUP_2_WITHOUT_CONFIG]
        "Job"           | [1, 2]       || [1: LABEL_JOB_1, 2: LABEL_JOB_2]
    }

    void "getting correct ids for names"() {
        given: "some data for retrievable domain clases"
        Location.build(uniqueIdentifierForServer: UNIQUE_IDENTIFIER_LOCATION_1)
        Location.build(uniqueIdentifierForServer: UNIQUE_IDENTIFIER_LOCATION_2)
        MeasuredEvent.build(name: NAME_MEASURED_EVENT_1)
        MeasuredEvent.build(name: NAME_MEASURED_EVENT_2)

        when: "user requests mappings"
        params.requestedDomains = ["${requestedDomain}": requestedNames]
        controller.getIdsForNames()

        then: "response contains correct mappings"
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.target[requestedDomain].size() == expectedMappings.size()
        resultJSON.target[requestedDomain].each { key, value ->
            expectedMappings[key] == value
        }

        where:
        requestedDomain | requestedNames                                               || expectedMappings
        "Browser"       | [NAME_BROWSER1, NAME_BROWSER2]                               || [1: NAME_BROWSER1, 2: NAME_BROWSER2]
        "Page"          | [NAME_PAGE1, NAME_PAGE2]                                     || [1: NAME_PAGE1, 2: NAME_PAGE2]
        "Location"      | [UNIQUE_IDENTIFIER_LOCATION_1, UNIQUE_IDENTIFIER_LOCATION_2] || [1: UNIQUE_IDENTIFIER_LOCATION_1, 2: UNIQUE_IDENTIFIER_LOCATION_2]
        "MeasuredEvent" | [NAME_MEASURED_EVENT_1, NAME_MEASURED_EVENT_2]               || [1: NAME_MEASURED_EVENT_1, 2: NAME_MEASURED_EVENT_2]
    }

    void "getting correct mappings for domain classes with serveral domains"() {
        when: "user requests mappings"
        params.requestedDomains = [
                Browser: [NAME_BROWSER1, NAME_BROWSER2],
                Page: [NAME_PAGE1, NAME_PAGE2]
        ]
        controller.getIdsForNames()

        then: "response contains correct mappings"
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.target["Browser"].size() == 2
        resultJSON.target["Browser"]["1"] == NAME_BROWSER1
        resultJSON.target["Browser"]["2"] == NAME_BROWSER2
        resultJSON.target["Page"].size() == 2
        resultJSON.target["Page"]["1"] == NAME_PAGE1
        resultJSON.target["Page"]["2"] == NAME_PAGE2
    }

    void "return 400 if requested domain class does not exists"() {
        when: "user requests names for ids with bad request"
        params.requestedDomains = [
                JobGroup: [1, 2],
                WrongDomain: [1, 2]
        ]
        controller.getNamesForIds()

        then: "response status code is 400"
        response.status == 400

        when: "user requests ids for names with bad request"
        params.requestedDomains = [
                Browser: [NAME_BROWSER1],
                WrongDomain: ["name"]
        ]
        controller.getIdsForNames()

        then: "response status code is 400"
        response.status == 400
    }

    void "return a map without elements for which the id is not found"() {
        when: "user requests names for ids where some doesn't exist"
        params.requestedDomains = ["${domainName}": idList]
        controller.getNamesForIds()

        then: "a map without elements for which the id is not found is returned"
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.target[domainName] == expectedResult

        where:
        domainName | idList      || expectedResult
        "JobGroup" | [20, 30]    || [:]
        "JobGroup" | [1, 20, 30] || ["1": NAME_JOBGROUP_1_WITH_CONFIG]
    }

    private void createTestDataCommonToAllTests() {

        page1 = Page.build(name: NAME_PAGE1)
        page2 = Page.build(name: NAME_PAGE2)

        browser1 = Browser.build(name: NAME_BROWSER1)
        browser2 = Browser.build(name: NAME_BROWSER2)

        csiConfiguration = CsiConfiguration.build()

        jobGroupWithCsiConfiguration1 = JobGroup.build(csiConfiguration: csiConfiguration, name: NAME_JOBGROUP_1_WITH_CONFIG)
        jobGroupWithCsiConfiguration2 = JobGroup.build(csiConfiguration: csiConfiguration, name: NAME_JOBGROUP_2_WITH_CONFIG)
        jobGroupWithoutCsiConfiguration1 = JobGroup.build(csiConfiguration: null, name: NAME_JOBGROUP_1_WITHOUT_CONFIG)
        jobGroupWithoutCsiConfiguration2 = JobGroup.build(csiConfiguration: null, name: NAME_JOBGROUP_2_WITHOUT_CONFIG)

    }

    private void initInnerServices() {
        controller.jobGroupDaoService = grailsApplication.mainContext.getBean('defaultJobGroupDaoService')
        controller.jobDaoService = new JobDaoService()
    }
}
