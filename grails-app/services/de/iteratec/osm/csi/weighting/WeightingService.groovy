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
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.util.PerformanceLoggingService
import grails.transaction.Transactional
import org.joda.time.DateTime

/**
 * 
 * @author nkuhn
 *
 */
@Transactional
class WeightingService {
	
	MeasuredValueTagService measuredValueTagService
	CustomerSatisfactionWeightService customerSatisfactionWeightService
    PerformanceLoggingService performanceLoggingService
	
	/**
     * Weights all csiValues respective given weightFactors. Delivers a list of all {@link de.iteratec.osm.csi.weighting.WeightedCsiValue}s.
     * @param csiValues
     * @param weightFactors
     * @param csiConfiguration
     * @return
     */
    public List<WeightedCsiValue> getWeightedCsiValues(List<CsiValue> csiValues, Set<WeightFactor> weightFactors, CsiConfiguration csiConfiguration) {
        List<WeightedCsiValue> weightedCsiValues = []
        Double value = 0
        Double weight = 0
        List<Long> underlyingResultIds = []

        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, '[getWeightedCsiValues] build weighted values', PerformanceLoggingService.IndentationDepth.TWO){
            csiValues.each {CsiValue csiValue ->
                if (csiValue.isCsiRelevant()) {

                    value = csiValue.retrieveValue()
                    weight = getWeight(csiValue, weightFactors, csiConfiguration)
                    underlyingResultIds = csiValue.retrieveUnderlyingEventResultIds()

                    if (value != null && weight != null && weight > 0) {
                        addNewWeightedValue(weightedCsiValues, value, weight, underlyingResultIds)
                    }

                }
            }
        }

