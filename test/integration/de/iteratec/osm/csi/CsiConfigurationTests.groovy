package de.iteratec.osm.csi

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import grails.test.spock.IntegrationSpec

/**
 * It seems like unit tests with cascading doesn't work -> integration test
 */
class CsiConfigurationTests extends IntegrationSpec {


    void 'test cascading delete'() {

        given: "A Saved CsiConfiguration"
            Page page = new Page(weight: 0.5, name: "aPage").save(failOnError: true)
            BrowserAlias browserAlias = new BrowserAlias(alias: "ab")
            Browser browser = new Browser(name: "a",weight: 0.5,browserAliases: [])
            browserAlias.setBrowser(browser)
            browser.addToBrowserAliases(browserAlias)
            browser.save(flush: true)
            browserAlias.save(flush: true)
            HourOfDay hourOfDay = new HourOfDay(fullHour: 1, weight: 0.5).save(failOnError: true)
            PageWeight pageWeight = new PageWeight(page: page, weight: 0.5).save(failOnError: true)
            TimeToCsMapping timeToCsMapping = new TimeToCsMapping(page: page, loadTimeInMilliSecs: 100, customerSatisfaction: 0.5, mappingVersion: 100 ).save(failOnError: true)
            CsiConfiguration csiConfiguration = new CsiConfiguration(label: "csiConfig", description: "some information")
            ConnectivityProfile connectivityProfile =  new ConnectivityProfile(latency: 1, packetLoss: 5, name: "a",active: true, bandwidthDown: 1, bandwidthUp: 1).save(failOnError: true)
            BrowserConnectivityWeight browserConnectivityWeight = new BrowserConnectivityWeight(browser: browser,weight: 0.5,
                    connectivity:connectivityProfile ).save(failOnError: true)
            csiConfiguration.addToBrowserConnectivityWeights(browserConnectivityWeight)
            csiConfiguration.addToPageWeights(pageWeight)
            csiConfiguration.addToTimeToCsMappings(timeToCsMapping)
            csiConfiguration.addToHourOfDays(hourOfDay)
            csiConfiguration.save(failOnError: true)
        when: "We delete this CsiConfiguration"
            csiConfiguration.delete()
        then: "There should be no child elements left"
            CsiConfiguration.count == 0
            HourOfDay.count == 0
            PageWeight.count == 0
            TimeToCsMapping.count == 0
            BrowserConnectivityWeight.count == 0
    }
}
