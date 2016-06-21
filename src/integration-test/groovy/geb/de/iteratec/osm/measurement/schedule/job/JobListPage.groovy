package geb.de.iteratec.osm.measurement.schedule.job

import geb.pages.de.iteratec.osm.I18nGebPage

class JobListPage extends I18nGebPage {


    static url = getUrl("/job/index")

    static at = {
//        title == getI18nMessage("springSecurity.login.title")
//        $("li", class:"controller active").text() == getI18nMessage("de.iteratec.isr.managementDashboard")
//        $("h5").text() == getI18nMessage("de.iteratec.sri.wptrd.jobs.filter.heading")
        true
    }


    static content = {
        nameFilter{$("#filterByLabel")}
        jobGroupFilter{$("#filterByJobGroup")}
        locationFilter{$("#filterByLocation")}
        tagFilter{$("#filterTags_chosen")}
        scriptFilter{$("#filterBySkript")}
        browserFilter{$("#filterByBrowser")}

        showOnlyCheckedJobs{$("#filterCheckedJobs")}
        showOnlyHighlightedJobs{$("#filterHighlightedJobs")}
        showOnlyRunningJobs{$("#filterRunningJobs")}
        showInactiveJobsToo{$("#filterInactiveJobs")}

        activate{$("input",name:"_action_activate")}
        deactivate{$("input",name:"_action_deactivate")}
        executeNow{$("input",name:"_action_execute")}
        createJob{$("a",href:"/job/create")}

        checkAll{$("#checkAll")}
    }
}