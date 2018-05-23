package geb.de.iteratec.osm.result

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.ConnectivityProfileService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.*
import de.iteratec.osm.security.Role
import de.iteratec.osm.security.User
import de.iteratec.osm.security.UserRole
import de.iteratec.osm.util.OsmTestLogin
import geb.CustomUrlGebReportingSpec
import geb.pages.de.iteratec.osm.result.EventResultDashboardPage
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.openqa.selenium.Keys
import spock.lang.Shared
import spock.lang.Stepwise
/**
 * Created by mwg on 07.08.2017.
 */
@Integration
@Rollback
@Stepwise
class EventResultDashboardUserTimingGebSpec extends CustomUrlGebReportingSpec implements OsmTestLogin {
    @Shared
    String script1Name = "TestScript1-564892#Afef1"
    @Shared
    String script2Name = "TestScript2-564892#Afef1"
    @Shared
    String job1Name = "TestJob1-564892#Afef1"
    @Shared
    String job2Name = "TestJob2-564892#Afef1"
    @Shared
    String location1Name = "TestLocation1-564892#Afef1"
    @Shared
    String Location2Name = "TestLocation2-564892#Afef1"
    @Shared
    String jobGroup1Name = "TestJobGroup1-564892#Afef1"
    @Shared
    String jobGroup2Name = "TestJobGroup2-564892#Afef1"
    @Shared
    String page1Name = "TestPage1-564892#Afef1"
    @Shared
    String page2Name = "TestPage2-564892#Afef1"
    @Shared
    String connectivityProfileName = "ConnectivityProfile-564892#Afef1"
    @Shared
    String measureEvent1Name = "MeasureEvent1-564892#Afef1"
    @Shared
    String measureEvent2Name = "MeasureEvent2-564892#Afef1"
    @Shared
    String measureEvent3Name = "MeasureEvent3-564892#Afef1"
    @Shared
    String userTimingMarkName = "Mark1-564892#Afef1"
    @Shared
    String userTimingMeasureName = "Measure1-564892#Afef1"
    @Shared
    int userTimingsSize = 2


    void cleanupSpec() {
        cleanUpData()
    }

    void "super old date no timings"() {
        given: "navigate to EventResultDashboard"
        createData()
        to EventResultDashboardPage

        when: "old date is set and measurands button is clicked"
        selectDateInDatepicker(fromDatepicker, "21.06.2015 00:00")
        selectDateInDatepicker(toDatepicker, "23.06.2015 23:59")
        clickVariableSelectionTab()

        then: "only measurands are shown"
        getFirstViewOptionsSizeFor("USER_TIMINGS") == 0
        isUserTimingsHidden() == true
    }

    void "matching date matches timings"() {
        given: "navigate to job selection"
        clickJobSelectionTab()

        when: "matching date is selected"
        selectDateInDatepicker(fromDatepicker, "21.06.2016 00:00")
        selectDateInDatepicker(toDatepicker, "23.06.2016 23:59")
        clickVariableSelectionTab()

        then: "timings are shown"
        getFirstViewOptionsSizeFor("USER_TIMINGS") == userTimingsSize
        firstViewHasOptionFor(SelectedMeasurandType.USERTIMING_MARK, userTimingMarkName) == true
        firstViewHasOptionFor(SelectedMeasurandType.USERTIMING_MEASURE, userTimingMeasureName) == true
    }

    void "timings disappear if date does not match"() {
        given: "navigate to job selection"
        clickJobSelectionTab()

        when: "not matching date is selected"
        selectDateInDatepicker(fromDatepicker, "15.06.2016 00:00")
        selectDateInDatepicker(toDatepicker, "16.06.2016 23:59")
        clickVariableSelectionTab()

        then: "only measurands are shown"
        getFirstViewOptionsSizeFor("USER_TIMINGS") == 0
        isUserTimingsHidden() == true
    }

