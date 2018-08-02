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

    static final Boolean DEFAULT_MEASUREMENTS_GENERALLY_ENABLED = false

    static final Boolean DEFAULT_DATABASE_CLEANUP_ENABLED = false

    static final Boolean PAUSE_JOBPROCESSING_FOR_OVERLOADED_LOCATIONS = false

    /** If false no measurements get started at all (even for active {@link Job}s). If true the active attribute of each {@link Job} decides whether or not it runs measurements. */
    Boolean measurementsGenerallyEnabled = DEFAULT_MEASUREMENTS_GENERALLY_ENABLED

    /** If false no nightly database cleanup get started. If true the nightly database cleanup jobs are active ({@link DailyOldJobResultsWithDependenciesCleanup} and {@link DbCleanupOldCsiAggregationsWithDependenciesJob}) */
    Boolean databaseCleanupEnabled = DEFAULT_DATABASE_CLEANUP_ENABLED

    Boolean pauseJobProcessingForOverloadedLocations = PAUSE_JOBPROCESSING_FOR_OVERLOADED_LOCATIONS

    /**
     * Gets status of measurementsGenerallyEnabled
     * If false no measurements get started at all (even for active {@link Job}s). If true the active attribute of each {@link Job} decides whether or not it runs measurements.
     * @return Whether the performance measurement is generally enabled or not.
     */
    boolean areMeasurementsGenerallyEnabled(){
        return measurementsGenerallyEnabled
    }

    /**
     * Gets status of databaseCleanupEnabled
     * If false no database-cleanup will run nightly. If true a nightly database-cleanup will be run.
     * @return Whether the nightly-cleanup is generally enabled or not.
     */
    boolean isNightlyDatabaseCleanupEnabled(){
        return databaseCleanupEnabled
    }

    /**
     * Sets measurementsGenerallyEnabled to true
     */
    /**
     * Activates measurements generally.
     */
    void setActiveStatusOfMeasurementsGenerally(Boolean activationToSet){
        if (activationToSet == true) activateMeasurementsGenerally()
        else if (activationToSet == false) deactivateMeasurementsGenerally()
    }

    /**
     * Sets measurementsGenerallyEnabled to true
     */
    /**
     * Activates measurements generally.
     */
    void activateMeasurementsGenerally(){
        measurementsGenerallyEnabled = true
    }

    /**
     * Sets measurementsGenerallyEnabled to false
     */
    void deactivateMeasurementsGenerally(){
        measurementsGenerallyEnabled = false
    }

    /**
     * Get status of databaseCleanupEnabled
     * If false no nightly database cleanup get started. If true the nightly database cleanup jobs are active ({@link DailyOldJobResultsWithDependenciesCleanup} and {@link DbCleanupOldCsiAggregationsWithDependenciesJob})
     * @return Whether the nightly database cleanup is enabled or not
     */
    boolean isDatabaseCleanupEnabled(){
        return databaseCleanupEnabled
    }

    /**
     * Activates databaseCleanup
     */
    void activateDatabaseCleanup(){
        databaseCleanupEnabled = true
    }

    /**
     * Deactivates databaseCleanup
     */
    void deactivateDatabaseCleanup(){
        databaseCleanupEnabled = false
    }
}
