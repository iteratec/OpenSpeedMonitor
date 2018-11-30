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

import de.iteratec.osm.ConfigService
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TimeToCsMapping
import de.iteratec.osm.d3Data.MultiLineChart
import de.iteratec.osm.d3Data.MultiLineChartLineData
import de.iteratec.osm.util.I18nService

class TimeToCsMappingService {

    ConfigService configService
    I18nService i18nService

    /**
     * Calculates customer satisfaction of given load time for given page.
     * @param docReadyTime
     * @param Page page
     * @return Calculated customer-satisfaction or null if page is undefined or no calculation specification exists for it.
     */
    Double getCustomerSatisfactionInPercent(Integer docReadyTimeInMilliSecs, Page page, CsiConfiguration csiConfiguration = null) {
        if (!isValid(page) || !csiConfiguration) {
            return null
        }
        List<TimeToCsMapping> mappingsForPage = csiConfiguration.getTimeToCsMappingByPage(page)
        if (!mappingsForPage) {
            return null
        }

        Integer loadtimeIncrement = 20
        Integer loadtimeNoUserWouldAccept = 20000
        Integer noOneIsSatisfied = 0
        Integer everybodyIsSatisfied = 100

        Integer lowerIncrementBoundaryMillisecs = Math.floor(docReadyTimeInMilliSecs / loadtimeIncrement) * loadtimeIncrement
        Integer upperIncrementBoundaryMillisecs = lowerIncrementBoundaryMillisecs + loadtimeIncrement
        Integer diffDocreadyToLowerIncrementBoundary = docReadyTimeInMilliSecs - lowerIncrementBoundaryMillisecs

        Double upperCs = lowerIncrementBoundaryMillisecs == 0 ?
                everybodyIsSatisfied :
                lowerIncrementBoundaryMillisecs > loadtimeNoUserWouldAccept ?
                        noOneIsSatisfied :
                        mappingsForPage.find {
                            it.loadTimeInMilliSecs == lowerIncrementBoundaryMillisecs
                        }.customerSatisfaction
        Double lowerCs = upperIncrementBoundaryMillisecs == 0 ?
                everybodyIsSatisfied :
                upperIncrementBoundaryMillisecs > loadtimeNoUserWouldAccept ?
                        noOneIsSatisfied :
                        mappingsForPage.find {
                            it.loadTimeInMilliSecs == upperIncrementBoundaryMillisecs
                        }.customerSatisfaction

        Double customerSatisfaction
        if (upperCs != null && lowerCs != null && upperCs >= lowerCs) {
            Double fractionOfUpperCs = (upperCs - lowerCs) * ((loadtimeIncrement - diffDocreadyToLowerIncrementBoundary) / loadtimeIncrement)
            customerSatisfaction = lowerCs + fractionOfUpperCs
        }
        log.debug("customerSatisfaction=${customerSatisfaction}")
        return customerSatisfaction
    }

    Boolean isValid(Page page) {
        return page != null && !page.isUndefinedPage()
    }

    MultiLineChart getPageMappingsAsChart(int maxLoadTime, CsiConfiguration csiConfiguration) {
        String xLabel = i18nService.msg("de.iteratec.osm.d3Data.multiLineChart.xAxisLabel", "ms")
        String yLabel = i18nService.msg("de.iteratec.osm.d3Data.multiLineChart.yAxisLabel", "Kundenzufriedenheit in %")
        MultiLineChart multiLineChart = new MultiLineChart(xLabel: xLabel, yLabel: yLabel)

        Collection<TimeToCsMapping> allTimeToCsMappings = csiConfiguration.timeToCsMappings.findAll {
            it.loadTimeInMilliSecs <= maxLoadTime
        }
        Map<String, MultiLineChartLineData> map = new HashMap<>()

        allTimeToCsMappings.each { mapping ->
            if (!map.containsKey(mapping.page.name)) {
                map.put(mapping.page.name, new MultiLineChartLineData(name: mapping.page.name))
            }
            map.get(mapping.page.name).addDataPoint(mapping.loadTimeInMilliSecs, mapping.customerSatisfaction)
        }

        // sort and add
        map.values().sort { a, b -> a.name.compareTo(b.name) }.each { e -> multiLineChart.addLine(e) }

        return multiLineChart
    }
}
