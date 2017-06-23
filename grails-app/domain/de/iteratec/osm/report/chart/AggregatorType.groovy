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

package de.iteratec.osm.report.chart

import groovy.transform.EqualsAndHashCode

/**
 * To group {@link AggregatorType}s which are measureands.
 *
 * @author nkuhn
 *
 */
enum MeasurandGroup {
    /**
     * {@link AggregatorType}s with this group are no measurands.
     */
    NO_MEASURAND(""),
    /**
     * {@link AggregatorType}s with this group are measurands. But they are not dedicated to a measurand-group (yet).
     **/
     UNDEFINED(""),
    LOAD_TIMES("ms"),
    REQUEST_COUNTS("#"),
    REQUEST_SIZES("MB"),
    /**
     * Different measurands with unit percantage.
     *
     * <b>Note: </b>Should be persisted as a value between 0 and 1.
     * {@link MetricReportingService} multiplies the values of these measurands by 100 before they get reported.
     * Also in charts the values get multiplied by 100.
     */
            PERCENTAGES("%")

    private String unit;

    private MeasurandGroup(String value){
        unit = value;
    }
    String getUnit(){
        return unit;
    }
}

enum Measurand{
    DOC_COMPLETE_TIME(MeasurandGroup.LOAD_TIMES),
    DOM_TIME(MeasurandGroup.LOAD_TIMES),
    FIRST_BYTE(MeasurandGroup.LOAD_TIMES),
    FULLY_LOADED_REQUEST_COUNT(MeasurandGroup.REQUEST_COUNTS),
    FULLY_LOADED_TIME(MeasurandGroup.LOAD_TIMES),
    LOAD_TIME(MeasurandGroup.LOAD_TIMES),
    START_RENDER(MeasurandGroup.LOAD_TIMES),
    DOC_COMPLETE_INCOMING_BYTES(MeasurandGroup.REQUEST_SIZES),
    DOC_COMPLETE_REQUESTS(MeasurandGroup.REQUEST_COUNTS),
    FULLY_LOADED_INCOMING_BYTES(MeasurandGroup.REQUEST_SIZES),
    CUSTOMER_SATISFACTION(MeasurandGroup.PERCENTAGES),
    SPEED_INDEX(MeasurandGroup.UNDEFINED),
    VISUALLY_COMPLETE(MeasurandGroup.LOAD_TIMES),
    CS_BASED_ON_VISUALLY_COMPLETE(MeasurandGroup.PERCENTAGES),
    CS_BV_WPT_DOC_COMPLETE(MeasurandGroup.PERCENTAGES),
    CS_BV_WPT_VISUALLY_COMPLETE(MeasurandGroup.PERCENTAGES)

    private MeasurandGroup group

    private Measurand(MeasurandGroup value){
        group = value
    }

    MeasurandGroup getMeasurandGroup(){
        return group
    }
}
/**
 *
 * <p>
 * TODO mze-2013-07-15: Why this is not an enum?
 * </p>
 */
@EqualsAndHashCode
class AggregatorType {

    /*Types for CSI*/

    /**
     * The name of the aggregation by {@link MeasuredEvent}s.
     */
    public static final String MEASURED_EVENT = "measuredEvent"

    /**
     * The name of the aggregation by {@link Page}.
     */
    public static final String PAGE = "page"

    /**
     * The name of the aggregation by page and browser.
     * <em>Note:</em>nku 2013-08-21: Not yet implemented.
     */
    public static final String PAGE_AND_BROWSER = "pageAndBrowser"

    /**
     * The name of the aggregation by shop (means by {@link JobGroup} of groupType {@link JobGroup#TYPE_CSI}).
     */
    public static final String SHOP = "shop"

    /**
     * The name of the aggregation by a {@link csiSystem}
     */
    public static final String CSI_SYSTEM = "csiSystem"

