package geb.pages.de.iteratec.osm.csi

import geb.pages.de.iteratec.osm.I18nGebPage
/**
 * Created by marko on 07.07.16.
 */
class CsiDashboardPage extends I18nGebPage{
    static url = getUrl("/csiDashboard/showAll")

    static at = {
        title == "Dashboard"
    }

    static content = {
        adjustChartButton{$("#rickshaw_adjust_chart_link")}
        adjustChartApplyButton{$('#adjustChartApply')}
        timeFrameSelect{$("#timeframeSelect").find("option").contextElements[0]}
        showButton (to: CsiDashboardPage) {$("#chart-submit")}
        fromDatepicker{$("#fromDatepicker")}
        toDatepicker{$("#toDatepicker")}
        aggregationRadioButtons{$("#aggregationRadioButtons")} //possible values measured_event, daily_page, weekly_page, daily_shop, weekly_shop, daily_system, weekly_system
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
        chartContainer { $("#rickshaw_main") }
        graphYGridFirstTick { $("#rickshaw_chart .y_grid .tick")[0].attr("data-y-value") }
        graphYGridLastTick { $("#rickshaw_chart .y_grid .tick")[-1].attr("data-y-value") }
        graphLines { $("#rickshaw_chart .path") }
        chartTitleInputField { $("#dia-title") }
        chartTitle { $("#rickshaw_chart_title").attr("innerHTML") }
        chartWidthInputField { $("#dia-width") }
        chartheightInputField { $("#dia-height") }
        diaYAxisMinInputField { $(".dia-y-axis-min") }
        diaYAxisMaxInputField { $(".dia-y-axis-max") }
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
        customDashboardSelectionDropdown { $("#customDashboardDropdownButton") }
        customDashboardSelectionList { $("#customDashBoardSelection") }
        pageTab{$("a",href:"#tab1")}
        browserTab{$("a",href:"#tab2")}
        connectivityTab{$("a",href:"#tab3")}
    }

    public void clickSaveAsDashboardButton() {
        $("#chart-action-dropdown").click()
        sleep(200)
        $("a",href:'#CreateUserspecifiedDashboardModal').click()
    }
}
