/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* 	http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

package de.iteratec.osm.measurement.environment.wptserverproxy

import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.PageService
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(ResultPersisterService)
@Mock([WebPageTestServer, Browser, Location, Job, JobResult, EventResult, BrowserAlias, Page, MeasuredEvent, JobGroup, Script, ConnectivityProfile])
class PersistConnectivityInNewEventResultSpec extends Specification {

    public static final String PROXY_IDENTIFIER_WPT_SERVER = 'dev.server02.wpt.iteratec.de'

    public static
    final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO = 'MULTISTEP_FORK_ITERATEC_1Run_3Events_JustFirstView_WithoutVideo.xml'
    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_1 = 'otto_homepage'
    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_2 = 'otto_search_shoes'
    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_3 = 'otto_product_boot'

    public static
    final String RESULT_XML_SINGLESTEP_1RUN_WITHVIDEO = 'BEFORE_MULTISTEP_1Run_WithVideo.xml'
    public static final String RESULT_XML_SINGLESTEP_1RUN_WITHVIDEO_EVENTNAME = 'IE_otto_hp_singlestep'

    public static Page UNDEFINED_PAGE
    public static final String LABEL_MULTISTEP_JOB = 'FF_Otto_multistep'
    public static final String LABEL_SINGLESTEP_JOB = 'IE_otto_hp_singlestep'
    public static final String NAME_PREDEFINED_CONNECTIVITY_PROFILE = 'DSL 6.000'

    ResultPersisterService serviceUnderTest
    ServiceMocker mocker
    def doWithSpring = {
        pageService(PageService)
        performanceLoggingService(PerformanceLoggingService)
        jobDaoService(JobDaoService)
    }

    void setup() {

        serviceUnderTest = service

        createTestDataCommonForAllTests()
        createMocksCommonForAllTests()

    }

