package geb.de.iteratec.osm.result

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.result.*
import de.iteratec.osm.security.Role
import de.iteratec.osm.security.User
import de.iteratec.osm.security.UserRole
import de.iteratec.osm.util.OsmTestLogin
import geb.CustomUrlGebReportingSpec
import geb.pages.de.iteratec.osm.result.EventResultDashboardPage
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.openqa.selenium.Keys
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * Created by marko on 22.06.16.
 */
@Integration
@Rollback
@Stepwise
class EventResultDashboardAdjustChartGebSpec extends CustomUrlGebReportingSpec implements OsmTestLogin {

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
    String page1Name = "TestPage1-564892#Afef1"
    @Shared
    String connectivityProfileName = "ConnectivityProfile-564892#Afef1"
    @Shared
    String measureEvent1Name = "MeasureEvent1-564892#Afef1"

    void cleanupSpec() {
        cleanUpData()
    }

    void "Login makes \"Save as Dashboard\"-Button visible"() {
        when: "User is not logged in"
        createData()
        to EventResultDashboardPage
        then: "The button is invisible"
        !saveAsDashboardButton.present

        when: "User is logged in"
        doLogin()
        then: "Button is visible"
        at EventResultDashboardPage
        saveAsDashboardButton.present

    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Document Complete graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        timeFrameSelect.click()
        selectDateInDatepicker(fromDatepicker, "21.06.2016")
        selectDateInDatepicker(toDatepicker, "23.06.2016")
        jobGroupList[0].click()
        pageList[0].click()
        tabVariableSelection.click()

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect {
            [x: it.x, y: it.y]
        } == [[x: 1466565180, y: 838], [x: 1466565300, y: 238], [x: 1466565480, y: 638]]
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Adjust Chart Title"() {
        given: "User opens Adjust Chart"

        adjustChartButton.click()

        when: "User edits title"
        waitFor { chartTitleInputField.displayed }
        sleep(100)
        chartTitleInputField << "CustomTitle"
        sleep(100)
        adjustChartApply.click()

        then: "Chart title is changed"
        waitFor { chartTitle == "CustomTitle" }
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Adjust Chart Size to illegal values"() {
        given: "User edits chart size"
        waitFor { adjustChartButton.click() }
        sleep(200)
        chartWidthInputField.firstElement().clear()
        chartWidthInputField << "0"
        chartheightInputField.firstElement().clear()
        chartheightInputField << "9999999"

        when: "User clicks \"apply\""
        sleep(100)
        def result = withAlert { adjustChartApply.click() }

        then: "Error message is shown"
        result == "Width and height of diagram must be numeric values. Maximum is 5.000 x 3.000 pixels, minimum width is 540 pixels."
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Adjust Chart Size"() {
        given: "User edits chart size"
        chartWidthInputField.firstElement().clear()
        chartWidthInputField << "600"
        chartheightInputField.firstElement().clear()
        chartheightInputField << "600"

        when: "User clicks \"apply\""
        waitFor { adjustChartApply.displayed }
        adjustChartApply.click()

        then: "Chart changed"
        waitFor { graphLines.displayed }
        graphLines.size() == 1

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect {
            [x: it.x, y: it.y]
        } == [[x: 1466565180, y: 838], [x: 1466565300, y: 238], [x: 1466565480, y: 638]]

        chartContainer.width == 600
        chartContainer.height == 650
        graphYGridFirstTick == "0"
        graphYGridLastTick == "840"
    }

    @Ignore("Not yet sure how to trigger the js part")
    void "Add graph alias"() {
        given: "User clicks on the add graph aliases button"
        waitFor { addAliasButton.click() }

        when: "User provides graph alias"
        sleep(500)
        waitFor { aliasInputField.displayed }
        aliasInputField << "CustomAlias"
        sleep(500)

        then: "Graph is renamed"
        waitFor { graphName == "CustomAlias" }
    }

