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

package de.iteratec.osm.measurement.environment

import de.iteratec.osm.api.dto.BrowserInfoDto
import de.iteratec.osm.api.dto.DeviceTypeDto
import de.iteratec.osm.result.DeviceType
import de.iteratec.osm.result.OperatingSystem
import grails.gorm.transactions.Transactional

@Transactional
class BrowserService {

    Set<Browser> findAll() {
        Set<Browser> result = Collections.checkedSet(new HashSet<Browser>(), Browser.class)
        result.addAll(Browser.list())
        return Collections.unmodifiableSet(result)
    }

    Browser findByNameOrAlias(String browserName) {
        Browser browser = Browser.findByName(browserName)
        if (!browser) {
            return findByAlias(browserName)
        } else {
            return browser
        }
    }

    private Browser findByAlias(String browserName) {
        return BrowserAlias.findByAlias(browserName)?.browser
    }

    Browser findOrCreateByNameOrAlias(String browserName) {
        Browser browser = findByNameOrAlias(browserName)
        if (!browser) {
            browser = new Browser(name: browserName).save(failOnError: true)
        }
        return browser
    }

    List<BrowserInfoDto> getBrowserInfos() {
        return Location.list().groupBy {
            it.browser
        }.findResults { Browser browser, List<Location> locationsOfBrowser ->
            List<OperatingSystem> operatingSystemsOfBrowser = locationsOfBrowser*.operatingSystem.unique(false)
            List<DeviceType> devTypesOfBrowser = locationsOfBrowser*.deviceType.unique(false)
            if (operatingSystemsOfBrowser.size() == 1 && devTypesOfBrowser.size() == 1) {
                return new BrowserInfoDto(browserId: browser.id, browserName: browser.name, operatingSystem: operatingSystemsOfBrowser[0].getOSLabel(), deviceType:
                        new DeviceTypeDto(name: devTypesOfBrowser[0].getDeviceTypeLabel(), icon: devTypesOfBrowser[0].getDeviceTypeIcon()))
            } else {
                return null // will be skipped
            }
        }
    }
}
