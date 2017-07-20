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

import de.iteratec.osm.ConfigService
import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.BatchActivityUpdaterDummy
import de.iteratec.osm.csi.*
import de.iteratec.osm.csi.transformation.TimeToCsMappingCacheService
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.dao.CriteriaSorting
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.environment.wptserverproxy.ProxyService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.report.external.MetricReportingService
import de.iteratec.osm.result.*
import de.iteratec.osm.result.dao.EventResultDaoService
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.web.mapping.LinkGenerator
import groovy.mock.interceptor.StubFor
import org.joda.time.DateTime

import static de.iteratec.osm.OsmConfiguration.DEFAULT_MIN_VALID_LOADTIME
import static de.iteratec.osm.OsmConfiguration.DEFAULT_MAX_VALID_LOADTIME

/**
 * <p>
 * Mocks grails-Services.
 * These services get injected into instance-variables of other services in production by spring .
 * In unit-tests these services has to be mocked. To avoid duplication these mocks are assembled in this class.
 * </p>
 * @author nkuhn
 *
 */
@TestMixin(GrailsUnitTestMixin)
class ServiceMocker {

	private ServiceMocker(){}
	public static ServiceMocker create(){
		return new ServiceMocker()
	}

	//TODO: Write one generic method to mock arbitrary methods of arbitrary services:
//	void mockServiceMethod(serviceToMockInjectedServiceIn, Class serviceClassToMock, String nameOfMethodToMock){
//		def serviceMock = new StubFor(serviceClassToMock, true)
//		serviceMock.demand.nameOfMethodToMock(
//			1..10000,
//			getClosureToExecute(serviceClassToMock, nameOfMethodToMock)
//		)
//		serviceToMockInjectedServiceIn.metaClass.setAttribute(
//			this, serviceToMockInjectedServiceIn, withLowerFirstLetter(serviceClassToMock.getName()),  serviceMock.proxyInstance(), false, false)
//	}
	Closure getClosureToExecute(serviceClassToMock, nameOfMethodToMock){
		//TODO: should deliver the closure to be executed if the method nameOfMethodToMock of service serviceClassToMock is called in unit-tests
	}


	/**
	 * Mocks methods of {@link CsiAggregationUpdateEventDaoService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockCsiAggregationUpdateEventDaoService(serviceToMockIn){
		def csiAggregationUpdateEventDaoService = new CsiAggregationUpdateEventDaoService()
		csiAggregationUpdateEventDaoService.metaClass.createUpdateEvent = { Long csiAggregationId, CsiAggregationUpdateEvent.UpdateCause cause ->
				new CsiAggregationUpdateEvent(
					dateOfUpdate: new Date(),
					csiAggregationId: csiAggregationId,
					updateCause: cause
				).save(failOnError: true)

		}
		serviceToMockIn.csiAggregationUpdateEventDaoService = csiAggregationUpdateEventDaoService
	}
	/**
	 * Mocks methods of {@link CsiConfigCacheService}
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockOsmConfigCacheService(serviceToMockIn){
		def osmConfigCacheService = new OsmConfigCacheService()
		Integer minTimeToExpect = DEFAULT_MIN_VALID_LOADTIME
		osmConfigCacheService.metaClass.getMinValidLoadtime = {Double ageToleranceInHours ->
			return minTimeToExpect
		}
		Integer maxTimeToExpect = DEFAULT_MAX_VALID_LOADTIME
		osmConfigCacheService.metaClass.getMaxValidLoadtime = {Double ageToleranceInHours ->
			return maxTimeToExpect
		}
		serviceToMockIn.osmConfigCacheService = osmConfigCacheService
	}

	/**
	 * Mocks methods in {@link CsiAggregationUtilService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockCsiAggregationUtilService(serviceToMockIn, DateTime toReturnFromGetNowInUtc){
		def csiAggregationUtilService = new CsiAggregationUtilService()
		csiAggregationUtilService.metaClass.getNowInUtc = {	->
			return toReturnFromGetNowInUtc
		}
		serviceToMockIn.csiAggregationUtilService = csiAggregationUtilService
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
	/**
	 * Mocks methods in {@link CsTargetGraphDaoService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param labelOfActualCsTargetGraph
	 * 		The label of the {@link CsTargetGraph} to be returned from {@link CsTargetGraphDaoService#getActualCsTargetGraph()}.
	 */
	void mockCsTargetGraphDaoService(serviceToMockIn, String labelOfActualCsTargetGraph){
		def csTargetGraphDaoService = new CsTargetGraphDaoService()
		csTargetGraphDaoService.metaClass.getActualCsTargetGraph = { ->
			return CsTargetGraph.findByLabel(labelOfActualCsTargetGraph)
		}
		serviceToMockIn.csTargetGraphDaoService = csTargetGraphDaoService
	}
	/**
	 * Mocks methods of {@link LinkGenerator}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param toReturnFromLink
	 * 		To be returned from method {@link LinkGenerator#link()}.
	 */
	void mockLinkGenerator(serviceToMockIn, String toReturnFromLink){
		def grailsLinkGeneratorMocked = new LinkGenerator(){

			@Override
			String resource(Map params) {
				return null
			}

			@Override
			String link(Map params) {
				return toReturnFromLink
			}

			@Override
			String link(Map params, String encoding) {
				return null
			}

			@Override
			String getContextPath() {
				return null
			}

			@Override
			String getServerBaseURL() {
				return null
			}
		}
		serviceToMockIn.grailsLinkGenerator = grailsLinkGeneratorMocked
	}

