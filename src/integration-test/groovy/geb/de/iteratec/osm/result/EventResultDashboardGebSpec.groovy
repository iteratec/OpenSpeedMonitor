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
import de.iteratec.osm.result.MeasurandGroup
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
class EventResultDashboardGebSpec extends CustomUrlGebReportingSpec implements OsmTestLogin {

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
            showButton.disabled
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
            showButton.disabled
        }
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
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
            showButton.disabled
        }
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
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
            !showButton.disabled
        }
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Valid selection graph is shown"() {

        when: "User wants to see the graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
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
        graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]

    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
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
        graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
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
        graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
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
        graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]
    }

    @Ignore("[IT-1415] need to implement new connectivity selection")
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
        graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]
    }

    @Ignore("[IT-1415] need to implement new connectivity selection")
    void "Graph is shown for native connectivity"(){
        given: "User selects native connectivity"
        connectivityTab.click()
        waitFor {includeNativeConnectivityButton.displayed}
        includeNativeConnectivityButton.click()

        when: "User clicks on \"Show\" button"
        clickShowButton()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 2

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 2
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]
        graphSeries[1].data.collect { [x:it.x, y:it.y]} == [[x:1466565300, y:158], [x:1466565480, y:258]]

        cleanup:
        connectivityTab.click()
        waitFor {includeNativeConnectivityButton.displayed}
        includeNativeConnectivityButton.click()
        pageTab.click()
    }

    @Ignore("[IT-1415] need to implement new connectivity selection")
    void "Graph is shown for custom connectivity"(){
        given: "User selects custom connectivity"
        connectivityTab.click()
        waitFor {includeCustomConnectivityButton.displayed}
        includeCustomConnectivityButton.click()

        when: "User clicks on \"Show\" button"
        clickShowButton()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 2

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 2
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]
        graphSeries[1].data.collect { [x:it.x, y:it.y]} == [[x:1466565300, y:458], [x:1466565480, y:558]]

        cleanup:
        connectivityTab.click()
        waitFor {includeCustomConnectivityButton.displayed}
        includeCustomConnectivityButton.click()
        pageTab.click()
    }

    @Ignore("[IT-1415] need to implement new connectivity selection")
    void "Graph is shown for \"Select all Connectivity Profiles\""(){
        given: "User selects NotUsedBrowser"
        connectivityTab.click()
        waitFor {selectAllConnectivityButton.displayed}
        selectAllConnectivityButton.click()

        when: "User clicks on \"Show\" button"
        clickShowButton()

        then: "Graphs are displayed"
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:238], [x:1466565480, y:638]]
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
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
        graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565480, y:638]]

        cleanup:
        clickVariableSelectionTab()
        waitFor { appendedInputBelowLoadTimesTextField.displayed }
        appendedInputBelowLoadTimesTextField << Keys.chord(Keys.CONTROL, "a")
        appendedInputBelowLoadTimesTextField << Keys.chord(Keys.DELETE)

    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Trim maximal time"() {
        given: "User defines maximal load time"
        appendedInputAboveLoadTimesTextField << "830"

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565300, y:238], [x:1466565480, y:638]]

        cleanup:
        clickVariableSelectionTab()
        waitFor { appendedInputAboveLoadTimesTextField.displayed }
        appendedInputAboveLoadTimesTextField << Keys.chord(Keys.CONTROL, "a")
        appendedInputAboveLoadTimesTextField << Keys.chord(Keys.DELETE)
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Trim minimal requests"() {
        given: "User defines maximal request count"
        waitFor { appendedInputBelowRequestCountsTextField.displayed }
        appendedInputBelowRequestCountsTextField << "3"
        firstViewList[3].click()
        firstViewList[7].click()

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv requests doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:4], [x:1466565480, y:5]]

        cleanup:
        clickVariableSelectionTab()
        waitFor { appendedInputBelowRequestCountsTextField.displayed }
        appendedInputBelowRequestCountsTextField << Keys.chord(Keys.CONTROL, "a")
        appendedInputBelowRequestCountsTextField << Keys.chord(Keys.DELETE)
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
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
        graphName == 'fv requests doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:4], [x:1466565300, y:3]]

        cleanup:
        clickVariableSelectionTab()
        waitFor { appendedInputAboveRequestCountsTextField.displayed }
        appendedInputAboveRequestCountsTextField << Keys.chord(Keys.CONTROL, "a")
        appendedInputAboveRequestCountsTextField << Keys.chord(Keys.DELETE)
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Trim minimal size"() {
        given: "User defines minimal bytes until doc"
        waitFor { appendedInputBelowRequestSizesTimesTextField.displayed }
        appendedInputBelowRequestSizesTimesTextField << "30"
        firstViewList[7].click()
        firstViewList[9].click()

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv docCompleteIncomingBytesUncached | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:74.476], [x:1466565480, y:71.976]]

        cleanup:
        clickVariableSelectionTab()
        waitFor { appendedInputBelowRequestSizesTimesTextField.displayed }
        appendedInputBelowRequestSizesTimesTextField << Keys.chord(Keys.CONTROL, "a")
        appendedInputBelowRequestSizesTimesTextField << Keys.chord(Keys.DELETE)
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Trim maximal size"() {
        given: "User defines maximal bytes until doc"
        waitFor { appendedInputAboveRequestSizesTextField.displayed }
        insertIntoAboveRequestSizeTextField("73")

        when: "User wants to see a graph"
        scrollTop()
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv docCompleteIncomingBytesUncached | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565300, y:21.976], [x:1466565480, y:71.976]]

        cleanup:
        scrollBottom()
        clickVariableSelectionTab()
        clearAboveRequestSizeTextField()
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Load time graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        firstViewList[9].click()
        firstViewList[0].click()

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv load time | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:838], [x:1466565300, y:266], [x:1466565480, y:366]]

        cleanup:
        clickVariableSelectionTab()
        firstViewList[0].click()
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Time to first byte graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"

        firstViewList[1].click()

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv ttfb | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:170], [x:1466565300, y:53], [x:1466565480, y:153]]

        cleanup:
        clickVariableSelectionTab()
        firstViewList[1].click()

    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Start render time graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"

        firstViewList[2].click()

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv start render | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:192], [x:1466565300, y:285], [x:1466565480, y:185]]

        cleanup:
        clickVariableSelectionTab()
        firstViewList[2].click()
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Visually complete graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"

        firstViewList[4].click()

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv visually complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:1300], [x:1466565300, y:266], [x:1466565480, y:1766]]

        cleanup:
        clickVariableSelectionTab()
        firstViewList[4].click()
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Dom time graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        firstViewList[5].click()

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv dom time | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:500], [x:1466565300, y:250], [x:1466565480, y:750]]

        cleanup:
        clickVariableSelectionTab()
        firstViewList[5].click()
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Fully loaded time graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        firstViewList[6].click()

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv fully loaded | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:2557], [x:1466565300, y:1005], [x:1466565480, y:2005]]

        cleanup:
        clickVariableSelectionTab()
        firstViewList[6].click()

    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Count of request to doc complete graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        firstViewList[7].click()

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv requests doc complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:4], [x:1466565300, y:3], [x:1466565480, y:5]]

        cleanup:
        clickVariableSelectionTab()
        firstViewList[7].click()
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Count of requestion to fully loaded graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        firstViewList[8].click()

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv requests fully loaded | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:48], [x:1466565300, y:26], [x:1466565480, y:36]]

        cleanup:
        clickVariableSelectionTab()
        firstViewList[8].click()
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Bytes until doc complete graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        firstViewList[9].click()

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv docCompleteIncomingBytesUncached | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:74.476], [x:1466565300, y:21.976], [x:1466565480, y:71.976]]

        cleanup:
        clickVariableSelectionTab()
        firstViewList[9].click()
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Bytes until fully loaded graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        firstViewList[10].click()

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv fullyLoadedIncomingBytesUncached | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:184.46], [x:1466565300, y:23.36], [x:1466565480, y:133.36]]

        cleanup:
        clickVariableSelectionTab()
        firstViewList[10].click()
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Customer satisfaction (visually complete) graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        firstViewList[11].click()

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv customer satisfaction (visually complete) | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:88], [x:1466565300, y:23], [x:1466565480, y:63]]

        cleanup:
        clickVariableSelectionTab()
        firstViewList[11].click()
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Customer satisfaction (doc complete) graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        firstViewList[12].click()

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv customer satisfaction (docComplete) | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect { [x:it.x, y:it.y]} == [[x:1466565180, y:99.6157], [x:1466565300, y:23.6157], [x:1466565480, y:73.6157]]

        cleanup:
        clickVariableSelectionTab()
        firstViewList[12].click()
    }

    @Ignore("[IT-1427] phantomJS doesn't get events triggered by jquery")
    void "Speed index graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        firstViewList[13].click()

        when: "User wants to see a graph"
        clickShowButton()

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv speed index | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

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

    private void selectDateInDatepicker(def datePicker, String date) {
        datePicker.click()
        datePicker << Keys.chord(Keys.END)
        25.times {
            datePicker << Keys.chord(Keys.BACK_SPACE)
        }
        datePicker << date
    }
}
