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
        nameFilter{$("#filterByLabel")}
        jobGroupFilter{$("#filterByJobGroup")[0]}
        locationFilter{$("#filterByLocation")}
        tagFilter{$("#filterTags_chosen")}
        scriptFilter{$("#filterBySkript")}
        browserFilter{$("#filterByBrowser")}
        //Checkboxes
        showOnlyCheckedJobs{$("#filterCheckedJobs")}
        showOnlyHighlightedJobs{$("#filterHighlightedJobs")}
        showOnlyRunningJobs{$("#filterRunningJobs")}
        showInactiveJobsToo{$("#filterInactiveJobs")}
        checkAll{$("#checkAll")[0]}
        //Buttons
        activate{$("input",name:"_action_activate")}
        deactivate{$("input",name:"_action_deactivate")}
        executeNow{$("input",name:"_action_execute")}
        createJob{$("a",href:"/job/create")}
        //"Data"
        inactiveJobs{$(".jobName.inactiveJob")}
        activeJobs{$(".jobName").not(".inactiveJob")}
        allJobs{$(".jobName")}
        invisibleRows{$("tr", style: 'display: none;')}
    }

    def enableShowInactiveJobs(boolean b){
        changeCheckbox(showInactiveJobsToo, b)
        true
    }

    def enableShowOnlyCheckedJobs(boolean b){
        changeCheckbox(showOnlyCheckedJobs,b)
        true
    }

    def enableShowOnlyHighlightedJobs(boolean b){
        changeCheckbox(showOnlyHighlightedJobs,b)
        true
    }
    def enableShowOnlyRunningJobs(boolean b){
        changeCheckbox(showOnlyRunningJobs,b)
        true
    }
    def enableShowInactiveJobsToo(boolean b){
        changeCheckbox(showInactiveJobsToo,b)
        true
    }
    def enableCheckAll(boolean b){
        changeCheckbox(checkAll,b)
        true
    }




    void changeCheckbox(def box, boolean value){
        if(value){
            box.value("on")
        } else{
            box.value("false")
        }
    }
}