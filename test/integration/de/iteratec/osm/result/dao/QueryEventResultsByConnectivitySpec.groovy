package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.ConnectivityProfileService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup

import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.dao.CriteriaSorting
import de.iteratec.osm.result.ErQueryParams
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.CsiAggregationTagService
import de.iteratec.osm.result.MvQueryParams
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification

import java.util.regex.Pattern

/**
 *
 */
class QueryEventResultsByConnectivitySpec extends Specification {

    EventResultDaoService eventResultDaoService

    public static final Pattern REGEX_NOT_MATCHING_ALL_CUSTOM_CONNS = ~/Custom \(6000.*/
    public static final Pattern REGEX_MATCHING_ALL_CUSTOM_CONNS = ~/Custom \(.*/
    public static final String CUSTOM_CONN_NAME_SHOULD_MATCH = 'Custom (6000/512 Kbps, 100ms, 0% PLR)'
    public static final String CUSTOM_CONN_NAME_SHOULD_NOT_MATCH = 'Custom (50000/6000 Kbps, 100ms, 0% PLR)'

    DateTime runDate
    private Job job
    private MeasuredEvent measuredEvent

    private EventResult withPredefinedProfile1
    private EventResult withPredefinedProfile2
    private EventResult withNativeConnectivity
    private EventResult withCustomConnectivityMatchingRegex
    private EventResult withCustomConnectivityNotMatchingRegex
    private ConnectivityProfile predefinedProfile1
    private ConnectivityProfile predefinedProfile2

    def setup() {
        createTestDataCommonToAllTests();
    }

    def cleanup() {
    }

    // selection by one type of selector: predefined profile(s), custom conn or native conn ///////////////////////////////////////////

    void "select by single predefined profile"() {
        when:
        MvQueryParams queryParams=new ErQueryParams();
        queryParams.browserIds.add(job.location.browser.id);
        queryParams.jobGroupIds.add(job.jobGroup.id);
        queryParams.measuredEventIds.add(measuredEvent.id);
        queryParams.pageIds.add(measuredEvent.testedPage.id);
        queryParams.locationIds.add(job.location.id);
        queryParams.connectivityProfileIds.add(predefinedProfile1.ident())

        Collection<EventResult> results = eventResultDaoService.getLimitedMedianEventResultsBy(
            runDate.toDate(),
            runDate.plusHours(1).toDate(),
            [
                CachedView.CACHED,
                CachedView.UNCACHED
            ] as Set,
            queryParams,
            [:],
            [:],
            [:],
            new CriteriaSorting(sortingActive: false)
        )

        then:
        results.size() == 1
        results[0].connectivityProfile.ident() == predefinedProfile1.ident()
        results[0].customConnectivityName == null
    }

    void "select by a list of predefined profiles"() {
        when:
        MvQueryParams queryParams=new ErQueryParams();
        queryParams.browserIds.add(job.location.browser.id);
        queryParams.jobGroupIds.add(job.jobGroup.id);
        queryParams.measuredEventIds.add(measuredEvent.id);
        queryParams.pageIds.add(measuredEvent.testedPage.id);
        queryParams.locationIds.add(job.location.id);
        queryParams.connectivityProfileIds.addAll([predefinedProfile1.ident(), predefinedProfile2.ident()])

        Collection<EventResult> results = eventResultDaoService.getLimitedMedianEventResultsBy(
                runDate.toDate(),
                runDate.plusHours(1).toDate(),
                [
                        CachedView.CACHED,
                        CachedView.UNCACHED
                ] as Set,
                queryParams,
                [:],
                [:],
                [:],
                new CriteriaSorting(sortingActive: false)
        )

        then:
        results.size() == 2
        List idsOfConnProfilesAssociatedToResults = results*.connectivityProfile*.ident()
        idsOfConnProfilesAssociatedToResults.contains(predefinedProfile1.ident())
        idsOfConnProfilesAssociatedToResults.contains(predefinedProfile2.ident())
        results[0].customConnectivityName == null
        results[1].customConnectivityName == null
    }

    void "select by custom conn name regex: not matching all"() {
        when:
        MvQueryParams queryParams=new ErQueryParams();
        queryParams.browserIds.add(job.location.browser.id);
        queryParams.jobGroupIds.add(job.jobGroup.id);
        queryParams.measuredEventIds.add(measuredEvent.id);
        queryParams.pageIds.add(measuredEvent.testedPage.id);
        queryParams.locationIds.add(job.location.id);
        queryParams.customConnectivityNameRegex = REGEX_NOT_MATCHING_ALL_CUSTOM_CONNS

        Collection<EventResult> results = eventResultDaoService.getLimitedMedianEventResultsBy(
                runDate.toDate(),
                runDate.plusHours(1).toDate(),
                [
                        CachedView.CACHED,
                        CachedView.UNCACHED
                ] as Set,
                queryParams,
                [:],
                [:],
                [:],
                new CriteriaSorting(sortingActive: false)
        )

        then:
        results.size() == 1
        results[0].connectivityProfile == null
        results[0].customConnectivityName == CUSTOM_CONN_NAME_SHOULD_MATCH
    }

    void "select by custom conn name regex: matching all"() {
        when:
        MvQueryParams queryParams=new ErQueryParams();
        queryParams.browserIds.add(job.location.browser.id);
        queryParams.jobGroupIds.add(job.jobGroup.id);
        queryParams.measuredEventIds.add(measuredEvent.id);
        queryParams.pageIds.add(measuredEvent.testedPage.id);
        queryParams.locationIds.add(job.location.id);
        queryParams.customConnectivityNameRegex = REGEX_MATCHING_ALL_CUSTOM_CONNS

        Collection<EventResult> results = eventResultDaoService.getLimitedMedianEventResultsBy(
                runDate.toDate(),
                runDate.plusHours(1).toDate(),
                [
                        CachedView.CACHED,
                        CachedView.UNCACHED
                ] as Set,
                queryParams,
                [:],
                [:],
                [:],
                new CriteriaSorting(sortingActive: false)
        )

        then:
        results.size() == 2
        List associatedPredefinedProfiles = results*.connectivityProfile
        associatedPredefinedProfiles[0] == null
        associatedPredefinedProfiles[1] == null
        results*.customConnectivityName.contains(withCustomConnectivityMatchingRegex.customConnectivityName)
        results*.customConnectivityName.contains(withCustomConnectivityNotMatchingRegex.customConnectivityName)
    }

    void "select only native conn"() {
        when:
        MvQueryParams queryParams=new ErQueryParams();
        queryParams.browserIds.add(job.location.browser.id);
        queryParams.jobGroupIds.add(job.jobGroup.id);
        queryParams.measuredEventIds.add(measuredEvent.id);
        queryParams.pageIds.add(measuredEvent.testedPage.id);
        queryParams.locationIds.add(job.location.id);
        queryParams.includeNativeConnectivity = true

        Collection<EventResult> results = eventResultDaoService.getLimitedMedianEventResultsBy(
                runDate.toDate(),
                runDate.plusHours(1).toDate(),
                [
                        CachedView.CACHED,
                        CachedView.UNCACHED
                ] as Set,
                queryParams,
                [:],
                [:],
                [:],
                new CriteriaSorting(sortingActive: false)
        )

        then:
        results.size() == 1
        results[0].connectivityProfile == null
        results[0].customConnectivityName == ConnectivityProfileService.CUSTOM_CONNECTIVITY_NAME_FOR_NATIVE
    }

    // selection by combinations of multiple types of selectors: predefined profile(s)/custom conn/native conn ///////////////////////////////////////////

    void "select by custom conn name regex AND native conn"() {
        when:
        MvQueryParams queryParams=new ErQueryParams();
        queryParams.browserIds.add(job.location.browser.id);
        queryParams.jobGroupIds.add(job.jobGroup.id);
        queryParams.measuredEventIds.add(measuredEvent.id);
        queryParams.pageIds.add(measuredEvent.testedPage.id);
        queryParams.locationIds.add(job.location.id);
        queryParams.customConnectivityNameRegex = REGEX_NOT_MATCHING_ALL_CUSTOM_CONNS
        queryParams.includeNativeConnectivity = true

        Collection<EventResult> results = eventResultDaoService.getLimitedMedianEventResultsBy(
                runDate.toDate(),
                runDate.plusHours(1).toDate(),
                [
                        CachedView.CACHED,
                        CachedView.UNCACHED
                ] as Set,
                queryParams,
                [:],
                [:],
                [:],
                new CriteriaSorting(sortingActive: false)
        )

        then:
        results.size() == 2
        results.findAll {it.connectivityProfile}.size() == 0
        results.findAll {it.customConnectivityName.equals(ConnectivityProfileService.CUSTOM_CONNECTIVITY_NAME_FOR_NATIVE)}.size() == 1
        results.findAll {it.customConnectivityName.equals(CUSTOM_CONN_NAME_SHOULD_MATCH)}.size() == 1
    }

    void "select by custom conn name regex AND predefined conn"() {
        when:
        MvQueryParams queryParams=new ErQueryParams();
        queryParams.browserIds.add(job.location.browser.id);
        queryParams.jobGroupIds.add(job.jobGroup.id);
        queryParams.measuredEventIds.add(measuredEvent.id);
        queryParams.pageIds.add(measuredEvent.testedPage.id);
        queryParams.locationIds.add(job.location.id);
        queryParams.customConnectivityNameRegex = REGEX_MATCHING_ALL_CUSTOM_CONNS
        queryParams.connectivityProfileIds.addAll([predefinedProfile1.ident()])

        Collection<EventResult> results = eventResultDaoService.getLimitedMedianEventResultsBy(
                runDate.toDate(),
                runDate.plusHours(1).toDate(),
                [
                        CachedView.CACHED,
                        CachedView.UNCACHED
                ] as Set,
                queryParams,
                [:],
                [:],
                [:],
                new CriteriaSorting(sortingActive: false)
        )

        then:
        results.size() == 3
        results.findAll {it.connectivityProfile}.size() == 1
        results.findAll {it.customConnectivityName.equals(ConnectivityProfileService.CUSTOM_CONNECTIVITY_NAME_FOR_NATIVE)}.size() == 0
        results.findAll {it.customConnectivityName.equals(CUSTOM_CONN_NAME_SHOULD_MATCH)}.size() == 1
        results.findAll {it.customConnectivityName.equals(CUSTOM_CONN_NAME_SHOULD_NOT_MATCH)}.size() == 1
    }

    void "select by native conn AND predefined conn"() {
        when:
        MvQueryParams queryParams=new ErQueryParams();
        queryParams.browserIds.add(job.location.browser.id);
        queryParams.jobGroupIds.add(job.jobGroup.id);
        queryParams.measuredEventIds.add(measuredEvent.id);
        queryParams.pageIds.add(measuredEvent.testedPage.id);
        queryParams.locationIds.add(job.location.id);
        queryParams.connectivityProfileIds.addAll([predefinedProfile2.ident()])
        queryParams.includeNativeConnectivity = true

        Collection<EventResult> results = eventResultDaoService.getLimitedMedianEventResultsBy(
                runDate.toDate(),
                runDate.plusHours(1).toDate(),
                [
                        CachedView.CACHED,
                        CachedView.UNCACHED
                ] as Set,
                queryParams,
                [:],
                [:],
                [:],
                new CriteriaSorting(sortingActive: false)
        )

        then:
        results.size() == 2
        results.findAll {it.connectivityProfile}.size() == 1
        results.findAll {it.customConnectivityName.equals(ConnectivityProfileService.CUSTOM_CONNECTIVITY_NAME_FOR_NATIVE)}.size() == 1
    }

    void "select by custom conn name regex AND native conn AND predefined conn"() {
        when:
        MvQueryParams queryParams=new ErQueryParams();
        queryParams.browserIds.add(job.location.browser.id);
        queryParams.jobGroupIds.add(job.jobGroup.id);
        queryParams.measuredEventIds.add(measuredEvent.id);
        queryParams.pageIds.add(measuredEvent.testedPage.id);
        queryParams.locationIds.add(job.location.id);
        queryParams.connectivityProfileIds.addAll([predefinedProfile2.ident()])
        queryParams.includeNativeConnectivity = true
        queryParams.customConnectivityNameRegex = REGEX_NOT_MATCHING_ALL_CUSTOM_CONNS

        Collection<EventResult> results = eventResultDaoService.getLimitedMedianEventResultsBy(
                runDate.toDate(),
                runDate.plusHours(1).toDate(),
                [
                        CachedView.CACHED,
                        CachedView.UNCACHED
                ] as Set,
                queryParams,
                [:],
                [:],
                [:],
                new CriteriaSorting(sortingActive: false)
        )

        then:
        results.size() == 3
        ArrayList<EventResult> resultsWithPredefinedProfiles = results.findAll { it.connectivityProfile }
        resultsWithPredefinedProfiles.size() == 1
        resultsWithPredefinedProfiles[0].connectivityProfile.ident() == predefinedProfile2.ident()
        results.findAll {it.customConnectivityName.equals(ConnectivityProfileService.CUSTOM_CONNECTIVITY_NAME_FOR_NATIVE)}.size() == 1
        results.findAll {it.customConnectivityName.equals(CUSTOM_CONN_NAME_SHOULD_MATCH)}.size() == 1
    }



    private void createTestDataCommonToAllTests() {

        predefinedProfile1 = TestDataUtil.createConnectivityProfile('connProfile 1: name')
        predefinedProfile2 = TestDataUtil.createConnectivityProfile('connProfile 2: name')

        WebPageTestServer server =
            TestDataUtil.createWebPageTestServer('server 1 - wpt server', 'server 1 - wpt server', true, 'http://server1.wpt.server.de')

        JobGroup jobGroup = TestDataUtil.createJobGroup("TestGroup")

        Browser fireFoxBrowser = TestDataUtil.createBrowser('FF', 0.55d)

        Location ffAgent1 = TestDataUtil.createLocation(server, 'physNetLabAgent01-FF', fireFoxBrowser, true)

        Page homepage = TestDataUtil.createPage('homepage', 0.5d)

        Script script = Script.createDefaultScript('Unnamed').save(failOnError: true)

        job = TestDataUtil.createJob('job with predefined connectivity', script, ffAgent1, jobGroup, 'irrelevantDescription', 1, false, 60)

        measuredEvent = TestDataUtil.createMeasuredEvent('Test event', homepage)

        String eventResultTag = "$jobGroup.id;$measuredEvent.id;$homepage.id;$fireFoxBrowser.id;$ffAgent1.id";

        /* Create TestData */
        runDate = new DateTime(2013, 5, 29, 0, 0, 0, DateTimeZone.UTC)

        JobResult jobRun = TestDataUtil.createJobResult('1', runDate.toDate(), job, job.location)

        CsiAggregationTagService tagService = new CsiAggregationTagService()
        withPredefinedProfile1 = TestDataUtil.createEventResult(job, jobRun, 123I, 456.5D, measuredEvent, tagService)
        withPredefinedProfile1.connectivityProfile = predefinedProfile1
        withPredefinedProfile1.customConnectivityName = null
        withPredefinedProfile1.save(failOnError: true)

        withPredefinedProfile2 = TestDataUtil.createEventResult(job, jobRun, 123I, 456.5D, measuredEvent, tagService)
        withPredefinedProfile2.connectivityProfile = predefinedProfile2
        withPredefinedProfile1.customConnectivityName = null
        withPredefinedProfile2.save(failOnError: true)

        withNativeConnectivity = TestDataUtil.createEventResult(job, jobRun, 123I, 456.5D, measuredEvent, tagService)
        withNativeConnectivity.connectivityProfile = null
        withNativeConnectivity.customConnectivityName = ConnectivityProfileService.CUSTOM_CONNECTIVITY_NAME_FOR_NATIVE
        withNativeConnectivity.save(failOnError: true)

        withCustomConnectivityMatchingRegex = TestDataUtil.createEventResult(job, jobRun, 123I, 456.5D, measuredEvent, tagService)
        withCustomConnectivityMatchingRegex.connectivityProfile = null
        withCustomConnectivityMatchingRegex.customConnectivityName = CUSTOM_CONN_NAME_SHOULD_MATCH
        withCustomConnectivityMatchingRegex.save(failOnError: true)

        withCustomConnectivityNotMatchingRegex = TestDataUtil.createEventResult(job, jobRun, 123I, 456.5D, measuredEvent, tagService)
        withCustomConnectivityNotMatchingRegex.connectivityProfile = null
        withCustomConnectivityNotMatchingRegex.customConnectivityName = CUSTOM_CONN_NAME_SHOULD_NOT_MATCH
        withCustomConnectivityNotMatchingRegex.save(failOnError: true)

    }

}
