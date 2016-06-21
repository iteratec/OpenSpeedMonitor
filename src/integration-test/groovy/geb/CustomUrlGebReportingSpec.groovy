package geb

import geb.spock.GebReportingSpec
import grails.util.Holders

/**
 * Sets the baseUrl for the test browser configured in the OpenSpeedMonitor-config.yml
 */
class CustomUrlGebReportingSpec extends GebReportingSpec{

    def setup(){
        String customBaseUrl = Holders.applicationContext.getBean("grailsApplication")?.config?.grails?.de?.iteratec?.test?.geb?.customBaseUrl
        if(customBaseUrl){
            println "Set custom base url: $customBaseUrl"
            browser.setBaseUrl(customBaseUrl)
        }
    }
}
