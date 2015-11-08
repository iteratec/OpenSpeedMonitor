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

package de.iteratec.osm.csi

class TimeToCsMappingService {
	
	TimeToCsMappingCacheService timeToCsMappingCacheService
	
	/**
	 * Uses database-table with frustrating load times of user-investigation to calculate customer satisfaction of given load time for given page.
	 * @param docReadyTime
	 * @param Page page
	 * @return Calculated customer-satisfaction or null if page is undefined.
	 */
	public Double getCustomerSatisfactionInPercent(Integer docReadyTimeInMilliSecs, Page page){
		if (page.isUndefinedPage()) {
			return null;
		}else{
			return getCustomerSatisfactionPercentRank(docReadyTimeInMilliSecs, page)
		}
	}
	
	/**
	 * <p>
	 * Alternative approach to translate the load-time of a specific page into a customer satisfaction.
	 * Uses database-table with time to csi mappings
	 * </p>
	 * @param docReadyTimeInMilliSecs
	 * @param page
	 * @return
	 */
	public Double getCustomerSatisfactionInPercentViaMapping(Integer docReadyTimeInMilliSecs, Page page){
        List<TimeToCsMapping> mappingsForPage = timeToCsMappingCacheService.getMappingsFor(page)
		
		Integer lowerHundredthMillisecs = Math.floor((docReadyTimeInMilliSecs/100))*100
        Integer upperHundredthMillisecs = lowerHundredthMillisecs + 100
		Integer diffDocreadyToLowerHundredthSec = docReadyTimeInMilliSecs-lowerHundredthMillisecs
		Double upperCs = lowerHundredthMillisecs == 0 ? 100 : lowerHundredthMillisecs > 20000 ? 0 :
                mappingsForPage.find {it.loadTimeInMilliSecs == lowerHundredthMillisecs}.customerSatisfaction
		Double lowerCs = upperHundredthMillisecs == 0 ? 100 : upperHundredthMillisecs > 20000 ? 0 :
                mappingsForPage.find {it.loadTimeInMilliSecs == upperHundredthMillisecs}.customerSatisfaction
		
		Double customerSatisfaction
		if (upperCs!=null && lowerCs!=null && upperCs>=lowerCs) {
			customerSatisfaction = lowerCs+(upperCs-lowerCs)*((100-diffDocreadyToLowerHundredthSec)/100)
		}
		if (log.infoEnabled) {
			log.info("customerSatisfaction=$customerSatisfaction")
		}
		return customerSatisfaction
	}
	
	/**
	 * Uses database-table with frustrating load times of user-investigation to calculate customer satisfaction of given load time for given page.
	 * @param docReadyTimeInMilliSecs
	 * @param Page page
	 * @return Calculated customer-satisfaction.
	 */
	public Double getCustomerSatisfactionPercentRank(Integer docReadyTimeInMilliSecs, Page page){
		List<Integer> frustrationLoadtimesForPage = timeToCsMappingCacheService.getCustomerFrustrations(page)
		Double rank
		Integer smaller
		Integer bigger
		if(frustrationLoadtimesForPage){
			smaller = frustrationLoadtimesForPage.findAll{it<docReadyTimeInMilliSecs}.size() 
			bigger = frustrationLoadtimesForPage.findAll{it>docReadyTimeInMilliSecs}.size()
			if (smaller+bigger==0) {
				throw new IllegalArgumentException("Percentrank couldn't be calculated for Page '${page.name}'")
			}
			rank = smaller / (smaller + bigger)
			return 1 - rank
		}else{
			throw new IllegalArgumentException("No customerFrustrationLoadtimes found for Page '${page.name}'")
		}
	}
	
	/**
	 * Reads frustration load times from db/cache for given page.
	 * @param page
	 * 	{@link Page} frustration load times should be read for.
	 * @return Frustration load times from db/cache for given page.
	 */
	public List<Integer> getCachedFrustrations(Page page){
		return timeToCsMappingCacheService.getCustomerFrustrations(page)
	}
	
	/**
	 * Checks whether more than one different frustration timings exist for given {@link Page} page.
	 * @param page
	 * @return true if more than one different frustration timings exist for given {@link Page} page. false otherwise. false if page is null or undefinde page, too.
	 */
	public Boolean validFrustrationsExistFor(Page page){
		return isValid(page) && getCachedFrustrations(page).unique().size()>1
	}

    public Boolean validMappingsExistFor(Page page){
        return isValid(page) && timeToCsMappingCacheService.getMappingsFor(page)
    }

    Boolean isValid(Page page){
        return page != null && !page.isUndefinedPage()
    }

	public getPageMappingAsChartData(Page page){
		List<Integer> frustrations = getCachedFrustrations(page)
		Integer countFrustrations = frustrations.size()
		frustrations.sort()
		int lastFloor = 0
		List<Integer> frustrationsSinceLastCount = []
		frustrations.each {frustration ->
			double actualFloor = Math.floor(frustration / 100)
			if (actualFloor > lastFloor){
				lastFloor = actualFloor
			}
		}
		int loadTimeInMillisecs = 100
		while(loadTimeInMillisecs < 12000){

			loadTimeInMillisecs+=100
		}
	}

}
