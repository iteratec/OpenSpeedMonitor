package geb.de.iteratec.osm.measurement.schedule.job

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.security.Role
import de.iteratec.osm.security.User
import de.iteratec.osm.security.UserRole
import de.iteratec.osm.util.OsmTestLogin
import geb.CustomUrlGebReportingSpec
import geb.pages.de.iteratec.osm.LoginPage
import geb.pages.de.iteratec.osm.measurement.schedule.job.JobCreatePage
import geb.pages.de.iteratec.osm.measurement.schedule.job.JobListPage
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import org.openqa.selenium.Keys
import spock.lang.Shared
import spock.lang.Stepwise

@Integration
@Rollback
@Stepwise
class JobListGebSpec extends CustomUrlGebReportingSpec implements OsmTestLogin {

    @Shared
    String script1Name = "TestScript1-564892#Afef1"
    @Shared
    String script2Name = "TestScript2-564892#Afef1"
    @Shared
    String job1Name = "TestJob1-564892#Afef1"
    @Shared
    String job2Name = "TestJob2-564892#Afef1"
    @Shared
    String job3Name = "TestJob3-564892#Afef1"
    @Shared
    String job4Name = "TestJob4-564892#Afef1"
    @Shared
    String location1Name = "TestLocation1-564892#Afef1"
    @Shared
    String location2Name = "TestLocation2-564892#Afef1"
    @Shared
    String jobGroup1Name = "TestJobGroup1-564892#Afef1"
    @Shared
    String jobGroup2Name = "TestJobGroup2-564892#Afef1"
    @Shared
    String job4Tag = "TestingTag-564892#Afef1"
    @Shared
    String browserName = "This is the very best browser i've ever seen"
    @Shared
    JobListPage page


    void "Anonymous can't create a job"() {
        given: "User is logged in"
        createData()
        page = to JobListPage

        when: "User wants to create a job"
        page.createJobButton.click()

        then: "He will be redirected to the Login Page"
        waitFor {
            at LoginPage
        }
    }

    void "User is able to reach create job page"() {
        given: "User is logged in"
        doLogin()
        page = to JobListPage

        when: "The user is able to click the create job button and fill the data in"
        page.createJobButton.click()
        then: "The user should see the create page"
        waitFor {
            at JobCreatePage
        }
    }

    void "User is able to create a Job"() {
        given:
        def createPage = to JobCreatePage
        waitFor { at JobCreatePage }

        when: "The user fills out all necessary data and submits"
        createPage.setLocation(location2Name)
        createPage.nameText << job4Name
        createPage.selectCustomCronString("10/20 * * * ? *")
        createPage.addTag(job4Tag)
        createPage.scriptTab.click()
        createPage.selectScript(script1Name)

        createPage.clickCreateButton()

        then: "There should be the new created job in the list and it should be highlighted"
        waitFor {
            at JobListPage
        }
        page.enableShowOnlyActiveJobs(false)
        page.allJobs.find { it.text().contains(job4Name) }
    }

    void "Disable show inactive jobs too"() {
        when: "The user clicks the show only active button"
        page.enableShowOnlyActiveJobs(true)

        then: "There should be no jobs visible since all are inacrive"
        $("tr").findAll { it.displayed }.find(".jobName").size() == 0
    }

    void "Enable show inactive jobs too"() {
        when: "The user clicks the show only active button"
        page.enableShowOnlyActiveJobs(false)

        then: "There should be (inactive) jobs in the list"
        page.inactiveJobs.size() > 0
    }

    void "Check all Boxes"() {
        given: "User is on the job list and inactive jos are shown"
        page.enableShowOnlyActiveJobs(false)

        when: "The user clicks the check all checkbox"
        page.enableCheckAll(true)

        then: "There should be no job checkbox, which is not selected"
        $("tbody").find(".jobCheckbox").findAll { it.value() == false }.size() == 0
    }

    void "Uncheck all Boxes"() {
        given: "User is on the job list and inactive jobs are shown"
        page.enableShowOnlyActiveJobs(false)

        when: "The user unmarks the checkall checkbox"
        page.enableCheckAll(false)

        then: "There should be no job checkbox, which is selected"
        $("tbody").find(".jobCheckbox").findAll { it.value() == true }.size() == 0
    }

