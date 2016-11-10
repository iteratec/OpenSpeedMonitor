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
import de.iteratec.osm.result.CsiValueService
import de.iteratec.osm.util.PerformanceLoggingService
import org.joda.time.DateTime

/**
 *
 * @author nkuhn
 *
 */
class WeightingService {

    CustomerSatisfactionWeightService customerSatisfactionWeightService
    PerformanceLoggingService performanceLoggingService
    CsiValueService csiValueService

    /**
     * Weights all csiValues respective given weightFactors. Delivers a list of all {@link de.iteratec.osm.csi.weighting.WeightedCsiValue}s.
     * @param csiValues
     * @param weightFactors
     * @param csiConfiguration
     * @return
     */
    public List<WeightedCsiValue> getWeightedCsiValues(List<CsiValue> csiValues, Set<WeightFactor> weightFactors, CsiConfiguration csiConfiguration) {
        List<CsiValue> csiRelevantValues = csiValues.findAll { csiValueService.isCsiRelevant(it) }

        return getWeightedAndFlattenedCsiValues(csiRelevantValues, weightFactors, csiConfiguration, { CsiValue value -> value.retrieveCsByWptDocCompleteInPercent() }, { CsiValue value -> value.retrieveUnderlyingEventResultsByDocComplete() })
    }

    /**
     * Weights all csiValues respective given jobGroupWeights in CsiSystem. Delivers a list of all {@link de.iteratec.osm.csi.weighting.WeightedCsiValue}s.
     * @param csiValues
     * @param csiSystem
     * @return
     */
    public List<WeightedCsiValue> getWeightedCsiValues(List<CsiValue> csiValues, CsiSystem csiSystem) {
        List<CsiValue> csiRelevantValues = csiValues.findAll { csiValueService.isCsiRelevant(it) }

        return getWeightedAndFlattenedCsiValuesForCsiSystem(csiRelevantValues, csiSystem, { CsiValue value -> value.retrieveCsByWptDocCompleteInPercent() }, { CsiValue value -> value.retrieveUnderlyingEventResultsByDocComplete() })
    }

    /**
     * Weights all csiValues respective given jobGroupWeights in CsiSystem. Delivers a list of all {@link de.iteratec.osm.csi.weighting.WeightedCsiValue}s.
     * @param csiValues
     * @param csiSystem
     * @return
     */
    public List<WeightedCsiValue> getWeightedCsiValuesByVisuallyComplete(List<CsiValue> csiValues, CsiSystem csiSystem) {
        List<CsiValue> mvsWithVisuallyCompleteValue = csiValues.findAll {
            it.retrieveCsByWptVisuallyCompleteInPercent() != null && csiValueService.isCsiRelevant(it)
        }

        return getWeightedAndFlattenedCsiValuesForCsiSystem(mvsWithVisuallyCompleteValue, csiSystem, { CsiValue value -> value.retrieveCsByWptVisuallyCompleteInPercent() }, { CsiValue value -> value.retrieveUnderlyingEventResultsByVisuallyComplete() })
    }

    /**
     * Weights all csiValues.CsByWptVisuallyCompleteInPercent respective given weightFactors.
     * @param csiValues the csiValues to weight
     * @param weightFactors the weightFactors to use
     * @param csiConfiguration the csi Configuration to use
     * @return list of all {@link de.iteratec.osm.csi.weighting.WeightedCsiValue}s
     */
    List<WeightedCsiValue> getWeightedCsiValuesByVisuallyComplete(List<CsiValue> csiValues, Set<WeightFactor> weightFactors, CsiConfiguration csiConfiguration) {
        List<CsiValue> mvsWithVisuallyCompleteValue = csiValues.findAll {
            it.retrieveCsByWptVisuallyCompleteInPercent() != null && csiValueService.isCsiRelevant(it)
        }

        return getWeightedAndFlattenedCsiValues(mvsWithVisuallyCompleteValue, weightFactors, csiConfiguration, { CsiValue value -> value.retrieveCsByWptVisuallyCompleteInPercent() }, { CsiValue value -> value.retrieveUnderlyingEventResultsByVisuallyComplete() })
    }

