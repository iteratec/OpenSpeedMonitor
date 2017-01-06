package geb.pages.de.iteratec.osm.measurement.schedule.job

import geb.pages.de.iteratec.osm.I18nGebPage

class JobListPage extends I18nGebPage {

    static url = getUrl("/job/index")

    static at = {
        title == "Jobs"
        $("li", class:"controller active").text() == getI18nMessage("de.iteratec.isr.managementDashboard")
    }


    static content = {
        //Filter
        filterTextbox{$("#filterInput")}
        filterByNameButton{$("#filterByName").parent()}
        filterByJobGroupButton{$("#filterByJobGroup").parent()}
        filterByLocationButton{$("#filterByLocation").parent()}
        filterByTagsButton{$("#filterByTags").parent()}
        filterByScriptButton{$("#filterByScript").parent()}
        filterByBrowserButton{$("#filterByBrowser").parent()}
        //Checkboxes
        showOnlyCheckedJobsButton{$("#filterCheckedJobs").parent()}
        showOnlyHighlightedJobsButton{$("#filterHighlightedJobs").parent()}
        showOnlyRunningJobsButton{$("#filterRunningJobs").parent()}
        showOnlyActiveJobsButton{$("#filterActiveJobs").parent()}
        checkAll{$("#checkAll")[0]}
        //Buttons
        actionMenuButton{$("#actionForSelected")}
        activateButton{$("input",name:"_action_activate")}
        deactivateButton{$("input",name:"_action_deactivate")}
        executeNowButton{$("input",name:"_action_execute")}
        createJobButton{$("a",href:"/job/create")}
        //"Data"
        inactiveJobs{$(".jobName.inactiveJob")}
        activeJobs{$(".jobName").not(".inactiveJob")}
        allJobs{$(".jobName")}
        invisibleRows(required:false){$("tr.hidden")}
    }

    def enableFilterByButtons(List enabledButtons) {
        def allButtons = [filterByJobGroupButton, filterByLocationButton, filterByTagsButton, filterByScriptButton,
                          filterByBrowserButton, filterByNameButton]
        allButtons.each {
            changeButton(it, it in enabledButtons)
        }
    }

    def enableShowOnlyActiveJobs(boolean b){
        changeButton(showOnlyActiveJobsButton, b)
        true
    }

    def enableShowOnlyCheckedJobs(boolean b){
        changeButton(showOnlyCheckedJobsButton,b)
        true
    }

    def enableShowOnlyHighlightedJobs(boolean b){
        changeButton(showOnlyHighlightedJobsButton,b)
        true
    }
    def enableShowOnlyRunningJobs(boolean b){
        changeButton(showOnlyRunningJobsButton,b)
        true
    }
    def enableCheckAll(boolean b){
        changeCheckbox(checkAll,b)
        true
    }

    int countVisibleActiveJobs(){
        $(".jobName").not(".inactiveJob").size()
    }

    def getCheckboxForJobName(String jobname){
        $("a",text: jobname).parent().parent().parent().find(".jobCheckbox")
    }

    def clickActivateJob() {
        actionMenuButton.click()
        waitFor {
            activateButton.displayed
        }
        activateButton.click()
    }

    def clickDeactivateJob() {
        actionMenuButton.click()
        waitFor {
            deactivateButton.displayed
        }
        deactivateButton.click()
    }

    void changeButton(def button, boolean active) {
        if (button.hasClass("active") != active) {
            button.click()
        }
    }

    void changeCheckbox(def box, boolean value){
        if(value){
            box.value("on")
        } else{
            box.value("false")
        }
    }
}
