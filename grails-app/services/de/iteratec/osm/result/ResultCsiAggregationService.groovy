/*
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
*
* Licensed under the Apache License, Version 2.0 (the "License")
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

package de.iteratec.osm.result

import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.report.chart.Measurand
import de.iteratec.osm.result.dao.EventResultDaoService
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.PerformanceLoggingService

/**
 * Calculates {@link de.iteratec.osm.report.chart.CsiAggregation}s for EventResults.
 *
 * @author rhe
 */
class ResultCsiAggregationService {

    /** injected by grails */
    CsiAggregationUtilService csiAggregationUtilService
    BrowserService browserService
    EventResultDaoService eventResultDaoService
    PerformanceLoggingService performanceLoggingService
    I18nService i18nService

    /**
     * <strong>Important:</strong> Update {@link #getEventResultPropertyForCalculation(AggregatorType, EventResult)}-Method
     * if you add an Aggregator. Otherwise it'll throw an IllegalArgumentException!
     */
    private static final Map<CachedView, List<String>> AGGREGATORS = getAggregatorMap()

    static Map<MeasurandGroup, List<Measurand>> getAggregatorMapForOptGroupSelect() {
        Map<MeasurandGroup, List<Measurand>> result = [:]
        MeasurandGroup.values().each { result.put(it, [])}
        Measurand.values().each {result.get(it.getMeasurandGroup()).add(it)}

        return Collections.unmodifiableMap(result)
    }

    static Map<CachedView, List<String>> getAggregatorMap() {
        Map<CachedView, List<String>> fillMap = new HashMap<CachedView, List<String>>()

        fillMap.put(CachedView.CACHED, [
                AggregatorType.RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES,
                AggregatorType.RESULT_CACHED_DOC_COMPLETE_REQUESTS,
                AggregatorType.RESULT_CACHED_FULLY_LOADED_INCOMING_BYTES,
                AggregatorType.RESULT_CACHED_DOC_COMPLETE_TIME,
                AggregatorType.RESULT_CACHED_DOM_TIME,
                AggregatorType.RESULT_CACHED_FIRST_BYTE,
                AggregatorType.RESULT_CACHED_FULLY_LOADED_REQUEST_COUNT,
                AggregatorType.RESULT_CACHED_FULLY_LOADED_TIME,
                AggregatorType.RESULT_CACHED_LOAD_TIME,
                AggregatorType.RESULT_CACHED_START_RENDER,
                AggregatorType.RESULT_CACHED_CS_BASED_ON_DOC_COMPLETE_IN_PERCENT,
                AggregatorType.RESULT_CACHED_SPEED_INDEX,
                AggregatorType.RESULT_CACHED_VISUALLY_COMPLETE,
                AggregatorType.RESULT_CACHED_CS_BASED_ON_VISUALLY_COMPLETE_IN_PERCENT
        ])

        fillMap.put(CachedView.UNCACHED, [
                AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES,
                AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_REQUESTS,
                AggregatorType.RESULT_UNCACHED_FULLY_LOADED_INCOMING_BYTES,
                AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME,
                AggregatorType.RESULT_UNCACHED_DOM_TIME,
                AggregatorType.RESULT_UNCACHED_FIRST_BYTE,
                AggregatorType.RESULT_UNCACHED_FULLY_LOADED_REQUEST_COUNT,
                AggregatorType.RESULT_UNCACHED_FULLY_LOADED_TIME,
                AggregatorType.RESULT_UNCACHED_LOAD_TIME,
                AggregatorType.RESULT_UNCACHED_START_RENDER,
                AggregatorType.RESULT_UNCACHED_CS_BASED_ON_DOC_COMPLETE_IN_PERCENT,
                AggregatorType.RESULT_UNCACHED_SPEED_INDEX,
                AggregatorType.RESULT_UNCACHED_VISUALLY_COMPLETE,
                AggregatorType.RESULT_UNCACHED_CS_BASED_ON_VISUALLY_COMPLETE_IN_PERCENT
        ])

        return Collections.unmodifiableMap(fillMap)
    }

    /**
     * Gets the value of an property corresponding to the {@link AggregatorType}
     *
     * @param aggType the AggregatorType
     * @param result an Event Result, should never be <code>null</code>.
     * @return double value of the property, can be <code>null</code>
     *
     * @throws IllegalArgumentException if no property is defined for the {@link AggregatorType}
     */
    Double getEventResultPropertyForCalculation(Measurand measurand, EventResult result) {
        return result.getProperty(measurand.getEventResultField()) ? Double.valueOf(result.getProperty(measurand.getEventResultField())) : null
    }

    /**
     * Retrieves attribute-name of {@link EventResult} which is associated with the given {@link AggregatorType}.
     * @param aggregator
     * @return
     * @throws IllegalArgumentException if aggregator is not a measurand or the {@link EventResult}-attribute can't be conducted from aggregator-name.
     */
    public String getEventResultAttributeNameFromMeasurand(AggregatorType aggregator) {
        String eventResultAttributeName = getCachedViewIndependentPartOf(aggregator.name)
        if (aggregator.measurandGroup == null || aggregator.measurandGroup == MeasurandGroup.NO_MEASURAND) {
            throw new IllegalArgumentException("AggregatorType ${aggregator} is not a measurand")
        } else if (!EventResult.metaClass.properties*.name.contains(eventResultAttributeName)) {
            throw new IllegalArgumentException("The EventResult-attribute '${eventResultAttributeName}' doesn't exist")
        }
        return eventResultAttributeName
    }

    private String getCachedViewIndependentPartOf(String aggregatorName) {
        String cachedViewIndependentPart = aggregatorName
        Integer positionOfUncachedSuffix = aggregatorName.indexOf(AggregatorType.UNCACHED_SUFFIX)
        Integer positionOfCachedSuffix = aggregatorName.indexOf(AggregatorType.CACHED_SUFFIX)
        if (positionOfUncachedSuffix > -1) {
            cachedViewIndependentPart = aggregatorName.substring(0, positionOfUncachedSuffix)
        } else if (positionOfCachedSuffix > -1) {
            cachedViewIndependentPart = aggregatorName.substring(0, positionOfCachedSuffix)
        }
        return cachedViewIndependentPart
    }

    /**
     * Returns the {@link CachedView} the Result-{@link AggregatorType} is bound to.
     *
     * <p>
     * Requires the {@link AggregatorType} to be bound to the {@link CachedView}, see {@link ResultCsiAggregationService#getAggregatorMap()}
     * </p>
     *
     * @param aggregator
     * @return CachedView.CACHED or CachedView.UNCACHED
     * @since IT-60
     *
     * @throws IllegalArgumentException if the {@link AggregatorType} is not bound to a {@link CachedView}!
     */
    public CachedView getAggregatorTypeCachedViewType(AggregatorType aggregator) {

        if (AGGREGATORS.get(CachedView.CACHED).contains(aggregator.name)) {
            return CachedView.CACHED
        } else if (AGGREGATORS.get(CachedView.UNCACHED).contains(aggregator.name)) {
            return CachedView.UNCACHED
        } else {
            throw new IllegalArgumentException("AggregatorType '${aggregator}' is not bound to CACHED or UNCACHED view!")
        }

    }

}
