package geb.de.iteratec.osm.csi

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.BrowserConnectivityWeight
import de.iteratec.osm.csi.CsTargetGraph
import de.iteratec.osm.csi.CsTargetValue
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiDay
import de.iteratec.osm.csi.CsiSystem
import de.iteratec.osm.csi.JobGroupWeight
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.PageWeight
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.csi.TimeToCsMapping
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.security.Role
import de.iteratec.osm.security.User
import de.iteratec.osm.security.UserRole
import de.iteratec.osm.util.OsmTestLogin
import geb.CustomUrlGebReportingSpec
import geb.pages.de.iteratec.osm.csi.CsiDashboardPage
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.openqa.selenium.Keys
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * Created by marko on 07.07.16.
 */

@Integration
@Rollback
@Stepwise
class CsiDashboardGebSpec extends CustomUrlGebReportingSpec implements OsmTestLogin {
    @Shared
    String script1Name = "TestScript1-564892#Afef1"
    @Shared
    String job1Name = "TestJob1-564892#Afef1"
    @Shared
    String location1Name = "TestLocation1-564892#Afef1"
    @Shared
    String jobGroup1Name = "TestJobGroup1-564892#Afef1"
    @Shared
    String jobGroup2Name = "TestJobGroup2-564892#Afef1"
    @Shared
    String page1Name = "TestPage1-564892#Afef1"
    @Shared
    String connectivityProfileName = "ConnectivityProfile-564892#Afef1"
    @Shared
    String measureEvent1Name = "MeasureEvent1-564892#Afef1"
    @Shared
    String csiConfigurationName = "CsiConfiguration1-564892#Afef1"

    void cleanupSpec() {
        cleanUpData()
    }