        List<WeightedCsiValue> flattened
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, '[getWeightedCsiValues] flatten weighted values', PerformanceLoggingService.IndentationDepth.TWO){
            flattened = flattenWeightedCsiValues(weightedCsiValues)
        }

        return flattened
    }

    /**
     * Weights all csiValues respective given jobGroupWeights in CsiSystem. Delivers a list of all {@link de.iteratec.osm.csi.weighting.WeightedCsiValue}s.
     * @param csiValues
     * @param csiSystem
     * @return
     */
    public List<WeightedCsiValue> getWeightedCsiValues(List<CsiValue> csiValues, CsiSystem csiSystem) {
        List<WeightedCsiValue> weightedCsiValues = []
        Double value = 0
        Double weight = 0
        List<Long> underlyingResultIds = []

        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, '[getWeightedCsiValues] build weighted values', PerformanceLoggingService.IndentationDepth.TWO){
            csiValues.each {CsiValue csiValue ->
                if (csiValue.isCsiRelevant()) {

                    value = csiValue.retrieveValue()
                    JobGroup jobGroupOfCsiValue = JobGroup.findById(measuredValueTagService.getJobGroupIdFromWeeklyOrDailyPageTag(csiValue.retrieveTag()))
                    JobGroupWeight jobGroupWeightOfCsiValue = csiSystem.jobGroupWeights.find {
                        it.jobGroup == jobGroupOfCsiValue
                    }
                    weight = jobGroupWeightOfCsiValue.weight
                    underlyingResultIds = csiValue.retrieveUnderlyingEventResultIds()

                    if (value != null && weight != null && weight > 0) {
                        addNewWeightedValue(weightedCsiValues, value, weight, underlyingResultIds)
                    }

                }
            }
        }

        List<WeightedCsiValue> flattened
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, '[getWeightedCsiValues] flatten weighted values', PerformanceLoggingService.IndentationDepth.TWO){
            flattened = flattenWeightedCsiValues(weightedCsiValues)
        }

        return flattened
    }
	
	void addNewWeightedValue(List<WeightedCsiValue> weightedCsiValues, Double value, Double weight, List<Long> underlyingResultIds){
		WeightedCsiValue weightedCsiValue = new WeightedCsiValue(weightedValue: new WeightedValue(value: value, weight: weight))
		if (underlyingResultIds.size()>0) {
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
	public List<WeightedCsiValue> flattenWeightedCsiValues(List<WeightedCsiValue> toFlatten){
		
		List<Double> uniqueWeights = toFlatten*.weightedValue*.weight.unique()
		
		return uniqueWeights.inject( [] ) { List<WeightedCsiValue> flattenedList, Double uniqueWeight ->
			
			List<WeightedCsiValue> weightedCsiValuesOfUniqueWeight = toFlatten.findAll { it.weightedValue.weight ==  uniqueWeight }
			flattenedList.add(
				new WeightedCsiValue(
					underlyingEventResultIds: weightedCsiValuesOfUniqueWeight*.underlyingEventResultIds.flatten(),
					weightedValue: new WeightedValue(
						value: weightedCsiValuesOfUniqueWeight.inject(0){ sum, weightedCsiValue -> sum+=weightedCsiValue.weightedValue.value; return sum} / weightedCsiValuesOfUniqueWeight.size(),
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
	 * 						{@link Set} of {@link WeightFactor}s. The csiValue is weighted respective all of the factors in this set.
	 * 						This set should not be null. If it is empty the csiValue's weight is 1.
     * @param browserConnectivityWeights
     *                      List of browserConnectivityWeights which are necessary if {@link WeightFactor#BROWSER_CONNECTIVITY_COMBINATION} is included.
	 * @return
	 */
	public Double getWeight(CsiValue csiValue, Set<WeightFactor> weightFactors, CsiConfiguration csiConfiguration){
		
		Contract.requiresArgumentNotNull("csiValue", csiValue)
		Contract.requiresArgumentNotNull("weightFactors", weightFactors)
		
		Double weight = 1

        if (weightFactors.contains(WeightFactor.HOUROFDAY)) {
            performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.TRACE, '[getWeight] HOUROFDAY', PerformanceLoggingService.IndentationDepth.THREE){
                weight *= getHourOfDayWeight(csiValue)
            }
            if (weight == 0d){
                return weight
            }
        }

        if (weightFactors.contains(WeightFactor.BROWSER_CONNECTIVITY_COMBINATION)) {
            performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.TRACE, '[getWeight] BROWSER_CONNECTIVITY_COMBINATION', PerformanceLoggingService.IndentationDepth.THREE){
                weight *= getBrowserConnectivityWeight(csiValue, csiConfiguration.browserConnectivityWeights)
            }
            if (weight == 0d){
                return weight
            }
        }

        if (weightFactors.contains(WeightFactor.PAGE)) {
            performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.TRACE, '[getWeight] PAGE', PerformanceLoggingService.IndentationDepth.THREE){
                weight *= getPageWeightFrom(csiValue, csiConfiguration.pageWeights)
            }
            if (weight == 0d){
                return weight
            }
        }

		return weight
	}

    private double getPageWeightFrom(CsiValue csiValue, List<PageWeight> pageWeights) {

        Page page = csiValue.retrieveTag().split(';').size() == 5 ?
                measuredValueTagService.findPageOfHourlyEventTag(csiValue.retrieveTag()) :
                measuredValueTagService.findPageByPageTag(csiValue.retrieveTag())
        if (page == null || pageWeights.empty) {
            return 0
        } else {
            PageWeight pageWeight = pageWeights.find { it.page == page }
            return pageWeight.weight
        }

    }

    /**
     * Should be used when persistence of connectivity profile in MeasuredValues is implemented instead of getBrowserWeight().
     *
     * @param csiValue
     * @param browserConnectivityWeights
     * @return
     */
    private getBrowserConnectivityWeight = { CsiValue csiValue, List<BrowserConnectivityWeight> browserConnectivityWeights ->

        double browserConnectivityWeight

        Browser browser
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.TRACE, '[getWeight] BCC - get browser', PerformanceLoggingService.IndentationDepth.FOUR) {
            browser = measuredValueTagService.findBrowserOfHourlyEventTag(csiValue.retrieveTag())
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

    /**
     * Calculates browserConnectivityWeight just for given browser. ConnectivityProfile isn't respected.
     * This method should be removed when persistence of connectivity profile in MeasuredValues is implemented.
     *
     * @param csiValue
     * @param browserConnectivityWeights
     * @return
     * @deprecated Better use further {@link #getBrowserConnectivityWeight}
     */
    @Deprecated
    private double getBrowserWeight(CsiValue csiValue, List<BrowserConnectivityWeight> browserConnectivityWeights) {

        double browserWeight

        Browser browser
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.TRACE, '[getWeight] BCC - get browser', PerformanceLoggingService.IndentationDepth.FOUR) {
            browser = measuredValueTagService.findBrowserOfHourlyEventTag(csiValue.retrieveTag())
        }
        if (browser == null) {
            browserWeight = 0
        } else {
            performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.TRACE, '[getWeight] BCC - get browser connectivity weight', PerformanceLoggingService.IndentationDepth.FOUR) {
                Double browserWeightFromDb = browserConnectivityWeights.find {
                    it.browser == browser
                }?.weight
                if (browserWeightFromDb == null || browserWeightFromDb <= 0) {
                    browserWeight = 0
                }else{
                    browserWeight = browserWeightFromDb
                }
            }
        }
        return browserWeight
    }

    public double getHourOfDayWeight(CsiValue csiValue) {
        Long jobGroupID = measuredValueTagService.getJobGroupIdFromWeeklyOrDailyPageTag(csiValue.retrieveTag())
        JobGroup jobGroup = JobGroup.get(jobGroupID)
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
