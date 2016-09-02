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
class EventResultDashboardGebSpec extends CustomUrlGebReportingSpec implements OsmTestLogin {

    CsiAggregationTagService csiAggregationTagService
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

    void "Nothing selected causes error-message"() {
        given: "Data is available but neither page nor jobgroup is selected"
        to EventResultDashboardPage

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "The system shows him a helpful error message"
        waitFor { at EventResultDashboardPage }
        waitFor {
            $("div", class: "alert alert-error")[0].attr("innerHTML").contains("Please check your selection, you made the following mistakes:")
        } //check that the error box appears
        waitFor {
            $("div", class: "alert alert-error")[0].find("li")[0].attr("innerHTML").contains("Please select at least one system")
        } //check that the correct error message is displayed
        waitFor {
            $("div", class: "alert alert-error")[0].find("li")[1].attr("innerHTML").contains("Please select at least one page")
        } //check that the correct error message is displayed
    }

    void "Wrong timeframe causes no data available message"() {
        given: "User selects valid timeframe, page and jobgroup"
        to EventResultDashboardPage
        timeFrameSelect.click()
        selectDateInDatepicker(fromDatepicker, "21.06.2015")
        selectDateInDatepicker(toDatepicker, "23.06.2015")
        jobGroupList[0].click()
        pageList[0].click()

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor {$("#noDataForCurrentSelectionWarning").attr("innerHTML").contains("No data available for your selection.")}
    }

    void "Valid selection graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        to EventResultDashboardPage
        timeFrameSelect.click()
        selectDateInDatepicker(fromDatepicker, "21.06.2016")
        selectDateInDatepicker(toDatepicker, "23.06.2016")
        jobGroupList[0].click()
        pageList[0].click()

