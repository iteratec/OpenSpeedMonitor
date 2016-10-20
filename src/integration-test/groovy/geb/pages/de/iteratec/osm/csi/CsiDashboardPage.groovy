package geb.pages.de.iteratec.osm.csi

import geb.Page

/**
 * Created by marko on 07.07.16.
 */
class CsiDashboardPage extends Page{
    static url = "/csiDashboard/showAll"

    static at = { title == "Dashboard" }

    static content = {
        timeFrameSelect{$("#timeframeSelect").find("option").contextElements[0]}
        showButton (to: CsiDashboardPage) {$("#chart-submit")}
        fromDatepicker{$("#fromDatepicker")}
        toDatepicker{$("#toDatepicker")}
        aggregationRadioButtons{$(".span6")} //possible values measured_event, daily_page, weekly_page, daily_shop, weekly_shop, daily_system, weekly_system
        basedOnVisuallyCompleteButton{$("#csiTypeVisuallyComplete")}
        saveAsDashboardButton(required: false) { $("a", href: "#CreateUserspecifiedDashboardModal") }
        jobGroupList { $("#folderSelectHtmlId").find("option").contextElements }
        pageList { $("#pageSelectHtmlId").find("option").contextElements }
        selectBrowsersList{$("#selectedBrowsersHtmlId").find("option").contextElements }
        selectAllBrowserButton{$("#selectedAllBrowsers")}
        selectConnectivityProfilesList{$("#selectedConnectivityProfilesHtmlId").find("option").contextElements }
        selectAllConnectivityButton{$("#selectedAllConnectivityProfiles")}
        selectLocationField{$("#selectedLocationsHtmlId_chosen")}
        selectLocationList{$(".active-result")}
        selectAllLocationsButton{$("#selectedAllLocations")}
        csiSystem { $("#folderSelectCsiSystem").find("option").contextElements}
        graphLineDiv { $(".path") }
        graphLine1 { $(".path")[0].attr("d") }
        graphLine2 { $(".path")[1].attr("d") }
        graphLine3 { $(".path")[2].attr("d") }
        chartTitleInputField { $("#dia-title") }
        chartTitle { $("#rickshaw_chart_title").attr("innerHTML") }
        chartWidthInputField { $("#dia-width") }
        chartheightInputField { $("#dia-height") }
        diaChangeChartsizeButton { $("#dia-change-chartsize") }
        diaYAxisMinInputField { $("#dia-y-axis-min") }
        diaYAxisMaxInputField { $("#dia-y-axis-max") }
        diaChangeYAxisButton { $("#dia-change-yaxis") }
        showDataMarkersCheckBox { $("#to-enable-marker") }
        showDataLabelsCheckBox { $("#to-enable-label") }
        optimizeForWideScreenCheckBox { $("#wide-screen-diagram-montage") }
        addAliasButton { $("#addAliasButton") }
        aliasInputField { $(".input-alias")[0] }
        graphName { $("span.label").attr("innerHTML") }
        dataMarker { $(".pointMarker") }
        dataLabel { $(".dataLabel") }
        dashboardNameFromModalTextField(required: false) { $("#dashboardNameFromModal") }
        saveDashboardButtonButton { $("#saveDashboardButton") }
        saveDashboardSuccessMessage(required: false) { $("#saveDashboardSuccessDiv") }
        customDashboardSelectionDropdown { $("a",href:"/csiDashboard/showAll?dashboardID=1").parent().parent().parent() }
        customDashboardSelectionList { $("a",href:"/csiDashboard/showAll?dashboardID=1") }
        pageTab{$("a",href:"#tab1")}
        browserTab{$("a",href:"#tab2")}
        connectivityTab{$("a",href:"#tab3")}
    }

    public void clickAdjustChartAccordion() {
        // Scroll object into view so it becomes clickable
        sleep(100)
        String jqueryString = "jQuery(\'.accordion-toggle\')[0].scrollIntoView();"
        js.exec(jqueryString)
        sleep(100)
        js.exec('document.body.scrollTop -= 70;') // scroll a little more cause of the sticky header
        sleep(100)
        $('.accordion-toggle')[0].click()
    }

    public void clickSaveAsDashboardButton() {
        // Scroll object into view so it becomes clickable
        sleep(100)
        String jqueryString = "jQuery(\'#chart-submit\')[0].scrollIntoView();"
        js.exec(jqueryString)
        sleep(100)
        js.exec('document.body.scrollTop -= 70;') // scroll a little more cause of the sticky header
        sleep(100)
        $("a",href:'#CreateUserspecifiedDashboardModal').click()

    }
}
