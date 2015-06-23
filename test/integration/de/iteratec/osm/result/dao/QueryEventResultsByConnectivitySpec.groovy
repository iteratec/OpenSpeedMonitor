package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MvQueryParams
import org.apache.commons.lang.time.DateUtils
import spock.lang.Specification

/**
 *
 */
class QueryEventResultsByConnectivitySpec extends Specification {

    public static final String CONN_PROFILE_NAME = 'connProfile 1: name'
    EventResultDaoService eventResultDaoService

    Date runDatePlus_Zero, runDatePlus_Ten, runDatePlus_Twenty, runDatePlus_Thirty, runDatePlus_Day

    EventResult resultRunDatePlus_Zero, resultRunDatePlus_Ten, resultRunDatePlus_Twenty, resultRunDatePlus_Thirty, resultRunDatePlus_Day

    private Job job

    private MeasuredEvent measuredEvent
    private static final String MEASURAND_AGGREGATOR_TYPE_NAME_1 = 'measurand1'
    private static final String MEASURAND_AGGREGATOR_TYPE_NAME_2 = 'measurand2'
    private static final String MEASURAND_AGGREGATOR_TYPE_NAME_3 = 'measurand3'
    private static final String NON_MEASURAND_AGGREGATOR_TYPE_NAME = 'nonMeasurand'

    def setup() {
        initTestData();
    }

    def cleanup() {
    }

    void "get only the results with one single predefined profile"() {
        when:
        MvQueryParams qp=new MvQueryParams();
        qp.browserIds.add(job.location.browser.id);
        qp.jobGroupIds.add(job.jobGroup.id);
        qp.measuredEventIds.add(measuredEvent.id);
        qp.pageIds.add(measuredEvent.testedPage.id);
        qp.locationIds.add(job.location.id);
        qp.connectivityProfileIds.add(ConnectivityProfile.findByName(CONN_PROFILE_NAME).ident())
        Collection<EventResult> results = eventResultDaoService.getByStartAndEndTimeAndMvQueryParams(
            runDatePlus_Zero,
            runDatePlus_Ten,
            [
                CachedView.CACHED,
                CachedView.UNCACHED
            ] as Set,
            qp
        )

        then:
        results.size() == 2
    }

    private void initTestData() {

        ConnectivityProfile profile = TestDataUtil.createConnectivityProfile('irrelevant in these tests')

        WebPageTestServer server= new WebPageTestServer(
                baseUrl : 'http://server1.wpt.server.de',
                active : true,
                label : 'server 1 - wpt server',
                proxyIdentifier : 'server 1 - wpt server'
        ).save(failOnError: true);

        JobGroup jobGroup = new JobGroup(
                name: "TestGroup",
                groupType: JobGroupType.CSI_AGGREGATION
        ).save(failOnError: true)

        Browser fireFoxBrowser = new Browser(
                name:'FF',
                weight: 0.55).save(failOnError:true)
        Browser ieBrowser = new Browser(
                name:'IE',
                weight: 0.25).save(failOnError:true)
        Browser i8eBrowser = new Browser(
                name:'I8E',
                weight: 0.20).save(failOnError:true)

        Location ffAgent1 = new Location(
                active: true,
                valid: 1,
                location: 'physNetLabAgent01-FF',
                label: 'physNetLabAgent01 - FF up to date',
                browser: fireFoxBrowser,
                wptServer: server
        ).save(failOnError: true)

        Page homepage = new Page(
                name: 'homepage',
                weight: 0.5
        ).save(failOnError: true)

        Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)

