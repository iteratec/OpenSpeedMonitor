/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* 	http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

package de.iteratec.osm.csi

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import grails.test.mixin.*
import org.junit.*
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(CsiConfiguration)
@Mock([CsiConfiguration, HourOfDay, PageWeight, TimeToCsMapping, BrowserConnectivityWeight, Page, Browser])
class CsiConfigurationSpec extends Specification {

    void 'test cascading delete'() {

        given:
            Page page = new Page(weight: 0.5, name: "aPage").save()
            Browser browser = new Browser(name: "a",weight: 0.5,browserAliases: [new BrowserAlias(alias: "ab")])
            HourOfDay hourOfDay = new HourOfDay(fullHour: 1, weight: 0.5)
            PageWeight pageWeight = new PageWeight(page: page, weight: 0.5)
            TimeToCsMapping timeToCsMapping = new TimeToCsMapping(page: page, loadTimeInMilliSecs: 100, customerSatisfaction: 0.5, mappingVersion: 100 )
            CsiConfiguration csiConfiguration = new CsiConfiguration(label: "csiConfig", description: "some information")
            BrowserConnectivityWeight browserConnectivityWeight = new BrowserConnectivityWeight(browser: browser,weight: 0.5,connectivity: new ConnectivityProfile(latency: 1, packetLoss: 5, name: "a",active: true, bandwidthDown: 1, bandwidthUp: 1))
            csiConfiguration.addToBrowserConnectivityWeights(browserConnectivityWeight)
            csiConfiguration.addToPageWeights(pageWeight)
            csiConfiguration.addToTimeToCsMappings(timeToCsMapping)
            csiConfiguration.addToHourOfDays(hourOfDay)
            csiConfiguration.save()
        when:
            csiConfiguration.delete()
        then:
            CsiConfiguration.count == 0
            HourOfDay.count == 0
            PageWeight.count == 0
            TimeToCsMapping.count == 0
            BrowserConnectivityWeight.count == 0
            PageWeight.count == 0
    }

    void 'test cascading save'(){
        when:
            Page page = new Page(weight: 0.5, name: "aPage").save()
            Browser browser = new Browser(name: "a",weight: 0.5,browserAliases: [new BrowserAlias(alias: "ab")])
            HourOfDay hourOfDay = new HourOfDay(fullHour: 1, weight: 0.5)
            PageWeight pageWeight = new PageWeight(page: page, weight: 0.5)
            TimeToCsMapping timeToCsMapping = new TimeToCsMapping(page: page, loadTimeInMilliSecs: 100, customerSatisfaction: 0.5, mappingVersion: 100 )
            CsiConfiguration csiConfiguration = new CsiConfiguration(label: "csiConfig", description: "some information")
            BrowserConnectivityWeight browserConnectivityWeight = new BrowserConnectivityWeight(browser: browser,weight: 0.5,connectivity: new ConnectivityProfile(latency: 1, packetLoss: 5, name: "a",active: true, bandwidthDown: 1, bandwidthUp: 1))
            csiConfiguration.addToBrowserConnectivityWeights(browserConnectivityWeight)
            csiConfiguration.addToPageWeights(pageWeight)
            csiConfiguration.addToTimeToCsMappings(timeToCsMapping)
            csiConfiguration.addToHourOfDays(hourOfDay)
            csiConfiguration.save()
        then:
            CsiConfiguration.count == 1
            HourOfDay.count == 1
            PageWeight.count == 1
            TimeToCsMapping.count == 1
            BrowserConnectivityWeight.count == 1
            PageWeight.count == 1
    }
}