    void createTestDataCommonForAllTests() {
        JobGroup jobGroup = TestDataUtil.createJobGroup(JobGroup.UNDEFINED_CSI)
        WebPageTestServer wptServer = TestDataUtil.createWebPageTestServer(PROXY_IDENTIFIER_WPT_SERVER, PROXY_IDENTIFIER_WPT_SERVER, true, "http://${PROXY_IDENTIFIER_WPT_SERVER}/")
        Browser ff = TestDataUtil.createBrowser('Firefox', 1d)
        Browser ie = TestDataUtil.createBrowser('IE', 1d)
        Location locationFirefox = TestDataUtil.createLocation(wptServer, 'iteratec-dev-hetzner-win7:Firefox', ff, true)
        Location locationIe = TestDataUtil.createLocation(wptServer, 'NewYork:IE 11', ie, true)
        UNDEFINED_PAGE = TestDataUtil.createUndefinedPage()

        Script testScript = TestDataUtil.createScript('test-script', 'description', 'navigate   http://my-url.de')
        TestDataUtil.createJob(LABEL_MULTISTEP_JOB, testScript, locationFirefox, jobGroup, '', 1, false, 60)
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_1, UNDEFINED_PAGE)
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_2, UNDEFINED_PAGE)
        TestDataUtil.createMeasuredEvent(RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO_EVENTNAME_3, UNDEFINED_PAGE)
        TestDataUtil.createJob(LABEL_SINGLESTEP_JOB, testScript, locationIe, jobGroup, '', 1, false, 60)
        TestDataUtil.createMeasuredEvent(RESULT_XML_SINGLESTEP_1RUN_WITHVIDEO_EVENTNAME, UNDEFINED_PAGE)
        TestDataUtil.createConnectivityProfile(NAME_PREDEFINED_CONNECTIVITY_PROFILE)
    }

    void createMocksCommonForAllTests() {
        mocker = ServiceMocker.create()
        mocker.mockProxyService(serviceUnderTest)
        serviceUnderTest.pageService = grailsApplication.mainContext.getBean('pageService')
        mocker.mockCsiAggregationTagService(serviceUnderTest, [:], [:], [:], [:], [:])
        serviceUnderTest.metaClass.informDependents = { List<EventResult> results ->
            // not the concern of this test
        }
        mocker.mockTTCsMappingService(serviceUnderTest)
        serviceUnderTest.performanceLoggingService = grailsApplication.mainContext.getBean('performanceLoggingService')
    }

    // multistep ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    void "MULTISTEP: persisting predefined connectivity profile in event result"() {
        setup:
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO}")
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(xmlResultFile))
        TestDataUtil.setPredefinedConnectivityForJob(
                ConnectivityProfile.findByName(NAME_PREDEFINED_CONNECTIVITY_PROFILE),
                Job.findByLabel(LABEL_MULTISTEP_JOB)
        )

        when:
        serviceUnderTest.listenToResult(xmlResult, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))

        then:
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == 3
        allResults[0].connectivityProfile.name == NAME_PREDEFINED_CONNECTIVITY_PROFILE
        allResults[0].customConnectivityName == null
        allResults[1].connectivityProfile.name == NAME_PREDEFINED_CONNECTIVITY_PROFILE
        allResults[1].customConnectivityName == null
        allResults[2].connectivityProfile.name == NAME_PREDEFINED_CONNECTIVITY_PROFILE
        allResults[2].customConnectivityName == null

    }

    void "MULTISTEP: persisting custom connectivity in event result"() {
        setup:
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        Job multistepJob = Job.findByLabel(LABEL_MULTISTEP_JOB)
        TestDataUtil.setCustomConnectivityForJob(multistepJob)
        multistepJob.refresh()

        when:
        serviceUnderTest.listenToResult(xmlResult, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))

        then:
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == 3
        allResults[0].connectivityProfile == null
        allResults[0].customConnectivityName == multistepJob.customConnectivityName
        allResults[1].connectivityProfile == null
        allResults[1].customConnectivityName == multistepJob.customConnectivityName
        allResults[2].connectivityProfile == null
        allResults[2].customConnectivityName == multistepJob.customConnectivityName

    }

    void "MULTISTEP: persisting native connectivity in event result"() {
        setup:
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY_WITHOUTVIDEO}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        Job multistepJob = Job.findByLabel(LABEL_MULTISTEP_JOB)
        TestDataUtil.setNativeConnectivityForJob(multistepJob)
        multistepJob.refresh()

        when:
        serviceUnderTest.listenToResult(xmlResult, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))

        then:
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == 3
        allResults[0].connectivityProfile == null
        allResults[0].customConnectivityName == null
        allResults[0].noTrafficShapingAtAll == true
        allResults[1].connectivityProfile == null
        allResults[1].customConnectivityName == null
        allResults[1].noTrafficShapingAtAll == true
        allResults[2].connectivityProfile == null
        allResults[2].customConnectivityName == null
        allResults[2].noTrafficShapingAtAll == true

    }

    // singlestep ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Expecting Repeated View && Lable = IE_otto_hp_singlestep && EventName = IE_otto_hp_singlestep
    void "SINGLESTEP: persisting predefined connectivity profile in event result"() {
        setup:
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_SINGLESTEP_1RUN_WITHVIDEO}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        TestDataUtil.setPredefinedConnectivityForJob(
                ConnectivityProfile.findByName(NAME_PREDEFINED_CONNECTIVITY_PROFILE),
                Job.findByLabel(LABEL_SINGLESTEP_JOB)
        )

        when:
        serviceUnderTest.listenToResult(xmlResult, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))

        then:
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == 2
        allResults[0].connectivityProfile.name == NAME_PREDEFINED_CONNECTIVITY_PROFILE
        allResults[0].customConnectivityName == null
        allResults[1].connectivityProfile.name == NAME_PREDEFINED_CONNECTIVITY_PROFILE
        allResults[1].customConnectivityName == null

    }
    // Expecting Repeated View && Lable = IE_otto_hp_singlestep && EventName = IE_otto_hp_singlestep
    void "SINGLESTEP: persisting custom connectivity in event result"() {
        setup:
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_SINGLESTEP_1RUN_WITHVIDEO}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        Job multistepJob = Job.findByLabel(LABEL_SINGLESTEP_JOB)
        TestDataUtil.setCustomConnectivityForJob(multistepJob)
        multistepJob.refresh()

        when:
        serviceUnderTest.listenToResult(xmlResult, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))

        then:
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == 2
        allResults[0].connectivityProfile == null
        allResults[0].customConnectivityName == multistepJob.customConnectivityName
        allResults[1].connectivityProfile == null
        allResults[1].customConnectivityName == multistepJob.customConnectivityName

    }
    // Expecting Repeated View && Lable = IE_otto_hp_singlestep && EventName = IE_otto_hp_singlestep
    void "SINGLESTEP: persisting native connectivity in event result"() {
        setup:
        File xmlResultFile = new File("test/resources/WptResultXmls/${RESULT_XML_SINGLESTEP_1RUN_WITHVIDEO}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        Job multistepJob = Job.findByLabel(LABEL_SINGLESTEP_JOB)
        TestDataUtil.setNativeConnectivityForJob(multistepJob)
        multistepJob.refresh()

        when:
        serviceUnderTest.listenToResult(xmlResult, WebPageTestServer.findByProxyIdentifier(PROXY_IDENTIFIER_WPT_SERVER))

        then:
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == 2
        allResults[0].connectivityProfile == null
        allResults[0].customConnectivityName == null
        allResults[0].noTrafficShapingAtAll == true
        allResults[1].connectivityProfile == null
        allResults[1].customConnectivityName == null
        allResults[1].noTrafficShapingAtAll == true

    }

}
