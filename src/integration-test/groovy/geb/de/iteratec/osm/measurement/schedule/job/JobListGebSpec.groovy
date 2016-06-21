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
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
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



    void "Anonymous can't create a job"() {
        given: "User is logged in"
        createData()
        to JobListPage

        when: "User wants to create a job"
        createJob.click()
        true

        then: "He will be redirected to the Login Page"
        at LoginPage
    }

    void "User is able to reach create job page"() {
        given: "User is logged in"
        to LoginPage
        username << getConfiguredUsername()
        password << getConfiguredPassword()
        submitButton.click()
        to JobListPage

        when: "The user is able to click the create job button and fill the data in"
        createJob.click()
        then: "The user should see the create page"
        at JobCreatePage
    }

    void "User is able to create a Job"(){
        given: "User is on the create page"
        to JobCreatePage

        when: "The user fills out all necessary data and submits"
        location.click() //Open the dropdown to choose location, otherwise the options won't be visible
        $("#location_chosen").find("li").find{it.text() == location2Name}.click()
        nameText << job4Name
        cronString << "10/20 * * * ? *"
        tags << job4Tag
        createButton.click()

        then: "There should be the new created job in the list and it should be highlighted"
        at JobListPage
        showInactiveJobsToo.click()
        $(".highlight.success").text().startsWith(job4Name)
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
            Browser browser = TestDataUtil.createBrowser("This is the very best browser i've ever seen",1d)
            Location location1 = TestDataUtil.createLocation(wpt,location1Name,browser,true)
            Location location2 = TestDataUtil.createLocation(wpt,location2Name,browser,true)
            TestDataUtil.createJob(job1Name,script1,location1,jobGroup1,"This is the first test job",1,false,12)
            TestDataUtil.createJob(job2Name,script2,location2,jobGroup1,"This is the second test job",1,false,12)
            TestDataUtil.createJob(job3Name,script1,location2,jobGroup1,"This is the third test job",1,false,12)
            //the last job creation will be a test
//            TestDataUtil.createJob("Label2",script2,location1,jobGroup2,"This is the first test job",1,false,5)
        }
    }
}
