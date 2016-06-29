package geb.de.iteratec.osm.measurement.schedule.job

import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.util.OsmTestLogin
import geb.CustomUrlGebReportingSpec
import geb.pages.de.iteratec.osm.LoginPage
import geb.pages.de.iteratec.osm.measurement.schedule.job.JobCreatePage
import geb.pages.de.iteratec.osm.measurement.schedule.job.JobListPage
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.openqa.selenium.Keys
import spock.lang.Shared
import spock.lang.Stepwise

@Integration
@Rollback
@Stepwise
class JobListGebSpec extends CustomUrlGebReportingSpec implements OsmTestLogin {

    @Shared String script1Name = "TestScript1-564892#Afef1"
    @Shared String script2Name = "TestScript2-564892#Afef1"
    @Shared String job1Name = "TestJob1-564892#Afef1"
    @Shared String job2Name = "TestJob2-564892#Afef1"
    @Shared String job3Name = "TestJob3-564892#Afef1"
    @Shared String job4Name = "TestJob4-564892#Afef1"
    @Shared String location1Name = "TestLocation1-564892#Afef1"
    @Shared String Location2Name = "TestLocation2-564892#Afef1"
    @Shared String jobGroup1Name = "TestJobGroup1-564892#Afef1"
    @Shared String jobGroup2Name = "TestJobGroup2-564892#Afef1"
    @Shared String job4Tag = "TestingTag-564892#Afef1"
    @Shared String browserName = "This is the very best browser i've ever seen"
    @Shared JobListPage page



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
        waitFor{
            at JobCreatePage
        }
    }

    void "User is able to create a Job"(){
        given:
        def createPage = to JobCreatePage

        when: "The user fills out all necessary data and submits"
        createPage.location.click() //Open the dropdown to choose location, otherwise the options won't be visible
        waitFor{
            createPage.location.isDisplayed()
        }
        $("#location_chosen").find("li").find{it.text() == location2Name}.click()
        createPage.nameText << job4Name
        createPage.cronString << "10/20 * * * ? *"
        createPage.tags << job4Tag
        createPage.createButton.click()

        then: "There should be the new created job in the list and it should be highlighted"
        waitFor {
            at JobListPage
        }
        enableShowInactiveJobs(true)
        $(".highlight.success").text().startsWith(job4Name)
    }

    void "Disable show inactive jobs too"(){
        when:"The user clicks a checkbox"
        page.enableShowInactiveJobs(false)

        then:"There should be jobs visible which are also inactive"
        $("tr").findAll{it.displayed}.find(".jobName").size() == 0
    }

    void "Enable show inactive jobs too"(){
        when:"The user clicks a checkbox"
        page.enableShowInactiveJobs(true)

        then:"There should be no inactive jobs"
        page.inactiveJobs.size()>0
    }

    void "Check all Boxes"(){
        given: "User is on the job list and inactive jos are shown"
        page.enableShowInactiveJobs(true)

        when:"The user clicks the check all checkbox"
        page.enableCheckAll(true)

        then:"There should be no job checkbox, which is not selected"
        $("tbody").find(".jobCheckbox").findAll{it.value() == false}.size() == 0
    }

    void "Uncheck all Boxes"(){
        given: "User is on the job list and inactive jobs are shown"
        page.enableShowInactiveJobs(true)

        when:"The user unmarks the checkall checkbox"
        page.enableCheckAll(false)

        then:"There should be no job checkbox, which is selected"
        $("tbody").find(".jobCheckbox").findAll{it.value() == true}.size() == 0
    }

    void "Show only selected jobs"(){
        given: "User is on the job list, inactive jobs are shown and no job checkbox is selected"
        page.enableShowInactiveJobs(true)
        page.enableCheckAll(false)

        when:"The user marks the first job and select the box to show only marked jobs"
        $(".jobCheckbox")[0].value("on")
        page.enableShowOnlyCheckedJobs(true)

        then:"There should just be the one selected entry and the rest should be hided"
        page.invisibleRows.size() == 3
    }

    void "Disable show only selected Jobs"(){
        given: "User is on the job list, inactive jobs are shown and no job checkbox is selected"
        page.enableShowInactiveJobs(true)
        page.enableCheckAll(false)

        when:"The user marks the first job and select the box to show only marked jobs"
        page.enableShowOnlyCheckedJobs(false)

        then:"There should be no hided row"
        page.invisibleRows.size() == 0
    }

    void "Filter Job by name"(){
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowInactiveJobs(true)

        when:"The user types the name of a job in the job filter"
        page.nameFilterTextbox << job1Name

        then:"There should just be the one selected entry and the rest should be hided"
        page.invisibleRows.size() == 3
    }

    void "Reset filter Job by name"(){
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowInactiveJobs(true)

        when:"The user deletes the input from the text field"
        page.nameFilterTextbox.value(Keys.chord(Keys.CONTROL, "A")+Keys.BACK_SPACE)

        then:"There should be no hided row"
        page.invisibleRows.size() == 0
    }

    void "Filter Job by JobGroup"(){
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowInactiveJobs(true)

        when:"The user types the name of a job in the job filter"
        page.jobGroupFilterTextbox << jobGroup2Name

        then:"There should just be the one selected entry and the rest should be hided"
        page.invisibleRows.size() == 3
    }

    void "Reset filter Job by JobGroup"(){
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowInactiveJobs(true)

        when:"The user deletes the input from the text field"
        page.jobGroupFilterTextbox.value(Keys.chord(Keys.CONTROL, "A")+Keys.BACK_SPACE)

        then:"There should be no hided row"
        page.invisibleRows.size() == 0
    }


    void "Filter Job by Tag"(){
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowInactiveJobs(true)

        when:"The user types the name of a job in the job filter"
        page.tagFilterTextbox.click()
        page.tagFilterTextbox << job4Tag
        page.tagFilterTextbox << Keys.ENTER

        then:"There should just be the one selected entry and the rest should be hided"
        page.invisibleRows.size() == 3
    }

    void "Reset filter Job by Tag"(){
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowInactiveJobs(true)

        when:"The user deletes the input from the text field"
        page.tagFilterTextbox .value(Keys.chord(Keys.CONTROL, "A")+Keys.BACK_SPACE)

        then:"There should be no hided row"
        page.invisibleRows.size() == 0
    }

    void "Filter Job by Script"(){
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowInactiveJobs(true)

        when:"The user types the name of a job in the job filter"
        page.scriptFilterTextbox << script1Name
        then:"There should just be the one selected entry and the rest should be hided"
        page.invisibleRows.size() == 1
    }

    void "Reset filter Job by Script"(){
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowInactiveJobs(true)

        when:"The user deletes the input from the text field"
        page.scriptFilterTextbox.value(Keys.chord(Keys.CONTROL, "A")+Keys.BACK_SPACE)

        then:"There should be no hided row"
        page.invisibleRows.size() == 0
    }

    void "Filter Job by Browser"(){
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowInactiveJobs(true)

        when:"The user types the name of a job in the job filter"
        page.browserFilterTextbox << browserName
        then:"There should be no hided row"
        page.invisibleRows.size() == 0
    }

    void "Reset filter Job by Browser"(){
        given: "User is on the job list, inactive jobs are shown"
        page.enableShowInactiveJobs(true)

        when:"The user deletes the input from the text field"
        page.browserFilterTextbox.value(Keys.chord(Keys.CONTROL, "A")+Keys.BACK_SPACE)

        then:"There should be no hided row"
        page.invisibleRows.size() == 0
    }

    void "Activate Job"(){
        given: "User is on the job list, inactive jobs are shown, no job is selected"
        page.enableShowInactiveJobs(false)
        def visibleActiveJobsBefore = countVisibleActiveJobs()
        page.enableShowInactiveJobs(true)
        page.enableCheckAll(true)
        page.enableCheckAll(false)


        when:"The user activates a job"
        page.changeCheckbox(getCheckboxForJobName(job4Name), true)
        page.activateButton.click()

        then:"There should be one more active job"
        at JobListPage
        sleep(2000)//Otherwise the element is not clickable with chrome
        page.enableShowInactiveJobs(false)
        page.countVisibleActiveJobs() == visibleActiveJobsBefore + 1
    }

    void "Deactivate Job"(){
        given: "User is on the job list, only active jobs are shown"
        def visibleActiveJobsBefore = countVisibleActiveJobs()

        when:"The user deletes the input from the text field"
        page.changeCheckbox(getCheckboxForJobName(job4Name), true)
        $("input",name:"_action_deactivate").click()

        then:"There should be no hided row"
        at JobListPage
        sleep(2000)//Otherwise the element is not clickable with chrome
        page.countVisibleActiveJobs() == visibleActiveJobsBefore - 1
    }

    void cleanupSpec() {
        doLogout()
    }

    private void createData(){

        Job.withNewTransaction{
            TestDataUtil.createOsmConfig()
            TestDataUtil.createAdminUser()

            Script script1 = TestDataUtil.createScript(script1Name,"This is for test purposes","stuff",false)
            Script script2 =TestDataUtil.createScript(script2Name,"This is also for test purposes","stuff",false)
            JobGroup jobGroup1 = TestDataUtil.createJobGroup(jobGroup1Name)
            JobGroup jobGroup2 =TestDataUtil.createJobGroup(jobGroup2Name)
            WebPageTestServer wpt = TestDataUtil.createWebPageTestServer("TestWPTServer-564892#Afef1","TestIdentifier",true,"http://internet.de")
            Browser browser = TestDataUtil.createBrowser(browserName,1d)
            Location location1 = TestDataUtil.createLocation(wpt,location1Name,browser,true)
            Location location2 = TestDataUtil.createLocation(wpt,location2Name,browser,true)
            TestDataUtil.createJob(job1Name,script1,location1,jobGroup1,"This is the first test job",1,false,12)
            TestDataUtil.createJob(job2Name,script2,location2,jobGroup1,"This is the second test job",1,false,12)
            TestDataUtil.createJob(job3Name,script1,location2,jobGroup2,"This is the third test job",1,false,12)
            //the last job creation will be a test
//            TestDataUtil.createJob("Label2",script2,location1,jobGroup2,"This is the first test job",1,false,5)
        }
    }
}
