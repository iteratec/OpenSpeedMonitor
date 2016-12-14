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

import grails.transaction.Transactional
import org.joda.time.DateTime
import org.joda.time.Duration


/**
 * @deprecated Move database access to {@link de.iteratec.osm.measurement.environment.dao.BrowserDaoService}.
 */
@Deprecated
@Transactional
class BrowserService {

    Map<Long, Browser> browsermap
    private DateTime lastFetchOfBrowsermap = new DateTime(1980, 1, 1, 0, 0)

    /**
     * Returns a map of Browsers in db with id's of Browsers as keys.
     * @param ageToleranceInHours
     * @return id to object-map
     * 			[Browser1.id: Browser1,
     * 			Browser2.id: Browser2,
     * 			...
     * 			BrowserN.id: BrowserN]
     */
    Map<Long, Browser> getCachedBrowserMap(Integer ageToleranceInHours) {
        Duration durationSinceLastFetch = new Duration(lastFetchOfBrowsermap.getMillis(), new DateTime().getMillis())
        if (!browsermap || durationSinceLastFetch.getStandardHours() > ageToleranceInHours) {
            browsermap = getBrowserMap()
            lastFetchOfBrowsermap = new DateTime()
        }
        return browsermap
    }

    Map<Long, Browser> getBrowserMap() {
        Map<Long, Browser> browsers = [:]
        Browser.list().each {
            browsers[it.ident()] = it
        }
        return browsers
    }

    List<Browser> findAllByNameOrAlias(List<String> browserNameOrAlias) {
        List<Browser> result = []
        browserNameOrAlias.each {
            result << findByNameOrAlias(it)
        }
        return result
    }

    Browser findByNameOrAlias(String browserNameOrAlias) {
        Browser ret = Browser.findByName(browserNameOrAlias)
        if (ret == null) {
            return findByAlias(browserNameOrAlias)
        } else {
            return ret
        }
    }

    private Browser findByAlias(browserNameOrAlias) {
        Browser ret = Browser.findByName('undefined') ?: new Browser(name: 'undefined').save(failOnError: true)
        Browser.list().each { currBrowser ->
            def query = BrowserAlias.where {
                browser == currBrowser
            }
            query.findAll { it.alias.equals(browserNameOrAlias) }.each {
                ret = currBrowser
            }
        }
        return ret
    }
}
