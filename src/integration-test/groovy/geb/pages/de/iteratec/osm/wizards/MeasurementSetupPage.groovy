package geb.pages.de.iteratec.osm.wizards

import geb.pages.de.iteratec.osm.I18nGebPage

class MeasurementSetupPage extends I18nGebPage {

    static url = getUrl("/measurementSetup/create")

    static at = {
        title == getI18nMessage("de.iteratec.osm.setupMeasurementWizard.title")
    }

    void selectNewJobGroup() {
        jobSelect.click()
        $("#jobGroupSelect_chosen").find(".group-option").find {
            it.text() == getI18nMessage("default.button.create.new")
        }.click()
    }

    boolean canContinueToScript() {
        !nextButtonJobGroup.@disabled
    }

    boolean canContinueToLocation() {
        !nextButtonScript.@disabled
    }

    boolean canContinueToJob() {
        !nextButtonLocation.@disabled
    }

    void continueToScript() {
        nextButtonJobGroup.click()
    }

    void continueToLocation() {
        nextButtonScript.click()
    }

    void continueToJob() {
        nextButtonLocation.click()
    }

    boolean canClickCreateButton() {
        !createButton.@disabled
    }

    void clickCreateButton() {
        createButton.click()
    }


    boolean isJobGroupTabActive() {
        return isTabActive("#setJobGroupTab")
    }

    boolean isScriptTabActive() {
        return isTabActive("#createScriptTab")
    }

    boolean isLocationAndConnectivyTabActive() {
        return isTabActive("#selectLocationAndConnectivityTab")
    }

    boolean isJobCreateTabActive() {
        return isTabActive("#createJobTab")
    }

    private boolean isTabActive(String selector) {
        $(selector).parent().hasClass("active")
    }

    List<String> getConnectivities() {
        $("#inputConnectivity").find("option").collect { it.@value }
    }


    boolean scriptCodeHasErrors() {
        scriptHelpBlock.text()
    }

    void changeScript(String scriptCode) {
        js.exec(scriptCode, "OpenSpeedMonitor.script.codemirrorEditor.setNewContent(arguments[0])")
        js.exec("OpenSpeedMonitor.MeasurementSetupWizard.CreateScriptCard.validate()")
    }

    void select30MinuteInterval() {
        selectExecutionPlan("de.iteratec.osm.setupMeasurementWizard.selectExecutionSchedule.halfHourly")
    }

    void select15MinuteInterval() {
        selectExecutionPlan("de.iteratec.osm.setupMeasurementWizard.selectExecutionSchedule.15min")
    }

    void selectDailyInterval() {
        selectExecutionPlan("de.iteratec.osm.setupMeasurementWizard.selectExecutionSchedule.daily")
    }

    void selectHourlyInterval(){
        selectExecutionPlan("de.iteratec.osm.setupMeasurementWizard.selectExecutionSchedule.hourly")
    }

    void selectExecutionPlan(String i18nMessage) {
        executionScheduleSelectChosen.click()
        $("#selectExecutionSchedule_chosen").find(".group-option").find { it.text() == getI18nMessage(i18nMessage) }.click()
    }

    void clearExecutionScheduleInput(){
        executionScheduleInput.firstElement().clear()
    }

    void selectCustomInterval(){
        selectExecutionPlan("de.iteratec.isocsi.custom")
    }


    static content = {
        jobSelect { $("#jobGroupSelect_chosen") }
        jobGroupName { $("#inputNewJobGroupName") }
        nextButtonJobGroup { $("#setJobGroubTabNextButton") }
        nextButtonScript { $("#createScriptTabNextButton") }
        nextButtonLocation { $("#selectLocationAndConnectivityTabNextButton") }
        previousButton { $("#selectLocationAndConnectivityTabPreviousButton") }
        scriptName { $("#inputScriptName") }
        scriptDescirption { $("#inputScriptDescription") }
        scriptHelpBlock { $("#navigationScriptHelpBlock") }
        locationSelect { $("#inputLocation") }
        connectivitySelect { $("#inputConnectivity") }
        jobNameInput { $("#inputJobName") }
        executionScheduleSelectChosen { $("#selectExecutionSchedule_chosen") }
        executionScheduleSelect { $("#selectExecutionSchedule") }
        executionScheduleInput { $("#executionSchedule") }
        createButton { $("#createJobTabCreationButton") }
    }

}
