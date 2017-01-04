package geb.pages.de.iteratec.osm.measurement.schedule.job

import geb.pages.de.iteratec.osm.I18nGebPage

class JobCreatePage extends I18nGebPage {

    static url = getUrl("/job/create")

    static at = {
        title == "Create Job"
    }

    public void scrollBottom(){
        js.exec("scrollTo(0,document.body.scrollHeight);")
    }

    public void clickCreateButton(){
        waitFor { createButton.displayed }
        scrollBottom()
        sleep(200)
        createButton.click()
    }

    static content = {
        nameText{$("input",name:"label")}
        location{$("#location_chosen")}
        cronString{$("#execution-schedule-shown")}
        tags{$("#tags").find("input")}
        jobGroup{$("#jobgroup_chosen")}
        connection{$("#connectivityProfile_chosen")}
        createButton{$("input",name:"_action_save")}
    }
}
