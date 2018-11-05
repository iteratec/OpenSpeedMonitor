package geb.de.iteratec.osm.result

import de.iteratec.osm.OsmConfiguration
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
import de.iteratec.osm.result.*
import de.iteratec.osm.security.Role
import de.iteratec.osm.security.User
import de.iteratec.osm.security.UserRole
import geb.CustomUrlGebReportingSpec
import geb.pages.de.iteratec.osm.result.EventResultDashboardPage
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.openqa.selenium.Keys
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * Created by marko on 22.06.16.
 */
@Integration
@Rollback
@Stepwise
class EventResultDashboardGebSpec extends CustomUrlGebReportingSpec {
    @Shared
    String location1Name = "TestLocation1-564892#Afef1"
    @Shared
    String Location2Name = "TestLocation2-564892#Afef1"
    @Shared
    String jobGroup1Name = "TestJobGroup1-564892#Afef1"
    @Shared
    String connectivityProfileName = "ConnectivityProfile-564892#Afef1"
    @Shared
    String measureEvent1Name = "MeasureEvent1-564892#Afef1"

    void cleanupSpec() {
        cleanUpData()
    }

    void "Wrong timeframe causes no data available message"() {
        given: "User goes to the dashboard page"
        createData()
        to EventResultDashboardPage

        when: "User selects a wrong time frame again"
        selectDateInDatepicker(fromDatepicker, "21.06.2015 00:00")
        selectDateInDatepicker(toDatepicker, "23.06.2015 23:59")

        then: "The selections show only warnings, a no data warning appears, and the show button is disabled"
        at EventResultDashboardPage
        waitFor {
            !$("#warning-no-job-group").displayed
        }
        waitFor {
            !$("#warning-no-page").displayed
        }
        waitFor {
            $("#warning-no-data").displayed
        }
        waitFor {
            pageList.size() == 1
            !pageList[0].enabled
        }
        waitFor {
            selectBrowsersList.size() == 1
            !selectBrowsersList[0].enabled
        }
        waitFor {
            selectConnectivityProfilesList.size() == 1
            !selectConnectivityProfilesList[0].enabled
        }
        waitFor {
            showButton.@disabled
        }
    }

    void "Only valid options and error messages to select job group and page are shown"() {
        given: "User is on dashboard page"
        to EventResultDashboardPage

        when: "User selects valid timeframe"
        selectDateInDatepicker(fromDatepicker, "21.06.2016 00:00")
        selectDateInDatepicker(toDatepicker, "23.06.2016 23:59")

        then:
        waitFor {
            pageList.size() == 1
            pageList[0].enabled
        }
        waitFor {
            selectBrowsersList.size() == 1
            selectBrowsersList[0].enabled
        }
        waitFor {
            selectConnectivityProfilesList.size() == 3
            selectConnectivityProfilesList[0].enabled
        }
        waitFor {
            $("#warning-no-job-group").displayed
        }
        waitFor {
            $("#warning-no-page").displayed
        }
        waitFor {
            !$("#warning-no-data").displayed
        }
        waitFor {
            showButton.@disabled
        }
    }

    void "No page selection warning is shown"() {
        given: "User is on dashboard page"
        to EventResultDashboardPage

        when: "User selects valid time frame and jobgroup"
        selectDateInDatepicker(fromDatepicker, "21.06.2016 00:00")
        selectDateInDatepicker(toDatepicker, "23.06.2016 23:59")
        jobGroupList[0].click()

        then:
        waitFor {
            !$("#warning-no-job-group").displayed
        }
        waitFor {
            $("#warning-no-page").displayed
        }
        waitFor {
            !$("#warning-no-data").displayed
        }
        waitFor {
            showButton.@disabled
        }
    }

    void "The user sees no warning on valid selection"() {
        given: "User is on dashboard page"
        to EventResultDashboardPage

        when: "User selects time frame, job group and page"
        selectDateInDatepicker(fromDatepicker, "21.06.2016 00:00")
        selectDateInDatepicker(toDatepicker, "23.06.2016 23:59")
        jobGroupList[0].click()
        pageList[0].click()

        then:
        waitFor {
            !$("#warning-no-job-group").displayed
        }
        waitFor {
            !$("#warning-no-data").displayed
        }
        waitFor {
            !$("#warning-no-page").displayed
        }
        waitFor {
            !showButton.@disabled
        }
    }

