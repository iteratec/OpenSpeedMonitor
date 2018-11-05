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

package de.iteratec.osm.result.utils

import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.*
import de.iteratec.osm.result.dao.EventResultDaoService
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.apache.commons.lang.time.DateUtils

@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class EventResultDaoServiceIntegrationSpec extends NonTransactionalIntegrationSpec {

    EventResultDaoService eventResultDaoService

    Date runDatePlus_Zero, runDatePlus_Ten, runDatePlus_Twenty, runDatePlus_Thirty, runDatePlus_Day

    EventResult resultRunDatePlus_Zero, resultRunDatePlus_Ten, resultRunDatePlus_Twenty, resultRunDatePlus_Thirty, resultRunDatePlus_Day

    private Job job

    private MvQueryParams qp

    private MeasuredEvent measuredEvent

    def setup() {
        initTestData()
    }

    void "Test getByStartAndEndTimeAndMvQueryParams with JUST_ONE_DATE"() {
        when: "a query for one date is executed"
        Collection<EventResult> results = eventResultDaoService.getByStartAndEndTimeAndMvQueryParams(runDatePlus_Zero, runDatePlus_Zero, [
                CachedView.CACHED,
                CachedView.UNCACHED
        ], qp)

        then: "one result is found, which matches the date"
        results.size() == 1
        results.getAt(0).id == resultRunDatePlus_Zero.id
    }

    void "Test getByStartAndEndTimeAndMvQueryParams with ZERO_TO_TEN"() {
        when: "a query for a start and an end time is executed"
        Collection<EventResult> results = eventResultDaoService.getByStartAndEndTimeAndMvQueryParams(runDatePlus_Zero, runDatePlus_Ten, [
                CachedView.CACHED,
                CachedView.UNCACHED
        ], qp)

        then: "two results are found, which match the date range"
        results.size() == 2
        results.find { it.jobResultDate == runDatePlus_Zero }.id == resultRunDatePlus_Zero.id
        results.find { it.jobResultDate == runDatePlus_Ten }.id == resultRunDatePlus_Ten.id
    }

    void "Test getByStartAndEndTimeAndMvQueryParams with ZERO_TO_TEN_AND_BROWSER"() {
        given: "a certain browser to query for"
        qp.browserIds.add(job.location.browser.id)
        when: "a query for a start and an end time with a certain browser is executed"
        Collection<EventResult> results = eventResultDaoService.getByStartAndEndTimeAndMvQueryParams(runDatePlus_Zero, runDatePlus_Ten, [
                CachedView.CACHED,
                CachedView.UNCACHED
        ], qp)

        then: "one result is found, which matches date and browser"
        results.size() == 1
        results.find { it.jobResultDate == runDatePlus_Zero }.id == resultRunDatePlus_Zero.id
    }

    void "Test getByStartAndEndTimeAndMvQueryParams with ZERO_TO_TEN_AND_EMPTY_MV_PARAMS"() {
        when: "a query with no parameters is executed"
        Collection<EventResult> results = eventResultDaoService.getByStartAndEndTimeAndMvQueryParams(runDatePlus_Zero, runDatePlus_Ten, [
                CachedView.CACHED,
                CachedView.UNCACHED
        ], new MvQueryParams())

        then: "all (two) results are found"
        results.size() == 2
    }

    private void initTestData() {
        ConnectivityProfile profile = ConnectivityProfile.build(name: 'irrelevant in these tests')

        WebPageTestServer server = WebPageTestServer.build(
                baseUrl: 'http://server1.wpt.server.de',
                active: true,
                label: 'server 1 - wpt server',
                proxyIdentifier: 'server 1 - wpt server'
        ).save(failOnError: true)

        JobGroup jobGroup = JobGroup.build(
                name: "TestGroup").save(failOnError: true)

        Browser fireFoxBrowser = Browser.build(name: 'FF').save(failOnError: true)
        Browser ieBrowser = Browser.build(name: 'IE').save(failOnError: true)

        Location ffAgent1 = Location.build(
                active: true,
                location: 'physNetLabAgent01-FF',
                label: 'physNetLabAgent01 - FF up to date',
                browser: fireFoxBrowser,
                wptServer: server
        ).save(failOnError: true)

        Page homepage = Page.build(name: 'homepage').save(failOnError: true)

        Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)

        job = Job.build(
                id: 1,
                active: false,
                label: 'BV1 - Step 01',
                description: 'This is job 01...',
                location: ffAgent1,
                frequencyInMin: 5,
                runs: 1,
                jobGroup: jobGroup,
                script: script,
                maxDownloadTimeInMinutes: 60,
                connectivityProfile: profile
        ).save(failOnError: true)

        measuredEvent = MeasuredEvent.build(
                name: 'Test event',
                testedPage: homepage
        ).save(failOnError: true)

        /* Create TestData */
        /*  2013-05-29T10:13:02.564+02:00   1369815182564 */
        runDatePlus_Zero = DateUtils.setMinutes(DateUtils.setSeconds(new Date(1369815182564), 1), 1);
        runDatePlus_Ten = DateUtils.addMinutes(runDatePlus_Zero, +10)
        runDatePlus_Twenty = DateUtils.addMinutes(runDatePlus_Zero, +20)
        runDatePlus_Thirty = DateUtils.addMinutes(runDatePlus_Zero, +20)
        runDatePlus_Day = DateUtils.addDays(runDatePlus_Zero, +1)

        /* Runs in Hour */
        JobResult jobRunDatePlus_Zero = JobResult.build(
                job: job,
                date: runDatePlus_Zero,
                testId: '1',
                description: '',
                jobConfigLabel: job.label,
                jobConfigRuns: job.runs,
                jobGroupName: job.jobGroup.name,
                frequencyInMin: 5,
                locationLocation: job.location.location,
                locationBrowser: job.location.browser.name,
                wptStatus: WptStatus.COMPLETED,
        ).save(failOnError: true)

        resultRunDatePlus_Zero = EventResult.build(
                numberOfWptRun: 1,
                cachedView: CachedView.UNCACHED,
                medianValue: true,
                docCompleteIncomingBytes: 1,
                docCompleteRequests: 2,
                docCompleteTimeInMillisecs: 3,
                domTimeInMillisecs: 4,
                firstByteInMillisecs: 5,
                fullyLoadedIncomingBytes: 6,
                fullyLoadedRequestCount: 7,
                fullyLoadedTimeInMillisecs: 8,
                loadTimeInMillisecs: 9,
                startRenderInMillisecs: 10,
                downloadAttempts: 1,
                firstStatusUpdate: jobRunDatePlus_Zero.date,
                lastStatusUpdate: jobRunDatePlus_Zero.date,
                wptStatus: WptStatus.SUCCESSFUL.getWptStatusCode(),
                validationState: 'validationState',
                csByWptDocCompleteInPercent: 1,
                jobResult: jobRunDatePlus_Zero,
                jobResultDate: jobRunDatePlus_Zero.date,
                jobResultJobConfigId: jobRunDatePlus_Zero.job.ident(),
                jobGroup: jobGroup,
                measuredEvent: measuredEvent,
                page: homepage,
                browser: fireFoxBrowser,
                location: ffAgent1,
                speedIndex: null,
                connectivityProfile: profile
        ).save(failOnError: true)

        /* + 10 Minutes */
        JobResult jobRunDatePlus_Ten = JobResult.build(
                job: job,
                date: runDatePlus_Ten,
                testId: '1',
                description: '',
                jobConfigLabel: job.label,
                jobConfigRuns: job.runs,
                jobGroupName: job.jobGroup.name,
                frequencyInMin: 5,
                locationLocation: job.location.location,
                locationBrowser: job.location.browser.name,
                wptStatus: WptStatus.COMPLETED,
        ).save(failOnError: true)

        resultRunDatePlus_Ten = EventResult.build(
                numberOfWptRun: 1,
                cachedView: CachedView.UNCACHED,
                medianValue: true,
                docCompleteIncomingBytes: 1,
                docCompleteRequests: 2,
                docCompleteTimeInMillisecs: 3,
                domTimeInMillisecs: 4,
                firstByteInMillisecs: 5,
                fullyLoadedIncomingBytes: 6,
                fullyLoadedRequestCount: 7,
                fullyLoadedTimeInMillisecs: 8,
                loadTimeInMillisecs: 9,
                startRenderInMillisecs: 10,
                downloadAttempts: 1,
                firstStatusUpdate: jobRunDatePlus_Ten.date,
                lastStatusUpdate: jobRunDatePlus_Ten.date,
                wptStatus: WptStatus.SUCCESSFUL.getWptStatusCode(),
                validationState: 'validationState',
                csByWptDocCompleteInPercent: 1,
                jobResult: jobRunDatePlus_Ten,
                jobResultDate: jobRunDatePlus_Ten.date,
                jobResultJobConfigId: jobRunDatePlus_Ten.job.ident(),
                jobGroup: jobGroup,
                measuredEvent: measuredEvent,
                page: homepage,
                browser: ieBrowser,
                location: ffAgent1,
                speedIndex: null,
                connectivityProfile: profile
        ).save(failOnError: true)

        /* + 20 Minutes */
        JobResult jobRunDatePlus_Twenty = JobResult.build(
                job: job,
                date: runDatePlus_Twenty,
                testId: '1',
                description: '',
                jobConfigLabel: job.label,
                jobConfigRuns: job.runs,
                jobGroupName: job.jobGroup.name,
                frequencyInMin: 5,
                locationLocation: job.location.location,
                locationBrowser: job.location.browser.name,
                wptStatus: WptStatus.COMPLETED,
        ).save(failOnError: true)

        resultRunDatePlus_Twenty = EventResult.build(
                numberOfWptRun: 1,
                cachedView: CachedView.UNCACHED,
                medianValue: true,
                docCompleteIncomingBytes: 1,
                docCompleteRequests: 2,
                docCompleteTimeInMillisecs: 3,
                domTimeInMillisecs: 4,
                firstByteInMillisecs: 5,
                fullyLoadedIncomingBytes: 6,
                fullyLoadedRequestCount: 7,
                fullyLoadedTimeInMillisecs: 8,
                loadTimeInMillisecs: 9,
                startRenderInMillisecs: 10,
                downloadAttempts: 1,
                firstStatusUpdate: jobRunDatePlus_Twenty.date,
                lastStatusUpdate: jobRunDatePlus_Twenty.date,
                wptStatus: WptStatus.SUCCESSFUL.getWptStatusCode(),
                validationState: 'validationState',
                csByWptDocCompleteInPercent: 1,
                jobResult: jobRunDatePlus_Twenty,
                jobResultDate: jobRunDatePlus_Twenty.date,
                jobResultJobConfigId: jobRunDatePlus_Twenty.job.ident(),
                jobGroup: jobGroup,
                measuredEvent: measuredEvent,
                page: homepage,
                browser: fireFoxBrowser,
                location: ffAgent1,
                speedIndex: null,
                connectivityProfile: profile
        ).save(failOnError: true)

        /* + 30 Minutes */
        JobResult jobRunDatePlus_Thirty = JobResult.build(
                job: job,
                date: runDatePlus_Thirty,
                testId: '1',
                description: '',
                jobConfigLabel: job.label,
                jobConfigRuns: job.runs,
                jobGroupName: job.jobGroup.name,
                frequencyInMin: 5,
                locationLocation: job.location.location,
                locationBrowser: job.location.browser.name,
                wptStatus: WptStatus.COMPLETED,
        ).save(failOnError: true)

        resultRunDatePlus_Thirty = EventResult.build(
                numberOfWptRun: 1,
                cachedView: CachedView.UNCACHED,
                medianValue: true,
                docCompleteIncomingBytes: 1,
                docCompleteRequests: 2,
                docCompleteTimeInMillisecs: 3,
                domTimeInMillisecs: 4,
                firstByteInMillisecs: 5,
                fullyLoadedIncomingBytes: 6,
                fullyLoadedRequestCount: 7,
                fullyLoadedTimeInMillisecs: 8,
                loadTimeInMillisecs: 9,
                startRenderInMillisecs: 10,
                downloadAttempts: 1,
                firstStatusUpdate: jobRunDatePlus_Thirty.date,
                lastStatusUpdate: jobRunDatePlus_Thirty.date,
                wptStatus: WptStatus.SUCCESSFUL.getWptStatusCode(),
                validationState: 'validationState',
                csByWptDocCompleteInPercent: 1,
                jobResult: jobRunDatePlus_Thirty,
                jobResultDate: jobRunDatePlus_Thirty.date,
                jobResultJobConfigId: jobRunDatePlus_Thirty.job.ident(),
                jobGroup: jobGroup,
                measuredEvent: measuredEvent,
                page: homepage,
                browser: fireFoxBrowser,
                location: ffAgent1,
                speedIndex: null,
                connectivityProfile: profile
        ).save(failOnError: true)

        /* + 1 Day */
        JobResult jobRunDatePlus_Day = JobResult.build(
                job: job,
                date: runDatePlus_Day,
                testId: '1',
                description: '',
                jobConfigLabel: job.label,
                jobConfigRuns: job.runs,
                jobGroupName: job.jobGroup.name,
                frequencyInMin: 5,
                locationLocation: job.location.location,
                locationBrowser: job.location.browser.name,
                wptStatus: WptStatus.COMPLETED,
        ).save(failOnError: true)

        resultRunDatePlus_Day = EventResult.build(
                numberOfWptRun: 1,
                cachedView: CachedView.UNCACHED,
                medianValue: true,
                docCompleteIncomingBytes: 1,
                docCompleteRequests: 2,
                docCompleteTimeInMillisecs: 3,
                domTimeInMillisecs: 4,
                firstByteInMillisecs: 5,
                fullyLoadedIncomingBytes: 6,
                fullyLoadedRequestCount: 7,
                fullyLoadedTimeInMillisecs: 8,
                loadTimeInMillisecs: 9,
                startRenderInMillisecs: 10,
                downloadAttempts: 1,
                firstStatusUpdate: jobRunDatePlus_Day.date,
                lastStatusUpdate: jobRunDatePlus_Day.date,
                wptStatus: WptStatus.SUCCESSFUL.getWptStatusCode(),
                validationState: 'validationState',
                csByWptDocCompleteInPercent: 1,
                jobResult: jobRunDatePlus_Day,
                jobResultDate: jobRunDatePlus_Day.date,
                jobResultJobConfigId: jobRunDatePlus_Day.job.ident(),
                jobGroup: jobGroup,
                measuredEvent: measuredEvent,
                page: homepage,
                browser: fireFoxBrowser,
                location: ffAgent1,
                speedIndex: null,
                connectivityProfile: profile
        ).save(failOnError: true)

        qp = new MvQueryParams()
        qp.jobGroupIds.add(job.jobGroup.id)
        qp.measuredEventIds.add(measuredEvent.id)
        qp.pageIds.add(measuredEvent.testedPage.id)
        qp.locationIds.add(job.location.id)
    }

}
