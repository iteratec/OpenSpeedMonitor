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

import de.iteratec.osm.result.CachedView
import de.iteratec.osm.util.Constants
import groovy.transform.EqualsAndHashCode

/**
 * To group {@link AggregatorType}s which are measureands.
 *
 * @author nkuhn
 *
 */
enum MeasurandGroup {
    LOAD_TIMES(Unit.MILLISECONDS),
    REQUEST_COUNTS(Unit.NUMBER),
    REQUEST_SIZES(Unit.MEGABYTE),
    PERCENTAGES(Unit.PERCENT),
    UNDEFINED(Unit.OTHER),
    NO_MEASURAND(Unit.OTHER)

    private Unit unit

    private MeasurandGroup(Unit unit){
        this.unit = unit
    }
    Unit getUnit(){
        return unit
    }
}

enum Unit{
    KILOBYTE("KB",1000),
    MEGABYTE("MB",1000000),
    MILLISECONDS("ms",1),
    SECONDS("s",1000),
    PERCENT("%",0.01),
    NUMBER("#",1),
    OTHER("",1)

    private String label
    private Double divisor


    private Unit(String label,Double divisor){
        this.label = label
        this.divisor = divisor
    }

    String getLabel(){
        return label
    }

    Double getDivisor(){
        return divisor
    }
}

enum Measurand{
    DOC_COMPLETE_TIME(MeasurandGroup.LOAD_TIMES,"docCompleteTimeInMillisecs"),
    DOM_TIME(MeasurandGroup.LOAD_TIMES,"domTimeInMillisecs"),
    FIRST_BYTE(MeasurandGroup.LOAD_TIMES, "firstByteInMillisecs"),
    FULLY_LOADED_REQUEST_COUNT(MeasurandGroup.REQUEST_COUNTS, "fullyLoadedRequestCount"),
    FULLY_LOADED_TIME(MeasurandGroup.LOAD_TIMES, "fullyLoadedTimeInMillisecs"),
    LOAD_TIME(MeasurandGroup.LOAD_TIMES, "loadTimeInMillisecs"),
    START_RENDER(MeasurandGroup.LOAD_TIMES, "startRenderInMillisecs"),
    DOC_COMPLETE_INCOMING_BYTES(MeasurandGroup.REQUEST_SIZES,"docCompleteIncomingBytes"),
    DOC_COMPLETE_REQUESTS(MeasurandGroup.REQUEST_COUNTS, "docCompleteRequests"),
    FULLY_LOADED_INCOMING_BYTES(MeasurandGroup.REQUEST_SIZES, "fullyLoadedIncomingBytes"),
    SPEED_INDEX(MeasurandGroup.UNDEFINED, "speedIndex"),
    VISUALLY_COMPLETE(MeasurandGroup.LOAD_TIMES, "visuallyCompleteInMillisecs"),
    CS_BY_WPT_DOC_COMPLETE(MeasurandGroup.PERCENTAGES, "csByWptDocCompleteInPercent"),
    CS_BY_WPT_VISUALLY_COMPLETE(MeasurandGroup.PERCENTAGES, "csByWptVisuallyCompleteInPercent")

    private MeasurandGroup group
    private String eventResultField

    private (MeasurandGroup value, String name){
        group = value
        eventResultField = name
    }

    MeasurandGroup getMeasurandGroup(){
        return group
    }
    String getEventResultField(){
        return eventResultField
    }
}

class SelectedMeasurand{
    Measurand measurand
    CachedView cachedView

    SelectedMeasurand(Measurand measurand, CachedView cachedView){
        this.measurand = measurand
        this.cachedView = cachedView
    }

    @Override
    String toString(){
        return this.measurand.toString()+Constants.UNIQUE_STRING_DELIMITTER+this.cachedView.toString()
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
