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

package de.iteratec.osm.measurement.environment.wptserver

import de.iteratec.osm.ConfigService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.external.MetricReportingService
import de.iteratec.osm.result.*
import de.iteratec.osm.util.PerformanceLoggingService
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

import static de.iteratec.osm.OsmConfiguration.DEFAULT_MAX_VALID_LOADTIME
import static de.iteratec.osm.OsmConfiguration.DEFAULT_MIN_VALID_LOADTIME


/**
 * These Tests check that EventResults written when new result xml arrives get same Internet Connectivity as
 * assigned measurement Job. This is tested for multistep and single step results.
 *
 * Internet Connectivities can be:
 *      * A predefined ConnectivityProfile
 *      * A custom connectivity
 *      * Native Connectivity (no traffic shaping)
 *
 */
@Build([Job, Location, WebPageTestServer, MeasuredEvent, ConnectivityProfile])
class PersistConnectivityInNewEventResultSpec extends Specification implements BuildDataTest,
        ServiceUnitTest<EventResultPersisterService> {

    static WebPageTestServer WPT_SERVER

    public static final String RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY = 'MULTISTEP_FORK_ITERATEC_1Run_3Events_JustFirstView_WithoutVideo.xml'
    public static final String RESULT_XML_SINGLESTEP_1RUN_2EVENTS_FVONLY = 'BEFORE_MULTISTEP_1Run_WithVideo.xml'

    public static Job MULTISTEP_JOB
    public static Job SINGLESTEP_JOB

    Closure doWithSpring() {
        return {
            pageService(PageService)
            performanceLoggingService(PerformanceLoggingService)
            jobDaoService(JobDaoService)
        }
    }

    void setup() {
        createTestDataCommonForAllTests()
        createMocksCommonForAllTests()

        service.configService = Stub(ConfigService) {
            getMaxValidLoadtime() >> DEFAULT_MAX_VALID_LOADTIME
            getMinValidLoadtime() >> DEFAULT_MIN_VALID_LOADTIME
        }
    }

    void setupSpec() {
        mockDomains(WebPageTestServer, Browser, Location, Job, JobResult, EventResult, BrowserAlias, Page,
                MeasuredEvent, JobGroup, Script, ConnectivityProfile)
    }

    void "Every written EventResult of MULTISTEP_1RUN_3EVENTS test get assigned to Jobs connectivity profile correctly."() {

        setup: "Result xml and matching Job with predefined connectivity profile in place."
        File xmlResultFile = new File("src/test/resources/WptResultXmls/${RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY}")
        WptResultXml xmlResult = new WptResultXml(new XmlSlurper().parse(xmlResultFile))
        setPredefinedConnectivity(MULTISTEP_JOB)
        int numberRuns = 1
        int numberSteps = 3
        JobResult.build(job: MULTISTEP_JOB, testId: xmlResult.testId, jobResultStatus: JobResultStatus.SUCCESS)

        when: "EventResultPersisterService listens to result xml."
        service.listenToResult(xmlResult, WPT_SERVER, MULTISTEP_JOB.id)

        then: "Connectivity profile of Job is assigned to every new EventResult."
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == numberRuns * numberSteps
        allResults.every{it.connectivityProfile == MULTISTEP_JOB.connectivityProfile}
        allResults.every{it.customConnectivityName == null}
        allResults.every {it.noTrafficShapingAtAll == false}

    }

    void "Every written EventResult of MULTISTEP_1RUN_3EVENTS test get custom connectivity of Job."() {

        setup: "Result xml and matching Job with custom connectivity in place."
        File xmlResultFile = new File("src/test/resources/WptResultXmls/${RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        setCustomConnectivity(MULTISTEP_JOB)
        int numberRuns = 1
        int numberSteps = 3
        JobResult.build(job: MULTISTEP_JOB, testId: xmlResult.testId, jobResultStatus: JobResultStatus.SUCCESS)

        when: "EventResultPersisterService listens to result xml."
        service.listenToResult(xmlResult, WPT_SERVER, MULTISTEP_JOB.id)

        then: "Custom connectivity of Job is set for every new EventResult."
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == numberRuns * numberSteps
        allResults.every{it.connectivityProfile == null}
        allResults.every{it.noTrafficShapingAtAll == false}
        allResults.every{it.customConnectivityName == MULTISTEP_JOB.customConnectivityName}

    }

    void "Every written EventResult of MULTISTEP_1RUN_3EVENTS test get native connectivity of Job."() {

        setup: "Result xml and matching Job with native connectivity in place."
        File xmlResultFile = new File("src/test/resources/WptResultXmls/${RESULT_XML_MULTISTEP_1RUN_3EVENTS_FVONLY}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        setNativeConnectivity(MULTISTEP_JOB)
        int numberRuns = 1
        int numberSteps = 3
        JobResult.build(job: MULTISTEP_JOB, testId: xmlResult.testId, jobResultStatus: JobResultStatus.SUCCESS)

        when: "EventResultPersisterService listens to result xml."
        service.listenToResult(xmlResult, WPT_SERVER, MULTISTEP_JOB.id)

        then: "Native connectivity of Job is set for every new EventResult."
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == numberRuns * numberSteps
        allResults.every {it.connectivityProfile == null}
        allResults.every {it.customConnectivityName == null}
        allResults.every {it.noTrafficShapingAtAll == true}

    }

    void "Every written EventResult of SINGLESTEP_1RUN_2EVENTS test get assigned to Jobs connectivity profile correctly."() {

        setup: "Result xml and matching Job with predefined connectivity profile in place."
        File xmlResultFile = new File("src/test/resources/WptResultXmls/${RESULT_XML_SINGLESTEP_1RUN_2EVENTS_FVONLY}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        setPredefinedConnectivity(SINGLESTEP_JOB)
        int numberRuns = 1
        int numberSteps = 2
        JobResult.build(job: SINGLESTEP_JOB, testId: xmlResult.testId, jobResultStatus: JobResultStatus.SUCCESS)

        when: "EventResultPersisterService listens to result xml."
        service.listenToResult(xmlResult, WPT_SERVER, SINGLESTEP_JOB.id)

        then: "Connectivity profile of Job is assigned to every new EventResult."
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == numberRuns * numberSteps
        allResults.every{it.connectivityProfile == SINGLESTEP_JOB.connectivityProfile}
        allResults.every{it.customConnectivityName == null}
        allResults.every {it.noTrafficShapingAtAll == false}

    }

    void "Every written EventResult of SINGLESTEP_1RUN_2EVENTS test get custom connectivity of Job."() {

        setup: "Result xml and matching Job with custom connectivity in place."
        File xmlResultFile = new File("src/test/resources/WptResultXmls/${RESULT_XML_SINGLESTEP_1RUN_2EVENTS_FVONLY}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        setCustomConnectivity(SINGLESTEP_JOB)
        int numberRuns = 1
        int numberSteps = 2
        JobResult.build(job: SINGLESTEP_JOB, testId: xmlResult.testId, jobResultStatus: JobResultStatus.SUCCESS)

        when: "EventResultPersisterService listens to result xml."
        service.listenToResult(xmlResult, WPT_SERVER, SINGLESTEP_JOB.id)

        then: "Custom connectivity of Job is set for every new EventResult."
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == numberRuns * numberSteps
        allResults.every{it.connectivityProfile == null}
        allResults.every{it.noTrafficShapingAtAll == false}
        allResults.every{it.customConnectivityName == SINGLESTEP_JOB.customConnectivityName}

    }

    void "Every written EventResult of SINGLESTEP_1RUN_2EVENTS test get native connectivity of Job."() {

        setup: "Result xml and matching Job with native connectivity in place."
        File xmlResultFile = new File("src/test/resources/WptResultXmls/${RESULT_XML_SINGLESTEP_1RUN_2EVENTS_FVONLY}")
        WptResultXml xmlResult = new WptResultXml (new XmlSlurper().parse(xmlResultFile))
        setNativeConnectivity(SINGLESTEP_JOB)
        int numberRuns = 1
        int numberSteps = 2
        JobResult.build(job: SINGLESTEP_JOB, testId: xmlResult.testId, jobResultStatus: JobResultStatus.SUCCESS)

        when: "EventResultPersisterService listens to result xml."
        service.listenToResult(xmlResult, WPT_SERVER, SINGLESTEP_JOB.id)

        then: "Native connectivity of Job is set for every new EventResult."
        List<EventResult> allResults = EventResult.getAll()
        allResults.size() == numberRuns * numberSteps
        allResults.every {it.connectivityProfile == null}
        allResults.every {it.customConnectivityName == null}
        allResults.every {it.noTrafficShapingAtAll == true}

    }

    void createTestDataCommonForAllTests() {

        WPT_SERVER = WebPageTestServer.build()

        MULTISTEP_JOB = Job.build(
            label: 'FF_Otto_multistep',
            location: Location.build(wptServer: WPT_SERVER, uniqueIdentifierForServer: 'iteratec-dev-hetzner-win7:Firefox')
        )
        SINGLESTEP_JOB = Job.build(
            label: 'IE_otto_hp_singlestep',
            location: Location.build(wptServer: WPT_SERVER, uniqueIdentifierForServer: 'NewYork:IE 11')
        )

    }

    void createMocksCommonForAllTests() {
        service.csiValueService = Stub(CsiValueService){
            isCsiRelevant(_) >> false
        }
        service.metricReportingService = Mock(MetricReportingService)
    }

    private void setPredefinedConnectivity(Job job) {
        job.connectivityProfile = ConnectivityProfile.build()
    }

    private void setCustomConnectivity(Job job) {
        job.connectivityProfile = null
        job.noTrafficShapingAtAll = false
        job.customConnectivityProfile = true
        job.customConnectivityName = 'Custom (6.000/512 Kbps, 40ms, 0% PLR)'
        job.bandwidthDown = 6000
        job.bandwidthUp = 512
        job.latency = 40
        job.packetLoss = 0
    }
    private void setNativeConnectivity(Job job){
        job.connectivityProfile = null
        job.noTrafficShapingAtAll = true
        job.customConnectivityProfile = false
        job.customConnectivityName = null
        job.bandwidthDown = null
        job.bandwidthUp = null
        job.latency = null
        job.packetLoss = null
    }

}
