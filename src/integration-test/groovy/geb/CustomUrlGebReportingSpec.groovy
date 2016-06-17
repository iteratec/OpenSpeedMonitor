package geb

import geb.spock.GebReportingSpec
import grails.util.Holders

/**
 * Created by benni on 17.06.16.
 */
class CustomUrlGebReportingSpec extends GebReportingSpec{

    def setup(){
        String customBaseUrl = Holders.applicationContext.getBean("grailsApplication")?.config?.grails?.de?.iteratec?.test?.geb?.customBaseUrl
        if(customBaseUrl){
            browser.setBaseUrl(customBaseUrl)
        }
    }
}