    void "Valid selection graph is shown"() {

        when: "User wants to see the graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.every{ it.displayed } }
        graphLines.size() == 3

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 3
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]
        graphSeries[1].data.collect { [x:it.x, y:it.y]} == [[x:1466565300, y:458], [x:1466565480, y:558]]
        graphSeries[2].data.collect { [x:it.x, y:it.y]} == [[x:1466565300, y:158], [x:1466565480, y:258]]
    }

    void "Graph is shown for native connectivity"(){
        given: "User selects native connectivity"
        connectivityTab.click()
        selectConnectivityProfilesList.find { it.getAttribute("value") == "native" }.click()

        when: "User clicks on \"Show\" button"
        clickShowButton()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 1

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565300, y:158], [x:1466565480, y:258]]
    }

    void "Graph is shown for custom connectivity"(){
        given: "User selects custom connectivity"
        connectivityTab.click()
        selectAllConnectivityButton.click()
        selectConnectivityProfilesList.find { it.getAttribute("value").startsWith("Custom") }.click()

        when: "User clicks on \"Show\" button"
        clickShowButton()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 1

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565300, y:458], [x:1466565480, y:558]]
    }

    void "All graphs are shown for \"Select all Connectivity Profiles\""(){
        given: "User selects NotUsedBrowser"
        connectivityTab.click()
        waitFor {selectAllConnectivityButton.displayed}
        selectAllConnectivityButton.click()

        when: "User clicks on \"Show\" button"
        clickShowButton()

        then: "Graphs are displayed"
        waitFor { graphLines.every { it.displayed } }
        graphLines.size() == 3

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 3
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]
        graphSeries[1].data.collect { [x:it.x, y:it.y]} == [[x:1466565300, y:458], [x:1466565480, y:558]]
        graphSeries[2].data.collect { [x:it.x, y:it.y]} == [[x:1466565300, y:158], [x:1466565480, y:258]]
    }


    void "Graph is shown for correct Connectivity Profile"(){
        given: "User selects NotUsedBrowser"
        connectivityTab.click()
        waitFor {selectConnectivityProfilesList.displayed}
        selectConnectivityProfilesList[0].click()

        when: "User clicks on \"Show\" button"
        clickShowButton()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Document Complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]
    }


    void "Graph is shown for correct Browser"(){
        given: "User selects NotUsedBrowser"
        browserTab.click()
        waitFor {selectBrowsersList.displayed}
        selectBrowsersList[0].click()

        when: "User clicks on \"Show\" button"
        clickShowButton()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Document Complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]

    }

    void "Graph is shown for \"Select all Browsers\""(){
        given: "User selects NotUsedBrowser"
        browserTab.click()
        waitFor {selectAllBrowserButton.displayed}
        selectAllBrowserButton.click()

        when: "User clicks on \"Show\" button"
        clickShowButton()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Document Complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]
    }

    void "Graph is shown for correct Location"(){
        given: "User selects NotUsedLocation"
        browserTab.click()
        waitFor {selectLocationField.displayed}
        selectLocationField.click()
        waitFor {selectLocationList[0].displayed}
        selectLocationList[0].click()

        when: "User clicks on \"Show\" button"
        clickShowButton()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Document Complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]
    }

    void "Graph is shown for \"Select all Locations\""(){
        given: "User selects NotUsedBrowser"
        browserTab.click()
        waitFor {selectAllLocationsButton.displayed}
        selectAllLocationsButton.click()

        when: "User clicks on \"Show\" button"
        clickShowButton()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Document Complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]
    }

    void "Trim minimal time"() {
        given: "User defines minimal load time"

        clickVariableSelectionTab()
        appendedInputBelowLoadTimesTextField << "250"

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Document Complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565480, y:638]]

        cleanup:
        clickVariableSelectionTab()
        waitFor { appendedInputBelowLoadTimesTextField.displayed }
        appendedInputBelowLoadTimesTextField << Keys.chord(Keys.END)
        5.times {
            appendedInputBelowLoadTimesTextField << Keys.chord(Keys.BACK_SPACE)
        }
    }

    void "Trim maximal time"() {
        given: "User defines maximal load time"
        appendedInputAboveLoadTimesTextField << "830"

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Document Complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565300, y:238], [x:1466565480, y:638]]

        cleanup:
        clickVariableSelectionTab()
        waitFor { appendedInputAboveLoadTimesTextField.displayed }
        appendedInputAboveLoadTimesTextField << Keys.chord(Keys.END)
        5.times {
            appendedInputAboveLoadTimesTextField << Keys.chord(Keys.BACK_SPACE)
        }
    }

    void "Trim minimal requests"() {
        given: "User defines maximal request count"
        waitFor { appendedInputBelowRequestCountsTextField.displayed }
        appendedInputBelowRequestCountsTextField << "3"
        clickFirstViewMeasurand("DOC_COMPLETE_TIME")
        clickFirstViewMeasurand("DOC_COMPLETE_REQUESTS")

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Requests Doc Complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:4], [x:1466565480, y:5]]

        cleanup:
        clickVariableSelectionTab()
        waitFor { appendedInputBelowRequestCountsTextField.displayed }
        appendedInputBelowRequestCountsTextField << Keys.chord(Keys.END)
        3.times {
            appendedInputBelowRequestCountsTextField << Keys.chord(Keys.BACK_SPACE)
        }
    }

    void "Trim maximal requests"() {
        given: "User defines maximal request count"

        waitFor { appendedInputAboveRequestCountsTextField.displayed }
        appendedInputAboveRequestCountsTextField << "5"

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Requests Doc Complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:4], [x:1466565300, y:3]]

        cleanup:
        clickVariableSelectionTab()
        waitFor { appendedInputAboveRequestCountsTextField.displayed }
        appendedInputAboveRequestCountsTextField << Keys.chord(Keys.END)
        3.times {
            appendedInputAboveRequestCountsTextField << Keys.chord(Keys.BACK_SPACE)
        }
    }

    void "Trim minimal size"() {
        given: "User defines minimal bytes until doc"
        waitFor { appendedInputBelowRequestSizesTimesTextField.displayed }
        appendedInputBelowRequestSizesTimesTextField << "0.03"
        clickFirstViewMeasurand("DOC_COMPLETE_REQUESTS")
        clickFirstViewMeasurand("DOC_COMPLETE_INCOMING_BYTES")

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Bytes Doc Complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:0.074476], [x:1466565480, y:0.071976]]

        cleanup:
        clickVariableSelectionTab()
        waitFor { appendedInputBelowRequestSizesTimesTextField.displayed }
        appendedInputBelowRequestSizesTimesTextField << Keys.chord(Keys.END)
        4.times {
            appendedInputBelowRequestSizesTimesTextField << Keys.chord(Keys.BACK_SPACE)
        }
    }

    void "Trim maximal size"() {
        given: "User defines maximal bytes until doc"
        waitFor { appendedInputAboveRequestSizesTextField.displayed }
        insertIntoAboveRequestSizeTextField("0.073")

        when: "User wants to see a graph"
        scrollTop()
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Bytes Doc Complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565300, y:0.021976], [x:1466565480, y:0.071976]]

        cleanup:
        scrollBottom()
        clickVariableSelectionTab()
        appendedInputAboveRequestSizesTextField << Keys.chord(Keys.END)
        5.times {
            appendedInputAboveRequestSizesTextField << Keys.chord(Keys.BACK_SPACE)
        }
    }

    void "Load time graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        clickFirstViewMeasurand("DOC_COMPLETE_INCOMING_BYTES")
        clickFirstViewMeasurand("LOAD_TIME")

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Load Time | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:266], [x:1466565480, y:366]]

        cleanup:
        clickVariableSelectionTab()
        clickFirstViewMeasurand("LOAD_TIME")
    }

    void "Time to first byte graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"

        clickFirstViewMeasurand("FIRST_BYTE")

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv First Byte | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:170], [x:1466565300, y:53], [x:1466565480, y:153]]

        cleanup:
        clickVariableSelectionTab()
        clickFirstViewMeasurand("FIRST_BYTE")

    }

    void "Start render time graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"

        clickFirstViewMeasurand("START_RENDER")

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Start Render | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:192], [x:1466565300, y:285], [x:1466565480, y:185]]

        cleanup:
        clickVariableSelectionTab()
        clickFirstViewMeasurand("START_RENDER")
    }

    void "Visually complete graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"

        clickFirstViewMeasurand("VISUALLY_COMPLETE")

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Visually Complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:1300], [x:1466565300, y:266], [x:1466565480, y:1766]]

        cleanup:
        clickVariableSelectionTab()
        clickFirstViewMeasurand("VISUALLY_COMPLETE")
    }

    void "Dom time graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        clickFirstViewMeasurand("DOM_TIME")

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv DOM Time | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:500], [x:1466565300, y:250], [x:1466565480, y:750]]

        cleanup:
        clickVariableSelectionTab()
        clickFirstViewMeasurand("DOM_TIME")
    }

    void "Fully loaded time graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        clickFirstViewMeasurand("FULLY_LOADED_TIME")

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Fully Loaded | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:2557], [x:1466565300, y:1005], [x:1466565480, y:2005]]

        cleanup:
        clickVariableSelectionTab()
        clickFirstViewMeasurand("FULLY_LOADED_TIME")

    }

    void "Count of request to doc complete graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        clickFirstViewMeasurand("DOC_COMPLETE_REQUESTS")

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Requests Doc Complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:4], [x:1466565300, y:3], [x:1466565480, y:5]]

        cleanup:
        clickVariableSelectionTab()
        clickFirstViewMeasurand("DOC_COMPLETE_REQUESTS")
    }

    void "Count of requestion to fully loaded graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        clickFirstViewMeasurand("FULLY_LOADED_REQUEST_COUNT")

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Requests Fully Loaded | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:48], [x:1466565300, y:26], [x:1466565480, y:36]]

        cleanup:
        clickVariableSelectionTab()
        clickFirstViewMeasurand("FULLY_LOADED_REQUEST_COUNT")
    }

    void "Bytes until doc complete graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        clickFirstViewMeasurand("DOC_COMPLETE_INCOMING_BYTES")

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Bytes Doc Complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:0.074476], [x:1466565300, y:0.021976], [x:1466565480, y:0.071976]]

        cleanup:
        clickVariableSelectionTab()
        clickFirstViewMeasurand("DOC_COMPLETE_INCOMING_BYTES")
    }

    void "Bytes until fully loaded graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        clickFirstViewMeasurand("FULLY_LOADED_INCOMING_BYTES")

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Bytes Fully Loaded | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:0.18446], [x:1466565300, y:0.02336], [x:1466565480, y:0.13336]]

        cleanup:
        clickVariableSelectionTab()
        clickFirstViewMeasurand("FULLY_LOADED_INCOMING_BYTES")
    }

    void "Customer satisfaction (visually complete) graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        clickFirstViewMeasurand("CS_BY_WPT_VISUALLY_COMPLETE")

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Customer Satisfaction (Visually Complete) | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:88], [x:1466565300, y:23], [x:1466565480, y:63]]

        cleanup:
        clickVariableSelectionTab()
        clickFirstViewMeasurand("CS_BY_WPT_VISUALLY_COMPLETE")
    }

    void "Customer satisfaction (doc complete) graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        clickFirstViewMeasurand("CS_BY_WPT_DOC_COMPLETE")

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Customer Satisfaction (Document Complete) | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:99.6157], [x:1466565300, y:23.6157], [x:1466565480, y:73.6157]]

        cleanup:
        clickVariableSelectionTab()
        clickFirstViewMeasurand("CS_BY_WPT_DOC_COMPLETE")
    }

    void "Speed index graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        clickFirstViewMeasurand("SPEED_INDEX")

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Speed Index | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:607], [x:1466565300, y:277], [x:1466565480, y:577]]
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
            TestDataUtil.createAdminUser()

            Script script1 = Script.build().save(failOnError: true)
            Script script2 = Script.build().save(failOnError: true)
            JobGroup jobGroup1 = JobGroup.build(name: jobGroup1Name)
            WebPageTestServer wpt = WebPageTestServer.build().save(failOnError: true)
            Browser browser = Browser.build().save(failOnError: true)
            Location location1 = Location.build(uniqueIdentifierForServer: location1Name, browser: browser, wptServer: wpt).save(failOnError: true)
            Location location2 = Location.build(uniqueIdentifierForServer: location2Name, browser: browser, wptServer: wpt).save(failOnError: true)
            Job job1 = Job.build().save(failOnError: true)
            Job job2 = Job.build().save(failOnError: true)
            Page page1 = Page.build().save(failOnError: true)
            JobResult jobResult1 = createJobResult("Test1", new DateTime(2016, 06, 22, 3, 13, DateTimeZone.UTC).toDate(), job1, location1)
            JobResult jobResult2 = createJobResult("Test2", new DateTime(2016, 06, 22, 3, 18, DateTimeZone.UTC).toDate(), job1, location1)
            JobResult jobResult3 = createJobResult("Test3", new DateTime(2016, 06, 22, 3, 15, DateTimeZone.UTC).toDate(), job1, location1)
            ConnectivityProfile connectivityProfile = createConnectivityProfile(connectivityProfileName)
            MeasuredEvent measuredEvent1 = MeasuredEvent.build(name: measureEvent1Name).save(failOnError: true)

            Browser notUsedBrowser = Browser.build().save(failOnError: true)
            createConnectivityProfile("NotUsedConnectivityProfile")
            Location.build().save(failOnError: true)

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: WptStatus.COMPLETED.getWptStatusCode(),
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
                    jobGroup: jobGroup1,
                    measuredEvent: measuredEvent1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    connectivityProfile: connectivityProfile,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: false
            ).save()
            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: WptStatus.COMPLETED.getWptStatusCode(),
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
                    jobGroup: jobGroup1,
                    measuredEvent: measuredEvent1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    connectivityProfile: connectivityProfile,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: false
            ).save()

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: WptStatus.COMPLETED.getWptStatusCode(),
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
                    jobGroup: jobGroup1,
                    measuredEvent: measuredEvent1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    connectivityProfile: connectivityProfile,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: false
            ).save()

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: WptStatus.COMPLETED.getWptStatusCode(),
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
                    jobGroup: jobGroup1,
                    measuredEvent: measuredEvent1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: true
            ).save()

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: WptStatus.COMPLETED.getWptStatusCode(),
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
                    jobGroup: jobGroup1,
                    measuredEvent: measuredEvent1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: true
            ).save()

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: WptStatus.COMPLETED.getWptStatusCode(),
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
                    jobGroup: jobGroup1,
                    measuredEvent: measuredEvent1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    customConnectivityName: "Custom (6.000/512 Kbps, 50ms)",
                    noTrafficShapingAtAll: false
            ).save()

            new EventResult(
                    numberOfWptRun: 1,
                    cachedView: CachedView.UNCACHED,
                    medianValue: true,
                    wptStatus: WptStatus.COMPLETED.getWptStatusCode(),
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
        datePicker << Keys.chord(Keys.ESCAPE)
        Thread.sleep(500l)
        datePicker.click()
        datePicker << Keys.chord(Keys.END)
        25.times {
            datePicker << Keys.chord(Keys.BACK_SPACE)
        }
        datePicker << date
        datePicker << Keys.chord(Keys.ESCAPE)
        Thread.sleep(500l)
    }

    /**
     * <p>
     * Creates a job result for the specified data.
     * </p>
     *
     * <p>
     * None of the arguments may be <code>null</code>.
     * </p>
     *
     * @param testId The ID of the test-result.
     * @param dateOfJobRun The date of the test run.
     * @param parentJob The job the result belongs to.
     * @param agentLocation The location where the agent is working.
     *
     * @return A newly created result, not <code>null</code>.
     */
    private JobResult createJobResult(String testId, Date dateOfJobRun, Job parentJob, Location agentLocation) {
        return JobResult.build(
                date: dateOfJobRun,
                testId: testId,
                wptStatus: WptStatus.COMPLETED,
                jobConfigLabel: parentJob.label,
                jobConfigRuns: 1,
                description: '',
                locationBrowser: agentLocation.browser.name,
                locationLocation: agentLocation.location,
                jobGroupName: parentJob.jobGroup.name,
                job: parentJob
        ).save(failOnError: true)
    }
}
