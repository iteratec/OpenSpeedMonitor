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

package de.iteratec.osm.csi.transformation

import de.iteratec.osm.csi.CustomerFrustration
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TimeToCsMapping
import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.Duration

@Transactional
class TimeToCsMappingCacheService {

    private List<TimeToCsMapping> timeToCsMappings
    private final Integer fetchingMappingsFrequencyInHours = 24
    private DateTime lastFetchOfMappings = new DateTime(1980, 1, 1, 0, 0)

    private Map<String, List<Integer>> frustrations
    private final Integer fetchingFrustrationsFrequencyInHours = 24
    private DateTime lastFetchOfFrustrations = new DateTime(1980, 1, 1, 0, 0)

    List<TimeToCsMapping> getMappingsFor(Page page) {
        Duration durationSinceLastFetch = new Duration(lastFetchOfMappings.getMillis(), new DateTime().getMillis())
        if (!timeToCsMappings || durationSinceLastFetch.getStandardHours() > fetchingMappingsFrequencyInHours) {
            fetchMappings()
        }
        return timeToCsMappings.findAll { it.page.ident() == page.ident() }
    }

    List<TimeToCsMapping> getMappings() {
        Duration durationSinceLastFetch = new Duration(lastFetchOfMappings.getMillis(), new DateTime().getMillis())
        if (!timeToCsMappings || durationSinceLastFetch.getStandardHours() > fetchingMappingsFrequencyInHours) {
            fetchMappings()
        }
        return timeToCsMappings
    }

    List<Integer> getCustomerFrustrations(Page page) {
        Duration durationSinceLastFetch = new Duration(lastFetchOfFrustrations.getMillis(), new DateTime().getMillis())

        log.info("lastFetchOfFrustrations=$lastFetchOfFrustrations")
        log.info("durationSinceLastFetch=$durationSinceLastFetch")

        if (frustrations != null) {
            log.debug("begin of getCustomerFrustrations: frustrations.getClass()=${frustrations.getClass()}")
            logFrustrationEntries()
        }
        log.debug("fetchingFrustrationsFrequencyInHours=${fetchingFrustrationsFrequencyInHours}")
        log.debug("durationSinceLastFetch.getStandardHours()=${durationSinceLastFetch.getStandardHours()}")


        boolean fetchNecessary = !frustrations ||
                durationSinceLastFetch.getStandardHours() > fetchingFrustrationsFrequencyInHours
        log.debug("fetchNecessary=${fetchNecessary}")

        if (fetchNecessary) {
            fetchFrustrations()
        }

        log.debug("end of getCustomerFrustrations: frustrations=${frustrations}")
        return frustrations[page.name]
    }

    void fetchMappings() {

        log.info('start fetch mapping for cache')
        timeToCsMappings = getActualMappingsFromDb()

        lastFetchOfMappings = new DateTime()

        List<Page> pagesWithInvalidMappings = []
        timeToCsMappings*.page.unique().each { page ->
            List<TimeToCsMapping> mappingsOfActualPage = timeToCsMappings.findAll { it.page.ident() == page.ident() }
            if (!isValid(mappingsOfActualPage)) {
                pagesWithInvalidMappings.add(page)
            }
        }
        pagesWithInvalidMappings.each { page ->
            log.error("The page '${page.name}' has an invalid mapping in database!")
            timeToCsMappings.removeAll { it.page.ident() == page.ident() }
        }
    }

    /**
     * Checks validity of mapping.
     * @param mappingsForOnePage
     * @return True if mapping is valid.
     */
    Boolean isValid(List<TimeToCsMapping> mappingsForOnePage) {

        if (mappingsForOnePage.size() == 0)
            return false

        List loadTimes = mappingsForOnePage*.loadTimeInMilliSecs
        if (loadTimes.size() > loadTimes.unique().size())
            return false

        201.times {
            if (!loadTimes.contains(it * 100))
                return false
        }

        return true

    }

    List getActualMappingsFromDb() {
        def query = TimeToCsMapping.where {
            mappingVersion >= max(mappingVersion)//bug in grails 2.1.1: == doesn't work with subqueries
        }
        List<TimeToCsMapping> timeToCsMappingsFromDb = query.findAll()
        return TimeToCsMapping.findAllByMappingVersion(timeToCsMappingsFromDb ? timeToCsMappingsFromDb[0].mappingVersion : -1)
    }

    private fetchFrustrations() {

        frustrations = [:].withDefault { [] }
        Map<String, List<Integer>> fetchedFrustrations = [:].withDefault { [] }

        log.debug("after reset/initialization of frustrations map: frustrations.getClass()=${frustrations.getClass()}")
        log.debug("after reset/initialization of frustrations map: frustrations=${frustrations}")

        def query = CustomerFrustration.where {
            investigationVersion >= max(investigationVersion)//bug in grails 2.1.1: == doesn't work with subqueries
        }
        List<CustomerFrustration> frustrationsWithMaxVersion = query.findAll()
        log.debug("all frustrations from db: frustrationsWithMaxVersion.size()=${frustrationsWithMaxVersion.size()}")
        log.debug("all frustrations from db: frustrationsWithMaxVersion=${frustrationsWithMaxVersion}")

        frustrationsWithMaxVersion.each {
            log.debug("add ${it.loadTimeInMilliSecs} for page '${it.page.name}'")
            fetchedFrustrations[it.page.name].add(it.loadTimeInMilliSecs)
        }
        fetchedFrustrations.each { pageName, frustrationTimes ->
            frustrations[pageName] = Collections.unmodifiableList(frustrationTimes)
        }

        DateTime now = new DateTime()
        log.debug("set lastFetchOfFrustrations to '${now}'")
        lastFetchOfFrustrations = now
        if (log.debugEnabled) {
            log.debug "end of fetching of CustomerFrustrations:"
            logFrustrationEntries()
        }
    }

    private logFrustrationEntries() {
        frustrations.each { entry ->
            log.debug "page=${entry.key}\nclass of page=${entry.key.getClass()}\nclass of frustrations collection=${entry.value.getClass()}" +
                    "\ncount=${entry.value.size()}\nlist of frustration-loadtimes=${entry.value}"
        }
    }
}