    /**
     * Weights and flattens the given csiValues
     * @param csiValues the csiValues
     * @param weightFactors the weightFactors to use
     * @param csiConfiguration the csiConfiguration to use
     * @param getCsiValueClosure a closure to get the value for a csiValue (ex. {value -> value.retrieveCsiValue()}* @param getUnderlyingEventResultsClosure a closure to get the underlyings event results for a csiValue (ex. {value -> value.retrieveUnderlyingEventResults()}* @return a list of weightedCsiValues
     */
    private List<WeightedCsiValue> getWeightedAndFlattenedCsiValues(List<CsiValue> csiValues, Set<WeightFactor> weightFactors, CsiConfiguration csiConfiguration, Closure getCsiValueClosure, Closure getUnderlyingEventResultsClosure) {
        List<WeightedCsiValue> weightedCsiValues = []
        Double value = 0
        Double weight = 0
        List<Long> underlyingResultIds = []

        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, '[getWeightedCsiValuesByVisuallyComplete] build weighted values', PerformanceLoggingService.IndentationDepth.TWO) {
            csiValues.each { CsiValue csiValue ->
                value = getCsiValueClosure(csiValue)
                weight = getWeight(csiValue, weightFactors, csiConfiguration)
                underlyingResultIds = getUnderlyingEventResultsClosure(csiValue)

                if (value != null && weight != null && weight > 0) {
                    addNewWeightedValue(weightedCsiValues, value, weight, underlyingResultIds)
                }

            }
        }

