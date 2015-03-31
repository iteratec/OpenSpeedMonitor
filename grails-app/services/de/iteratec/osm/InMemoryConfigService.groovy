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
 * InMemoryConfigService
 * A service class encapsulates the core business logic of a Grails application
 */
class InMemoryConfigService {

    def grailsApplication

    static transactional = true

    static final Boolean DEFAULT_MEASUREMENTS_GENERALLY_ENABLED = false

    static final Integer DEFAULT_MAX_DATA_STORAGE_TIME_IN_MONTHS = 13

    static final Boolean DEFAULT_DATABASE_CLEANUP_ENABLED = false

    /** If false no measurements get started at all (even for active {@link Job}s). If true the active attribute of each {@link Job} decides whether or not it runs measurements. */
    Boolean measurementsGenerallyEnabled = DEFAULT_MEASUREMENTS_GENERALLY_ENABLED

    /** Maximum Number of months osm keeps results in database   */
    Integer maxDataStorageTimeInMonths = DEFAULT_MAX_DATA_STORAGE_TIME_IN_MONTHS

    /** If false no nightly database cleanup get started. If true the nightly database cleanup jobs are active ({@link DailyOldJobResultsWithDependenciesCleanup} and {@link DbCleanupOldMeasuredValuesWithDependenciesJob}) */
    Boolean databaseCleanupEnabled = DEFAULT_DATABASE_CLEANUP_ENABLED

    /**
     * Gets status of measurementsGenerallyEnabled
     * If false no measurements get started at all (even for active {@link Job}s). If true the active attribute of each {@link Job} decides whether or not it runs measurements.
     * @return Whether the performance measurement is generally enabled or not.
     */
    boolean areMeasurementsGenerallyEnabled(){
        return measurementsGenerallyEnabled
    }

    /**
     * Sets measurementsGenerallyEnabled to true
     */
    void activateMeasurementsGenerallyEnabled(){
        measurementsGenerallyEnabled = true
    }

    /**
     * Sets measurementsGenerallyEnabled to false
     */
    void deactivateMeasurementsGenerallyEnabled(){
        measurementsGenerallyEnabled = false
    }

    /**
     * Get status of databaseCleanupEnabled
     * If false no nightly database cleanup get started. If true the nightly database cleanup jobs are active ({@link DailyOldJobResultsWithDependenciesCleanup} and {@link DbCleanupOldMeasuredValuesWithDependenciesJob})
     * @return Whether the nightly database cleanup is enabled or not
     */
    boolean isDatabaseCleanupEnabled(){
        return databaseCleanupEnabled
    }

    /**
     * Sets databaseCleanupEnabled to true
     */
    void activateDatabaseCleanupEnabled(){
        databaseCleanupEnabled = true
    }

    /**
     * Sets databaseCleanupEnabled to false
     */
    void deactivateDatabaseCleanupEnabled(){
        databaseCleanupEnabled = false
    }

    /**
     * Gets the maximum number of months osm keeps results in database
     * @return The number in months wich osm keeps results in database
     */
    Integer getMaxDataStorageTimeInMonths(){
        return maxDataStorageTimeInMonths
    }

    /**
     * Sets the maximum number of months wich osm keeps results in database
     * @param number in months
     */
    void setMaxDataStorageTimeInMonths(Integer months){
        maxDataStorageTimeInMonths = months
    }
}
