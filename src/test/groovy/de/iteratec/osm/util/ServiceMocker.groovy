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

package de.iteratec.osm.util


import de.iteratec.osm.csi.EventCsiAggregationService
import de.iteratec.osm.csi.JobGroupCsiAggregationService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.PageCsiAggregationService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.MvQueryParams
import org.grails.testing.GrailsUnitTest

/**
 * <p>
 * Mocks grails-Services.
 * These services get injected into instance-variables of other services in production by spring .
 * In unit-tests these services has to be mocked. To avoid duplication these mocks are assembled in this class.
 * </p>
 * @author nkuhn
 *
 */
class ServiceMocker implements GrailsUnitTest {

	private ServiceMocker(){}

	static ServiceMocker create() {
		return new ServiceMocker()
	}

	/**
	 * Mocks methods of {@link EventCsiAggregationService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param toReturnFromGetOrCalculateHourlyCsiAggregations
	 * 		To return from mocked method {@link EventCsiAggregationService#getOrCalculateHourylCsiAggregations}.
	 */
	void mockEventCsiAggregationService(serviceToMockIn, List<CsiAggregation> toReturnFromGetOrCalculateHourlyCsiAggregations){
		def eventCsiAggregationServiceMocked = new EventCsiAggregationService()
		eventCsiAggregationServiceMocked.metaClass.getHourlyCsiAggregations = { Date from, Date to, MvQueryParams mvQueryParams ->
			return 	toReturnFromGetOrCalculateHourlyCsiAggregations
		}
		serviceToMockIn.eventCsiAggregationService = eventCsiAggregationServiceMocked
	}
	/**
	 * Mocks methods of {@link PageCsiAggregationService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param toReturnFromGetOrCalculateWeeklyPageCsiAggregations
	 * 		List of {@link CsiAggregation}s, the method {@link PageCsiAggregationService#getOrCalculatePageCsiAggregations(java.util.Date, java.util.Date, CsiAggregationInterval,List<JobGroup>,List<Page>)} should return.
	 */
	void mockPageCsiAggregationService(serviceToMockIn, List<CsiAggregation> toReturnFromGetOrCalculateWeeklyPageCsiAggregations){
		def pageCsiAggregationServiceMocked = new PageCsiAggregationService()
		// new Version:
		pageCsiAggregationServiceMocked.metaClass.getOrCalculatePageCsiAggregations = {
			Date from, Date to, CsiAggregationInterval mvInterval, List<JobGroup> csiGroups, List<Page> pages ->
			return toReturnFromGetOrCalculateWeeklyPageCsiAggregations
		}
		serviceToMockIn.pageCsiAggregationService = pageCsiAggregationServiceMocked
	}
	/**
	 * Mocks {@link JobGroupCsiAggregationService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param toReturnFromGetOrCalculateWeeklyShopCsiAggregations
	 * 		List of {@link CsiAggregation}s, the method {@link JobGroupCsiAggregationService#getOrCalculateWeeklyShopCsiAggregations(java.util.Date, java.util.Date)} should return.
	 */
	void mockJobGroupCsiAggregationService(serviceToMockIn, List<CsiAggregation> toReturnFromGetOrCalculateWeeklyShopCsiAggregations){
		def jobGroupCsiAggregationServiceMocked = new JobGroupCsiAggregationService()
		jobGroupCsiAggregationServiceMocked.metaClass.getOrCalculateWeeklyShopCsiAggregations =  { Date from, Date to ->
			return toReturnFromGetOrCalculateWeeklyShopCsiAggregations
		}
		jobGroupCsiAggregationServiceMocked.metaClass.getOrCalculateShopCsiAggregations = { Date from, Date to, CsiAggregationInterval interval, List csiGroups ->
			return toReturnFromGetOrCalculateWeeklyShopCsiAggregations
		}
		serviceToMockIn.jobGroupCsiAggregationService = jobGroupCsiAggregationServiceMocked
	}
}