    /**
     * The Aggegation Type for uncached results
     */
    private static final String UNCACHED_SUFFIX = "Uncached"
    public static final String RESULT_UNCACHED_DOC_COMPLETE_TIME = "docCompleteTimeInMillisecs${UNCACHED_SUFFIX}"
    public static final String RESULT_UNCACHED_DOM_TIME = "domTimeInMillisecs${UNCACHED_SUFFIX}"
    public static final String RESULT_UNCACHED_FIRST_BYTE = "firstByteInMillisecs${UNCACHED_SUFFIX}"
    public static final String RESULT_UNCACHED_FULLY_LOADED_REQUEST_COUNT = "fullyLoadedRequestCount${UNCACHED_SUFFIX}"
    public static final String RESULT_UNCACHED_FULLY_LOADED_TIME = "fullyLoadedTimeInMillisecs${UNCACHED_SUFFIX}"
    public static final String RESULT_UNCACHED_LOAD_TIME = "loadTimeInMillisecs${UNCACHED_SUFFIX}"
    public static final String RESULT_UNCACHED_START_RENDER = "startRenderInMillisecs${UNCACHED_SUFFIX}"
    public static
    final String RESULT_UNCACHED_DOC_COMPLETE_INCOMING_BYTES = "docCompleteIncomingBytes${UNCACHED_SUFFIX}"
    public static final String RESULT_UNCACHED_DOC_COMPLETE_REQUESTS = "docCompleteRequests${UNCACHED_SUFFIX}"
    public static
    final String RESULT_UNCACHED_FULLY_LOADED_INCOMING_BYTES = "fullyLoadedIncomingBytes${UNCACHED_SUFFIX}"
    public static
    final String RESULT_UNCACHED_CS_BASED_ON_DOC_COMPLETE_IN_PERCENT = "csByWptDocCompleteInPercent${UNCACHED_SUFFIX}"
    public static final String RESULT_UNCACHED_SPEED_INDEX = "speedIndex${UNCACHED_SUFFIX}"
    public static final String RESULT_UNCACHED_VISUALLY_COMPLETE = "visuallyCompleteInMillisecs${UNCACHED_SUFFIX}"
    public static
    final String RESULT_UNCACHED_CS_BASED_ON_VISUALLY_COMPLETE_IN_PERCENT = "csByWptVisuallyCompleteInPercent${UNCACHED_SUFFIX}"

    /**
     * The Aggegation Type for cached results
     */
    private static final String CACHED_SUFFIX = "Cached"
    public static final String RESULT_CACHED_DOC_COMPLETE_TIME = "docCompleteTimeInMillisecs${CACHED_SUFFIX}"
    public static final String RESULT_CACHED_DOM_TIME = "domTimeInMillisecs${CACHED_SUFFIX}"
    public static final String RESULT_CACHED_FIRST_BYTE = "firstByteInMillisecs${CACHED_SUFFIX}"
    public static final String RESULT_CACHED_FULLY_LOADED_REQUEST_COUNT = "fullyLoadedRequestCount${CACHED_SUFFIX}"
    public static final String RESULT_CACHED_FULLY_LOADED_TIME = "fullyLoadedTimeInMillisecs${CACHED_SUFFIX}"
    public static final String RESULT_CACHED_LOAD_TIME = "loadTimeInMillisecs${CACHED_SUFFIX}"
    public static final String RESULT_CACHED_START_RENDER = "startRenderInMillisecs${CACHED_SUFFIX}"
    public static final String RESULT_CACHED_DOC_COMPLETE_INCOMING_BYTES = "docCompleteIncomingBytes${CACHED_SUFFIX}"
    public static final String RESULT_CACHED_DOC_COMPLETE_REQUESTS = "docCompleteRequests${CACHED_SUFFIX}"
    public static final String RESULT_CACHED_FULLY_LOADED_INCOMING_BYTES = "fullyLoadedIncomingBytes${CACHED_SUFFIX}"
    public static
    final String RESULT_CACHED_CS_BASED_ON_DOC_COMPLETE_IN_PERCENT = "csByWptDocCompleteInPercent${CACHED_SUFFIX}"
    public static final String RESULT_CACHED_SPEED_INDEX = "speedIndex${CACHED_SUFFIX}"
    public static final String RESULT_CACHED_VISUALLY_COMPLETE = "visuallyCompleteInMillisecs${CACHED_SUFFIX}"
    public static
    final String RESULT_CACHED_CS_BASED_ON_VISUALLY_COMPLETE_IN_PERCENT = "csByWptVisuallyCompleteInPercent${CACHED_SUFFIX}"

    String name
    MeasurandGroup measurandGroup

    static constraints = {
        name(unique: true)
        measurandGroup(nullable: false)
    }
    static mapping = {
        cache usage: 'read-only'
    }

    static transients = ['cached', 'cachedCriteriaApplicable']

    /**
     * <p>
     * Determines weather this aggregator type is a cached state (repeated
     * view) or an un-cached state (first view). For aggregator types on which
     * this criteria is not applicable this method constantly
     * returns <code>false</code>.
     * </p>
     *
     * @return <code>true</code> if this is a cached state,
     *         <code>false</code> else.
     * @see #isCachedCriteriaApplicable()
     */
    boolean isCached() {
        return this.getName().endsWith(CACHED_SUFFIX)
    }

    /**
     * <p>
     * Determines weather the criteria of caching state is applicable to
     * this aggregator type.
     * </p>
     *
     * @return <code>true</code> if the criteria of caching state is
     *         applicable, <code>false</code> else.
     * @see #isCached()
     */
    boolean isCachedCriteriaApplicable() {
        return this.getName().endsWith(CACHED_SUFFIX) || this.getName().endsWith(UNCACHED_SUFFIX)
    }

    @Override
    String toString() {
        return name
    }
}
