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

package de.iteratec.osm.result

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiSystem
import de.iteratec.osm.csi.CsiValue
import de.iteratec.osm.csi.JobGroupWeight
import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.csi.weighting.WeightedCsiValue
import de.iteratec.osm.csi.weighting.WeightedValue
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.util.PerformanceLoggingService

class CsiValueService {

    OsmConfigCacheService osmConfigCacheService
    PerformanceLoggingService performanceLoggingService
    WeightingService weightingService

    /**
     * Weights all csiValues respective given weightFactors. Delivers a list of all {@link de.iteratec.osm.csi.weighting.WeightedCsiValue}s.
     * @param csiValues
     * @param weightFactors
     * @param csiConfiguration
     * @return
     */
    public List<WeightedCsiValue> getWeightedCsiValues(List<CsiValue> csiValues, Set<WeightFactor> weightFactors, CsiConfiguration csiConfiguration) {

        List<CsiValue> csiRelevantValues
        performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[getWeightedCsiValues] filter csiValues by relevance', 3) {
            csiRelevantValues = csiValues.findAll { isCsiRelevant(it) }
        }

        return getWeightedAndFlattenedCsiValues(csiRelevantValues, weightFactors, csiConfiguration, { CsiValue value -> value.retrieveCsByWptDocCompleteInPercent() }, { CsiValue value -> value.retrieveUnderlyingEventResultsByDocComplete() })
    }

    /**
     * Weights all csiValues respective given jobGroupWeights in CsiSystem. Delivers a list of all {@link de.iteratec.osm.csi.weighting.WeightedCsiValue}s.
     * @param csiValues
     * @param csiSystem
     * @return
     */
    public List<WeightedCsiValue> getWeightedCsiValues(List<CsiValue> csiValues, CsiSystem csiSystem) {
        List<CsiValue> csiRelevantValues = csiValues.findAll { isCsiRelevant(it) }

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
            it.retrieveCsByWptVisuallyCompleteInPercent() != null && isCsiRelevant(it)
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
            it.retrieveCsByWptVisuallyCompleteInPercent() != null && isCsiRelevant(it)
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

        performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[getWeightedCsiValues] build weighted values', 3) {
            csiValues.each { CsiValue csiValue ->
                performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[build weighted values] get value', 4) {
                    value = getCsiValueClosure(csiValue)
                }
                performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[build weighted values] get weight', 4) {
                    weight = weightingService.getWeight(csiValue, weightFactors, csiConfiguration)
                }
                performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[build weighted values] get underlying event results', 4) {
                    underlyingResultIds = getUnderlyingEventResultsClosure(csiValue)
                }
                performanceLoggingService.logExecutionTimeSilently(PerformanceLoggingService.LogLevel.DEBUG, '[build weighted values] create new weighted value and add it to list', 4) {
                    if (value != null && weight != null && weight > 0) {
                        addNewWeightedValue(weightedCsiValues, value, weight, underlyingResultIds)
                    }
                }
            }
        }

        List<WeightedCsiValue> flattened
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, '[getWeightedCsiValues] flatten weighted values', 1) {
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

        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, '[getWeightedCsiValues] build weighted values', 1) {
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
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, '[getWeightedCsiValues] flatten weighted values', 1) {
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
     * Whether or not this value should be factored in csi-calculations.
     * <ul>
     * <li>{@link EventResult}s are relevant if they have set a loadTimeInMillisecs and customerSatisfactionInPercent and customerSatisfactionInPercent
     * is within valid range.</li>
     * <li>{@link MesauredValue}s are relevant if they have a state of {@link Calculated#Yes}</li>
     * </ul>
     * @return
     * @see CsiConfigCacheService
     */
    boolean isCsiRelevant(CsiValue csiValue){
        switch (csiValue){
            case {it instanceof EventResult}:
                return isCsiRelevant((EventResult) csiValue)
            case {it instanceof CsiAggregation}:
                return isCsiRelevant((CsiAggregation) csiValue)
            default:
                return false
        }
    }

    boolean isCsiRelevant(EventResult eventResult) {
        return eventResult.csByWptDocCompleteInPercent && eventResult.docCompleteTimeInMillisecs &&
                (eventResult.docCompleteTimeInMillisecs >= osmConfigCacheService.getMinValidLoadtime(24) &&
                        eventResult.docCompleteTimeInMillisecs <= osmConfigCacheService.getMaxValidLoadtime(24))
    }

    boolean isCsiRelevant(CsiAggregation csiAggregation) {
        return csiAggregation.isCalculated() && csiAggregation.csByWptDocCompleteInPercent != null
    }
}
