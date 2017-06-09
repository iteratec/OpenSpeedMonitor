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

package de.iteratec.osm.csi.weighting

import de.iteratec.osm.csi.*
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.Contract
import de.iteratec.osm.util.PerformanceLoggingService
import org.joda.time.DateTime

/**
 *
 * @author nkuhn
 *
 */
class WeightingService {

    PerformanceLoggingService performanceLoggingService

    /**
     * Determines weight of csiValue respective given list of {@link WeightFactor}s. Weights of the respective different {@link WeightFactor}s get multiplied.
     * @param csiValue
     * 						Value to get Weight for. Should not be null.
     * @param weightFactors
     * {@link Set} of {@link WeightFactor}s. The csiValue is weighted respective all of the factors in this set.
     * 						This set should not be null. If it is empty the csiValue's weight is 1.
     * @param browserConnectivityWeights
     *                      List of browserConnectivityWeights which are necessary if {@link WeightFactor#BROWSER_CONNECTIVITY_COMBINATION} is included.
     * @return
     */
    public Double getWeight(CsiValue csiValue, Set<WeightFactor> weightFactors, CsiConfiguration csiConfiguration) {

        Contract.requiresArgumentNotNull("csiValue", csiValue)
        Contract.requiresArgumentNotNull("weightFactors", weightFactors)

        Double weight = 1
        if (weightFactors.contains(WeightFactor.HOUROFDAY)) {
            performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[getWeight] HOUROFDAY', 5) {
                weight *= getHourOfDayWeight(csiValue)
            }
            if (weight == 0d) {
                return weight
            }
        }

        if (weightFactors.contains(WeightFactor.BROWSER_CONNECTIVITY_COMBINATION)) {
            performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[getWeight] BROWSER_CONNECTIVITY_COMBINATION', 5) {
                weight *= getBrowserConnectivityWeight(csiValue, csiConfiguration.browserConnectivityWeights)
            }
            if (weight == 0d) {
                return weight
            }
        }

        if (weightFactors.contains(WeightFactor.PAGE)) {
            performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[getWeight] PAGE', 5) {
                weight *= getPageWeightFrom(csiValue, csiConfiguration.pageWeights)
            }
            if (weight == 0d) {
                return weight
            }
        }

        return weight
    }

    private double getPageWeightFrom(CsiValue csiValue, List<PageWeight> pageWeights) {

        Page pageOfCsiValue = csiValue.retrievePage()
        if (pageOfCsiValue == null || pageWeights.empty) {
            return 0
        } else {
            PageWeight pageWeight = pageWeights.find { it.page == pageOfCsiValue }
            return pageWeight ? pageWeight.weight : 0
        }

    }

    /**
     * Should be used when persistence of connectivity profile in CsiAggregations is implemented instead of getBrowserWeight().
     *
     * @param csiValue
     * @param browserConnectivityWeights
     * @return
     */
    private double getBrowserConnectivityWeight(CsiValue csiValue, List<BrowserConnectivityWeight> browserConnectivityWeights){

        double browserConnectivityWeight

        Browser browser
        performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[getWeight] BCC - get browser', 6) {
            browser = csiValue.retrieveBrowser()
        }
        ConnectivityProfile connectivityProfile
        performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[getWeight] BCC - get connectivity profile', 6) {
            connectivityProfile = csiValue.retrieveConnectivityProfile()
        }
        if (browser == null || connectivityProfile == null) {
            browserConnectivityWeight = 0
        } else {
            performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[getWeight] BCC - get browser connectivity weight', 6) {
                Double browserConnectivityWeightFromDb = browserConnectivityWeights.find {
                    it.browser == browser && it.connectivity == connectivityProfile
                }?.weight
                if (browserConnectivityWeightFromDb == null || browserConnectivityWeightFromDb <= 0) {
                    browserConnectivityWeight = 0
                } else {
                    browserConnectivityWeight = browserConnectivityWeightFromDb
                }
            }
        }
        return browserConnectivityWeight
    }

    public double getHourOfDayWeight(CsiValue csiValue) {
        JobGroup jobGroup = csiValue.retrieveJobGroup()
        CsiDay dayForCsiValue = jobGroup.csiConfiguration.csiDay
        int hour = new DateTime(csiValue.retrieveDate()).getHourOfDay()
        Double hourofdayWeight = dayForCsiValue.getHourWeight(hour)

        if (hourofdayWeight == null || hourofdayWeight <= 0) {
            return 0
        } else {
            return hourofdayWeight
        }

    }
}
