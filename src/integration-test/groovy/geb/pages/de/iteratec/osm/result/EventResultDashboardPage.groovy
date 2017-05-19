package geb.pages.de.iteratec.osm.result

import geb.pages.de.iteratec.osm.I18nGebPage
import org.openqa.selenium.Keys

/**
 * Created by marcus on 16.06.16.
 */
class EventResultDashboardPage extends I18nGebPage {

    static url = getUrl("/eventResultDashboard/showAll")

    static at = {
        title == "Time Series"
    }

    static content = {
        showButton(to: EventResultDashboardPage) { $("#graphButtonHtmlId") }
        jobGroupList { $("#folderSelectHtmlId").find("option").contextElements }
        pageList { $("#pageSelectHtmlId").find("option").contextElements }
        timeFrameSelect { $("#timeframeSelect").find("option").contextElements[0] }
        fromDatepicker { $(".timerange-userinput-from") }
        toDatepicker { $(".timerange-userinput-to") }
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
        addAliasButton { $("#addAliasButton") }
        aliasInputField { $(".input-alias")[0] }
        graphName { $("span.label").attr("innerHTML") }
        colorPicker { $("#color") }
        graphColorField { $(".swatch").attr("style") }
        chartContainer { $("#rickshaw_main") }
        graphYGridFirstTick { $("#rickshaw_chart .y_grid .tick")[0].attr("data-y-value") }
        graphYGridLastTick { $("#rickshaw_chart .y_grid .tick")[-1].attr("data-y-value") }
        graphLines { $("#rickshaw_chart .path") }
        dataMarker { $(".pointMarker") }
        dataLabel { $(".dataLabel") }
        saveAsDashboardButton(required: false) { $("a", href: "#CreateUserspecifiedDashboardModal") }
        dashboardNameFromModalTextField(required: false) { $("#dashboardNameFromModal") }
        saveDashboardButtonButton { $("#saveDashboardButton") }
        saveDashboardSuccessMessage(required: false) { $("#saveDashboardSuccessDiv") }
        customDashboardSelectionDropdown { $("#customDashboardDropdownButton") }
        customDashboardSelectionList { $("#customDashBoardSelection") }
        appendedInputBelowLoadTimesTextField { $("#appendedInputBelowLoadTimes") }
        appendedInputAboveLoadTimesTextField { $("#appendedInputAboveLoadTimes") }
        appendedInputBelowRequestCountsTextField { $("#appendedInputBelowRequestCounts") }
        appendedInputAboveRequestCountsTextField { $("#appendedInputAboveRequestCounts") }
        appendedInputBelowRequestSizesTimesTextField { $("#appendedInputBelowRequestSizes") }
        appendedInputAboveRequestSizesTextField { $("#appendedInputAboveRequestSizes") }

        pageTab{$("a",href:"#page-tab")}
        browserTab{$("a",href:"#browser-tab")}
        connectivityTab{$("a",href:"#connectivity-tab")}
        selectBrowsersList{$("#selectedBrowsersHtmlId").find("option").contextElements }
        selectAllBrowserButton{$("#selectedAllBrowsers")}
        selectConnectivityProfilesList{$("#selectedConnectivityProfilesHtmlId").find("option").contextElements }
        selectAllConnectivityButton{$("#selectedAllConnectivityProfiles")}
        selectLocationField{$("#selectedLocationsHtmlId_chosen")}
        selectLocationList{$(".active-result")}
        selectAllLocationsButton{$("#selectedAllLocations")}

        tabJobSelection{$("#tabJobSelectionElement")}
        tabVariableSelection{$("#tabVariableSelectionElement")}

        adjustChartButton{$("#rickshaw_adjust_chart_link")}
        adjustChartApply{$("#adjustChartApply")}
        cardTabsUl{$("#erd-card-tabs")}

    }

    public clickVariableSelectionTab(){
        if (!isVariableSelectionTabActive()){
            tabVariableSelection.click()
        }
    }

    public boolean isVariableSelectionTabActive(){
        return cardTabsUl.children("li")[1].classes().contains("active")
    }

    public void scrollTop(){
        js.exec("scrollTo(0, 0);")
    }
    public void scrollBottom(){
        js.exec("scrollTo(0,document.body.scrollHeight);")
    }

    public void clickShowButton(){
        waitFor { showButton.displayed }
        scrollTop()
        waitFor { showButton.click() }
    }

    public insertIntoAboveRequestSizeTextField(String aboveRequestSizeValueToSet){
        waitFor { appendedInputAboveRequestSizesTextField.displayed }
        appendedInputAboveRequestSizesTextField << aboveRequestSizeValueToSet
    }

    public clearAboveRequestSizeTextField(){
        waitFor { appendedInputAboveRequestSizesTextField.displayed }
        appendedInputAboveRequestSizesTextField << Keys.chord(Keys.CONTROL, "a")
        appendedInputAboveRequestSizesTextField << Keys.chord(Keys.DELETE)
    }

    public void clickSaveAsDashboardButton() {
        // Scroll object into view so it becomes clickable
        sleep(100)
        String jqueryString = "jQuery(\'#graphButtonHtmlId\')[0].scrollIntoView();"
        js.exec(jqueryString)
        sleep(100)
        js.exec('document.body.scrollTop -= 70;') // scroll a little more cause of the sticky header
        sleep(100)
        $('.btn.btn-primary.dropdown-toggle').click()
        $('#createUserspecificDashboardButton').click()

    }
}