        List<WeightedCsiValue> flattened
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, '[getWeightedCsiValuesByVisuallyComplete] flatten weighted values', PerformanceLoggingService.IndentationDepth.TWO) {
            flattened = flattenWeightedCsiValues(weightedCsiValues)
        }

        return flattened

    }

    /**
     * Weights and flattens the given csiValues
     * @param csiValues the csiValues
     * @param weightFactors the weightFactors to use
     * @param csiConfiguration the csiConfiguration to use
     * @param getCsiValueClosure a closure to get the value for a csiValue (ex. {value -> value.retrieveCsiValue()}* @param getUnderlyingEventResultsClosure a closure to get the underlyings event results for a csiValue (ex. {value -> value.retrieveUnderlyingEventResults()}* @return a list of weightedCsiValues
     */
    private List<CsiValue> getWeightedAndFlattenedCsiValuesForCsiSystem(List<CsiValue> csiValues, CsiSystem csiSystem, Closure getCsiValueClosure, Closure getUnderlyingEventresultsClosure) {
        List<WeightedCsiValue> weightedCsiValues = []
        Double value = 0
        Double weight = 0
        List<Long> underlyingResultIds = []

        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, '[getWeightedCsiValues] build weighted values', PerformanceLoggingService.IndentationDepth.TWO) {
            csiValues.each { CsiValue csiValue ->
                value = getCsiValueClosure(csiValue)
                JobGroup jobGroupOfCsiValue = csiValue.retrieveJobGroup()
                JobGroupWeight jobGroupWeightOfCsiValue = csiSystem.jobGroupWeights.find {
                    it.jobGroup.id == jobGroupOfCsiValue.id
                }
                weight = jobGroupWeightOfCsiValue.weight
                underlyingResultIds = getUnderlyingEventresultsClosure(csiValue)

                if (value != null && weight != null && weight > 0) {
                    addNewWeightedValue(weightedCsiValues, value, weight, underlyingResultIds)
                }

            }
        }

        List<WeightedCsiValue> flattened
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, '[getWeightedCsiValues] flatten weighted values', PerformanceLoggingService.IndentationDepth.TWO) {
            flattened = flattenWeightedCsiValues(weightedCsiValues)
        }

        return flattened
    }


    void addNewWeightedValue(List<WeightedCsiValue> weightedCsiValues, Double value, Double weight, List<Long> underlyingResultIds) {
        WeightedCsiValue weightedCsiValue = new WeightedCsiValue(weightedValue: new WeightedValue(value: value, weight: weight))
        if (underlyingResultIds.size() > 0) {
            weightedCsiValue.underlyingEventResultIds = underlyingResultIds
        }
        weightedCsiValues.add(weightedCsiValue)
    }

    /**
     * <p>
     * Groups the list of {@link WeightedCsiValue}s toFlatten by unique weights. For each unique weight, a
     * new {@link WeightedCsiValue} is created with the unique weight as weight and the average of all
     * {@link WeightedCsiValue}s of that weight as value.
     * </p>
     * @param toFlatten
     * @return A new list with one {@link WeightedCsiValue} for each unique weight in the original list toFlatten.
     */
    public List<WeightedCsiValue> flattenWeightedCsiValues(List<WeightedCsiValue> toFlatten) {

        List<Double> uniqueWeights = toFlatten*.weightedValue*.weight.unique()

        return uniqueWeights.inject([]) { List<WeightedCsiValue> flattenedList, Double uniqueWeight ->

            List<WeightedCsiValue> weightedCsiValuesOfUniqueWeight = toFlatten.findAll {
                it.weightedValue.weight == uniqueWeight
            }
            flattenedList.add(
                    new WeightedCsiValue(
                            underlyingEventResultIds: weightedCsiValuesOfUniqueWeight*.underlyingEventResultIds.flatten(),
                            weightedValue: new WeightedValue(
                                    value: weightedCsiValuesOfUniqueWeight.inject(0) { sum, weightedCsiValue -> sum += weightedCsiValue.weightedValue.value; return sum } / weightedCsiValuesOfUniqueWeight.size(),
                                    weight: uniqueWeight))
            )
            return flattenedList

        }
    }

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
            performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.TRACE, '[getWeight] HOUROFDAY', PerformanceLoggingService.IndentationDepth.THREE) {
                weight *= getHourOfDayWeight(csiValue)
            }
            if (weight == 0d) {
                return weight
            }
        }

        if (weightFactors.contains(WeightFactor.BROWSER_CONNECTIVITY_COMBINATION)) {
            performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.TRACE, '[getWeight] BROWSER_CONNECTIVITY_COMBINATION', PerformanceLoggingService.IndentationDepth.THREE) {
                weight *= getBrowserConnectivityWeight(csiValue, csiConfiguration.browserConnectivityWeights)
            }
            if (weight == 0d) {
                return weight
            }
        }

        if (weightFactors.contains(WeightFactor.PAGE)) {
            performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.TRACE, '[getWeight] PAGE', PerformanceLoggingService.IndentationDepth.THREE) {
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
            return pageWeight.weight
        }

    }

    /**
     * Should be used when persistence of connectivity profile in CsiAggregations is implemented instead of getBrowserWeight().
     *
     * @param csiValue
     * @param browserConnectivityWeights
     * @return
     */
    private getBrowserConnectivityWeight = { CsiValue csiValue, List<BrowserConnectivityWeight> browserConnectivityWeights ->

        double browserConnectivityWeight

        Browser browser
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.TRACE, '[getWeight] BCC - get browser', PerformanceLoggingService.IndentationDepth.FOUR) {
            browser = csiValue.retrieveBrowser()
        }
        ConnectivityProfile connectivityProfile
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.TRACE, '[getWeight] BCC - get connectivity profile', PerformanceLoggingService.IndentationDepth.FOUR) {
            connectivityProfile = csiValue.retrieveConnectivityProfile()
        }
        if (browser == null || connectivityProfile == null) {
            browserConnectivityWeight = 0
        } else {
            performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.TRACE, '[getWeight] BCC - get browser connectivity weight', PerformanceLoggingService.IndentationDepth.FOUR) {
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