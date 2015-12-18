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

import de.iteratec.osm.csi.DefaultTimeToCsMapping
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TimeToCsMapping
import de.iteratec.osm.d3Data.MultiLineChart
import de.iteratec.osm.d3Data.MultiLineChartLineData
import de.iteratec.osm.report.chart.RickshawHtmlCreater
import de.iteratec.osm.util.I18nService

/**
 * DefaultTimeToCsMappingService
 * A service class encapsulates the core business logic of a Grails application
 */
class DefaultTimeToCsMappingService {

    static transactional = false

    TimeToCsMappingCacheService timeToCsMappingCacheService
    I18nService i18nService

    List<DefaultTimeToCsMapping> getAll() {
        return DefaultTimeToCsMapping.list()
    }

    /**
     * Copies all Mapping values (load time to customer satisfaction) from given default mapping as actual mappings
     * for the given page.
     * <b>Note(!):</b> All existing latest mappings of the given page get deleted!
     * @param page
     * @param nameOfDefaultMapping
     *
     * //TODO: write a test for this method
     */
    void copyDefaultMappingToPage(Page page, String nameOfDefaultMapping){

        List<DefaultTimeToCsMapping> defaultMappingsToCopyToPage = DefaultTimeToCsMapping.findAllByName(nameOfDefaultMapping)
        if (defaultMappingsToCopyToPage.size() == 0)
            throw new IllegalArgumentException("No default csi mapping with name ${nameOfDefaultMapping} exists!")

        Integer actualMappingVersion
        TimeToCsMapping.withTransaction {
            List<TimeToCsMapping> actualMappings = timeToCsMappingCacheService.getActualMappingsFromDb()
            actualMappingVersion = actualMappings.size()>0 ? actualMappings[0].mappingVersion : 1
            actualMappings.findAll {it.page.ident() == page.ident()}*.delete()
        }
        TimeToCsMapping.withTransaction {
            defaultMappingsToCopyToPage.each {defaultMapping ->
                new TimeToCsMapping(
                        page: page,
                        loadTimeInMilliSecs: defaultMapping.loadTimeInMilliSecs,
                        customerSatisfaction: defaultMapping.customerSatisfactionInPercent,
                        mappingVersion: actualMappingVersion
                ).save(failOnError: true)
            }
        }
        TimeToCsMapping.withTransaction {
            timeToCsMappingCacheService.fetchMappings()
        }
    }

    def translateDefaultMappingsToJson(List<DefaultTimeToCsMapping> mappings){
        return new RickshawHtmlCreater().transformCSIMappingData(mappings)
    }

    MultiLineChart getDefaultMappingsAsChart(int maxLoadTime) {
        String xLabel = i18nService.msg("de.iteratec.osm.d3Data.multiLineChart.xAxisLabel", "ms")
        String yLabel = i18nService.msg("de.iteratec.osm.d3Data.multiLineChart.yAxisLabel", "Kundenzufriedenheit in %")
        MultiLineChart multiLineChart = new MultiLineChart(xLabel: xLabel, yLabel: yLabel)

        List<DefaultTimeToCsMapping> defaultTimeToCsMappings = getAll().findAll { it.loadTimeInMilliSecs <= maxLoadTime }
        Map<String, MultiLineChartLineData> map = new HashMap<>()

        defaultTimeToCsMappings.each { entry ->
            if (!map.containsKey(entry.name)) {
                map.put(entry.name, new MultiLineChartLineData(name: entry.name))
            }
            map.get(entry.name).addDataPoint(entry.loadTimeInMilliSecs, entry.customerSatisfactionInPercent)
        }

        // sort and add
        map.values().sort{a,b -> a.name.compareTo(b.name)}.each{e -> multiLineChart.addLine(e)}

        return multiLineChart
    }

    /**
     * Deletes all DefaultTimeToCsMapping entries with the given name.
     * If this name doesn't exist, this method will fail
     * @param name
     * @throws IllegalArgumentException
     */
    void deleteDefaultTimeToCsMapping(String name){
        List<DefaultTimeToCsMapping> mappings = DefaultTimeToCsMapping.findAllByName(name)
        if (mappings.size() == 0)
            throw new IllegalArgumentException("No default csi mapping with name ${name} exists!")

        DefaultTimeToCsMapping.withTransaction {
            mappings*.delete()
        }
        log.info("${DefaultTimeToCsMapping.getSimpleName()} $name deleted")
    }
}