        job = new Job(
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

        Job job2 = new Job(
                active: false,
                label: 'BV1 - Step 02',
                description: 'This is job 02...',
                location: ffAgent1,
                frequencyInMin: 5,
                runs: 1,
                jobGroup: jobGroup,
                script: script,
                maxDownloadTimeInMinutes: 60,
                connectivityProfile: profile
        ).save(failOnError: true)

        measuredEvent = new MeasuredEvent()
        measuredEvent.setName('Test event')
        measuredEvent.setTestedPage(homepage)
        measuredEvent.save(failOnError:true)

        String eventResultTag = "$jobGroup.id;$measuredEvent.id;$homepage.id;$fireFoxBrowser.id;$ffAgent1.id";

        /* Create TestData */
        /*  2013-05-29T10:13:02.564+02:00   1369815182564 */
        runDatePlus_Zero = DateUtils.setMinutes(DateUtils.setSeconds(new Date(1369815182564), 1), 1);
        runDatePlus_Ten = DateUtils.addMinutes(runDatePlus_Zero, +10)
        Date runDatePlus_TenOneSec = DateUtils.addSeconds(runDatePlus_Zero, +1)
        runDatePlus_Twenty = DateUtils.addMinutes(runDatePlus_Zero, +20)
        runDatePlus_Thirty = DateUtils.addMinutes(runDatePlus_Zero, +20)
        runDatePlus_Day =  DateUtils.addDays(runDatePlus_Zero, +1)

        /* Runs in Hour */
        JobResult jobRunDatePlus_Zero = new JobResult(
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
                httpStatusCode : 200,
        ).save(failOnError: true)

        resultRunDatePlus_Zero = new EventResult(
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
                wptStatus: 0,
                validationState : 'validationState',
                harData: 'harData',
                customerSatisfactionInPercent:  1,
                jobResult: jobRunDatePlus_Zero,
                jobResultDate: jobRunDatePlus_Zero.date,
                jobResultJobConfigId: jobRunDatePlus_Zero.job.ident(),
                measuredEvent: measuredEvent,
                speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
                tag: eventResultTag,
                connectivityProfile: profile
        ).save(failOnError: true)

        jobRunDatePlus_Zero.save(failOnError: true)

        /* + 10 Minutes */
        JobResult jobRunDatePlus_Ten = new JobResult(
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
                httpStatusCode : 200,
        ).save(failOnError: true)

        resultRunDatePlus_Ten = new EventResult(
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
                wptStatus: 0,
                validationState : 'validationState',
                harData: 'harData',
                customerSatisfactionInPercent:  1,
                jobResult: jobRunDatePlus_Ten,
                jobResultDate: jobRunDatePlus_Ten.date,
                jobResultJobConfigId: jobRunDatePlus_Ten.job.ident(),
                measuredEvent: measuredEvent,
                speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
                tag: eventResultTag,
                connectivityProfile: profile
        ).save(failOnError: true)

        jobRunDatePlus_Ten.save(failOnError: true)

        /* + 20 Minutes */
        JobResult jobRunDatePlus_Twenty = new JobResult(
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
                httpStatusCode: 200,
        ).save(failOnError: true)

        resultRunDatePlus_Twenty = new EventResult(
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
                wptStatus: 0,
                validationState : 'validationState',
                harData: 'harData',
                customerSatisfactionInPercent:  1,
                jobResult: jobRunDatePlus_Twenty,
                jobResultDate: jobRunDatePlus_Twenty.date,
                jobResultJobConfigId: jobRunDatePlus_Twenty.job.ident(),
                measuredEvent: measuredEvent,
                speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
                tag: eventResultTag,
                connectivityProfile: profile
        ).save(failOnError: true)

        jobRunDatePlus_Twenty.save(failOnError: true)

        /* + 30 Minutes */
        JobResult jobRunDatePlus_Thirty = new JobResult(
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
                httpStatusCode: 200,
        ).save(failOnError: true)

        resultRunDatePlus_Thirty = new EventResult(
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
                wptStatus: 0,
                validationState : 'validationState',
                harData: 'harData',
                customerSatisfactionInPercent:  1,
                jobResult: jobRunDatePlus_Thirty,
                jobResultDate: jobRunDatePlus_Thirty.date,
                jobResultJobConfigId: jobRunDatePlus_Thirty.job.ident(),
                measuredEvent: measuredEvent,
                speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
                tag: eventResultTag,
                connectivityProfile: profile
        ).save(failOnError: true)

        jobRunDatePlus_Thirty.save(failOnError: true)


        /* + 1 Day */
        JobResult jobRunDatePlus_Day = new JobResult(
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
                httpStatusCode: 200,
        ).save(failOnError: true)

        resultRunDatePlus_Day = new EventResult(
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
                wptStatus: 0,
                validationState : 'validationState',
                harData: 'harData',
                customerSatisfactionInPercent:  1,
                jobResult: jobRunDatePlus_Day,
                jobResultDate: jobRunDatePlus_Day.date,
                jobResultJobConfigId: jobRunDatePlus_Day.job.ident(),
                measuredEvent: measuredEvent,
                speedIndex: EventResult.SPEED_INDEX_DEFAULT_VALUE,
                tag: eventResultTag,
                connectivityProfile: profile
        ).save(failOnError: true)

        jobRunDatePlus_Day.save(failOnError: true)

        new AggregatorType([name: MEASURAND_AGGREGATOR_TYPE_NAME_1, measurandGroup: MeasurandGroup.LOAD_TIMES]).save(failOnError:true)
        new AggregatorType([name: MEASURAND_AGGREGATOR_TYPE_NAME_2, measurandGroup: MeasurandGroup.LOAD_TIMES]).save(failOnError:true)
        new AggregatorType([name: MEASURAND_AGGREGATOR_TYPE_NAME_3, measurandGroup: MeasurandGroup.REQUEST_COUNTS]).save(failOnError:true)
        new AggregatorType([name: NON_MEASURAND_AGGREGATOR_TYPE_NAME, measurandGroup: MeasurandGroup.NO_MEASURAND]).save(failOnError:true)

        TestDataUtil.createConnectivityProfile(CONN_PROFILE_NAME)
    }
}
