package geb.pages.de.iteratec.osm.measurement.schedule.job

import geb.pages.de.iteratec.osm.I18nGebPage

class JobListPage extends I18nGebPage {


    static url = getUrl("/job/index")

    static at = {
        title == "Jobs"
        $("li", class:"controller active").text() == getI18nMessage("de.iteratec.isr.managementDashboard")
        $("h5").text() == getI18nMessage("de.iteratec.sri.wptrd.jobs.filter.heading")
    }


    static content = {
        //Filter
        nameFilterTextbox{$("#filterByLabel")}
        jobGroupFilterTextbox{$("#filterByJobGroup")[0]}
        locationFilterTextbox{$("#filterByLocation")}
        tagFilterTextbox{$("#filterTags_chosen").find("input")}
        scriptFilterTextbox{$("#filterBySkript")}
        browserFilterTextbox{$("#filterByBrowser")}
        //Checkboxes
        showOnlyCheckedJobsCheckbox{$("#filterCheckedJobs")}
        showOnlyHighlightedJobsCheckbox{$("#filterHighlightedJobs")}
        showOnlyRunningJobsCheckbox{$("#filterRunningJobs")}
        showInactiveJobsTooCheckbox{$("#filterInactiveJobs")}
        checkAll{$("#checkAll")[0]}
        //Buttons
        activateButton{$("input",name:"_action_activate")}
        deactivateButton{$("input",name:"_action_deactivate")}
        executeNowButton{$("input",name:"_action_execute")}
        createJobButton{$("a",href:"/job/create")}
        //"Data"
        inactiveJobs{$(".jobName.inactiveJob")}
        activeJobs{$(".jobName").not(".inactiveJob")}
        allJobs{$(".jobName")}
        invisibleRows(required:false){$("tr", style: 'display: none;')}
    }

    def enableShowInactiveJobs(boolean b){
        changeCheckbox(showInactiveJobsTooCheckbox, b)
        true
    }

    def enableShowOnlyCheckedJobs(boolean b){
        changeCheckbox(showOnlyCheckedJobsCheckbox,b)
        true
    }

    def enableShowOnlyHighlightedJobs(boolean b){
        changeCheckbox(showOnlyHighlightedJobsCheckbox,b)
        true
    }
    def enableShowOnlyRunningJobs(boolean b){
        changeCheckbox(showOnlyRunningJobsCheckbox,b)
        true
    }
    def enableShowInactiveJobsToo(boolean b){
        changeCheckbox(showInactiveJobsTooCheckbox,b)
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


    void changeCheckbox(def box, boolean value){
        if(value){
            box.value("on")
        } else{
            box.value("false")
        }
    }
}