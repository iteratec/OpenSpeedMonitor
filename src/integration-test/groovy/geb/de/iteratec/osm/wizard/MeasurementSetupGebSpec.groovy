package geb.de.iteratec.osm.wizard

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.security.User
import geb.CustomUrlGebReportingSpec
import geb.pages.de.iteratec.osm.wizards.MeasurementSetupPage
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Shared
import spock.lang.Stepwise

@Integration
@Rollback
@Stepwise
class MeasurementSetupGebSpec extends CustomUrlGebReportingSpec{

    @Shared
    Location location

    @Shared
    String newJobGroupName = "New Job Group"

    @Shared
    String scriptCode = "setEventName\tHomepage:::Homepage\r\nnavigate\thttp://google.com"

    @Shared
    String jobName = "New Job Group local"

    @Shared
    String schedule = "0 * * * ? *"

    @Shared
    MeasurementSetupPage msPage

    def setupAndLogin(){
        User.withNewTransaction {
            OsmConfiguration.build()
            createAdminUser()
            location = Location.build(label: "location1", location: "local", active: true, wptServer: WebPageTestServer.build(active: true))
            ConnectivityProfile.build(active: true)
        }
        doLogin()
    }

    void cleanupSpec() {
        doLogout()
        User.withNewTransaction {
            Job.list().each { it.delete() }
            JobGroup.list().each { it.delete() }
            ConnectivityProfile.list().each { it.delete() }
            Location.list().each { it.delete() }
            WebPageTestServer.list().each { it.delete() }
            Browser.list().each { it.delete() }
            Script.list().each { it.delete() }
            OsmConfiguration.list().each { it.delete() }
        }
    }

    void "Test default values on first step"(){
        given: "user is logged in"
        setupAndLogin()
        to MeasurementSetupPage
        msPage = page as MeasurementSetupPage //so we got IDE support for our methods

        expect: "to be in the job tab, with no group name and a disabled next button"
        msPage.isJobGroupTabActive()
        !msPage.jobGroupName.text()
        !msPage.canContinueToScript()
    }

    void "setup can't be continued without a job group"(){
        expect: "the user is not able to continue the setup if no job group is selected"
        !msPage.canContinueToScript()
    }

    void "setup can be continued with a name for a new job group"() {
        when: "user enters a name for a new Job Group"
        msPage.selectNewJobGroup()
        msPage.jobGroupName << newJobGroupName

        then: "the user can continue to the script tab"
        msPage.canContinueToScript()
    }

    void "script page appears on next step"(){
        when: "the next button was clicked"
        msPage.continueToScript()

        then: "the script tab appears"
        msPage.isScriptTabActive()
    }

    void "next can't be continued, if the default code wasn't changed"(){
        expect: "to be still on the measurement setup page, but the script has an error and the next button is disabled"
        at MeasurementSetupPage
        !msPage.canContinueToLocation()
        msPage.scriptCodeHasErrors()
    }

    void "it should be possible to continue after editing the script code"(){
        when: "a valid script was entered"
        msPage.changeScript(scriptCode)

        then: "the script should'nt be marked with errors and the setup can be continued"
        !msPage.scriptCodeHasErrors()
        msPage.canContinueToLocation()
    }

    void "location tab should appear after click on next"(){
        when: "the next button was clicked"
        msPage.continueToLocation()

        then: "we should be still on the setup page, but the location tab should be active"
        at MeasurementSetupPage
        msPage.isLocationAndConnectivyTabActive()
    }

    void "previously created location and connectivity should appear and should be preselected"(){
        expect: "the location and connectivity to be preselected and all connectivities should appear"
        msPage.locationSelect.value() == location.ident().toString()
        msPage.getConnectivities().size() == ConnectivityProfile.count()
        ConnectivityProfile.list()*.name.contains(msPage.connectivitySelect.value())
        msPage.canContinueToJob()
    }

    void "continue to create job"(){
        when: "the next button was clicked"
        msPage.continueToJob()

        then: "we should still be on the setup page, but the final tab should appear"
        at MeasurementSetupPage
        msPage.isJobCreateTabActive()
    }

    void "job create defaults"(){
        expect: "the job name to be set and a valid schedule to be selected, so that the setup could be finished"
        msPage.jobNameInput.value() == jobName
        msPage.executionScheduleSelect.value() == "0 * * * ? *"
        msPage.executionScheduleInput.@readonly
        msPage.canClickCreateButton()
    }

    void "can't continue with empty jobName"(){
        when: "we delete the job name"
        msPage.jobNameInput.firstElement().clear()
        then: "the setup can't be finished"
        !msPage.canClickCreateButton()
    }

    void "check every execution schedule to be valid"(){
        given:
        msPage.jobNameInput << jobName //to make sure we got a valid jobname

        when: "the 15 minute interval was selected"
        msPage.select15MinuteInterval()
        then: "the finish button should be clickable"
        msPage.canClickCreateButton()

        when: "the 30 minute interval was selected"
        msPage.select30MinuteInterval()
        then: "the finish button should be clickable"
        msPage.canClickCreateButton()

        when: "the hourly interval was selected"
        msPage.selectHourlyInterval()
        then: "the finish button should be clickable"
        msPage.canClickCreateButton()

        when: "the daily interval was selected"
        msPage.selectDailyInterval()
        then: "the finish button should be clickable"
        msPage.canClickCreateButton()
    }

    void "custom execution schedule"(){
        given: "the custom interval was selected"
        msPage.selectCustomInterval()

        when: "a invalid schedule was entered"
        msPage.clearExecutionScheduleInput()
        msPage.executionScheduleInput << "0"

        then: "the finish button should't be clickable"
        waitFor(5){
            !msPage.canClickCreateButton()
        }

        when: "a valid schedule was entered"
        msPage.clearExecutionScheduleInput()
        msPage.executionScheduleInput << schedule

        then: "the button should be clickable again"
        msPage.canClickCreateButton()
    }

    void "the created job should exist"(){
        when: "the setup was finished"
        msPage.clickCreateButton()
        Job job = Job.findByLabel(jobName)

        then: "the job with the defined values should exist"
        job
        job.location == location
        job.script.navigationScript == scriptCode
        job.executionSchedule == "0 " + schedule
        job.jobGroup
        job.jobGroup.name == newJobGroupName
        job.script
        job.script.label == newJobGroupName
        job.script.navigationScript == scriptCode
        Job.list().size() == 1
    }
}

