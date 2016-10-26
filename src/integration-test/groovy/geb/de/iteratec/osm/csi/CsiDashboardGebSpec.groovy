package geb.de.iteratec.osm.csi

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.*
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

    void "Login makes \"Save as Dashboard\"-Button visible"() {
        when: "User is not logged in"
        createData()
        to CsiDashboardPage
        then: "The button is invisible"
        !saveAsDashboardButton.present

        when: "User is logged in"
        doLogin()
        then: "Button is visible"
        at CsiDashboardPage
        saveAsDashboardButton.present


    }

    void "No selection leads to error message"() {
        given: "User is on CsiDashboardPage"
        to CsiDashboardPage
        when: "User clicks on \"Show\" button"

        waitFor { showButton.displayed }
        sleep(100)
        showButton.click()

        then: "Error message is displayed"
        waitFor { at CsiDashboardPage }
        waitFor {
            $("div", class: "alert alert-error")[0].attr("innerHTML").contains("Please check your selection, you made the following mistakes:")
        } //check that the error box appears
        waitFor {
            $("div", class: "alert alert-error")[0].find("li")[0].attr("innerHTML").contains("Please select at least one folder.")
        } //check that the correct error message is displayed
        waitFor {
            $("div", class: "alert alert-error")[0].find("li")[1].attr("innerHTML").contains("Please select at least one page.")
        } //check that the correct error message is displayed

    }

    void "Graph for \"Hourly mean per measured step\""() {
        given: "User selects appropriate timeframe, aggregation type, job group and page"
        timeFrameSelect.click()
        selectDateInDatepicker(fromDatepicker, "01.06.2016")
        selectDateInDatepicker(toDatepicker, "11.06.2016")
        jobGroupList[0].click()
        pageList[0].click()
        basedOnVisuallyCompleteButton.click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        graphLine1 == 'M0,348.84437596302L168.33333333333331,319.8404785643071L336.66666666666663,279.96011964107674L505,240.07976071784645L673.3333333333333,200.19940179461614L841.6666666666667,160.31904287138582L1010,134.94063264751202'
        graphLine2 == 'M0,200.19940179461614L168.33333333333331,189.3229402700988L336.66666666666663,153.06806852170757L505,105.93673524879904L673.3333333333333,80.55832502492518L841.6666666666667,225.57781201848996L1010,80.55832502492518'
    }

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

    void "Graph is shown for correct Browser"() {
        given: "User selects NotUsedBrowser"
        browserTab.click()
        selectBrowsersList[1].click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        graphLine1 == 'M0,348.84437596302L168.33333333333331,319.8404785643071L336.66666666666663,279.96011964107674L505,240.07976071784645L673.3333333333333,200.19940179461614L841.6666666666667,160.31904287138582L1010,134.94063264751202'
        graphLine2 == 'M0,200.19940179461614L168.33333333333331,189.3229402700988L336.66666666666663,153.06806852170757L505,105.93673524879904L673.3333333333333,80.55832502492518L841.6666666666667,225.57781201848996L1010,80.55832502492518'
    }

    void "Graph is shown for \"Select all Browsers\""() {
        given: "User selects NotUsedBrowser"
        browserTab.click()
        selectAllBrowserButton.click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        graphLine1 == 'M0,348.84437596302L168.33333333333331,319.8404785643071L336.66666666666663,279.96011964107674L505,240.07976071784645L673.3333333333333,200.19940179461614L841.6666666666667,160.31904287138582L1010,134.94063264751202'
        graphLine2 == 'M0,200.19940179461614L168.33333333333331,189.3229402700988L336.66666666666663,153.06806852170757L505,105.93673524879904L673.3333333333333,80.55832502492518L841.6666666666667,225.57781201848996L1010,80.55832502492518'
    }

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

    void "Graph is shown for correct Location"() {
        given: "User selects NotUsedLocation"
        browserTab.click()
        selectLocationField.click()
        selectLocationList[0].click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        graphLine1 == 'M0,348.84437596302L168.33333333333331,319.8404785643071L336.66666666666663,279.96011964107674L505,240.07976071784645L673.3333333333333,200.19940179461614L841.6666666666667,160.31904287138582L1010,134.94063264751202'
        graphLine2 == 'M0,200.19940179461614L168.33333333333331,189.3229402700988L336.66666666666663,153.06806852170757L505,105.93673524879904L673.3333333333333,80.55832502492518L841.6666666666667,225.57781201848996L1010,80.55832502492518'
    }

    void "Graph is shown for \"Select all Locations\""() {
        given: "User selects NotUsedBrowser"
        browserTab.click()
        selectAllLocationsButton.click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        graphLine1 == 'M0,348.84437596302L168.33333333333331,319.8404785643071L336.66666666666663,279.96011964107674L505,240.07976071784645L673.3333333333333,200.19940179461614L841.6666666666667,160.31904287138582L1010,134.94063264751202'
        graphLine2 == 'M0,200.19940179461614L168.33333333333331,189.3229402700988L336.66666666666663,153.06806852170757L505,105.93673524879904L673.3333333333333,80.55832502492518L841.6666666666667,225.57781201848996L1010,80.55832502492518'
    }

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

    void "Graph is shown for correct Connectivity Profile"() {
        given: "User selects NotUsedBrowser"
        connectivityTab.click()
        selectConnectivityProfilesList[0].click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        graphLine1 == 'M0,348.84437596302L168.33333333333331,319.8404785643071L336.66666666666663,279.96011964107674L505,240.07976071784645L673.3333333333333,200.19940179461614L841.6666666666667,160.31904287138582L1010,134.94063264751202'
        graphLine2 == 'M0,200.19940179461614L168.33333333333331,189.3229402700988L336.66666666666663,153.06806852170757L505,105.93673524879904L673.3333333333333,80.55832502492518L841.6666666666667,225.57781201848996L1010,80.55832502492518'
    }

    void "Graph is shown for \"Select all Connectivity Profiles\""() {
        given: "User selects NotUsedBrowser"
        connectivityTab.click()
        selectAllConnectivityButton.click()

        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        graphLine1 == 'M0,348.84437596302L168.33333333333331,319.8404785643071L336.66666666666663,279.96011964107674L505,240.07976071784645L673.3333333333333,200.19940179461614L841.6666666666667,160.31904287138582L1010,134.94063264751202'
        graphLine2 == 'M0,200.19940179461614L168.33333333333331,189.3229402700988L336.66666666666663,153.06806852170757L505,105.93673524879904L673.3333333333333,80.55832502492518L841.6666666666667,225.57781201848996L1010,80.55832502492518'
    }

    void "Graph for \"Daily mean per Page\""() {
        given: "User selects appropriate timeframe, aggregation type, job group and page"
        aggregationRadioButtons.aggrGroupAndInterval = "daily_page"
        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        graphLine1 == 'M0,73.307350675247L1010,73.307350675247'
        graphLine2 == 'M420.83333333333337,348.84437596302L505,319.8404785643071L589.1666666666667,279.96011964107674L673.3333333333333,240.07976071784645L757.5,200.19940179461614L841.6666666666667,160.31904287138582L925.8333333333334,134.94063264751202'
        graphLine3 == 'M420.83333333333337,200.19940179461614L505,189.3229402700988L589.1666666666667,153.06806852170757L673.3333333333333,105.93673524879904L757.5,80.55832502492518L841.6666666666667,225.57781201848996L925.8333333333334,80.55832502492518'
    }

    void "Graph for \"Weekly mean per Page\""() {
        given: "User selects appropriate timeframe, aggregation type, job group and page"
        aggregationRadioButtons.aggrGroupAndInterval = "weekly_page"
        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        graphLine1 == 'M0,73.307350675247L1010,73.307350675247'
        graphLine2 == 'M505,277.7848273361733L946.875,158.25251518172757'
        graphLine3 == 'M505,145.81709417202933L946.875,135.44820085198944'
    }

    void "Graph for \"Daily mean per Job Group\""() {
        given: "User selects appropriate timeframe, aggregation type, job group and page"
        aggregationRadioButtons.aggrGroupAndInterval = "daily_shop"
        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        graphLine1 == 'M0,73.307350675247L1010,73.307350675247'
        graphLine2 == 'M432.8571428571429,348.84437596302L505,319.8404785643071L577.1428571428572,279.96011964107674L649.2857142857143,240.07976071784645L721.4285714285714,200.19940179461614L793.5714285714286,160.31904287138582L865.7142857142858,134.94063264751202'
        graphLine3 == 'M432.8571428571429,200.19940179461614L505,189.3229402700988L577.1428571428572,153.06806852170757L649.2857142857143,105.93673524879904L721.4285714285714,80.55832502492518L793.5714285714286,225.57781201848996L865.7142857142858,80.55832502492518'
    }

    void "Graph for \"Weekly mean per Job Group\""() {
        given: "User selects appropriate timeframe, aggregation type, job group and page"
        aggregationRadioButtons.aggrGroupAndInterval = "weekly_shop"
        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        graphLine1 == 'M0,73.307350675247L1010,73.307350675247'
        graphLine2 == 'M505,277.7848273361733L897.7777777777778,158.25251518172757'
        graphLine3 == 'M505,145.81709417202933L897.7777777777778,135.44820085198944'
    }

    void "Graph for \"Daily mean per CSI System\""() {
        given: "User selects appropriate timeframe, aggregation type, job group and page"
        waitFor { aggregationRadioButtons.displayed }
        aggregationRadioButtons.aggrGroupAndInterval = "daily_system"
        csiSystem[0].click()
        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        graphLine1 == 'M0,73.307350675247L1010,73.307350675247'
        graphLine2 == 'M432.8571428571429,348.84437596302L505,319.8404785643071L577.1428571428572,279.96011964107674L649.2857142857143,240.07976071784645L721.4285714285714,200.19940179461614L793.5714285714286,160.31904287138582L865.7142857142858,134.94063264751202'
        graphLine3 == 'M432.8571428571429,200.19940179461614L505,189.3229402700988L577.1428571428572,153.06806852170757L649.2857142857143,105.93673524879904L721.4285714285714,80.55832502492518L793.5714285714286,225.57781201848996L865.7142857142858,80.55832502492518'
    }

    void "Graph for \"Weekly mean per CSI System\""() {
        given: "User selects appropriate timeframe, aggregation type, job group and page"
        aggregationRadioButtons.aggrGroupAndInterval = "weekly_system"
        when: "User clicks on \"Show\" button"
        waitFor { showButton.displayed }
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        graphLine1 == 'M0,73.307350675247L1010,73.307350675247'
        graphLine2 == 'M505,277.7848273361733L897.7777777777778,158.25251518172757'
        graphLine3 == 'M505,145.81709417202933L897.7777777777778,135.44820085198944'
    }

    void "Adjust Chart Title"() {
        given: "User opens Adjust Chart"
        clickAdjustChartAccordion()

        when: "User edits title"
        waitFor { chartTitleInputField.displayed }
        sleep(100)
        chartTitleInputField << Keys.chord(Keys.CONTROL, "a")
        chartTitleInputField << Keys.chord(Keys.DELETE)
        chartTitleInputField << "CustomTitle"

        then: "Chart title is changed"
        waitFor { chartTitle == "CustomTitle" }
    }


    void "Adjust Chart Size"() {
        given: "User edits chart size"
        chartWidthInputField << Keys.chord(Keys.CONTROL, "a")
        chartWidthInputField << Keys.chord(Keys.DELETE)
        chartWidthInputField << "600"
        chartheightInputField << Keys.chord(Keys.CONTROL, "a")
        chartheightInputField << Keys.chord(Keys.DELETE)
        chartheightInputField << "600"

        when: "User clicks \"apply\""
        waitFor { diaChangeChartsizeButton.displayed }
        diaChangeChartsizeButton.click()

        then: "Chart changed"
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine1 == "M0,109.96102601287049L540,109.96102601287049" }
        waitFor { graphLine2 == "M270,416.67724100425994L480.00000000000006,237.37877277259133" }
        waitFor { graphLine3 == "M270,218.725641258044L480.00000000000006,203.17230127798416" }
    }


    void "Adjust Chart Section"() {
        given: "User edits chart size"
        diaYAxisMinInputField << Keys.chord(Keys.CONTROL, "a")
        diaYAxisMinInputField << Keys.chord(Keys.DELETE)
        diaYAxisMinInputField << "70"
        diaYAxisMaxInputField << Keys.chord(Keys.CONTROL, "a")
        diaYAxisMaxInputField << Keys.chord(Keys.DELETE)
        diaYAxisMaxInputField << "80"

        when: "User clicks \"apply\""
        waitFor { diaChangeYAxisButton.displayed }
        diaChangeYAxisButton.click()

        then: "Chart changed"
        waitFor { graphLineDiv[2].displayed }
        waitFor { graphLine2 == "M270,2776.8693918245267L480.00000000000006,804.5862412761719" }
    }

    void "Enable Data-Markers"() {

        when: "User clicks \"Show data-marker\""
        waitFor { showDataMarkersCheckBox.displayed }
        showDataMarkersCheckBox.click()
        showDataMarkersCheckBox.click()

        then: "Data-markers show on the graph"
        waitFor { dataMarker }
        waitFor { dataMarker.attr("style").contains("top: 600px; left: 270px;") }
    }

    void "Enable Data-Labels"() {

        when: "User clicks \"Show data-marker\""
        waitFor { showDataLabelsCheckBox.displayed }
        showDataLabelsCheckBox.click()

        then: "Data-markers show on the graph"
        waitFor { dataLabel }
        waitFor {
            dataLabel.attr("style").contains('top: 595px; left: 261px; height: 100px; width: 100px; font-size: 13pt; font-weight: bold; color: rgb(179, 179, 179); cursor: default;')
        }
    }


    void "Save custom dashboard"() {
        given: "User clicked on \"Save as dashboard\"-button"
        clickSaveAsDashboardButton()

        when: "User enters new name for the dashboard"
        waitFor { dashboardNameFromModalTextField.displayed }
        dashboardNameFromModalTextField << "CustomDashboard"
        waitFor { saveDashboardButtonButton.displayed }
        sleep(100)
        saveDashboardButtonButton.click()

        then: "Success Message is displayed"
        at CsiDashboardPage
        waitFor { saveDashboardSuccessMessage.displayed }
        waitFor { graphLineDiv[2].displayed }
        waitFor { graphLine2 == "M270,2776.8693918245267L480.00000000000006,804.5862412761719" }

    }

    @Ignore
    void "Load custom dashboard"() {
        given: "User visits the EventResultDashboardPage"
        to CsiDashboardPage

        when: "User loads CustomDashboard"
        customDashboardSelectionDropdown.click()
        waitFor { customDashboardSelectionList.displayed }
        customDashboardSelectionList.click()
        then: "The old dashboard is loaded again"
        at CsiDashboardPage
        waitFor { graphLineDiv[2].displayed }
        waitFor { graphLine2 == "M270,2776.8693918245267L480.00000000000006,804.5862412761719" }
        waitFor { dataLabel }
        waitFor {
            dataLabel.attr("style").contains('top: 595px; left: 261px; height: 100px; width: 100px; font-size: 13pt; font-weight: bold; color: rgb(179, 179, 179); cursor: default;')
        }
        waitFor { dataMarker }
        waitFor { dataMarker.attr("style").contains("top: 600px; left: 270px;") }
        chartTitle == "CustomTitle"
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
        Browser browser = TestDataUtil.createBrowser("TestFireFox", 1d)
        ConnectivityProfile connectivityProfile = TestDataUtil.createConnectivityProfile(connectivityProfileName)
        BrowserConnectivityWeight browserConnectivityWeight = TestDataUtil.createBrowserConnectivityWeight(browser, connectivityProfile, 2)
        Page page1 = TestDataUtil.createPage(page1Name, 1.0)
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
        JobResult jobResult1 = TestDataUtil.createJobResult("Test1", new DateTime(2016, 06, 22, 5, 13).toDate(), job1, location1)
        MeasuredEvent measuredEvent1 = TestDataUtil.createMeasuredEvent(measureEvent1Name, page1)
        CsiAggregationInterval hourly = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
        AggregatorType aggregatorType = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
        new CsiAggregation([started: new DateTime(2016, 6, 5, 9, 10).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup: jobGroup1, measuredEvent: measuredEvent1, page: page1, browser: browser, location: location1, csByWptDocCompleteInPercent: 14, csByWptVisuallyCompleteInPercent: 55, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 6, 9, 10).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup:  jobGroup1, measuredEvent:  measuredEvent1, page:  page1, browser:  browser, location:  location1, csByWptDocCompleteInPercent: 22, csByWptVisuallyCompleteInPercent: 58, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 7, 9, 10).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup:  jobGroup1, measuredEvent:  measuredEvent1, page:  page1, browser:  browser, location:  location1, csByWptDocCompleteInPercent: 33, csByWptVisuallyCompleteInPercent: 68, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 8, 9, 10).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup:  jobGroup1, measuredEvent:  measuredEvent1, page:  page1, browser:  browser, location:  location1, csByWptDocCompleteInPercent: 44, csByWptVisuallyCompleteInPercent: 81, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 9, 9, 10).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup:  jobGroup1, measuredEvent:  measuredEvent1, page:  page1, browser:  browser, location:  location1, csByWptDocCompleteInPercent: 55, csByWptVisuallyCompleteInPercent: 88, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 10, 9, 10).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup:  jobGroup1, measuredEvent:  measuredEvent1, page:  page1, browser:  browser, location:  location1, csByWptDocCompleteInPercent: 66, csByWptVisuallyCompleteInPercent: 48, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 11, 9, 10).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup:  jobGroup1, measuredEvent:  measuredEvent1, page:  page1, browser:  browser, location:  location1, csByWptDocCompleteInPercent: 73, csByWptVisuallyCompleteInPercent: 88, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 12, 9, 10).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup:  jobGroup1, measuredEvent:  measuredEvent1, page:  page1, browser:  browser, location:  location1, csByWptDocCompleteInPercent: 24, csByWptVisuallyCompleteInPercent: 98, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 13, 9, 10).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup:  jobGroup1, measuredEvent:  measuredEvent1, page:  page1, browser:  browser, location:  location1, csByWptDocCompleteInPercent: 39, csByWptVisuallyCompleteInPercent: 65, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 14, 9, 10).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup:  jobGroup1, measuredEvent:  measuredEvent1, page:  page1, browser:  browser, location:  location1, csByWptDocCompleteInPercent: 77, csByWptVisuallyCompleteInPercent: 61, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 15, 9, 10).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup:  jobGroup1, measuredEvent:  measuredEvent1, page:  page1, browser:  browser, location:  location1, csByWptDocCompleteInPercent: 88, csByWptVisuallyCompleteInPercent: 72, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 16, 9, 10).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup:  jobGroup1, measuredEvent:  measuredEvent1, page:  page1, browser:  browser, location:  location1, csByWptDocCompleteInPercent: 99, csByWptVisuallyCompleteInPercent: 78, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 17, 9, 10).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup:  jobGroup1, measuredEvent:  measuredEvent1, page:  page1, browser:  browser, location:  location1, csByWptDocCompleteInPercent: 1, csByWptVisuallyCompleteInPercent: 84, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        new CsiAggregation([started: new DateTime(2016, 6, 18, 9, 10).toDate(), interval: hourly, aggregator: aggregatorType, jobGroup:  jobGroup1, measuredEvent:  measuredEvent1, page:  page1, browser:  browser, location:  location1, csByWptDocCompleteInPercent: 31, csByWptVisuallyCompleteInPercent: 88, underlyingEventResultsByWptDocComplete: jobResult1.id as String, closedAndCalculated: true, connectivityProfile: connectivityProfile]).save(failOnError: true)
        Browser notUsedBrowser = TestDataUtil.createBrowser("NotUsedBrowser", 0)
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
        }
    }
}
