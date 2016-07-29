package de.iteratec.osm.api

import de.iteratec.osm.api.dto.*
import de.iteratec.osm.csi.*
import de.iteratec.osm.measurement.environment.*
import de.iteratec.osm.measurement.schedule.DefaultJobGroupDaoService
import de.iteratec.osm.measurement.schedule.DefaultPageDaoService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.dao.DefaultMeasuredEventDaoService
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.grails.web.json.JSONObject
import spock.lang.Specification

@TestFor(RestApiController)
@Mock([CsiConfiguration, CsiDay, Page, TimeToCsMapping, JobGroup, MeasuredEvent, Page, Browser, Location, WebPageTestServer, Job])
class RestApiControllerSpec extends Specification {
    RestApiController controllerUnderTest
    CsiConfiguration csiConfiguration
    String csiConfigurationLabel
    Page page1
    Page page2
    Browser browser1
    Browser browser2
    JobGroup jobGroupWithCsiConfiguration1
    JobGroup jobGroupWithCsiConfiguration2
    JobGroup jobGroupWithoutCsiConfiguration1
    JobGroup jobGroupWithoutCsiConfiguration2
    def doWithSpring = {
        defaultJobGroupDaoService(DefaultJobGroupDaoService)
        defaultMeasuredEventDaoService(DefaultMeasuredEventDaoService)
        defaultBrowserDaoService(DefaultBrowserDaoService)
        defaultPageDaoService(DefaultPageDaoService)
        defaultLocationDaoService(DefaultLocationDaoService)
    }

    void "setup"() {
        controllerUnderTest = controller

        page1 = new Page(name: "testPage1").save(failOnError: true)
        page2 = new Page(name: "testPage2").save(failOnError: true)


        browser1 = TestDataUtil.createBrowser("browser1", 0)
        browser2 = TestDataUtil.createBrowser("browser2", 0)

        csiConfigurationLabel = "csiConfiguration"
        csiConfiguration = TestDataUtil.createCsiConfiguration()
        csiConfiguration.timeToCsMappings = TestDataUtil.createTimeToCsMappingForAllPages([page1])
        csiConfiguration.save()

        jobGroupWithCsiConfiguration1 = new JobGroup(csiConfiguration: csiConfiguration, name: "jobGroup1").save(failOnError: true)
        jobGroupWithCsiConfiguration2 = new JobGroup(csiConfiguration: csiConfiguration, name: "jobGroup2").save(failOnError: true)
        jobGroupWithoutCsiConfiguration1 = new JobGroup(csiConfiguration: null, name: "jobGroup3").save(failOnError: true)
        jobGroupWithoutCsiConfiguration2 = new JobGroup(csiConfiguration: null, name: "jobGroup4").save(failOnError: true)
        mockServices()
    }

    void "get all JobGroups as JSON, which have a csiConfiguration, when existing"() {
        given:
        Collection<JobGroupDto> jobGroupsAsJson = JobGroupDto.create([jobGroupWithCsiConfiguration1, jobGroupWithCsiConfiguration2])

        when:
        controllerUnderTest.allSystems()

        then:
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.has('target')
        resultJSON.target != null
        resultJSON.target.size() == jobGroupsAsJson.size()
    }

    void "get all Steps as JSON, when existing"() {
        given:
        MeasuredEvent event1 = TestDataUtil.createMeasuredEvent("event1", page1)
        MeasuredEvent event2 = TestDataUtil.createMeasuredEvent("event2", page2)

        Collection<MeasuredEventDto> measuredEventAsJson = MeasuredEventDto.create([event1, event2])

        when:
        controllerUnderTest.allSteps()

        then:
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.has('target')
        resultJSON.target != null
        resultJSON.target.size() == measuredEventAsJson.size()
    }

    void "get all Browsers as JSON, when existing"() {
        given:
        Collection<BrowserDto> browserAsJson = BrowserDto.create([browser1, browser2])

        when:
        controllerUnderTest.allBrowsers()

        then:
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.has('target')
        resultJSON.target != null
        resultJSON.target.size() == browserAsJson.size()
    }

    void "get all Pages as JSON, when existing"() {
        given:
        Collection<PageDto> pagesAsJson = PageDto.create([page1, page2])

        when:
        controllerUnderTest.allPages()

        then:
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.has('target')
        resultJSON.target != null
        resultJSON.target.size() == pagesAsJson.size()
    }

    void "get all Locations as JSON, when existing"() {
        given:
        WebPageTestServer server = TestDataUtil.createWebPageTestServer("server1", "web.de", true, "http://internet.de")
        Location location1 = TestDataUtil.createLocation(server, "location1", browser1, true)
        Location location2 = TestDataUtil.createLocation(server, "location2", browser2, true)

        Collection<LocationDto> locationsAsJson = LocationDto.create([location1, location2])

        when:
        controllerUnderTest.allPages()

        then:
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.has('target')
        resultJSON.target != null
        resultJSON.target.size() == locationsAsJson.size()
    }