    void "Show only selected jobs"() {
        given: "User is on the job list, inactive jobs are shown and no job checkbox is selected"
        page.enableShowOnlyActiveJobs(false)
        page.enableCheckAll(false)

        when: "The user marks the first job and select the box to show only marked jobs"
        $(".jobCheckbox")[0].value("on")
        page.enableShowOnlyCheckedJobs(true)

        then: "There should just be the one selected entry and the rest should be hidden"
        page.invisibleRows.size() == 3
    }

    void "Disable show only selected Jobs"() {
        given: "User is on the job list, inactive jobs are shown and no job checkbox is selected"
        page.enableShowOnlyActiveJobs(false)
        page.enableCheckAll(false)

        when: "The user marks the first job and select the box to show only marked jobs"
        page.enableShowOnlyCheckedJobs(false)

        then: "There should be no hided row"
        page.invisibleRows.size() == 0
    }

    void "Filter Job by name"() {
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowOnlyActiveJobs(false)

        when: "The user enables the name filter and types the name of a job in the job textbox"
        page.enableFilterByButtons([page.filterByNameButton])
        page.filterTextbox << job1Name

        then: "There should just be the one selected entry and the rest should be hided"
        page.invisibleRows.size() == 3
    }

    void "Reset filter Job by name"() {
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowOnlyActiveJobs(false)

        when: "The user deletes the input from the text field"
        page.filterTextbox.value(Keys.chord(Keys.CONTROL, "A") + Keys.BACK_SPACE)

        then: "There should be no hided row"
        page.invisibleRows.size() == 0
    }

    void "Filter Job by JobGroup"() {
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowOnlyActiveJobs(false)

        when: "The user enables the job group filter types the name of a job in the filter textbox"
        page.enableFilterByButtons([page.filterByJobGroupButton])
        page.filterTextbox << jobGroup2Name

        then: "There should just be the one selected entry and the rest should be hided"
        page.invisibleRows.size() == 3
    }

    void "Reset filter Job by JobGroup"() {
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowOnlyActiveJobs(false)

        when: "The user deletes the input from the text field"
        page.filterTextbox.value(Keys.chord(Keys.CONTROL, "A") + Keys.BACK_SPACE)

        then: "There should be no hided row"
        page.invisibleRows.size() == 0
    }


    void "Filter Job by Tag"() {
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowOnlyActiveJobs(false)

        when: "The user enables the tag filter and types the name of a tag in the filter textbox"
        page.enableFilterByButtons([page.filterByTagsButton])
        page.filterTextbox << job4Tag

        then: "There should just be the one selected entry and the rest should be hided"
        page.invisibleRows.size() == 3
    }

    void "Reset filter Job by Tag"() {
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowOnlyActiveJobs(false)

        when: "The user deletes the input from the text field"
        page.filterTextbox.value(Keys.chord(Keys.CONTROL, "A") + Keys.BACK_SPACE)

        then: "There should be no hided row"
        page.invisibleRows.size() == 0
    }

    void "Filter Job by Script"() {
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowOnlyActiveJobs(false)

        when: "The user enables the script filter and types the name of a script in the filter textbox"
        page.enableFilterByButtons([page.filterByScriptButton])
        page.filterTextbox << script1Name

        then: "There should just be the one selected entry and the rest should be hided"
        page.invisibleRows.size() == 1
    }

    void "Reset filter Job by Script"() {
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowOnlyActiveJobs(false)

        when: "The user deletes the input from the text field"
        page.filterTextbox.value(Keys.chord(Keys.CONTROL, "A") + Keys.BACK_SPACE)

        then: "There should be no hidden row"
        page.invisibleRows.size() == 0
    }

    void "Filter Job by Browser"() {
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowOnlyActiveJobs(false)

        when: "The user enables the browser filter and types the name of a browser in the filter textbox"
        page.enableFilterByButtons([page.filterByBrowserButton])
        page.filterTextbox << browserName


        then: "There should be no hidden row"
        page.invisibleRows.size() == 0
    }

    void "Reset filter Job by Browser"() {
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowOnlyActiveJobs(false)

        when: "The user deletes the input from the text field"
        page.filterTextbox.value(Keys.chord(Keys.CONTROL, "A") + Keys.BACK_SPACE)

        then: "There should be no hided row"
        page.invisibleRows.size() == 0
    }