    void "No selection leads to error message"() {
        given: "User is on CsiDashboardPage"
        createData()
        to CsiDashboardPage
        when: "User clicks on \"Show\" button"

        waitFor { showButton.displayed }
        sleep(100)
        showButton.click()

        then: "Error message is displayed"
        waitFor { at CsiDashboardPage }
        waitFor {
            warningNoJobGroupSelected.displayed
        }
        waitFor {
            warningNoPageSelected.displayed
        }

    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Graph for \"Hourly mean per measured step\""() {
        given: "User selects appropriate timeframe, aggregation type, job group and page"
        timeFrameSelect.click()
        selectDateInDatepicker(fromDatepicker, "01.06.2016")
        selectDateInDatepicker(toDatepicker, "11.06.2016")
        jobGroupList[0].click()
        pageList[0].click()
        basedOnVisuallyCompleteButton.click()

        when: "User clicks on \"Show\" button"
        js.exec("window.scrollTo(0,0);") // otherwise the fixed navbar overlaps the button
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 2

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 2
        graphSeries[0].data.collect { [x: it.x, y: it.y] } == [
                [x: 1465110600, y: 14], [x: 1465197000, y: 22], [x: 1465283400, y: 33], [x: 1465369800, y: 44],
                [x: 1465456200, y: 55], [x: 1465542600, y: 66], [x: 1465629000, y: 73]
        ]
        graphSeries[1].data.collect { [x: it.x, y: it.y] } == [
                [x: 1465110600, y: 55], [x: 1465197000, y: 58], [x: 1465283400, y: 68], [x: 1465369800, y: 81],
                [x: 1465456200, y: 88], [x: 1465542600, y: 48], [x: 1465629000, y: 88]
        ]
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "NotUsedBrowser leads to no data"() {
        given: "User selects NotUsedBrowser"
        waitFor { browserTab.click() }
        selectAllBrowserButton.click()
        selectBrowsersList[0].click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "No Data Warning is displayed"
        waitFor {
            $("#noDataForCurrentSelectionWarning").attr("innerHTML").contains("No data available for your selection.")
        }
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Graph is shown for correct Browser"() {
        given: "User selects NotUsedBrowser"
        browserTab.click()
        selectBrowsersList[1].click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 2

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 2
        graphSeries[0].data.collect { [x: it.x, y: it.y] } == [
                [x: 1465110600, y: 14], [x: 1465197000, y: 22], [x: 1465283400, y: 33], [x: 1465369800, y: 44],
                [x: 1465456200, y: 55], [x: 1465542600, y: 66], [x: 1465629000, y: 73]
        ]
        graphSeries[1].data.collect { [x: it.x, y: it.y] } == [
                [x: 1465110600, y: 55], [x: 1465197000, y: 58], [x: 1465283400, y: 68], [x: 1465369800, y: 81],
                [x: 1465456200, y: 88], [x: 1465542600, y: 48], [x: 1465629000, y: 88]
        ]
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Graph is shown for \"Select all Browsers\""() {
        given: "User selects NotUsedBrowser"
        browserTab.click()
        selectAllBrowserButton.click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 2

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 2
        graphSeries[0].data.collect { [x: it.x, y: it.y] } == [
                [x: 1465110600, y: 14], [x: 1465197000, y: 22], [x: 1465283400, y: 33], [x: 1465369800, y: 44],
                [x: 1465456200, y: 55], [x: 1465542600, y: 66], [x: 1465629000, y: 73]
        ]
        graphSeries[1].data.collect { [x: it.x, y: it.y] } == [
                [x: 1465110600, y: 55], [x: 1465197000, y: 58], [x: 1465283400, y: 68], [x: 1465369800, y: 81],
                [x: 1465456200, y: 88], [x: 1465542600, y: 48], [x: 1465629000, y: 88]
        ]
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "NotUsedLocation leads to no data"() {
        given: "User selects NotUsedLocation"
        browserTab.click()
        selectAllLocationsButton.click()
        selectLocationField.click()
        selectLocationList[0].click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "No Data Warning is displayed"
        waitFor {
            $("#noDataForCurrentSelectionWarning").attr("innerHTML").contains("No data available for your selection.")
        }
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Graph is shown for correct Location"() {
        given: "User selects NotUsedLocation"
        browserTab.click()
        selectLocationField.click()
        selectLocationList[1].click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 2

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 2
        graphSeries[0].data.collect { [x: it.x, y: it.y] } == [
                [x: 1465110600, y: 14], [x: 1465197000, y: 22], [x: 1465283400, y: 33], [x: 1465369800, y: 44],
                [x: 1465456200, y: 55], [x: 1465542600, y: 66], [x: 1465629000, y: 73]
        ]
        graphSeries[1].data.collect { [x: it.x, y: it.y] } == [
                [x: 1465110600, y: 55], [x: 1465197000, y: 58], [x: 1465283400, y: 68], [x: 1465369800, y: 81],
                [x: 1465456200, y: 88], [x: 1465542600, y: 48], [x: 1465629000, y: 88]
        ]
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Graph is shown for \"Select all Locations\""() {
        given: "User selects NotUsedBrowser"
        browserTab.click()
        selectAllLocationsButton.click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 2

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 2
        graphSeries[0].data.collect { [x: it.x, y: it.y] } == [
                [x: 1465110600, y: 14], [x: 1465197000, y: 22], [x: 1465283400, y: 33], [x: 1465369800, y: 44],
                [x: 1465456200, y: 55], [x: 1465542600, y: 66], [x: 1465629000, y: 73]
        ]
        graphSeries[1].data.collect { [x: it.x, y: it.y] } == [
                [x: 1465110600, y: 55], [x: 1465197000, y: 58], [x: 1465283400, y: 68], [x: 1465369800, y: 81],
                [x: 1465456200, y: 88], [x: 1465542600, y: 48], [x: 1465629000, y: 88]
        ]
    }

    @Ignore("[IT-1415] need to implement new connectivity selection")
    void "NotUsedConnectivity leads to no data"() {
        given: "User selects NotUsedBrowser"
        connectivityTab.click()
        selectAllConnectivityButton.click()
        selectConnectivityProfilesList[1].click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "No Data Warning is displayed"
        waitFor {
            $("#noDataForCurrentSelectionWarning").attr("innerHTML").contains("No data available for your selection.")
        }
    }

    @Ignore("[IT-1415] need to implement new connectivity selection")
    void "Graph is shown for correct Connectivity Profile"() {
        given: "User selects NotUsedBrowser"
        connectivityTab.click()
        selectConnectivityProfilesList[0].click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 2

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 2
        graphSeries[0].data.collect { [x: it.x, y: it.y] } == [
                [x: 1465110600, y: 14], [x: 1465197000, y: 22], [x: 1465283400, y: 33], [x: 1465369800, y: 44],
                [x: 1465456200, y: 55], [x: 1465542600, y: 66], [x: 1465629000, y: 73]
        ]
        graphSeries[1].data.collect { [x: it.x, y: it.y] } == [
                [x: 1465110600, y: 55], [x: 1465197000, y: 58], [x: 1465283400, y: 68], [x: 1465369800, y: 81],
                [x: 1465456200, y: 88], [x: 1465542600, y: 48], [x: 1465629000, y: 88]
        ]
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Graph is shown for \"Select all Connectivity Profiles\""() {
        given: "User selects NotUsedBrowser"
        connectivityTab.click()
        selectAllConnectivityButton.click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 2
        def graphSeries = js."window.rickshawGraphBuilder.graph.series"

        graphSeries.size() == 2
        graphSeries[0].data.collect { [x: it.x, y: it.y] } == [
                [x: 1465110600, y: 14], [x: 1465197000, y: 22], [x: 1465283400, y: 33], [x: 1465369800, y: 44],
                [x: 1465456200, y: 55], [x: 1465542600, y: 66], [x: 1465629000, y: 73]
        ]
        graphSeries[1].data.collect { [x: it.x, y: it.y] } == [
                [x: 1465110600, y: 55], [x: 1465197000, y: 58], [x: 1465283400, y: 68], [x: 1465369800, y: 81],
                [x: 1465456200, y: 88], [x: 1465542600, y: 48], [x: 1465629000, y: 88]
        ]
    }

    private void createData() {
        Job.withNewTransaction {
            TestDataUtil.createOsmConfig()
            TestDataUtil.createAdminUser()
            initChartData()
            initCsiData()
            createTestSpecificData()
        }

    }

    private void createTestSpecificData() {


        Script script1 = TestDataUtil.createScript(script1Name, "This is for test purposes", "stuff")
        Browser browser = TestDataUtil.createBrowser("TestFireFox")
        ConnectivityProfile connectivityProfile = TestDataUtil.createConnectivityProfile(connectivityProfileName)
        BrowserConnectivityWeight browserConnectivityWeight = TestDataUtil.createBrowserConnectivityWeight(browser, connectivityProfile, 2)
        Page page1 = TestDataUtil.createPage(page1Name)
        PageWeight pageWeight = TestDataUtil.createPageWeight(page1, 3)
        TimeToCsMapping timeToCsMapping = TestDataUtil.createTimeToCsMapping(page1)
        CsiDay csiDay = TestDataUtil.createCsiDay([0: 0, 1: 1, 2: 2, 3: 3, 4: 4, 5: 5, 6: 6, 7: 7, 8: 8, 9: 9, 10: 10, 11: 11, 12: 11, 13: 10, 14: 9, 15: 8, 16: 7, 17: 6, 18: 5, 19: 4, 20: 3, 21: 2, 22: 1, 23: 0])
        CsiConfiguration csiConfiguration = TestDataUtil.createCsiConfiguration(csiConfigurationName, "TestDescription", csiDay, [browserConnectivityWeight], [pageWeight], [timeToCsMapping])
        TestDataUtil.createCsTargetGraph(TestDataUtil.createCsTargetValue(), TestDataUtil.createCsTargetValue())
        JobGroup jobGroup1 = new JobGroup([csiConfiguration: csiConfiguration, name: jobGroup1Name]).save()
        JobGroup jobGroup2 = new JobGroup([csiConfiguration: csiConfiguration, name: jobGroup2Name]).save()
        WebPageTestServer wpt = TestDataUtil.createWebPageTestServer("TestWPTServer-564892#Afef1", "TestIdentifier", true, "http://internet.de")
        Location location1 = TestDataUtil.createLocation(wpt, location1Name, browser, true)
        Job job1 = TestDataUtil.createJob(job1Name, script1, location1, jobGroup1, "This is the first test job", 1, false, 12)
        CsiSystem csiSystem = new CsiSystem([label: "TestCsiSystem"])
        csiSystem.addToJobGroupWeights(new JobGroupWeight(jobGroup: jobGroup1, weight: 50))
        csiSystem.addToJobGroupWeights(new JobGroupWeight(jobGroup: jobGroup2, weight: 60))
        csiSystem.save(failOnError: true)
        JobResult jobResult1 = TestDataUtil.createJobResult("Test1", new DateTime(2016, 06, 22, 3, 13, DateTimeZone.UTC).toDate(), job1, location1)
        MeasuredEvent measuredEvent1 = TestDataUtil.createMeasuredEvent(measureEvent1Name, page1)
        CsiAggregationInterval hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
        AggregatorType aggregatorType = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
        new CsiAggregation([started: new DateTime(2016, 6, 5, 7, 10, DateTimeZone.UTC).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup: jobGroup1, measuredEvent: measuredEvent1, page: page1, browser: browser, location: location1, csByWptDocCompleteInPercent: 14, csByWptVisuallyCompleteInPercent: 55, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 6, 7, 10, DateTimeZone.UTC).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup: jobGroup1, measuredEvent: measuredEvent1, page: page1, browser: browser, location: location1, csByWptDocCompleteInPercent: 22, csByWptVisuallyCompleteInPercent: 58, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 7, 7, 10, DateTimeZone.UTC).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup: jobGroup1, measuredEvent: measuredEvent1, page: page1, browser: browser, location: location1, csByWptDocCompleteInPercent: 33, csByWptVisuallyCompleteInPercent: 68, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 8, 7, 10, DateTimeZone.UTC).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup: jobGroup1, measuredEvent: measuredEvent1, page: page1, browser: browser, location: location1, csByWptDocCompleteInPercent: 44, csByWptVisuallyCompleteInPercent: 81, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 9, 7, 10, DateTimeZone.UTC).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup: jobGroup1, measuredEvent: measuredEvent1, page: page1, browser: browser, location: location1, csByWptDocCompleteInPercent: 55, csByWptVisuallyCompleteInPercent: 88, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 10, 7, 10, DateTimeZone.UTC).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup: jobGroup1, measuredEvent: measuredEvent1, page: page1, browser: browser, location: location1, csByWptDocCompleteInPercent: 66, csByWptVisuallyCompleteInPercent: 48, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 11, 7, 10, DateTimeZone.UTC).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup: jobGroup1, measuredEvent: measuredEvent1, page: page1, browser: browser, location: location1, csByWptDocCompleteInPercent: 73, csByWptVisuallyCompleteInPercent: 88, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 12, 7, 10, DateTimeZone.UTC).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup: jobGroup1, measuredEvent: measuredEvent1, page: page1, browser: browser, location: location1, csByWptDocCompleteInPercent: 24, csByWptVisuallyCompleteInPercent: 98, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 13, 7, 10, DateTimeZone.UTC).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup: jobGroup1, measuredEvent: measuredEvent1, page: page1, browser: browser, location: location1, csByWptDocCompleteInPercent: 39, csByWptVisuallyCompleteInPercent: 65, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 14, 7, 10, DateTimeZone.UTC).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup: jobGroup1, measuredEvent: measuredEvent1, page: page1, browser: browser, location: location1, csByWptDocCompleteInPercent: 77, csByWptVisuallyCompleteInPercent: 61, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 15, 7, 10, DateTimeZone.UTC).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup: jobGroup1, measuredEvent: measuredEvent1, page: page1, browser: browser, location: location1, csByWptDocCompleteInPercent: 88, csByWptVisuallyCompleteInPercent: 72, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 16, 7, 10, DateTimeZone.UTC).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup: jobGroup1, measuredEvent: measuredEvent1, page: page1, browser: browser, location: location1, csByWptDocCompleteInPercent: 99, csByWptVisuallyCompleteInPercent: 78, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 17, 7, 10, DateTimeZone.UTC).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup: jobGroup1, measuredEvent: measuredEvent1, page: page1, browser: browser, location: location1, csByWptDocCompleteInPercent: 1, csByWptVisuallyCompleteInPercent: 84, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 18, 7, 10, DateTimeZone.UTC).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup: jobGroup1, measuredEvent: measuredEvent1, page: page1, browser: browser, location: location1, csByWptDocCompleteInPercent: 31, csByWptVisuallyCompleteInPercent: 88, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        Browser notUsedBrowser = TestDataUtil.createBrowser("NotUsedBrowser")
        TestDataUtil.createConnectivityProfile("NotUsedConnectivityProfile")
        TestDataUtil.createLocation(wpt, "NotUsedLocation", notUsedBrowser, true)

    }

    private void selectDateInDatepicker(def datePicker, String date) {
        datePicker.click()
        datePicker << Keys.chord(Keys.CONTROL, "a")
        datePicker << Keys.chord(Keys.DELETE)
        datePicker << Keys.chord(Keys.ESCAPE)
        datePicker << date
    }

    private void initChartData() {

        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME, MeasurandGroup.LOAD_TIMES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_DOM_TIME, MeasurandGroup.LOAD_TIMES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_FIRST_BYTE, MeasurandGroup.LOAD_TIMES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_FULLY_LOADED_REQUEST_COUNT, MeasurandGroup.REQUEST_COUNTS);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_FULLY_LOADED_TIME, MeasurandGroup.LOAD_TIMES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_LOAD_TIME, MeasurandGroup.LOAD_TIMES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_START_RENDER, MeasurandGroup.LOAD_TIMES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES, MeasurandGroup.REQUEST_SIZES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS, MeasurandGroup.REQUEST_COUNTS);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_FULLY_LOADED_INCOMING_BYTES, MeasurandGroup.REQUEST_SIZES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_CS_BASED_ON_DOC_COMPLETE_IN_PERCENT, MeasurandGroup.PERCENTAGES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_SPEED_INDEX, MeasurandGroup.UNDEFINED);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_VISUALLY_COMPLETE, MeasurandGroup.LOAD_TIMES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_UNCACHED_CS_BASED_ON_VISUALLY_COMPLETE_IN_PERCENT, MeasurandGroup.PERCENTAGES);

        TestDataUtil.createAggregatorType(AggregatorType.RESULT_CACHED_DOC_COMPLETE_TIME, MeasurandGroup.LOAD_TIMES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_CACHED_DOM_TIME, MeasurandGroup.LOAD_TIMES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_CACHED_FIRST_BYTE, MeasurandGroup.LOAD_TIMES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_CACHED_FULLY_LOADED_REQUEST_COUNT, MeasurandGroup.REQUEST_COUNTS);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_CACHED_FULLY_LOADED_TIME, MeasurandGroup.LOAD_TIMES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_CACHED_LOAD_TIME, MeasurandGroup.LOAD_TIMES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_CACHED_START_RENDER, MeasurandGroup.LOAD_TIMES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES, MeasurandGroup.REQUEST_SIZES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_CACHED_DOC_COMPLETE_REQUESTS, MeasurandGroup.REQUEST_COUNTS);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_CACHED_FULLY_LOADED_INCOMING_BYTES, MeasurandGroup.REQUEST_SIZES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_CACHED_CS_BASED_ON_DOC_COMPLETE_IN_PERCENT, MeasurandGroup.PERCENTAGES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_CACHED_SPEED_INDEX, MeasurandGroup.UNDEFINED);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_CACHED_VISUALLY_COMPLETE, MeasurandGroup.LOAD_TIMES);
        TestDataUtil.createAggregatorType(AggregatorType.RESULT_CACHED_CS_BASED_ON_VISUALLY_COMPLETE_IN_PERCENT, MeasurandGroup.PERCENTAGES);

    }

    private void initCsiData() {

        def csiGroupName = JobGroup.UNDEFINED_CSI
        JobGroup.findByName(csiGroupName) ?: new JobGroup(
                name: csiGroupName).save(failOnError: true)

        // here you can initialize the weights of the hours of the csiDay for csi calculation  (see de.iteratec.osm.csi.PageCsiAggregationService)
        if (CsiDay.count <= 0) {
            CsiDay initDay = new CsiDay()
            (0..23).each {
                initDay.setHourWeight(it, 1)
            }
            initDay.save(failOnError: true)
        }

        Page.findByName(Page.UNDEFINED) ?: new Page(name: Page.UNDEFINED).save(failOnError: true)

        TestDataUtil.createAggregatorType(AggregatorType.MEASURED_EVENT, MeasurandGroup.NO_MEASURAND)
        TestDataUtil.createAggregatorType(AggregatorType.PAGE, MeasurandGroup.NO_MEASURAND)
        TestDataUtil.createAggregatorType(AggregatorType.PAGE_AND_BROWSER, MeasurandGroup.NO_MEASURAND)
        TestDataUtil.createAggregatorType(AggregatorType.SHOP, MeasurandGroup.NO_MEASURAND)
        TestDataUtil.createAggregatorType(AggregatorType.CSI_SYSTEM, MeasurandGroup.NO_MEASURAND)

        CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY) ?: new CsiAggregationInterval(
                name: "hourly",
                intervalInMinutes: CsiAggregationInterval.HOURLY
        ).save(failOnError: true)

        CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY) ?: new CsiAggregationInterval(
                name: "daily",
                intervalInMinutes: CsiAggregationInterval.DAILY
        ).save(failOnError: true)

        CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY) ?: new CsiAggregationInterval(
                name: "weekly",
                intervalInMinutes: CsiAggregationInterval.WEEKLY
        ).save(failOnError: true)

        Date date = new DateTime(2000, 1, 1, 0, 0).toDate()
        Double percent = 90
        CsTargetValue val1 = CsTargetValue.findByDateAndCsInPercent(date, percent) ?: new CsTargetValue(
                date: date,
                csInPercent: percent,
        ).save(failOnError: true)
        date = new DateTime(2100, 12, 31, 23, 59).toDate()
        percent = 90
        CsTargetValue val2 = CsTargetValue.findByDateAndCsInPercent(date, percent) ?: new CsTargetValue(
                date: date,
                csInPercent: percent,
        ).save(failOnError: true)

        String labelTargetCsi_EN = 'Target-CSI'
        String descriptionTargetCsi_EN = 'Customer satisfaction index defined as target.'
        CsTargetGraph.findByLabel(labelTargetCsi_EN) ?: new CsTargetGraph(
                label: labelTargetCsi_EN,
                description: descriptionTargetCsi_EN,
                pointOne: val1,
                pointTwo: val2,
                defaultVisibility: true
        ).save(failOnError: true)



        if (CsiConfiguration.count <= 0) {
            CsiConfiguration initCsiConfiguration = new CsiConfiguration()
            initCsiConfiguration.with {
                label = "initial csi configuration"
                description = "a first csi configuration as template"
                csiDay = CsiDay.findAll()[0]
            }
            initCsiConfiguration.save(failOnError: true)
        }

    }

    private cleanUpData() {
        doLogout()
        Job.withNewTransaction {

            CsiAggregation.list().each {
                it.delete()
            }
            CsiAggregationInterval.list().each {
                it.delete()
            }
            MeasuredEvent.list().each {
                it.delete()
            }
            BrowserConnectivityWeight.list().each {
                it.delete()
            }
            ConnectivityProfile.list().each {
                it.delete()
            }
            JobResult.list().each {
                it.delete()
            }
            TimeToCsMapping.list().each {
                it.delete()
            }
            PageWeight.list().each {
                it.delete()
            }
            Page.list().each {
                it.delete()
            }
            Job.list().each {
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
            JobGroupWeight.list().each {
                it.delete()
            }
            JobGroup.list().each {
                it.delete()
            }
            CsiConfiguration.list().each {
                it.delete()
            }
            CsiDay.list().each {
                it.delete()
            }
            Script.list().each {
                it.delete()
            }
            AggregatorType.list().each {
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
            OsmConfiguration.list().each {
                it.delete()
            }
            CsiSystem.list().each {
                it.delete()
            }
        }
    }
}
