package geb.pages.de.iteratec.osm.result

import geb.Page

/**
 * Created by marcus on 16.06.16.
 */
class EventResultDashboardPage extends Page {
    static url = "/eventResultDashboard/showAll"

    static at = { title == "Dashboard" }

    static content = {
        showButton(to: EventResultDashboardPage) { $("#graphButtonHtmlId") }
        jobGroupList { $("#folderSelectHtmlId").find("option").contextElements }
        pageList { $("#pageSelectHtmlId").find("option").contextElements }
        timeFrameSelect { $("#timeframeSelect").find("option").contextElements[0] }
        fromDatepicker { $("#fromDatepicker") }
        toDatepicker { $("#toDatepicker") }
        firstViewDiv { $("#selectAggregatorUncachedHtmlId") }
        firstViewList { $("#selectAggregatorUncachedHtmlId").find("option").contextElements }
        filterJobsAccordionContent { $("#collapseTwo") }
        filterJobsAccordion { $(".accordion-toggle")[1] }
        chooseMeasuredVariablesAccordionContent { $("#collapseThree") }
        adjustChartAccordion { $(".accordion-toggle")[3] }
        chartTitleInputField { $("#dia-title") }
        chartTitle { $("#rickshaw_chart_title").attr("innerHTML") }
        chartWidthInputField { $("#dia-width") }
        chartheightInputField { $("#dia-height") }
        diaYAxisMinInputField { $(".dia-y-axis-min") }
        diaYAxisMaxInputField { $(".dia-y-axis-max") }
        diaChangeYAxisButton { $(".dia-change-yaxis") }
        showDataMarkersCheckBox { $("#to-enable-marker") }
        showDataLabelsCheckBox { $("#to-enable-label") }
        optimizeForWideScreenCheckBox { $("#wide-screen-diagram-montage") }
        addAliasButton { $("#addAliasButton") }
        aliasInputField { $(".input-alias")[0] }
        graphName { $("span.label").attr("innerHTML") }
        colorPicker { $("#color") }
        graphColorField { $(".swatch").attr("style") }
        graphLineDiv { $(".path") }
        graphLine { $(".path").attr("d") }
        graphLine2{ $(".path")[1].attr("d")}
        dataMarker { $(".pointMarker") }
        dataLabel { $(".dataLabel") }
        saveAsDashboardButton(required: false) { $("a", href: "#CreateUserspecifiedDashboardModal") }
        dashboardNameFromModalTextField(required: false) { $("#dashboardNameFromModal") }
        saveDashboardButtonButton { $("#saveDashboardButton") }
        saveDashboardSuccessMessage(required: false) { $("#saveDashboardSuccessDiv") }
        customDashboardSelectionDropdown { $("#customDashBoardSelection").parent() }
        customDashboardSelectionList { $("#customDashBoardSelection") }
        appendedInputBelowLoadTimesTextField { $("#appendedInputBelowLoadTimes") }
        appendedInputAboveLoadTimesTextField { $("#appendedInputAboveLoadTimes") }
        appendedInputBelowRequestCountsTextField { $("#appendedInputBelowRequestCounts") }
        appendedInputAboveRequestCountsTextField { $("#appendedInputAboveRequestCounts") }
        appendedInputBelowRequestSizesTimesTextField { $("#appendedInputBelowRequestSizes") }
        appendedInputAboveRequestSizesTextField { $("#appendedInputAboveRequestSizes") }

        pageTab{$("a",href:"#tab1")}
        browserTab{$("a",href:"#tab2")}
        connectivityTab{$("a",href:"#tab3")}
        selectBrowsersList{$("#selectedBrowsersHtmlId").find("option").contextElements }
        selectAllBrowserButton{$("#selectedAllBrowsers")}
        selectConnectivityProfilesList{$("#selectedConnectivityProfilesHtmlId").find("option").contextElements }
        selectAllConnectivityButton{$("#selectedAllConnectivityProfiles")}
        selectLocationField{$("#selectedLocationsHtmlId_chosen")}
        selectLocationList{$(".active-result")}
        selectAllLocationsButton{$("#selectedAllLocations")}
        includeNativeConnectivityButton{$("#includeNativeConnectivity")}
        includeCustomConnectivityButton{$("#includeCustomConnectivity")}

        tabJobSelection{$("#tabJobSelectionElement")}
        tabVariableSelection{$("#tabVariableSelectionElement")}

        adjustChartButton{$("#rickshaw_adjust_chart_link")}
        adjustChartApply{$("#adjustChartApply")}

    }

    public void clickChooseMeasuredVariablesAccordion() {
        // Scroll object into view so it becomes clickable
        sleep(100)
        String jqueryString = "jQuery(\'.accordion-toggle\')[2].scrollIntoView();"
        js.exec(jqueryString)
        sleep(100)
        js.exec('document.body.scrollTop -= 70;')// scroll a little more cause of the sticky header
        sleep(100)
        $('.accordion-toggle')[2].click()
    }

    public void clickFilterJobAccordion() {
        // Scroll object into view so it becomes clickable
        sleep(100)
        String jqueryString = "jQuery(\'.accordion-toggle\')[1].scrollIntoView();"
        js.exec(jqueryString)
        sleep(100)
        js.exec('document.body.scrollTop -= 70;')// scroll a little more cause of the sticky header
        sleep(100)
        $('.accordion-toggle')[1].click()
    }

    public void clickAdjustChartAccordion() {
        // Scroll object into view so it becomes clickable
        sleep(100)
        String jqueryString = "jQuery(\'.accordion-toggle\')[3].scrollIntoView();"
        js.exec(jqueryString)
        sleep(100)
        js.exec('document.body.scrollTop -= 70;') // scroll a little more cause of the sticky header
        sleep(100)
        $('.accordion-toggle')[3].click()
    }

    public void clickSaveAsDashboardButton() {
        // Scroll object into view so it becomes clickable
        sleep(100)
        String jqueryString = "jQuery(\'#graphButtonHtmlId\')[0].scrollIntoView();"
        js.exec(jqueryString)
        sleep(100)
        js.exec('document.body.scrollTop -= 70;') // scroll a little more cause of the sticky header
        sleep(100)
        $('#graphButtonHtmlId').click()

    }
}