    void "Filter Job by Group and Location"() {
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowOnlyActiveJobs(false)

        when: "The user enables the browser filter and types the name of a browser in the filter textbox"
        page.enableFilterByButtons([page.filterByJobGroupButton, page.filterByLocationButton])
        page.filterTextbox << jobGroup1Name << " " << location2Name


        then: "There should be 2 hidden rows"
        page.invisibleRows.size() == 2
    }

    void "Reset filter Job by Group and Location"() {
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowOnlyActiveJobs(false)

        when: "The user deletes the input from the text field"
        page.filterTextbox.value(Keys.chord(Keys.CONTROL, "A") + Keys.BACK_SPACE)

        then: "There should be no hided row"
        page.invisibleRows.size() == 0
    }

    void "Activate Job"() {
        given: "User is on the job list, inactive jobs are shown, no job is selected"
        page.enableShowOnlyActiveJobs(true)
        def visibleActiveJobsBefore = page.countVisibleActiveJobs()
        page.enableShowOnlyActiveJobs(false)
        page.enableCheckAll(true)
        page.enableCheckAll(false)


        when: "The user activates a job"
        page.changeCheckbox(page.getCheckboxForJobName(job4Name), true)
        page.clickActivateJob()

        then: "There should be one more active job"
        at JobListPage
        sleep(2000)//Otherwise the element is not clickable with chrome
        page.enableShowOnlyActiveJobs(true)
        page.countVisibleActiveJobs() == visibleActiveJobsBefore + 1
    }

    void "Deactivate Job"() {
        given: "User is on the job list, only active jobs are shown"
        def visibleActiveJobsBefore = page.countVisibleActiveJobs()

        when: "The user deletes the input from the text field"
        page.changeCheckbox(page.getCheckboxForJobName(job4Name), true)
        page.clickDeactivateJob()

        then: "There should be no hided row"
        at JobListPage
        sleep(2000)//Otherwise the element is not clickable with chrome
        page.countVisibleActiveJobs() == visibleActiveJobsBefore - 1
    }

    void cleanupSpec() {
        doLogout()
        Job.withNewTransaction {
            Job.list().each {
                it.delete()
            }
            Location.list().each {
                it.delete()
            }
            Browser.list().each {
                it.delete()
            }
            WebPageTestServer.list().each {
                it.delete()
            }
            JobGroup.list().each {
                it.delete()
            }
            Script.list().each {
                it.delete()
            }
            UserRole.list().each {
                it.delete()
            }
            User.list().each {
                it.delete()
            }
            Role.list().each {
                it.delete()
            }
            OsmConfiguration.list().each {
                it.delete()
            }
        }
    }

    private void createData() {

        Job.withNewTransaction {
            OsmConfiguration.build()
            createAdminUser()

            Script script1 = Script.build(label: script1Name, description: "This is for test purposes", navigationScript: "stuff")
            Script script2 = Script.build(label: script2Name, description: "This is also for test purposes", navigationScript: "stuff")
            JobGroup jobGroup1 = JobGroup.build(name: jobGroup1Name)
            JobGroup jobGroup2 = JobGroup.build(name: jobGroup2Name)
            WebPageTestServer wpt = WebPageTestServer.build(label: "TestWPTServer-564892#Afef1", proxyIdentifier: "TestIdentifier", active: true, baseUrl: "http://internet.de")

            Browser browser = Browser.build(name: browserName)
            Location location1 = Location.build(wptServer: wpt, uniqueIdentifierForServer: location1Name, browser: browser, active: true)
            Location location2 = Location.build(wptServer: wpt, uniqueIdentifierForServer: location2Name, browser: browser, active: true)
            Job.build(label: job1Name, script: script1, location: location1, jobGroup: jobGroup1, description: "This is the first test job", runs: 1, active: false, maxDownloadTimeInMinutes: 12)
            Job.build(label: job2Name, script: script2, location: location2, jobGroup: jobGroup1, description: "This is the second test job", runs: 1, active: false, maxDownloadTimeInMinutes: 12)
            Job.build(label: job3Name, script: script1, location: location2, jobGroup: jobGroup2, description: "This is the third test job", runs: 1, active: false, maxDownloadTimeInMinutes: 12)
        }
    }
}