    @Ignore("Not yet sure how to trigger the js part")
    void "Change Graph color"() {
        when: "User changes graph color"
        sleep(500)
//        colorPicker << '#AAAAAA'
//        $(".span2").find("input", type:"color")[0].jquery.attr("style", "width: 50%; background-color: rgb(170,170, 170);")
        $(".span2").find("input", type: "color")[0].jquery.attr("value", "#aaaaaa")
        sleep(500)

        then: "Graph is recolored"
        true

        waitFor { graphColorField == 'background-color: rgb(170, 170, 170);' }
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Adjust Chart Section"() {
        given: "User edits chart size"
        waitFor { adjustChartButton.click() }
        sleep(200)
        diaYAxisMinInputField.firstElement().clear()
        diaYAxisMinInputField << "200"
        diaYAxisMaxInputField.firstElement().clear()
        diaYAxisMaxInputField << "600"

        when: "User clicks \"apply\""
        waitFor { adjustChartApply.displayed }
        adjustChartApply.click()

        then: "Chart changed"
        waitFor { graphLines.displayed }
        graphLines.size() == 1

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect {
            [x: it.x, y: it.y]
        } == [[x: 1466565180, y: 838], [x: 1466565300, y: 238], [x: 1466565480, y: 638]]

        graphYGridFirstTick == "200"
        graphYGridLastTick == "600"
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Enable Data-Markers"() {
        given: "User clicked adjust chart"
        waitFor { adjustChartButton.click() }
        sleep(200)

        when: "User clicks \"Show data-marker\""
        waitFor { showDataMarkersCheckBox.displayed }
        sleep(200)
        showDataMarkersCheckBox.click()
        sleep(200)
        waitFor { adjustChartApply.displayed }
        sleep(200)
        adjustChartApply.click()
        sleep(200)

        then: "Data-markers show on the graph"
        waitFor { dataMarker }
        waitFor { dataMarker.attr("style").contains("top: 543px; left: 216px;") }
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Enable Data-Labels"() {
        given: "User clicked adjust chart"
        waitFor { adjustChartButton.click() }
        sleep(200)


        when: "User clicks \"Show data-marker\""
        waitFor { showDataLabelsCheckBox.displayed }
        showDataLabelsCheckBox.click()
        waitFor { adjustChartApply.displayed }
        sleep(200)
        adjustChartApply.click()
        sleep(200)


        then: "Data-markers show on the graph"
        waitFor { dataLabel }
        waitFor {
            dataLabel.attr("style").contains('top: 538px; left: 207px; height: 100px; width: 100px; font-size: 13pt; font-weight: bold; color: rgb(179, 179, 179); cursor: default;')
        }
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Save custom dashboard"() {
        given: "User clicked on \"Save as dashboard\"-button"
        clickSaveAsDashboardButton()

        when: "User enters new name for the dashboard"
        waitFor { dashboardNameFromModalTextField.displayed }
        dashboardNameFromModalTextField << "CustomDashboard"
        waitFor { saveDashboardButtonButton.displayed }
        saveDashboardButtonButton.click()

        then: "Success Message is displayed"
        at EventResultDashboardPage
        waitFor { saveDashboardSuccessMessage.displayed }
        waitFor { graphLines.displayed }
        graphLines.size() == 1

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect {
            [x: it.x, y: it.y]
        } == [[x: 1466565180, y: 838], [x: 1466565300, y: 238], [x: 1466565480, y: 638]]
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Load custom dashboard"() {
        given: "User visits the EventResultDashboardPage"
        to EventResultDashboardPage
        when: "User loads CustomDashboard"
        customDashboardSelectionDropdown.click()
        waitFor { customDashboardSelectionList.displayed }
        customDashboardSelectionList.find("a").click()
        then: "The old dashboard is loaded again"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect {
            [x: it.x, y: it.y]
        } == [[x: 1466565180, y: 838], [x: 1466565300, y: 238], [x: 1466565480, y: 638]]

        waitFor { dataLabel }
        waitFor {
            dataLabel.attr("style").contains('top: 538px; left: 207px; height: 100px; width: 100px; font-size: 13pt; font-weight: bold; color: rgb(179, 179, 179); cursor: default;')
        }
        waitFor { dataMarker }
        waitFor { dataMarker.attr("style").contains("top: 543px; left: 216px;") }
        chartTitle == "CustomTitle"

        chartContainer.width == 600
        chartContainer.height == 650
        graphYGridFirstTick == "200"
        graphYGridLastTick == "600"
    }


