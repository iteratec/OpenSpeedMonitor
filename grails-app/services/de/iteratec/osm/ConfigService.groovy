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

import de.iteratec.osm.csi.CsiTransformation

/**
 * ConfigService
 * Delivers application-wide configurations from backend.
 * @see OsmConfiguration
 */
class ConfigService {

	def grailsApplication
	InMemoryConfigService inMemoryConfigService


	/**
	 * Gets detail-data storage time in weeks from osm-configuration.
	 * @return Time in weeks to store detail-data of the application.
	 * @see OsmConfiguration
	 * @throws IllegalStateException if single {@link OsmConfiguration} can't be read from db or {@link OsmConfiguration#detailDataStorageTimeInWeeks} isn't set.
	 */
    Integer getDetailDataStorageTimeInWeeks() {
        return getConfig().detailDataStorageTimeInWeeks
    }
	
    /**
     * Gets detail-data storage time in weeks from osm-configuration.
     * @return Time in weeks to store detail-data of the application.
     * @see OsmConfiguration
     * @throws IllegalStateException if single {@link OsmConfiguration} can't be read from db or {@link OsmConfiguration#detailDataStorageTimeInWeeks} isn't set.
     */
    Integer getDefaultMaxDownloadTimeInMinutes() {
        return getConfig().defaultMaxDownloadTimeInMinutes
    }
	
	/** 
	 * Gets minDocCompleteTimeInMillisecs from osm-configuration.
	 * {@link EventResult}s with a loadTimeInMillisecs lower than this won't be factored in csi-{@link CsiAggregation}s.
	 * @return The minimum doc complete time in millisecs {@link EventResult}s get factored with in csi-{@link CsiAggregation}s.
     * @see OsmConfiguration
     * @throws IllegalStateException if single {@link OsmConfiguration} can't be read from db or {@link OsmConfiguration#minDocCompleteTimeInMillisecs} isn't set.
	 */
	Integer getMinDocCompleteTimeInMillisecs(){
        return getConfig().minDocCompleteTimeInMillisecs
	}
	
	/**
	 * Gets maxDocCompleteTimeInMillisecs from osm-configuration.
	 * {@link EventResult}s with a loadTimeInMillisecs lower than this won't be factored in csi-{@link CsiAggregation}s.
	 * @return The maximum doc complete time in millisecs {@link EventResult}s get factored with in csi-{@link CsiAggregation}s.
     * @see OsmConfiguration
     * @throws IllegalStateException if single {@link OsmConfiguration} can't be read from db or {@link OsmConfiguration#maxDocCompleteTimeInMillisecs} isn't set.
	 */
	Integer getMaxDocCompleteTimeInMillisecs(){
        return getConfig().maxDocCompleteTimeInMillisecs
	}
	
	/**
	 * Gets initial height of charts when opening dashboards from osm-configuration.
	 * @see OsmConfiguration
	 * @throws IllegalStateException if single {@link OsmConfiguration} can't be read from db or {@link OsmConfiguration#measurementsGenerallyEnabled} isn't set.
	 */
	Integer getInitialChartHeightInPixels(){
        return getConfig().initialChartHeightInPixels
	}

	/**
	 * Gets initial width of charts when opening dashboards from osm-configuration.
	 * @see OsmConfiguration
	 * @throws IllegalStateException if single {@link OsmConfiguration} can't be read from db or {@link OsmConfiguration#measurementsGenerallyEnabled} isn't set.
	 */
	Integer getInitialChartWidthInPixels(){
        return getConfig().initialChartWidthInPixels
	}

	/**
	 * Gets main url under test within this osm instance from osm-configuration. That url got shown in chart title of csi dashboard.
	 * @see OsmConfiguration
	 * @throws IllegalStateException if single {@link OsmConfiguration} can't be read from db or {@link OsmConfiguration#measurementsGenerallyEnabled} isn't set.
	 */
	String getMainUrlUnderTest(){
        return getConfig().mainUrlUnderTest
	}

    /**
     * Gets max result-data storage time in months from osm-configuration.
     * @return Time in months to store results of the application.
     * @see OsmConfiguration
     * @throws IllegalStateException if single {@link OsmConfiguration} can't be read from db or {@link OsmConfiguration#measurementsGenerallyEnabled} isn't set.
     */
    Integer getMaxDataStorageTimeInMonths(){
        return getConfig().maxDataStorageTimeInMonths
    }

	/**
	 * Gets max BatchActivity storage time in days from osm-configuration.
	 * @return Time in days to store BatchActivites of the application.
	 * @see OsmConfiguration
	 * @throws IllegalStateException if single {@link OsmConfiguration} can't be read from db or {@link OsmConfiguration#measurementsGenerallyEnabled} isn't set.
	 */
	Integer getMaxBatchActivityStorageTimeInDays(){
        return getConfig().maxBatchActivityStorageTimeInDays
	}

	/**
	 * Get status of databaseCleanupEnabled
	 * If false no nightly database cleanup get started. If true the nightly database cleanup jobs are active ({@link DailyOldJobResultsWithDependenciesCleanup} and {@link DbCleanupOldCsiAggregationsWithDependenciesJob})
	 * @return Whether the nightly database cleanup is enabled or not
	 */
	Boolean isDatabaseCleanupEnabled(){
		return inMemoryConfigService.isDatabaseCleanupEnabled()
	}

    /**
     * Get method load times should be calculated to percent of
     * users which are satisfied by that load time.
     */
    CsiTransformation getCsiTransformation(){
        return getConfig().csiTransformation
    }

    /**
     * Gets the name of the used database driver of running environment
     * @return {@link String} of the used database driver name
     */
    public String getDatabaseDriverClassName() {
        return grailsApplication.config.dataSource.driverClassName;
    }

    private OsmConfiguration getConfig(){
        List<OsmConfiguration> osmConfigs = OsmConfiguration.list()
        int confCount = osmConfigs.size()
        if (confCount != 1) {
            throw new IllegalStateException("It must exist exact one Configuration in database. Found ${confCount}!")
        }else{
            return osmConfigs[0]
        }
    }
	
}