    void "timings appear for correct timeframe within a day"() {
        given: "navigate to job selection"
        clickJobSelectionTab()

        when: "matching timeframe is selected"
        selectDateInDatepicker(fromDatepicker, "22.06.2016 03:15")
        selectDateInDatepicker(toDatepicker, "23.06.2016 04:00")
        clickVariableSelectionTab()

        then: "timings are shown"
        getFirstViewOptionsSizeFor("USER_TIMINGS") == userTimingsSize
        firstViewHasOptionFor(SelectedMeasurandType.USERTIMING_MARK, userTimingMarkName) == true
        firstViewHasOptionFor(SelectedMeasurandType.USERTIMING_MEASURE, userTimingMeasureName) == true
    }

    void "timings disappear if wrong jobGroup"() {
        given: "navigate to job selection"
        clickJobSelectionTab()

        when: "job group that does not match is selected"
        selectDateInDatepicker(fromDatepicker, "21.06.2016 00:00")
        selectDateInDatepicker(toDatepicker, "23.06.2016 23:59")
        jobGroupList[1].click()
        clickVariableSelectionTab()

        then: "only measurands are shown"
        getFirstViewOptionsSizeFor("USER_TIMINGS") == 0
        isUserTimingsHidden() == true
    }

    void "timings appear for matching job group"() {
        given: "navigate to job selection"
        clickJobSelectionTab()
        jobGroupList[1].click()

        when: "matching job group is selected"
        selectDateInDatepicker(fromDatepicker, "21.06.2016 00:00")
        selectDateInDatepicker(toDatepicker, "23.06.2016 23:59")
        jobGroupList[0].click()
        clickVariableSelectionTab()

        then: "timings are shown"
        getFirstViewOptionsSizeFor("USER_TIMINGS") == userTimingsSize
        firstViewHasOptionFor(SelectedMeasurandType.USERTIMING_MARK, userTimingMarkName) == true
        firstViewHasOptionFor(SelectedMeasurandType.USERTIMING_MEASURE, userTimingMeasureName) == true
    }

    void "timings disappear if wrong page"() {
        given: "navigate to job selection"
        clickJobSelectionTab()
        jobGroupList[0].click()

        when: "job group and page that does not match are selected"
        selectDateInDatepicker(fromDatepicker, "21.06.2016 00:00")
        selectDateInDatepicker(toDatepicker, "23.06.2016 23:59")
        jobGroupList[0].click()
        pageList[0].click()
        clickVariableSelectionTab()

        then: "only measurands are shown"
        getFirstViewOptionsSizeFor("USER_TIMINGS") == 0
        isUserTimingsHidden() == true
    }

    void "timings appear if page matches"() {
        given: "navigate to job selection"
        clickJobSelectionTab()
        pageList[0].click()

        when: "job group and page that does not match are selected"
        selectDateInDatepicker(fromDatepicker, "21.06.2016 00:00")
        selectDateInDatepicker(toDatepicker, "23.06.2016 23:59")
        pageList[1].click()
        clickVariableSelectionTab()

        then: "timings are shown"
        getFirstViewOptionsSizeFor("USER_TIMINGS") == userTimingsSize
        firstViewHasOptionFor(SelectedMeasurandType.USERTIMING_MARK, userTimingMarkName) == true
        firstViewHasOptionFor(SelectedMeasurandType.USERTIMING_MEASURE, userTimingMeasureName) == true
    }

    void "show graph for mark"() {
        given: "usertimings are additionally selected"
        findOptionInFirstViewForUserTiming(SelectedMeasurandType.USERTIMING_MARK, userTimingMarkName).click()
        findOptionInFirstViewForUserTiming(SelectedMeasurandType.USERTIMING_MEASURE, userTimingMeasureName).click()

        when: "show buttons is pushed"
        clickShowButton()

        then: "graphs appear with marks, measures and DOC_COMPLETE_TIME"
        waitFor { graphLines.every { it.displayed } }
        graphLines.size() == 9
        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 9
    }


