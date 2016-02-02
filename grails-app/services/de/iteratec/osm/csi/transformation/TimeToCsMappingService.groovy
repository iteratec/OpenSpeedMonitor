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
import de.iteratec.osm.csi.*
import de.iteratec.osm.d3Data.MultiLineChart
import de.iteratec.osm.d3Data.MultiLineChartLineData
import de.iteratec.osm.util.I18nService
import grails.transaction.Transactional

class TimeToCsMappingService {

    static transactional = false

    TimeToCsMappingCacheService timeToCsMappingCacheService
    ConfigService configService
    I18nService i18nService

    /**
     * Calculates customer satisfaction of given load time for given page.
     * @param docReadyTime
     * @param Page page
     * @return Calculated customer-satisfaction or null if page is undefined or no calculation specification exists for it.
     */
    public Double getCustomerSatisfactionInPercent(Integer docReadyTimeInMilliSecs, Page page, CsiConfiguration csiConfiguration = null) {

        if (page.isUndefinedPage() || noTransformationPossibleFor(page,csiConfiguration)) {
            return null;
        } else {

            return transformLoadTime(docReadyTimeInMilliSecs, page, csiConfiguration)

        }

    }

    private double transformLoadTime(int docReadyTimeInMilliSecs, Page page, CsiConfiguration csiConfiguration) {
        Double cs
        CsiTransformation csiTransformation = configService.getCsiTransformation()
        if (csiTransformation == CsiTransformation.BY_MAPPING) {
            cs = getCustomerSatisfactionInPercentViaMapping(docReadyTimeInMilliSecs, page, csiConfiguration)
        } else if (csiTransformation == CsiTransformation.BY_RANK) {
            cs = getCustomerSatisfactionPercentRank(docReadyTimeInMilliSecs, page)
        } else {
            throw new IllegalStateException("No valid Csi transformation configured in OSM Configuration: ${csiTransformation}")
        }
        return cs
    }

    private boolean noTransformationPossibleFor(Page page, CsiConfiguration csiConfiguration) {
        Boolean notPossible = true
        CsiTransformation csiTransformation = configService.getCsiTransformation()
        if (csiTransformation == CsiTransformation.BY_RANK && validFrustrationsExistFor(page)) {
            notPossible = false
        } else if (csiTransformation == CsiTransformation.BY_MAPPING && csiConfiguration != null && validMappingsExistFor(page,csiConfiguration)) {
            notPossible = false
        }
        return notPossible
    }

    /**
     * <p>
     * Alternative approach to translate the load-time of a specific page into a customer satisfaction.
     * Uses database-table with time to csi mappings
     * </p>
     * @param docReadyTimeInMilliSecs
     * @param page
     * @return
     */
    public Double getCustomerSatisfactionInPercentViaMapping(Integer docReadyTimeInMilliSecs, Page page, CsiConfiguration csiConfiguration) {
        List<TimeToCsMapping> mappingsForPage = csiConfiguration.getTimeToCsMappingByPage(page)

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
        if (log.infoEnabled) {
            log.info("customerSatisfaction=$customerSatisfaction")
        }
        return customerSatisfaction
    }

    /**
     * Uses database-table with frustrating load times of user-investigation to calculate customer satisfaction of given load time for given page.
     * @param docReadyTimeInMilliSecs
     * @param Page page
     * @return Calculated customer-satisfaction.
     */
    public Double getCustomerSatisfactionPercentRank(Integer docReadyTimeInMilliSecs, Page page) {
        List<Integer> frustrationLoadtimesForPage = timeToCsMappingCacheService.getCustomerFrustrations(page)
        Double rank
        Integer smaller
        Integer bigger
        if (frustrationLoadtimesForPage) {
            log.debug("getCustomerSatisfactionPercentRank: page=${page}")
            log.debug("getCustomerSatisfactionPercentRank: class of frustration load times=${frustrationLoadtimesForPage.getClass()}")
            log.debug("getCustomerSatisfactionPercentRank: count of frustration load times=${frustrationLoadtimesForPage.size()}")
            smaller = frustrationLoadtimesForPage.findAll { it < docReadyTimeInMilliSecs }.size()
            bigger = frustrationLoadtimesForPage.findAll { it > docReadyTimeInMilliSecs }.size()
            if (smaller + bigger == 0) {
                throw new IllegalArgumentException("Percentrank couldn't be calculated for Page '${page.name}'")
            }
            rank = smaller / (smaller + bigger)
            return (1 - rank) * 100
        } else {
            throw new IllegalArgumentException("No customerFrustrationLoadtimes found for Page '${page.name}'")
        }
    }

    /**
     * Reads frustration load times from db/cache for given page.
     * @param page
     * {@link Page} frustration load times should be read for.
     * @return Frustration load times from db/cache for given page.
     */
    public List<Integer> getCachedFrustrations(Page page) {
        return timeToCsMappingCacheService.getCustomerFrustrations(page)
    }

    /**
     * Checks whether more than one different frustration timings exist for given {@link Page} page.
     * @param page
     * @return true if more than one different frustration timings exist for given {@link Page} page. false otherwise. false if page is null or undefinde page, too.
     */
    public Boolean validFrustrationsExistFor(Page page) {
        return isValid(page) && getCachedFrustrations(page).unique(false).size() > 1
    }

    public Boolean validMappingsExistFor(Page page, CsiConfiguration csiConfiguration) {
        return isValid(page) && !csiConfiguration.getTimeToCsMappingByPage(page).isEmpty()
    }

    Boolean isValid(Page page) {
        return page != null && !page.isUndefinedPage()
    }

    public MultiLineChart getPageMappingsAsChart(int maxLoadTime, CsiConfiguration csiConfiguration) {
        String xLabel = i18nService.msg("de.iteratec.osm.d3Data.multiLineChart.xAxisLabel", "ms")
        String yLabel = i18nService.msg("de.iteratec.osm.d3Data.multiLineChart.yAxisLabel", "Kundenzufriedenheit in %")
        MultiLineChart multiLineChart = new MultiLineChart(xLabel: xLabel, yLabel: yLabel)

        List<TimeToCsMapping> allTimeToCsMappings = csiConfiguration.timeToCsMappings.findAll { it.loadTimeInMilliSecs <= maxLoadTime }
        Map<String, MultiLineChartLineData> map = new HashMap<>()

        allTimeToCsMappings.each { mapping ->
            if (!map.containsKey(mapping.page.name)) {
                map.put(mapping.page.name, new MultiLineChartLineData(name: mapping.page.name))
            }
            map.get(mapping.page.name).addDataPoint(mapping.loadTimeInMilliSecs, mapping.customerSatisfaction)
        }

        // sort and add
        map.values().sort{a,b -> a.name.compareTo(b.name)}.each{e -> multiLineChart.addLine(e)}

        return multiLineChart
    }

    public getPageMappingAsChartData(Page page) {
        List<Integer> frustrations = getCachedFrustrations(page)
        Integer countFrustrations = frustrations.size()
        frustrations.sort()
        int lastFloor = 0
        List<Integer> frustrationsSinceLastCount = []
        frustrations.each { frustration ->
            double actualFloor = Math.floor(frustration / 100)
            if (actualFloor > lastFloor) {
                lastFloor = actualFloor
            }
        }
        int loadTimeInMillisecs = 100
        while (loadTimeInMillisecs < 12000) {

            loadTimeInMillisecs += 100
        }
    }

}
