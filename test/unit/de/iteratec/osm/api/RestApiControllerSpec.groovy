package de.iteratec.osm.api

import de.iteratec.osm.api.dto.BrowserDto
import de.iteratec.osm.api.dto.CsiConfigurationDto
import de.iteratec.osm.api.dto.JobGroupDto
import de.iteratec.osm.api.dto.LocationDto
import de.iteratec.osm.api.dto.MeasuredEventDto
import de.iteratec.osm.api.dto.PageDto
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiDay
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.csi.TimeToCsMapping
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.DefaultBrowserDaoService
import de.iteratec.osm.measurement.environment.DefaultLocationDaoService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.DefaultJobGroupDaoService
import de.iteratec.osm.measurement.schedule.DefaultPageDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.dao.DefaultMeasuredEventDaoService
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.json.JSONObject
import spock.lang.Specification

@TestFor(RestApiController)
@Mock([CsiConfiguration, CsiDay, Page, TimeToCsMapping, JobGroup, MeasuredEvent,Page, Browser, Location, WebPageTestServer])
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
        WebPageTestServer server = TestDataUtil.createWebPageTestServer("server1","web.de",true,"http://internet.de")
        Location location1 = TestDataUtil.createLocation(server,"location1",browser1,true)
        Location location2 = TestDataUtil.createLocation(server,"location2",browser2,true)

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

    private void mockServices() {
        controllerUnderTest.jobGroupDaoService = new DefaultJobGroupDaoService()
        controllerUnderTest.measuredEventDaoService = new DefaultMeasuredEventDaoService()
        controllerUnderTest.browserDaoService = new DefaultBrowserDaoService()
        controllerUnderTest.pageDaoService = new DefaultPageDaoService()
        controllerUnderTest.locationDaoService = new DefaultLocationDaoService()
    }
}
