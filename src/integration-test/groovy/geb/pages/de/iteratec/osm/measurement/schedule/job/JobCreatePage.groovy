package geb.pages.de.iteratec.osm.measurement.schedule.job

import geb.pages.de.iteratec.osm.I18nGebPage
import org.openqa.selenium.Keys

class JobCreatePage extends I18nGebPage {

    static url = getUrl("/job/create")

    static at = {
        title == "Create Job"
    }

    void selectCustomCronString(String customCronString) {
        executionPlanSelect.click()
        executionPlanSelect.find("option").find{ it.value() == "" }.click()
        executionScheduleInput.value(Keys.chord(Keys.CONTROL, "A") + Keys.BACK_SPACE)
        executionScheduleInput << customCronString
    }

    void selectScript(String scriptName) {
        scriptSelect.click()
        waitFor {
            scriptSelect.isDisplayed()
        }
        scriptSelect.find("li").find { it.text() == scriptName }.click()
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
            executionScheduleInput.displayed
        }
    }

    public void setLocation(String locationToSet) {
        location.click() //Open the dropdown to choose location, otherwise the options won't be visible
        waitFor {
            location.isDisplayed()
        }
        $("#location_chosen").find("li").find { it.text() == locationToSet }.click()
    }

    static content = {
        nameText{$("input",name:"label")}
        location(wait: true){$("#location_chosen")}
        executionScheduleInput {$("#executionSchedule")}
        tags{$("#tags").find("input")}
        jobGroup{$("#jobgroup_chosen")}
        connection{$("#connectivityProfile_chosen")}
        createButton{$("input",name:"_action_save")}
        jobSettingsTab{$("#jobSettingsTabLink")}
        scriptTab{$("#scriptTabLink")}
        advancedSettingsTab{$("#advancedSettingsTabLink")}
        executionPlanSelect{$("#selectExecutionSchedule")}
        scriptSelect{$("#script_chosen")}
    }
}
