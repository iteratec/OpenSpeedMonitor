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

    /**
     * Adds tag to job to create and switches back to job setting tab again afterwards.
     * @param tagToAdd
     *          Tag to be added to Job to be created.
     */
    public void addTag(String tagToAdd){
        advancedSettingsTab.click()
        waitFor {
            tags.displayed
        }
        tags << tagToAdd
        jobSettingsTab.click()
        waitFor {
            cronString.displayed
        }
    }

    static content = {
        nameText{$("input",name:"label")}
        location{$("#location_chosen")}
        cronString{$("#execution-schedule-shown")}
        tags{$("#tags").find("input")}
        jobGroup{$("#jobgroup_chosen")}
        connection{$("#connectivityProfile_chosen")}
        createButton{$("input",name:"_action_save")}
        jobSettingsTab{$("div.card ul.nav.nav-tabs > li", 0)}
        scriptTab{$("div.card ul.nav.nav-tabs > li", 1)}
        advancedSettingsTab{$("div.card ul.nav.nav-tabs > li", 2)}
    }
}