	/**
	 * Mocks methods of {@link de.iteratec.osm.csi.transformation.TimeToCsMappingCacheService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param timeToCsMappings
	 * 		To be returned from method {@link de.iteratec.osm.csi.transformation.TimeToCsMappingCacheService#getMappings()}.
	 * @param frustrations
	 * 		To be returned from method {@link de.iteratec.osm.csi.transformation.TimeToCsMappingCacheService#getCustomerFrustrations(de.iteratec.osm.csi.Page)}
	 */
	void mockTimeToCsMappingCacheService(serviceToMockIn, timeToCsMappings, frustrations){
		TimeToCsMappingCacheService timeToCsMappingCacheService = new TimeToCsMappingCacheService()

		timeToCsMappingCacheService.metaClass.getMappingsFor = {Page page ->
			return timeToCsMappings
		}
		timeToCsMappingCacheService.metaClass.getCustomerFrustrations = {Page page ->
			return frustrations
		}

		serviceToMockIn.timeToCsMappingCacheService = timeToCsMappingCacheService
	}
	/**
	 * Mocks methods of {@link de.iteratec.osm.csi.transformation.TimeToCsMappingService}. The methods do not deliver
	 * sensible return values. Using tests should not depend on these values!
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockTTCsMappingService(serviceToMockIn){
		def timeToCsMappingService = new StubFor(TimeToCsMappingService, true)
		timeToCsMappingService.demand.getCustomerSatisfactionInPercent (0 .. 1000) { Integer docCompleteTime, Page testedPage, csiConfiguration ->
			return 1
		}
		timeToCsMappingService.demand.validFrustrationsExistFor(0 .. 1000) { Page testedPage ->
			//not the concern of this test
		}
        timeToCsMappingService.demand.validMappingsExistFor(0 .. 1000) { Page testedPage ->
            //not the concern of this test
        }
		serviceToMockIn.timeToCsMappingService = timeToCsMappingService.proxyInstance()
	}
	/**
	 * Mocks methods of {@link de.iteratec.osm.ConfigService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param toReturnFromGetDatabaseDriverClassName
	 * 		To be returned from method {@link de.iteratec.osm.ConfigService#getDatabaseDriverClassName()}.
	 */
	void mockConfigService(serviceToMockIn, Integer toReturnFromGetDefaultMaxDownloadTimeInMinutes, CsiTransformation toReturnFromGetCsiTransformation){
		ConfigService configServiceMock = new ConfigService()
		configServiceMock.metaClass.getDefaultMaxDownloadTimeInMinutes = { ->
			return toReturnFromGetDefaultMaxDownloadTimeInMinutes
		}
		configServiceMock.metaClass.getCsiTransformation = { ->
			return toReturnFromGetCsiTransformation
		}
		serviceToMockIn.configService = configServiceMock
	}
	/**
	 * Mocks methods in list methodsToMock in service of class classOfServiceToMock. That service get mocked in owning service serviceToMockIn.
	 * @param classOfServiceToMock
	 * 		Class of grails service which should be mocked.
	 * @param serviceToMockIn
	 * 		Grails service with the service to mock as instance-variable.
	 * @param methodsToMock
	 * 		List of methods that get mocked. For each method a return value is included.
	 * @see MethodToMock
	 */
	void mockService(Class classOfServiceToMock, serviceToMockIn, List<MethodToMock> methodsToMock){
		def serviceMock = new StubFor(classOfServiceToMock, true)
		methodsToMock.each{methodToMock ->
			String methodName = methodToMock.method.getName()
			if (methodToMock.method.getParameterTypes().size()==0){
				serviceMock.demand."$methodName"{ ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==1){
				serviceMock.demand."$methodName"{paramOne ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==2){
				serviceMock.demand."$methodName"{paramOne, paramTwo ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==3){
				serviceMock.demand."$methodName"{paramOne, paramTwo, paramThree ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==4){
				serviceMock.demand."$methodName"{paramOne, paramTwo, paramThree, paramFour ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==5){
				serviceMock.demand."$methodName"{paramOne, paramTwo, paramThree, paramFour, paramFive ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==6){
				serviceMock.demand."$methodName"{paramOne, paramTwo, paramThree, paramFour, paramFive, paramSix ->
					return methodToMock.toReturn
				}
			}else{
				throw new IllegalArgumentException('Nobody should write functions with more than six parameters. Please refactor your code!')
			}
		}
		String nameOfServiceField = classOfServiceToMock.getSimpleName()
		nameOfServiceField = nameOfServiceField[0].toLowerCase() + nameOfServiceField.substring(1)
		serviceToMockIn."$nameOfServiceField" = serviceMock.proxyInstance()
	}
	/**
	 * Mocks methods of{@link ProxyService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockProxyService(serviceToMockIn){
		ProxyService proxyServiceMock = new ProxyService()
		proxyServiceMock.metaClass.fetchLocations = {WebPageTestServer wptserver ->
			//do nothing, using tests will have to create necessary locations on their own
		}
		serviceToMockIn.proxyService = proxyServiceMock
	}
	/**
	 * Mocks methods of{@link PageService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockPageService(serviceToMockIn, Page pageToReturnFromGetPageByStepName, String innerStepNameToReturnFromExcludePagenamePart) {
		PageService pageServiceMock = new PageService()
		pageServiceMock.metaClass.getPageByStepName = {String stepname ->
			return pageToReturnFromGetPageByStepName
		}
		pageServiceMock.metaClass.excludePagenamePart = {String stepname ->
			return innerStepNameToReturnFromExcludePagenamePart
		}
		serviceToMockIn.pageService = pageServiceMock
	}

    /**
     * Mocks methods of{@link I18nService}.
     */
    void mockI18nService(serviceToMockIn) {
		def i18nService = new I18nService()
        i18nService.metaClass.msg {
            String msgKey, String defaultMessage, List objs = null ->
                return defaultMessage
        }
        serviceToMockIn.i18nService = i18nService
    }

    /**
     * Mocks methods of{@link PerformanceLoggingService}.
     */
    void mockPerformanceLoggingService(serviceToMockIn) {
		PerformanceLoggingService performanceLoggingService = new PerformanceLoggingService()
        performanceLoggingService.metaClass.logExecutionTime = {
            PerformanceLoggingService.LogLevel level, String description, Integer indentationDepth, Closure toMeasure ->
                toMeasure.call()
        }
        serviceToMockIn.performanceLoggingService = performanceLoggingService
    }

    /**
     * Mocks methods of{@link EventResultDaoService}.
     */
    void mockEventResultDaoService(serviceToMockIn, ArrayList<EventResult> eventResults) {
		EventResultDaoService eventResultDaoService = new EventResultDaoService()
        eventResultDaoService.metaClass.getLimitedMedianEventResultsBy = {
            Date fromDate,
            Date toDate,
            Set<CachedView> cachedViews,
            ErQueryParams queryParams,
            Map listCriteriaRestrictionMap,
            CriteriaSorting sorting ->
                return eventResults
        }
        serviceToMockIn.eventResultDaoService = eventResultDaoService
    }

	/**
	 * Mocks methods of{@link MetricReportingService}.
	 */
	void mockMetricReportingService(serviceToMockIn) {
		MetricReportingService metricReportingService = new MetricReportingService()
		metricReportingService.metaClass.reportEventResultToGraphite = {EventResult ->
			//Do Nothing
		}
		serviceToMockIn.metricReportingService = metricReportingService
	}

	void mockBatchActivityService(serviceToMockIn){
		BatchActivityService service = new BatchActivityService()
		service.metaClass.getActiveBatchActivity = {Class c, Activity activity, String name, int maxStages, boolean observe ->
			return new BatchActivityUpdaterDummy(name,c.name,activity, maxStages, 5000)
		}
		serviceToMockIn.batchActivityService = service
	}

}