    private cleanUpData() {
        doLogout()
        Job.withNewTransaction {
            ResultSelectionInformation.list().each {
                it.delete()
            }
            UserTimingSelectionInformation.list().each {
                it.delete()
            }
            EventResult.list().each {
                it.delete()
            }
            UserTiming.list().each {
                it.delete()
            }
            MeasuredEvent.list().each {
                it.delete()
            }
            JobResult.list().each {
                it.delete()
            }
            Job.list().each {
                it.delete()
            }
            ConnectivityProfile.list().each {
                it.delete()
            }
            Page.list().each {
                it.delete()
            }
            Location.list().each {
                it.delete()
            }
            Browser.list().each {
                it.delete()
            }
            WebPageTestServer.list().each {
                it.delete()
            }
            JobGroup.list().each {
                it.delete()
            }
            Script.list().each {
                it.delete()
            }
            UserRole.list().each {
                it.delete()
            }
            User.list().each {
                it.delete()
            }
            Role.list().each {
                it.delete()
            }
            OsmConfiguration.first().delete()
        }
    }


    private createData() {
        Job.withNewTransaction {
            OsmConfiguration.build()
            createAdminUser()

            Script script1 = Script.build(label: script1Name, description: "This is for test purposes", navigationScript: "stuff")
            Script script2 = Script.build(label: script2Name, description: "This is also for test purposes", navigationScript: "stuff")
            JobGroup jobGroup1 = JobGroup.build(name: jobGroup1Name)
            JobGroup jobGroup2 = JobGroup.build(name: jobGroup2Name)
            WebPageTestServer wpt = WebPageTestServer.build(label: "TestWPTServer-564892#Afef1", proxyIdentifier: "TestIdentifier", active: true, baseUrl: "http://internet.de")

            Browser browser = Browser.build(name: "This is the very best browser I've ever seen")
            Location location1 = Location.build(wptServer: wpt, uniqueIdentifierForServer: location1Name, browser: browser, active: true)
            Location location2 = Location.build(wptServer: wpt, uniqueIdentifierForServer: location2Name, browser: browser, active: true)
            Job job1 = Job.build(label: job1Name, script: script1, location: location1, jobGroup: jobGroup1, description: "This is the first test job", runs: 1, active: false, maxDownloadTimeInMinutes: 12)
            Job job2 = Job.build(label: job2Name, script: script2, location: location2, jobGroup: jobGroup2, description: "This is the second test job", runs: 1, active: false, maxDownloadTimeInMinutes: 12)

            Page page1 = Page.build(name: page1Name)
            Page page2 = Page.build(name: page2Name)

            MeasuredEvent measuredEvent1 = MeasuredEvent.build(name: measureEvent1Name, testedPage: page1)
            MeasuredEvent measuredEvent2 = MeasuredEvent.build(name: measureEvent2Name, testedPage: page2)
            MeasuredEvent measuredEvent3 = MeasuredEvent.build(name: measureEvent3Name, testedPage: page2)

            Browser notUsedBrowser = Browser.build(name: "NotUsedBrowser")
            createConnectivityProfile("NotUsedConnectivityProfile")
            Location.build(wptServer: wpt, uniqueIdentifierForServer: "NotUsedLocation", browser: notUsedBrowser, active: true)

            JobResult jobResult1 = JobResult.build(testId:"Test1", date: new DateTime(2016, 06, 22, 3, 13, DateTimeZone.UTC).toDate(), job: job1, locationLocation: location1)
            JobResult jobResultWithUserTimings1 = JobResult.build(testId:"Test2", date: new DateTime(2016, 06, 22, 3, 18, DateTimeZone.UTC).toDate(), job: job1, locationLocation: location1)
            JobResult jobResult2 = JobResult.build(testId:"Test3", date: new DateTime(2016, 06, 22, 3, 15, DateTimeZone.UTC).toDate(), job: job1, locationLocation: location1)
            JobResult jobResult3 = JobResult.build(testId:"Test1", date: new DateTime(2016, 06, 22, 3, 13, DateTimeZone.UTC).toDate(), job: job2, locationLocation: location2)
            JobResult jobResultWithUserTimings2 = JobResult.build(testId:"Test2", date: new DateTime(2016, 06, 22, 3, 19, DateTimeZone.UTC).toDate(), job: job1, locationLocation: location1)

            ConnectivityProfile connectivityProfile = createConnectivityProfile(connectivityProfileName)

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: 200,
                    docCompleteIncomingBytes: 74476,
                    docCompleteRequests: 4,
                    docCompleteTimeInMillisecs: 838,
                    domTimeInMillisecs: 500,
                    firstByteInMillisecs: 170,
                    fullyLoadedIncomingBytes: 184460,
                    fullyLoadedRequestCount: 48,
                    fullyLoadedTimeInMillisecs: 2557,
                    loadTimeInMillisecs: 838,
                    startRenderInMillisecs: 192,
                    csByWptDocCompleteInPercent: 99.6157,
                    csByWptVisuallyCompleteInPercent: 88,
                    speedIndex: 607,
                    visuallyCompleteInMillisecs: 1300,
                    jobResult: jobResult1,
                    jobResultDate: jobResult1.date,
                    jobResultJobConfigId: jobResult1.job.ident(),
                    measuredEvent: measuredEvent1,
                    connectivityProfile: connectivityProfile,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: false,
                    jobGroup: jobGroup1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1
            ).save()
            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: 200,
                    docCompleteIncomingBytes: 71976,
                    docCompleteRequests: 5,
                    docCompleteTimeInMillisecs: 638,
                    domTimeInMillisecs: 750,
                    firstByteInMillisecs: 153,
                    fullyLoadedIncomingBytes: 133360,
                    fullyLoadedRequestCount: 36,
                    fullyLoadedTimeInMillisecs: 2005,
                    loadTimeInMillisecs: 366,
                    startRenderInMillisecs: 185,
                    csByWptDocCompleteInPercent: 73.6157,
                    csByWptVisuallyCompleteInPercent: 63,
                    speedIndex: 577,
                    visuallyCompleteInMillisecs: 1766,
                    jobResult: jobResultWithUserTimings1,
                    jobResultDate: jobResultWithUserTimings1.date,
                    jobResultJobConfigId: jobResultWithUserTimings1.job.ident(),
                    measuredEvent: measuredEvent2,
                    connectivityProfile: connectivityProfile,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: false,
                    jobGroup: jobGroup1,
                    page: measuredEvent2.testedPage,
                    browser: browser,
                    location: location1,
                    userTimings: createUserTimingsForJobResult()
            ).save()

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: 200,
                    docCompleteIncomingBytes: 71976,
                    docCompleteRequests: 5,
                    docCompleteTimeInMillisecs: 638,
                    domTimeInMillisecs: 750,
                    firstByteInMillisecs: 153,
                    fullyLoadedIncomingBytes: 133360,
                    fullyLoadedRequestCount: 36,
                    fullyLoadedTimeInMillisecs: 2005,
                    loadTimeInMillisecs: 366,
                    startRenderInMillisecs: 185,
                    csByWptDocCompleteInPercent: 73.6157,
                    csByWptVisuallyCompleteInPercent: 63,
                    speedIndex: 577,
                    visuallyCompleteInMillisecs: 1766,
                    jobResult: jobResultWithUserTimings2,
                    jobResultDate: jobResultWithUserTimings2.date,
                    jobResultJobConfigId: jobResultWithUserTimings2.job.ident(),
                    measuredEvent: measuredEvent2,
                    connectivityProfile: connectivityProfile,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: false,
                    jobGroup: jobGroup1,
                    page: measuredEvent2.testedPage,
                    browser: browser,
                    location: location1,
                    userTimings: createUserTimingsForJobResult()
            ).save()

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: 200,
                    docCompleteIncomingBytes: 21976,
                    docCompleteRequests: 3,
                    docCompleteTimeInMillisecs: 238,
                    domTimeInMillisecs: 250,
                    firstByteInMillisecs: 53,
                    fullyLoadedIncomingBytes: 23360,
                    fullyLoadedRequestCount: 26,
                    fullyLoadedTimeInMillisecs: 1005,
                    loadTimeInMillisecs: 266,
                    startRenderInMillisecs: 285,
                    csByWptDocCompleteInPercent: 23.6157,
                    csByWptVisuallyCompleteInPercent: 23,
                    speedIndex: 277,
                    visuallyCompleteInMillisecs: 266,
                    jobResult: jobResult2,
                    jobResultDate: jobResult2.date,
                    jobResultJobConfigId: jobResult2.job.ident(),
                    measuredEvent: measuredEvent1,
                    connectivityProfile: connectivityProfile,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: false,
                    jobGroup: jobGroup1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1
            ).save()

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: 200,
                    docCompleteIncomingBytes: 15976,
                    docCompleteRequests: 35,
                    docCompleteTimeInMillisecs: 158,
                    domTimeInMillisecs: 150,
                    firstByteInMillisecs: 153,
                    fullyLoadedIncomingBytes: 15360,
                    fullyLoadedRequestCount: 15,
                    fullyLoadedTimeInMillisecs: 1505,
                    loadTimeInMillisecs: 156,
                    startRenderInMillisecs: 155,
                    csByWptDocCompleteInPercent: 15.6157,
                    csByWptVisuallyCompleteInPercent: 15,
                    speedIndex: 157,
                    visuallyCompleteInMillisecs: 156,
                    jobResult: jobResult2,
                    jobResultDate: jobResult2.date,
                    jobResultJobConfigId: jobResult2.job.ident(),
                    measuredEvent: measuredEvent1,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: true,
                    jobGroup: jobGroup1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1
            ).save()

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: 200,
                    docCompleteIncomingBytes: 25976,
                    docCompleteRequests: 25,
                    docCompleteTimeInMillisecs: 258,
                    domTimeInMillisecs: 250,
                    firstByteInMillisecs: 253,
                    fullyLoadedIncomingBytes: 25360,
                    fullyLoadedRequestCount: 25,
                    fullyLoadedTimeInMillisecs: 2505,
                    loadTimeInMillisecs: 256,
                    startRenderInMillisecs: 255,
                    csByWptDocCompleteInPercent: 25.6157,
                    csByWptVisuallyCompleteInPercent: 25,
                    speedIndex: 257,
                    visuallyCompleteInMillisecs: 256,
                    jobResult: jobResultWithUserTimings1,
                    jobResultDate: jobResultWithUserTimings1.date,
                    jobResultJobConfigId: jobResultWithUserTimings1.job.ident(),
                    measuredEvent: measuredEvent2,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: true,
                    jobGroup: jobGroup1,
                    page: measuredEvent2.testedPage,
                    browser: browser,
                    location: location1,
                    userTimings: createUserTimingsForJobResult()
            ).save()

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: 200,
                    docCompleteIncomingBytes: 25976,
                    docCompleteRequests: 25,
                    docCompleteTimeInMillisecs: 258,
                    domTimeInMillisecs: 250,
                    firstByteInMillisecs: 253,
                    fullyLoadedIncomingBytes: 25360,
                    fullyLoadedRequestCount: 25,
                    fullyLoadedTimeInMillisecs: 2505,
                    loadTimeInMillisecs: 256,
                    startRenderInMillisecs: 255,
                    csByWptDocCompleteInPercent: 25.6157,
                    csByWptVisuallyCompleteInPercent: 25,
                    speedIndex: 257,
                    visuallyCompleteInMillisecs: 256,
                    jobResult: jobResultWithUserTimings2,
                    jobResultDate: jobResultWithUserTimings2.date,
                    jobResultJobConfigId: jobResultWithUserTimings2.job.ident(),
                    measuredEvent: measuredEvent2,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: true,
                    jobGroup: jobGroup1,
                    page: measuredEvent2.testedPage,
                    browser: browser,
                    location: location1,
                    userTimings: createUserTimingsForJobResult()
            ).save()

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: 200,
                    docCompleteIncomingBytes: 45976,
                    docCompleteRequests: 45,
                    docCompleteTimeInMillisecs: 458,
                    domTimeInMillisecs: 450,
                    firstByteInMillisecs: 453,
                    fullyLoadedIncomingBytes: 45360,
                    fullyLoadedRequestCount: 45,
                    fullyLoadedTimeInMillisecs: 4505,
                    loadTimeInMillisecs: 456,
                    startRenderInMillisecs: 455,
                    csByWptDocCompleteInPercent: 45.6157,
                    csByWptVisuallyCompleteInPercent: 45,
                    speedIndex: 457,
                    visuallyCompleteInMillisecs: 456,
                    jobResult: jobResult2,
                    jobResultDate: jobResult2.date,
                    jobResultJobConfigId: jobResult2.job.ident(),
                    measuredEvent: measuredEvent1,
                    customConnectivityName: "Custom (6.000/512 Kbps, 50ms)",
                    noTrafficShapingAtAll: false,
                    jobGroup: jobGroup1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1
            ).save()

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: 200,
                    docCompleteIncomingBytes: 55976,
                    docCompleteRequests: 55,
                    docCompleteTimeInMillisecs: 558,
                    domTimeInMillisecs: 550,
                    firstByteInMillisecs: 553,
                    fullyLoadedIncomingBytes: 55360,
                    fullyLoadedRequestCount: 55,
                    fullyLoadedTimeInMillisecs: 5505,
                    loadTimeInMillisecs: 556,
                    startRenderInMillisecs: 555,
                    csByWptDocCompleteInPercent: 55.6157,
                    csByWptVisuallyCompleteInPercent: 55,
                    speedIndex: 557,
                    visuallyCompleteInMillisecs: 556,
                    jobResult: jobResultWithUserTimings1,
                    jobResultDate: jobResultWithUserTimings1.date,
                    jobResultJobConfigId: jobResultWithUserTimings1.job.ident(),
                    measuredEvent: measuredEvent2,
                    customConnectivityName: "Custom (6.000/512 Kbps, 50ms)",
                    noTrafficShapingAtAll: false,
                    jobGroup: jobGroup1,
                    page: measuredEvent2.testedPage,
                    browser: browser,
                    location: location1,
                    userTimings: createUserTimingsForJobResult()
            ).save()

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: 200,
                    docCompleteIncomingBytes: 55976,
                    docCompleteRequests: 55,
                    docCompleteTimeInMillisecs: 558,
                    domTimeInMillisecs: 550,
                    firstByteInMillisecs: 553,
                    fullyLoadedIncomingBytes: 55360,
                    fullyLoadedRequestCount: 55,
                    fullyLoadedTimeInMillisecs: 5505,
                    loadTimeInMillisecs: 556,
                    startRenderInMillisecs: 555,
                    csByWptDocCompleteInPercent: 55.6157,
                    csByWptVisuallyCompleteInPercent: 55,
                    speedIndex: 557,
                    visuallyCompleteInMillisecs: 556,
                    jobResult: jobResultWithUserTimings2,
                    jobResultDate: jobResultWithUserTimings2.date,
                    jobResultJobConfigId: jobResultWithUserTimings2.job.ident(),
                    measuredEvent: measuredEvent2,
                    customConnectivityName: "Custom (6.000/512 Kbps, 50ms)",
                    noTrafficShapingAtAll: false,
                    jobGroup: jobGroup1,
                    page: measuredEvent2.testedPage,
                    browser: browser,
                    location: location1,
                    userTimings: createUserTimingsForJobResult()
            ).save()

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: 200,
                    docCompleteIncomingBytes: 55976,
                    docCompleteRequests: 55,
                    docCompleteTimeInMillisecs: 558,
                    domTimeInMillisecs: 550,
                    firstByteInMillisecs: 553,
                    fullyLoadedIncomingBytes: 55360,
                    fullyLoadedRequestCount: 55,
                    fullyLoadedTimeInMillisecs: 5505,
                    loadTimeInMillisecs: 556,
                    startRenderInMillisecs: 555,
                    csByWptDocCompleteInPercent: 55.6157,
                    csByWptVisuallyCompleteInPercent: 55,
                    speedIndex: 557,
                    visuallyCompleteInMillisecs: 556,
                    jobResult: jobResult3,
                    jobResultDate: jobResult3.date,
                    jobResultJobConfigId: jobResult3.job.ident(),
                    measuredEvent: measuredEvent3,
                    customConnectivityName: "Custom (6.000/512 Kbps, 50ms)",
                    noTrafficShapingAtAll: false,
                    jobGroup: jobGroup2,
                    page: measuredEvent3.testedPage,
                    browser: browser,
                    location: location2
            ).save()

            List<UserTimingSelectionInformation> userTimingSelectionInfomationForJobResult2 = []
            userTimingSelectionInfomationForJobResult2.add(
                    new UserTimingSelectionInformation(
                            name: userTimingMarkName,
                            type: UserTimingType.MARK
                    )
            )
            userTimingSelectionInfomationForJobResult2.add(
                    new UserTimingSelectionInformation(
                            name: userTimingMeasureName,
                            type: UserTimingType.MEASURE
                    )
            )

            new ResultSelectionInformation(
                    jobResultDate: jobResult2.date,
                    jobGroup: jobGroup1,
                    measuredEvent: measuredEvent1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    connectivityProfile: connectivityProfile,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: false
            ).save()
            new ResultSelectionInformation(
                    jobResultDate: jobResultWithUserTimings1.date,
                    jobGroup: jobGroup1,
                    measuredEvent: measuredEvent2,
                    page: measuredEvent2.testedPage,
                    browser: browser,
                    location: location1,
                    customConnectivityName: "Custom (6.000/512 Kbps, 50ms)",
                    noTrafficShapingAtAll: false,
                    userTimings: userTimingSelectionInfomationForJobResult2
            ).save()
            new ResultSelectionInformation(
                    jobResultDate: jobResult2.date,
                    jobGroup: jobGroup1,
                    measuredEvent: measuredEvent1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: true
            ).save()

            new ResultSelectionInformation(
                    jobResultDate: jobResult3.date,
                    jobGroup: jobGroup2,
                    measuredEvent: measuredEvent3,
                    page: measuredEvent3.testedPage,
                    browser: browser,
                    location: location2,
                    connectivityProfile: connectivityProfile,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: false
            ).save()
        }

    }

    private ConnectivityProfile createConnectivityProfile(String profileName) {
        ConnectivityProfile existingWithName = ConnectivityProfile.findByName(profileName)
        if (existingWithName) {
            return existingWithName
        }
        ConnectivityProfile result = ConnectivityProfile.build(
                name: profileName,
                bandwidthDown: 6000,
                bandwidthUp: 512,
                latency: 40,
                packetLoss: 0,
                active: true
        )
        result.connectivityProfileService = new ConnectivityProfileService()
        return result
    }

    private void selectDateInDatepicker(def datePicker, String date) {
        datePicker.click()
        datePicker << Keys.chord(Keys.END)
        25.times {
            datePicker << Keys.chord(Keys.BACK_SPACE)
        }
        datePicker << date
    }

    private List<UserTiming> createUserTimingsForJobResult() {
        List<UserTiming> userTimingsForJobResult = []
        userTimingsForJobResult.add(
                new UserTiming(
                        name: userTimingMarkName,
                        type: UserTimingType.MARK,
                        startTime: 123
                ))
        userTimingsForJobResult.add(
                new UserTiming(
                        name: userTimingMeasureName,
                        type: UserTimingType.MEASURE,
                        startTime: 123,
                        duration: 456
                ))

        return userTimingsForJobResult
    }
}
