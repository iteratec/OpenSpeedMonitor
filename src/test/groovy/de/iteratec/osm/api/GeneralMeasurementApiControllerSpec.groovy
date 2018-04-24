package de.iteratec.osm.api

import de.iteratec.osm.api.dto.*
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiDay
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TimeToCsMapping
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.DefaultJobGroupDaoService
import de.iteratec.osm.measurement.schedule.Job
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
class GeneralMeasurementApiControllerSpec extends Specification implements BuildDataTest,
        ControllerUnitTest<GeneralMeasurementApiController> {

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

    def setup() {
        createTestDataCommonToAllTests()
        initInnerServices()
    }

    void setupSpec() {
        mockDomains(CsiConfiguration, CsiDay, Page, TimeToCsMapping, JobGroup, MeasuredEvent, Page, Browser, Location,
                WebPageTestServer, Job, Script)
    }

    def cleanup() {
    }

    void "get all JobGroups as JSON, which have a csiConfiguration, when existing"() {
        given: "2 JobGroups exist"
        Collection<JobGroupDto> jobGroupsAsJson = JobGroupDto.create([jobGroupWithCsiConfiguration1, jobGroupWithCsiConfiguration2])

        when: "REST method to get all JobGroups is called"
        controller.allSystems()

        then: "it returns DTO json representation of these JobGroups"
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.has('target')
        resultJSON.target != null
        resultJSON.target.size() == jobGroupsAsJson.size()
    }

    void "get all Steps as JSON, when existing"() {
        given: "2 MeasuredEvents exist"
        MeasuredEvent event1 = MeasuredEvent.build()
        MeasuredEvent event2 = MeasuredEvent.build()

        Collection<MeasuredEventDto> measuredEventAsJson = MeasuredEventDto.create([event1, event2])

        when: "REST method to get all MeasuredEvents is called"
        controller.allSteps()

        then: "it returns DTO representation of these MeasuredEvents"
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.has('target')
        resultJSON.target != null
        resultJSON.target.size() == measuredEventAsJson.size()
    }

    void "get all Browsers as JSON, when existing"() {
        given: "2 Browsers exist"
        Collection<BrowserDto> browserAsJson = BrowserDto.create([browser1, browser2])

        when: "REST method to get all Browsers is called"
        controller.allBrowsers()

        then: "it returns DTO representation of these Browsers"
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.has('target')
        resultJSON.target != null
        resultJSON.target.size() == browserAsJson.size()
    }

    void "get all Pages as JSON, when existing"() {
        given: "2 Pages exist"
        Collection<PageDto> pagesAsJson = PageDto.create([page1, page2])

        when: "REST method to get all Pages is called"
        controller.allPages()

        then: "it returns DTO representation of these Pages"
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.has('target')
        resultJSON.target != null
        resultJSON.target.size() == pagesAsJson.size()
    }

    void "get all Locations as JSON, when existing and active"() {
        given: "2 active Locations exist"
        Location location1 = Location.build(active: true, wptServer: WebPageTestServer.build(active: true))
        Location location2 = Location.build(active: true, wptServer: WebPageTestServer.build(active: true))

        Collection<LocationDto> locationsAsJson = LocationDto.create([location1, location2])

        when: "REST method to get all Locations is called"
        controller.allLocations()

        then: "it returns DTO representation of these Locations"
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.has('target')
        resultJSON.target != null
        resultJSON.target.size() == locationsAsJson.size()
    }

    void "get just active Locations as JSON, when active and not active existing"() {
        given: "1 active, 1 not active and 1 active but wptServer not active Locations exist"
        Location locationActive = Location.build(active: true, wptServer: WebPageTestServer.build(active: true))
        Location.build(active: false, wptServer: WebPageTestServer.build(active: true))
        Location.build(active: true, wptServer: WebPageTestServer.build(active: false))

        Collection<LocationDto> activeLocationsAsJson = LocationDto.create([locationActive])

        when: "REST method to get all Locations is called"
        controller.allLocations()

        then: "it returns DTO representation of just the active Location"
        response.status == 200
        JSONObject resultJSON = JSON.parse(response.text)
        resultJSON.has('target')
        resultJSON.target != null
        resultJSON.target.size() == activeLocationsAsJson.size()
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
    }
}