    private cleanUpData() {
        doLogout()
        Job.withNewTransaction {
            ResultSelectionInformation.list().each {
                it.delete()
            }
            EventResult.list().each {
                it.delete()
            }
            MeasuredEvent.list().each {
                it.delete()
            }
            ConnectivityProfile.list().each {
                it.delete()
            }
            JobResult.list().each {
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
            JobGroup.list().each {
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
        }
    }


    private createData() {
        Job.withNewTransaction {
            TestDataUtil.createOsmConfig()
            TestDataUtil.createAdminUser()
            initChartData()

            Script script1 = TestDataUtil.createScript(script1Name, "This is for test purposes", "stuff")
            Script script2 = TestDataUtil.createScript(script2Name, "This is also for test purposes", "stuff")
            JobGroup jobGroup1 = TestDataUtil.createJobGroup(jobGroup1Name)
            WebPageTestServer wpt = TestDataUtil.createWebPageTestServer("TestWPTServer-564892#Afef1", "TestIdentifier", true, "http://internet.de")
            Browser browser = TestDataUtil.createBrowser("This is the very best browser I've ever seen")
            Location location1 = TestDataUtil.createLocation(wpt, location1Name, browser, true)
            Location location2 = TestDataUtil.createLocation(wpt, location2Name, browser, true)
            Job job1 = TestDataUtil.createJob(job1Name, script1, location1, jobGroup1, "This is the first test job", 1, false, 12)
            Job job2 = TestDataUtil.createJob(job2Name, script2, location2, jobGroup1, "This is the second test job", 1, false, 12)
            Page page1 = TestDataUtil.createPage(page1Name)
            JobResult jobResult1 = TestDataUtil.createJobResult("Test1", new DateTime(2016, 06, 22, 3, 13, DateTimeZone.UTC).toDate(), job1, location1)
            JobResult jobResult2 = TestDataUtil.createJobResult("Test2", new DateTime(2016, 06, 22, 3, 18, DateTimeZone.UTC).toDate(), job1, location1)
            JobResult jobResult3 = TestDataUtil.createJobResult("Test3", new DateTime(2016, 06, 22, 3, 15, DateTimeZone.UTC).toDate(), job1, location1)
            ConnectivityProfile connectivityProfile = TestDataUtil.createConnectivityProfile(connectivityProfileName)
            MeasuredEvent measuredEvent1 = TestDataUtil.createMeasuredEvent(measureEvent1Name, page1)

            Browser notUsedBrowser = TestDataUtil.createBrowser("NotUsedBrowser")
            TestDataUtil.createConnectivityProfile("NotUsedConnectivityProfile")
            TestDataUtil.createLocation(wpt, "NotUsedLocation", notUsedBrowser, true)

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
                    jobResult: jobResult3,
                    jobResultDate: jobResult3.date,
                    jobResultJobConfigId: jobResult3.job.ident(),
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
                    jobResult: jobResult3,
                    jobResultDate: jobResult3.date,
                    jobResultJobConfigId: jobResult3.job.ident(),
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
                    jobResult: jobResult3,
                    jobResultDate: jobResult3.date,
                    jobResultJobConfigId: jobResult3.job.ident(),
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

            new ResultSelectionInformation(
                    jobResultDate: jobResult3.date,
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
                    jobResultDate: jobResult2.date,
                    jobGroup: jobGroup1,
                    measuredEvent: measuredEvent1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    customConnectivityName: "Custom (6.000/512 Kbps, 50ms)",
                    noTrafficShapingAtAll: false
            ).save()
            new ResultSelectionInformation(
                    jobResultDate: jobResult3.date,
                    jobGroup: jobGroup1,
                    measuredEvent: measuredEvent1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: true
            ).save()
        }

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

}
