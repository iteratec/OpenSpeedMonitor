package geb.de.iteratec.osm.wizard

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.security.User
import geb.CustomUrlGebReportingSpec
import geb.pages.de.iteratec.osm.LandingPage
import geb.pages.de.iteratec.osm.LoginPage
import geb.pages.de.iteratec.osm.wizards.MeasurementSetupPage
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Stepwise

@Integration
@Rollback
@Stepwise
class MeasurementSetupGebSpec extends CustomUrlGebReportingSpec{

    def login(){
        User.withNewTransaction {
            TestDataUtil.createAdminUser()
        }
        to LoginPage
        username << "admin"
        password << "admin"
        submitButton.click()

    }

    void "Test default values on first step"(){
        given:
        login()
        to MeasurementSetupPage
        MeasurementSetupPage currentPage = page as MeasurementSetupPage
        expect:
        currentPage.isJobGroupTabActive()
        !currentPage.jobGroupName.text()
        !currentPage.canContinueToScript()
    }

    void "setup can be continued with undefined jobgroup"(){
        given:
        MeasurementSetupPage currentPage = page as MeasurementSetupPage

        when:
        currentPage.selectUndefinedJobGroup()

        then:
        currentPage.canContinueToScript()
    }

    void "script page appears on next step"(){
        given:
        MeasurementSetupPage currentPage = page as MeasurementSetupPage

        when:
        currentPage.continueToScript()

        then:
        currentPage.isScriptTabActive()
    }

    void "next can't be continued, if the default code wasn't changed"(){
        given:
        MeasurementSetupPage currentPage = page as MeasurementSetupPage

        expect:
        at MeasurementSetupPage
        !currentPage.canContinueToLocation()
        currentPage.scriptCodeHasErrors()
    }

    void "it should be possible to continue after editing the script code"(){
        given:
        MeasurementSetupPage currentPage = page as MeasurementSetupPage

        when:
        currentPage.changeScript("setEventName\tHomepage:::Homepage\nnavigate\thttp://google.com")

        then:
        !currentPage.scriptCodeHasErrors()
        currentPage.canContinueToLocation()
    }

    void "location tab should appear after click on next"(){
        given:
        MeasurementSetupPage currentPage = page as MeasurementSetupPage

        when:
        currentPage.continueToLocation()

        then:
        at MeasurementSetupPage
        currentPage.isLocationAndConnectivyTabActive()
    }

//    void "previously created location and connectivity should appear"(){
//        given:
//        MeasurementSetupPage currentPage = page as MeasurementSetupPage
//
//        expect:
//        false
//    }
}

