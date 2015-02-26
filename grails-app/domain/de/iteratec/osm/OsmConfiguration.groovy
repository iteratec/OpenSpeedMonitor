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

package de.iteratec.osm

/**
 * Configuration of OpenSpeedMonitor to be changed at runtime.
 */
class OsmConfiguration {

    static final Integer DEFAULT_DETAIL_DATA_STORAGE_TIME_IN_WEEKS = 2
    static final Integer DEFAULT_MAX_DOWNLOAD_TIME_IN_MINUTES = 60
    static final Integer DEFAULT_MIN_DOCCOMPLETE_TIME_IN_MILLISECS = 250
    static final Integer DEFAULT_MAX_DOCCOMPLETE_TIME_IN_MILLISECS = 180000
    static final Boolean DEFAULT_MEASUREMENTS_GENERALLY_ENABLED = false
    static final Integer DEFAULT_INITIAL_CHART_HEIGHT_IN_PIXELS = 400
    static final String DEFAULT_MAIN_URL_UNDER_TEST = ''

    /* Default (injected) attributes of GORM */
    Long	id
    //String	version

    /* Automatic timestamping of GORM */
    Date	dateCreated
    Date	lastUpdated

    /** Number of weeks very detailed data (waterfalls) is stored. Nightly cleanup deletes older data. */
    Integer detailDataStorageTimeInWeeks = DEFAULT_DETAIL_DATA_STORAGE_TIME_IN_WEEKS
    /** Maximum time in minutes osm polls result of a job run. If no result with status >= 200 returned within this time run is marked as failed */
    Integer defaultMaxDownloadTimeInMinutes = DEFAULT_MAX_DOWNLOAD_TIME_IN_MINUTES
    /** {@link EventResult}s with a docCompleteTimeInMillisecs lower than this won't be factored in csi-{@link MeasuredValue} */
    Integer minDocCompleteTimeInMillisecs = DEFAULT_MIN_DOCCOMPLETE_TIME_IN_MILLISECS
    /** {@link EventResult}s with a docCompleteTimeInMillisecs greater than this won't be factored in csi-{@link MeasuredValue} */
    Integer maxDocCompleteTimeInMillisecs = DEFAULT_MAX_DOCCOMPLETE_TIME_IN_MILLISECS
    /** If false no measurements get started at all (even for active {@link Job}s). If true the active attribute of each {@link Job} decides whether or not it runs measurements. */
    Boolean measurementsGenerallyEnabled = DEFAULT_MEASUREMENTS_GENERALLY_ENABLED
    /** Initial height of charts when opening dashboards. */
    Integer initialChartHeightInPixels = DEFAULT_INITIAL_CHART_HEIGHT_IN_PIXELS
    /** Main url under test within this osm instance. Got shown in chart title of csi dashboard. */
    String mainUrlUnderTest = DEFAULT_MAIN_URL_UNDER_TEST

    static mapping = {
    }

    static constraints = {
        detailDataStorageTimeInWeeks(defaultValue: DEFAULT_DETAIL_DATA_STORAGE_TIME_IN_WEEKS, min: -2147483648, max: 2147483647)
        defaultMaxDownloadTimeInMinutes(defaultValue: DEFAULT_MAX_DOWNLOAD_TIME_IN_MINUTES, min: -2147483648, max: 2147483647)
        minDocCompleteTimeInMillisecs(defaultValue: DEFAULT_MIN_DOCCOMPLETE_TIME_IN_MILLISECS, min: -2147483648, max: 2147483647)
        maxDocCompleteTimeInMillisecs(defaultValue: DEFAULT_MAX_DOCCOMPLETE_TIME_IN_MILLISECS, min: -2147483648, max: 2147483647)
        measurementsGenerallyEnabled(defaultValue: DEFAULT_MEASUREMENTS_GENERALLY_ENABLED)
        initialChartHeightInPixels(defaultValue: DEFAULT_INITIAL_CHART_HEIGHT_IN_PIXELS, min: -2147483648, max: 2147483647)
        mainUrlUnderTest(defaultValue: DEFAULT_MAIN_URL_UNDER_TEST, maxSize: 255)
    }

}
