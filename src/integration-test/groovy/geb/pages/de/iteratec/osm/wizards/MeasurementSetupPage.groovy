package geb.pages.de.iteratec.osm.wizards

import geb.pages.de.iteratec.osm.I18nGebPage

class MeasurementSetupPage extends I18nGebPage {

    static url = getUrl("/measurementSetup/create")

    static at = {
        title == getI18nMessage("de.iteratec.osm.setupMeasurementWizard.title")
    }

    void selectUndefinedJobGroup(){
        jobSelect.click()
        $("#jobGroupSelect_chosen").find(".group-option").find{it.text() == "undefined"}.click()
    }

    void selectNewJobGroup(){
        jobSelect.click()
        $("#jobGroupSelect_chosen").find(".group-option").find{it.text() == getI18nMessage("default.button.create.new")}.click()
    }

    boolean canContinueToScript(){
        !nextButtonJobGroup.@disabled
    }

    boolean canContinueToLocation(){
        !nextButtonScript.@disabled
    }

    boolean canContinueToJob(){
        !nextButtonLocation.@disabled
    }

    void continueToScript(){
        nextButtonJobGroup.click()
    }

    void continueToLocation(){
        nextButtonScript.click()
    }

    void continueToJob(){
        nextButtonLocation.click()
    }


    boolean isJobGroupTabActive(){
        return isTabActive("#setJobGroupTab")
    }

    boolean isScriptTabActive(){
        return isTabActive("#createScriptTab")
    }

    boolean isLocationAndConnectivyTabActive(){
        return isTabActive("#selectLocationAndConnectivityTab")
    }

    boolean isJobCreateTabActive(){
        return isTabActive("#createJobTab")
    }

    private boolean isTabActive(String selector){
        $(selector).parent().hasClass("active")
    }


    boolean scriptCodeHasErrors(){
        scriptHelpBlock.text()
    }

    void changeScript(String scriptCode){
        js.exec(scriptCode,"OpenSpeedMonitor.script.codemirrorEditor.setNewContent(arguments[0])")
        js.exec("OpenSpeedMonitor.MeasurementSetupWizard.CreateScriptCard.validate()")
    }


    static content = {
        jobSelect { $("#jobGroupSelect_chosen") }
        jobGroupName { $("#inputNewJobGroupName")}
        nextButtonJobGroup { $("#setJobGroubTabNextButton")}
        nextButtonScript { $("#createScriptTabNextButton")}
        nextButtonLocation { $("#selectLocationAndConnectivityTabNextButton")}
        previousButton { $("#selectLocationAndConnectivityTabPreviousButton")}
        scriptName { $("#inputScriptName")}
        scriptDescirption { $("#inputScriptDescription")}
        scriptHelpBlock {$("#navigationScriptHelpBlock")}
        locationSelect { $("#inputLocation_chosen")}
        connectivitySelect { $("#inputConnectivity_chosen")}
        jobNameInput {$("#inputJobName")}
        executionScheduleSelect {$("#selectExecutionSchedule_chosen")}
        executionScheduleInput {$("#executionSchedule")}
        createButton {$("#createJobTabCreationButton")}
    }

}
