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
        to EventResultDashboardPage

        then: "Button is visible"
        saveAsDashboardButton.present

    }

    void "Document Complete graph is shown"() {
        given: "User selects valid timeframe, page and jobgroup"
        timeFrameSelect.click()
        selectDateInDatepicker(fromDatepicker, "21.06.2016 00:00")
        selectDateInDatepicker(toDatepicker, "23.06.2016 23:59")
        sleep(500)
        jobGroupList[0].click()
        pageList[0].click()
        connectivityTab.click()
        waitFor {selectConnectivityProfilesList.displayed}
        selectConnectivityProfilesList[0].click()
        tabVariableSelection.click()

        when: "User wants to see a graph"
        waitFor { showButton.displayed }
        sleep(100)
        waitFor { showButton.click() }

        then: "A graph with a line is shown"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1
        graphName == 'fv Document Complete | TestJobGroup1-564892#Afef1 | MeasureEvent1-564892#Afef1 | TestLocation1-564892#Afef1 | ConnectivityProfile-564892#Afef1'

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect {
            [x: it.x, y: it.y]
        } == [[x: 1466565180, y: 838], [x: 1466565300, y: 238], [x: 1466565480, y: 638]]
    }

    void "Adjust Chart Title"() {
        given: "User opens Adjust Chart"
        clickAdjustChartButton()

        when: "User edits title"
        waitFor { chartTitleInputField.displayed }
        sleep(300)
        chartTitleInputField << "CustomTitle"
        sleep(300)
        adjustChartApply.click()

        then: "Chart title is changed"
        waitFor { chartTitle == "CustomTitle" }
    }

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

    void "Add graph alias"() {
        given: "User clicks on the add graph aliases button"
        sleep(200)
        adjustChartButton.click()
        waitFor { addAliasButton.displayed }
        sleep(200)
        addAliasButton.click()

        when: "User provides graph alias"
        waitFor { graphNameSelect[0].displayed }
        graphNameSelect[0].click()
        sleep(200)
        graphNameSelectOptions[1].click()
        aliasInputField << "CustomAlias"
        adjustChartApply.click()
        sleep(200)

        then: "Graph is renamed"
        waitFor { graphName == "CustomAlias" }
    }

    void "Change Graph color"() {
        when: "User changes graph color"
        waitFor{ adjustChartButton.click() }
        waitFor { colorPicker[0].displayed }
        sleep(300)
        setColorPicker("#aaaaaa")
        adjustChartApply.click()

        then: "Graph is recolored"
        true

        waitFor { graphColorField == 'background-color: rgb(170, 170, 170);' }
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
        waitFor { graphLines.displayed }
        graphLines.size() == 1

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect {
            [x: it.x, y: it.y]
        } == [[x: 1466565180, y: 838], [x: 1466565300, y: 238], [x: 1466565480, y: 638]]
    }

    void "Load custom dashboard"() {
        given: "User visits the EventResultDashboardPage"
        to EventResultDashboardPage
        when: "User loads CustomDashboard"
        customDashboardSelectionDropdown.click()
        waitFor { customDashboardSelectionList.displayed }
        firstCustomDashboardLink.click()
        then: "The old dashboard is loaded again"
        at EventResultDashboardPage
        waitFor { graphLines.displayed }
        graphLines.size() == 1

        def graphSeries = js."window.rickshawGraphBuilder.graph.series"
        graphSeries.size() == 1
        graphSeries[0].data.collect {
            [x: it.x, y: it.y]
        } == [[x: 1466565180, y: 838], [x: 1466565300, y: 238], [x: 1466565480, y: 638]]

        chartTitle == "CustomTitle"

        chartContainer.width == 600
        chartContainer.height == 650
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
            createAdminUser()


            Script script1 = Script.build(label: script1Name, description: "This is for test purposes", navigationScript: "stuff")
            Script script2 = Script.build(label: script2Name, description: "This is also for test purposes", navigationScript: "stuff")
            JobGroup jobGroup1 = JobGroup.build(name: jobGroup1Name)
            WebPageTestServer wpt = WebPageTestServer.build(label: "TestWPTServer-564892#Afef1", proxyIdentifier: "TestIdentifier", active: true, baseUrl: "http://internet.de")

            Browser browser = Browser.build(name: "This is the very best browser I've ever seen")
            Location location1 = Location.build(wptServer: wpt, uniqueIdentifierForServer: location1Name, browser: browser, deviceType: DeviceType.DESKTOP, operatingSystem: OperatingSystem.WINDOWS, active: true)
            Location location2 = Location.build(wptServer: wpt, uniqueIdentifierForServer: location2Name, browser: browser, deviceType: DeviceType.DESKTOP, operatingSystem: OperatingSystem.WINDOWS, active: true)
            Job job1 = Job.build(label: job1Name, script: script1, location: location1, jobGroup: jobGroup1, description: "This is the first test job", runs: 1, active: false, maxDownloadTimeInMinutes: 12)
            Job job2 = Job.build(label: job2Name, script: script2, location: location2, jobGroup: jobGroup1, description: "This is the second test job", runs: 1, active: false, maxDownloadTimeInMinutes: 12)

            Page page1 = Page.build(name: page1Name)
            JobResult jobResult1 = JobResult.build(testId: "Test1", date: new DateTime(2016, 06, 22, 3, 13, DateTimeZone.UTC).toDate(), job: job1, locationLocation: location1)
            JobResult jobResult2 = JobResult.build(testId: "Test2", date: new DateTime(2016, 06, 22, 3, 18, DateTimeZone.UTC).toDate(), job: job1, locationLocation: location1)
            JobResult jobResult3 = JobResult.build(testId: "Test3", date: new DateTime(2016, 06, 22, 3, 15, DateTimeZone.UTC).toDate(), job: job1, locationLocation: location1)
            ConnectivityProfile connectivityProfile = createConnectivityProfile(connectivityProfileName)
            MeasuredEvent measuredEvent1 = MeasuredEvent.build(name: measureEvent1Name, testedPage: page1)

            Browser notUsedBrowser = Browser.build(name: "NotUsedBrowser")
            createConnectivityProfile("NotUsedConnectivityProfile")
            Location.build(wptServer: wpt, uniqueIdentifierForServer: "NotUsedLocation", browser: notUsedBrowser, active: true)

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
                    measuredEvent: measuredEvent1,
                    connectivityProfile: connectivityProfile,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: false,
                    jobGroup: jobGroup1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    deviceType: location1.deviceType,
                    operatingSystem: location1.operatingSystem
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
                    measuredEvent: measuredEvent1,
                    connectivityProfile: connectivityProfile,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: false,
                    jobGroup: jobGroup1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    deviceType: location1.deviceType,
                    operatingSystem: location1.operatingSystem
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
                    measuredEvent: measuredEvent1,
                    connectivityProfile: connectivityProfile,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: false,
                    jobGroup: jobGroup1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    deviceType: location1.deviceType,
                    operatingSystem: location1.operatingSystem
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
                    measuredEvent: measuredEvent1,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: true,
                    jobGroup: jobGroup1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    deviceType: location1.deviceType,
                    operatingSystem: location1.operatingSystem
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
                    measuredEvent: measuredEvent1,
                    customConnectivityName: null,
                    noTrafficShapingAtAll: true,
                    jobGroup: jobGroup1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    deviceType: location1.deviceType,
                    operatingSystem: location1.operatingSystem
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
                    measuredEvent: measuredEvent1,
                    customConnectivityName: "Custom (6.000/512 Kbps, 50ms)",
                    noTrafficShapingAtAll: false,
                    jobGroup: jobGroup1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    deviceType: location1.deviceType,
                    operatingSystem: location1.operatingSystem
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
                    measuredEvent: measuredEvent1,
                    customConnectivityName: "Custom (6.000/512 Kbps, 50ms)",
                    noTrafficShapingAtAll: false,
                    jobGroup: jobGroup1,
                    page: measuredEvent1.testedPage,
                    browser: browser,
                    location: location1,
                    deviceType: location1.deviceType,
                    operatingSystem: location1.operatingSystem
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
        datePicker.click()
        datePicker << Keys.chord(Keys.END)
        25.times {
            datePicker << Keys.chord(Keys.BACK_SPACE)
        }
        datePicker << date
    }

}