    void "existing csiConfiguration by id as JSON"() {
        given:
        int csiConfigurationId = csiConfiguration.id
        CsiConfigurationDto jsonCsiConfiguration = CsiConfigurationDto.create(csiConfiguration)

        when:
        params.id = csiConfigurationId
        controllerUnderTest.getCsiConfiguration()

        then:
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.has('target')
        resultJSON.target != null
        resultJSON.target.each { key, value ->
            value == jsonCsiConfiguration.getProperty(key)
        }
    }

    void "return 400 when asking for non-existing csiConfiguration"() {
        given:
        int csiConfigurationId = Integer.MAX_VALUE

        when:
        params.id = csiConfigurationId
        controllerUnderTest.getCsiConfiguration()

        then:
        response.status == 400
    }

    void "getting correct mappings for domain classes"() {
        when: "user requests mappings"
        def requestMap = [:]
        requestMap.put(requestedDomain, requestedIDs)
        params.requestedDomains = requestMap
        controllerUnderTest.getNamesForIds()

        then: "response contains correct mappings"
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.target[requestedDomain].size() == expectedMappings.size()
        resultJSON.target[requestedDomain].each { key, value ->
            expectedMappings[key] == value
        }

        where:
        requestedDomain | requestedIDs || expectedMappings
        "JobGroup"      | [1, 2, 3, 4] || [1: "jobGroup1", 2: "jobGroup2", 3: "jobGroup3", 4: "jobGroup4"]
    }

    void "getting correct ids for names"() {
        given: "some data for retrievable domain clases"
        WebPageTestServer wptServer = TestDataUtil.createWebPageTestServer("wptServer", "identifier", false, "http://internet.de")
        TestDataUtil.createLocation(wptServer, "location1", browser1, false)
        TestDataUtil.createLocation(wptServer, "location2", browser2, false)
        TestDataUtil.createMeasuredEvent("measuredEvent1", page1)
        TestDataUtil.createMeasuredEvent("measuredEvent2", page2)

        when: "user requests mappings"
        def requestMap = [:]
        requestMap.put(requestedDomain, requestedNames)
        params.requestedDomains = requestMap
        controllerUnderTest.getIdsForNames()

        then: "response contains correct mappings"
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.target[requestedDomain].size() == expectedMappings.size()
        resultJSON.target[requestedDomain].each { key, value ->
            expectedMappings[key] == value
        }

        where:
        requestedDomain | requestedNames                       || expectedMappings
        "Browser"       | ["browser1", "browser2"]             || [1: "browser1", 2: "browser2"]
        "Page"          | ["testPage1", "testPage2"]           || [1: "testPage1", 2: "testPage2"]
        "Location"      | ["location1", "location2"]           || [1: "location1", 2: "location2"]
        "MeasuredEvent" | ["measuredEvent1", "measuredEvent2"] || [1: "measuredEvent1", 2: "measuredEvent2"]
    }

    void "getting correct mappings for domain classes with serveral domains"() {
        when: "user requests mappings"
        def requestMap = [:]
        requestMap.put("Browser", ["browser1", "browser2"])
        requestMap.put("Page", ["testPage1", "testPage2"])
        params.requestedDomains = requestMap
        controllerUnderTest.getIdsForNames()

        then: "response contains correct mappings"
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.target["Browser"].size() == 2
        resultJSON.target["Browser"]["1"] == "browser1"
        resultJSON.target["Browser"]["2"] == "browser2"
        resultJSON.target["Page"].size() == 2
        resultJSON.target["Page"]["1"] == "testPage1"
        resultJSON.target["Page"]["2"] == "testPage2"
    }

    void "return 400 if requested domain class does not exists"() {
        when: "user requests names for ids with bad request"
        def requestMap = [:]
        requestMap.put("JobGroup", [1, 2])
        requestMap.put("wrong domain", [1, 2])
        params.requestedDomains = requestMap
        controllerUnderTest.getNamesForIds()

        then: "response contains correct mappings"
        response.status == 400

        when: "user requests ids for names with bad request"
        requestMap = [:]
        requestMap.put("Browser", ["browser1"])
        requestMap.put("wrong domain", ["name"])
        params.requestedDomains = requestMap
        controllerUnderTest.getIdsForNames()

        then: "response contains correct mappings"
        response.status == 400
    }

    void "return empty map if id is not found"() {
        when: "user requests names for non exiisting ids"
        def requestMap = [:]
        requestMap.put(domainName, idList)
        params.requestedDomains = requestMap
        controllerUnderTest.getNamesForIds()

        then: "a empty map is returned"
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.target[domainName] == expectedResult

        where:
        domainName | idList      || expectedResult
        "JobGroup" | [20, 30]    || [:]
        "JobGroup" | [1, 20, 30] || ["1": "jobGroup1"]
    }

    private void mockServices() {
        controllerUnderTest.jobGroupDaoService = grailsApplication.mainContext.getBean('defaultJobGroupDaoService')
        controllerUnderTest.measuredEventDaoService = grailsApplication.mainContext.getBean('defaultMeasuredEventDaoService')
        controllerUnderTest.browserDaoService = grailsApplication.mainContext.getBean('defaultBrowserDaoService')
        controllerUnderTest.pageDaoService = grailsApplication.mainContext.getBean('defaultPageDaoService')
        controllerUnderTest.locationDaoService = grailsApplication.mainContext.getBean('defaultLocationDaoService')
    }
}
