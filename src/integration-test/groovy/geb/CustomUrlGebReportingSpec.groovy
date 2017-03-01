package geb

import de.iteratec.osm.util.OsmTestLogin
import geb.pages.de.iteratec.osm.LoginPage
import geb.spock.GebReportingSpec
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

/**
 * Sets the baseUrl for the test browser configured in the OpenSpeedMonitor-config.yml
 */
class CustomUrlGebReportingSpec extends GebReportingSpec implements OsmTestLogin{

    SpringSecurityService springSecurityService

    def setup(){
        String customBaseUrl = Holders.applicationContext.getBean("grailsApplication")?.config?.grails?.de?.iteratec?.osm?.test?.geb?.baseUrl
        if(customBaseUrl){
            println "Set custom base url: $customBaseUrl"
            browser.setBaseUrl(customBaseUrl)
        }
    }

    protected void doLogin() {
        to LoginPage
        username << configuredUsername
        password << configuredPassword
        submitButton.click()
    }

    protected void doLogout() {
        go "/logout/index"
    }
}