        when: "User wants to see a graph"

        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,1.7471395337794888L404,286.60684612828175L1010,96.70037506528024" }
        waitFor { graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1' }
    }
    void "NotUsedBrowser leads to no data"(){
        given: "User selects NotUsedBrowser"

        clickFilterJobAccordion()
        browserTab.click()
        waitFor {selectAllBrowserButton.click()}
        waitFor {selectBrowsersList.displayed}
        selectBrowsersList[0].click()

        when: "User clicks on \"Show\" button"
        waitFor{showButton.displayed}
        sleep(100)

        showButton.click()

        then: "No Data Warning is displayed"
        waitFor {$("#noDataForCurrentSelectionWarning").attr("innerHTML").contains("No data available for your selection.")}
    }
    void "Graph is shown for correct Browser"(){
        given: "User selects NotUsedBrowser"
        clickFilterJobAccordion()
        browserTab.click()
        waitFor {selectBrowsersList.displayed}
        selectBrowsersList[1].click()

        when: "User clicks on \"Show\" button"
        waitFor{showButton.displayed}
        sleep(100)
        showButton.click()

        then: "Graphs are displayed"

        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,1.7471395337794888L404,286.60684612828175L1010,96.70037506528024" }
        waitFor { graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1' }
    }

    void "Graph is shown for \"Select all Browsers\""(){
        given: "User selects NotUsedBrowser"
        clickFilterJobAccordion()
        browserTab.click()
        waitFor {selectAllBrowserButton.displayed}
        selectAllBrowserButton.click()

        when: "User clicks on \"Show\" button"
        waitFor{showButton.displayed}
        sleep(100)
        showButton.click()

        then: "Graphs are displayed"

        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,1.7471395337794888L404,286.60684612828175L1010,96.70037506528024" }
        waitFor { graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1' }
    }

    void "NotUsedLocation leads to no data"(){
        given: "User selects NotUsedLocation"
        clickFilterJobAccordion()
        browserTab.click()
        waitFor {selectAllLocationsButton.displayed}
        selectAllLocationsButton.click()
        waitFor {selectLocationField.displayed}
        selectLocationField.click()
        waitFor { selectLocationList[0].displayed }
        selectLocationList[0].click()

        when: "User clicks on \"Show\" button"
        waitFor{showButton.displayed}
        sleep(100)
        showButton.click()

        then: "No Data Warning is displayed"
        waitFor {$("#noDataForCurrentSelectionWarning").attr("innerHTML").contains("No data available for your selection.")}
    }

    void "Graph is shown for correct Location"(){
        given: "User selects NotUsedLocation"
        clickFilterJobAccordion()
        browserTab.click()
        waitFor {selectLocationField.displayed}
        selectLocationField.click()
        waitFor {selectLocationList[0].displayed}
        selectLocationList[0].click()

        when: "User clicks on \"Show\" button"
        waitFor{showButton.displayed}
        sleep(100)
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,1.7471395337794888L404,286.60684612828175L1010,96.70037506528024" }
        waitFor { graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1' }
    }

    void "Graph is shown for \"Select all Locations\""(){
        given: "User selects NotUsedBrowser"
        clickFilterJobAccordion()
        browserTab.click()
        waitFor {selectAllLocationsButton.displayed}
        selectAllLocationsButton.click()

        when: "User clicks on \"Show\" button"
        waitFor{showButton.displayed}
        sleep(100)
        showButton.click()

        then: "Graphs are displayed"

        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,1.7471395337794888L404,286.60684612828175L1010,96.70037506528024" }
        waitFor { graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1' }
    }

    void "NotUsedConnectivity leads to no data"(){
        given: "User selects NotUsedBrowser"
        clickFilterJobAccordion()
        connectivityTab.click()
        waitFor {selectAllConnectivityButton.displayed}
        selectAllConnectivityButton.click()
        waitFor {selectConnectivityProfilesList.displayed}
        selectConnectivityProfilesList[1].click()

        when: "User clicks on \"Show\" button"
        waitFor{showButton.displayed}
        sleep(100)
        showButton.click()

        then: "No Data Warning is displayed"
        waitFor {$("#noDataForCurrentSelectionWarning").attr("innerHTML").contains("No data available for your selection.")}
    }

    void "Graph is shown for correct Connectivity Profile"(){
        given: "User selects NotUsedBrowser"
        clickFilterJobAccordion()
        connectivityTab.click()
        waitFor {selectConnectivityProfilesList.displayed}
        selectConnectivityProfilesList[0].click()

        when: "User clicks on \"Show\" button"
        waitFor{showButton.displayed}
        sleep(100)
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,1.7471395337794888L404,286.60684612828175L1010,96.70037506528024" }
        waitFor { graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1' }
    }
    void "Graph is shown for native connectivity"(){
        given: "User selects native connectivity"
        clickFilterJobAccordion()
        connectivityTab.click()
        waitFor {includeNativeConnectivityButton.displayed}
        includeNativeConnectivityButton.click()

        when: "User clicks on \"Show\" button"
        waitFor{showButton.displayed}
        sleep(100)
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,1.7471395337794888L404,286.60684612828175L1010,96.70037506528024" }
        waitFor { graphLine2 == "M404,324.5881403408821L1010,277.11152257513174" }

        cleanup:
        clickFilterJobAccordion()
        connectivityTab.click()
        waitFor {includeNativeConnectivityButton.displayed}
        includeNativeConnectivityButton.click()
        pageTab.click()
        clickFilterJobAccordion()
    }

    void "Graph is shown for custom connectivity"(){
        given: "User selects custom connectivity"
        clickFilterJobAccordion()
        connectivityTab.click()
        waitFor {includeCustomConnectivityButton.displayed}
        includeCustomConnectivityButton.click()

        when: "User clicks on \"Show\" button"
        waitFor{showButton.displayed}
        sleep(100)
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,1.7471395337794888L404,286.60684612828175L1010,96.70037506528024" }
        waitFor { graphLine2 == "M404,182.15828704363096L1010,134.6816692778806" }

        cleanup:
        clickFilterJobAccordion()
        connectivityTab.click()
        waitFor {includeCustomConnectivityButton.displayed}
        includeCustomConnectivityButton.click()
        pageTab.click()
        clickFilterJobAccordion()
    }

    void "Graph is shown for \"Select all Connectivity Profiles\""(){
        given: "User selects NotUsedBrowser"
        clickFilterJobAccordion()
        connectivityTab.click()
        waitFor {selectAllConnectivityButton.displayed}
        selectAllConnectivityButton.click()

        when: "User clicks on \"Show\" button"
        waitFor{showButton.displayed}
        sleep(100)
        showButton.click()

        then: "Graphs are displayed"
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,1.7471395337794888L404,286.60684612828175L1010,96.70037506528024" }
        waitFor { graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1' }
    }

    void "Trimm minimal time"() {
        given: "User defines minimal load time"
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }
        clickChooseMeasuredVariablesAccordion()
        waitFor { appendedInputBelowLoadTimesTextField.displayed }
        appendedInputBelowLoadTimesTextField << "250"

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,1.7471395337794888L1010,96.70037506528024" }
        waitFor {
            graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        waitFor { appendedInputBelowLoadTimesTextField.displayed }
        appendedInputBelowLoadTimesTextField << Keys.chord(Keys.CONTROL, "a")
        appendedInputBelowLoadTimesTextField << Keys.chord(Keys.DELETE)

    }

    void "Trimm maximal time"() {
        given: "User defines maximal load time"
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }
        clickChooseMeasuredVariablesAccordion()
        waitFor { appendedInputAboveLoadTimesTextField.displayed }
        appendedInputAboveLoadTimesTextField << "830"

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,251.29611166500496L1010,2.0438683948154335" }
        waitFor {
            graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        waitFor { appendedInputAboveLoadTimesTextField.displayed }
        appendedInputAboveLoadTimesTextField << Keys.chord(Keys.CONTROL, "a")
        appendedInputAboveLoadTimesTextField << Keys.chord(Keys.DELETE)
    }

    void "Trimm minimal requests"() {
        given: "User defines maximal request count"
        waitFor { appendedInputBelowRequestCountsTextField.displayed }
        appendedInputBelowRequestCountsTextField << "3"
        firstViewList[3].click()
        firstViewList[7].click()

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,80.55832502492513L1010,0.7976071784646024" }
        waitFor {
            graphName == 'fv requests doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        waitFor { appendedInputBelowRequestCountsTextField.displayed }
        appendedInputBelowRequestCountsTextField << Keys.chord(Keys.CONTROL, "a")
        appendedInputBelowRequestCountsTextField << Keys.chord(Keys.DELETE)
    }

    void "Trimm maximal requests"() {
        given: "User defines maximal request count"
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }
        clickChooseMeasuredVariablesAccordion()
        waitFor { appendedInputAboveRequestCountsTextField.displayed }
        appendedInputAboveRequestCountsTextField << "5"

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,80.55832502492513L1010,160.3190428713858" }
        waitFor {
            graphName == 'fv requests doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        waitFor { appendedInputAboveRequestCountsTextField.displayed }
        appendedInputAboveRequestCountsTextField << Keys.chord(Keys.CONTROL, "a")
        appendedInputAboveRequestCountsTextField << Keys.chord(Keys.DELETE)
    }

    void "Trimm minimal size"() {
        given: "User defines minimal bytes until doc"
        waitFor { appendedInputBelowRequestSizesTimesTextField.displayed }
        appendedInputBelowRequestSizesTimesTextField << "30"
        firstViewList[7].click()
        firstViewList[9].click()

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,3.5839149219008846L1010,16.877367896310943" }
        waitFor {
            graphName == 'fv docCompleteIncomingBytesUncached | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        waitFor { appendedInputBelowRequestSizesTimesTextField.displayed }
        appendedInputBelowRequestSizesTimesTextField << Keys.chord(Keys.CONTROL, "a")
        appendedInputBelowRequestSizesTimesTextField << Keys.chord(Keys.DELETE)
    }

    void "Trimm maximal size"() {
        given: "User defines maximal bytes until doc"
        waitFor { appendedInputAboveRequestSizesTextField.displayed }
        appendedInputAboveRequestSizesTextField << "73"

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,282.7464273845131L1010,16.877367896310943" }
        waitFor {
            graphName == 'fv docCompleteIncomingBytesUncached | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        waitFor { appendedInputAboveRequestSizesTextField.displayed }
        appendedInputAboveRequestSizesTextField << Keys.chord(Keys.CONTROL, "a")
        appendedInputAboveRequestSizesTextField << Keys.chord(Keys.DELETE)
    }


    void "Load time graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        firstViewList[9].click()
        firstViewList[0].click()

        when: "User wants to see a graph"


        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,1.7471395337794888L404,273.3133931538717L1010,225.8367753881213" }
        waitFor {
            graphName == 'fv load time | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        firstViewList[0].click()
    }


    void "Time to first byte graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }
        clickChooseMeasuredVariablesAccordion()
        firstViewList[1].click()

        when: "User wants to see a graph"

        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,0.7976071784646024L404,275.268312708932L1010,40.677966101694835" }
        waitFor {
            graphName == 'fv ttfb | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        firstViewList[1].click()

    }

    void "Start render time graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }
        clickChooseMeasuredVariablesAccordion()
        firstViewList[2].click()

        when: "User wants to see a graph"

        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,130.93351524374242L404,0.7976071784646024L1010,140.72869111962353" }
        waitFor {
            graphName == 'fv start render | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        firstViewList[2].click()
    }

    void "Visually complete graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"

        waitFor { chooseMeasuredVariablesAccordionContent.displayed }
        clickChooseMeasuredVariablesAccordion()
        firstViewList[4].click()

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,106.69460544918911L404,339.66800164478315L1010,1.698858227577091" }
        waitFor {
            graphName == 'fv visually complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        firstViewList[4].click()
    }

    void "Dom time graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }
        clickChooseMeasuredVariablesAccordion()
        firstViewList[5].click()

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,133.7321369225656L404,266.66666666666663L1010,0.7976071784646024" }
        waitFor {
            graphName == 'fv dom time | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        firstViewList[5].click()
    }

    void "Fully loaded time graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }
        clickChooseMeasuredVariablesAccordion()
        firstViewList[6].click()

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,1.264955134596164L404,243.03963110667993L1010,87.25697906281152" }
        waitFor {
            graphName == 'fv fully loaded | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        firstViewList[6].click()

    }

    void "Count of request to doc complete graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }
        clickChooseMeasuredVariablesAccordion()
        firstViewList[7].click()

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,80.55832502492513L404,160.3190428713858L1010,0.7976071784646024" }
        waitFor {
            graphName == 'fv requests doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        firstViewList[7].click()
    }

    void "Count of requestion to fully loaded graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }
        clickChooseMeasuredVariablesAccordion()
        firstViewList[8].click()

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,16.749750747756707L404,192.22333000997006L1010,112.46261216350945" }
        waitFor {
            graphName == 'fv requests fully loaded | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        firstViewList[8].click()
    }

    void "Bytes until doc complete graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }
        clickChooseMeasuredVariablesAccordion()
        firstViewList[9].click()

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,3.5839149219008846L404,282.7464273845131L1010,16.877367896310943" }
        waitFor {
            graphName == 'fv docCompleteIncomingBytesUncached | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        firstViewList[9].click()
    }

    void "Bytes until fully loaded graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }
        clickChooseMeasuredVariablesAccordion()
        firstViewList[10].click()

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,1.9616825200074572L404,349.24415941365095L1010,112.11770095120033" }
        waitFor {
            graphName == 'fv fullyLoadedIncomingBytesUncached | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        firstViewList[10].click()
    }

    void "Customer satisfaction (visually complete) graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }
        clickChooseMeasuredVariablesAccordion()
        firstViewList[11].click()

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,80.55832502492518L404,316.21499138946797L1010,171.19550439590316" }
        waitFor {
            graphName == 'fv customer satisfaction (visually complete) | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        firstViewList[11].click()
    }

    void "Customer satisfaction (doc complete) graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }
        clickChooseMeasuredVariablesAccordion()
        firstViewList[12].click()

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,38.44575364814642L404,313.9827789359195L1010,132.7084201939635" }
        waitFor {
            graphName == 'fv customer satisfaction (docComplete) | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }

        cleanup:
        clickChooseMeasuredVariablesAccordion()
        firstViewList[12].click()
    }

    void "Speed index graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        waitFor { chooseMeasuredVariablesAccordionContent.displayed }
        clickChooseMeasuredVariablesAccordion()
        firstViewList[13].click()

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,2.758936305836528L404,218.50514031675462L1010,22.372227579556352" }
        waitFor {
            graphName == 'fv speed index | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'
        }
    }

    void "Adjust Chart Title"() {
        given: "User opens Adjust Chart"
        clickAdjustChartAccordion()

        when: "User edits title"
        waitFor { chartTitleInputField.displayed }
        sleep(100)
        chartTitleInputField << "CustomTitle"

        then: "Chart title is changed"
        waitFor { chartTitle == "CustomTitle" }
    }

    void "Adjust Chart Size to illigal values"() {
        given: "User edits chart size"
        chartWidthInputField << Keys.chord(Keys.CONTROL, "a")
        chartWidthInputField << Keys.chord(Keys.DELETE)
        chartWidthInputField << "0"
        chartheightInputField << Keys.chord(Keys.CONTROL, "a")
        chartheightInputField << Keys.chord(Keys.DELETE)
        chartheightInputField << "9999999"

        when: "User clicks \"apply\""
        sleep(100)
        def result = withAlert{diaChangeChartsizeButton.click()}

        then: "Error message is shown"
        result== "Width and height of diagram must be numeric values. Maximum is 5.000 x 3.000 pixels, minimum width is 540 pixels."
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
        waitFor { graphLine == "M0,4.138404458754849L216,327.75771047513194L540,33.558341369334585" }
    }

    //TODO:
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

    //TODO:
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


    void "Adjust Chart Section"() {
        given: "User edits chart size"
        diaYAxisMinInputField << Keys.chord(Keys.CONTROL, "a")
        diaYAxisMinInputField << Keys.chord(Keys.DELETE)
        diaYAxisMinInputField << "200"
        diaYAxisMaxInputField << Keys.chord(Keys.CONTROL, "a")
        diaYAxisMaxInputField << Keys.chord(Keys.DELETE)
        diaYAxisMaxInputField << "600"

        when: "User clicks \"apply\""
        waitFor { diaChangeYAxisButton.displayed }
        diaChangeYAxisButton.click()

        then: "Chart changed"
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,-9.272183449651038L216,484.247258225324L540,35.593220338982974" }
    }

    void "Enable Data-Markers"() {

        when: "User clicks \"Show data-marker\""
        waitFor { showDataMarkersCheckBox.displayed }
        showDataMarkersCheckBox.click()

        then: "Data-markers show on the graph"
        waitFor { dataMarker }
        waitFor { dataMarker.attr("style").contains("top: 484.5px; left: 216px;") }
    }

    void "Enable Data-Labels"() {

        when: "User clicks \"Show data-marker\""
        waitFor { showDataLabelsCheckBox.displayed }
        showDataLabelsCheckBox.click()

        then: "Data-markers show on the graph"
        waitFor { dataLabel }
        waitFor {
            dataLabel.attr("style").contains('top: 479px; left: 207px; height: 100px; width: 100px; font-size: 13pt; font-weight: bold; color: rgb(179, 179, 179); cursor: default')
        }
    }


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
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,-9.272183449651038L216,484.247258225324L540,35.593220338982974" }
    }

    @Ignore
    void "Load custom dashboard"() {
        given: "User visits the EventResultDashboardPage"
        to EventResultDashboardPage
        when: "User loads CustomDashboard"
        customDashboardSelectionDropdown.click()
        waitFor { customDashboardSelectionList.displayed }
        customDashboardSelectionList.find("a").click()
        then: "The old dashboard is loaded again"
        at EventResultDashboardPage
        waitFor { graphLineDiv.displayed }
        waitFor { graphLine == "M0,-9.272183449651038L216,484.247258225324L540,35.593220338982974" }
        waitFor { dataLabel }
        waitFor {
            dataLabel.attr("style").contains('top: 479px; left: 207px; height: 100px; width: 100px; font-size: 13pt; font-weight: bold; color: rgb(179, 179, 179); cursor: default; fill: rgb(179, 179, 179);')
        }
        waitFor { dataMarker }
        waitFor { dataMarker.attr("style").contains("top: 484.5px; left: 216px;") }
        chartTitle == "CustomTitle"
    }


    private cleanUpData() {
        doLogout()
        Job.withNewTransaction {
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
            Browser browser = TestDataUtil.createBrowser("This is the very best browser I've ever seen", 1d)
            Location location1 = TestDataUtil.createLocation(wpt, location1Name, browser, true)
            Location location2 = TestDataUtil.createLocation(wpt, location2Name, browser, true)
            Job job1 = TestDataUtil.createJob(job1Name, script1, location1, jobGroup1, "This is the first test job", 1, false, 12)
            Job job2 = TestDataUtil.createJob(job2Name, script2, location2, jobGroup1, "This is the second test job", 1, false, 12)
            Page page1 = TestDataUtil.createPage(page1Name, 1.0)
            JobResult jobResult1 = TestDataUtil.createJobResult("Test1", new DateTime(2016, 06, 22, 5, 13).toDate(), job1, location1)
            JobResult jobResult2 = TestDataUtil.createJobResult("Test2", new DateTime(2016, 06, 22, 5, 18).toDate(), job1, location1)
            JobResult jobResult3 = TestDataUtil.createJobResult("Test3", new DateTime(2016, 06, 22, 5, 15).toDate(), job1, location1)
            ConnectivityProfile connectivityProfile = TestDataUtil.createConnectivityProfile(connectivityProfileName)
            MeasuredEvent measuredEvent1 = TestDataUtil.createMeasuredEvent(measureEvent1Name, page1)

            Browser notUsedBrowser = TestDataUtil.createBrowser("NotUsedBrowser",0)
            TestDataUtil.createConnectivityProfile("NotUsedConnectivityProfile")
            TestDataUtil.createLocation(wpt,"NotUsedLocation",notUsedBrowser, true)

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
                    tag: csiAggregationTagService.createEventResultTag(jobGroup1, measuredEvent1, measuredEvent1.testedPage, browser, location1)
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
                    tag: csiAggregationTagService.createEventResultTag(jobGroup1, measuredEvent1, measuredEvent1.testedPage, browser, location1)
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
                    tag: csiAggregationTagService.createEventResultTag(jobGroup1, measuredEvent1, measuredEvent1.testedPage, browser, location1)
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
                    tag: csiAggregationTagService.createEventResultTag(jobGroup1, measuredEvent1, measuredEvent1.testedPage, browser, location1)
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
                    tag: csiAggregationTagService.createEventResultTag(jobGroup1, measuredEvent1, measuredEvent1.testedPage, browser, location1)
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
                    tag: csiAggregationTagService.createEventResultTag(jobGroup1, measuredEvent1, measuredEvent1.testedPage, browser, location1)
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
                    tag: csiAggregationTagService.createEventResultTag(jobGroup1, measuredEvent1, measuredEvent1.testedPage, browser, location1)
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